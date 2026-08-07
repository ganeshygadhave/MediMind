import pytest
from unittest.mock import patch, MagicMock, AsyncMock
from langchain_core.messages import AIMessage, HumanMessage

from app.services.health_agent import (
    _get_app,
    _get_extract_app,
    tools,
)

def test_graph_compilation():
    """Verify that both StateGraphs compile successfully."""
    app = _get_app()
    assert app is not None
    
    extract_app = _get_extract_app()
    assert extract_app is not None

def test_tools_registration():
    """Verify that the core medical assistant tools are correctly registered."""
    tool_names = [t.name for t in tools]
    assert "get_user_profile" in tool_names
    assert "get_active_medications" in tool_names
    assert "get_recent_medical_reports" in tool_names

@pytest.mark.asyncio
async def test_health_agent_execution_mocked():
    """Verify the health agent logic runs and parses response using mock LLM."""
    from app.services.health_agent import HealthResponse
    mock_health_response = HealthResponse(
        answer="Mocked health response: you are taking Aspirin.",
        warning_level="none",
        sources_used=["get_active_medications"],
        suggested_actions=["Verify dosage"]
    )
    
    # We mock _get_llm to return mock LLM components
    mock_llm = MagicMock()
    mock_llm_with_tools = AsyncMock()
    mock_structured_llm = AsyncMock()
    mock_structured_llm.ainvoke.return_value = mock_health_response

    # Setup the flow:
    # 1st call to agent: it calls a tool
    # 2nd call to agent: it provides the final answer response without tool calls
    first_response = AIMessage(
        content="",
        tool_calls=[{"name": "get_active_medications", "args": {"user_id": "test_user_id"}, "id": "call_1"}]
    )
    second_response = AIMessage(content="I've checked your medications.")
    mock_llm_with_tools.ainvoke.side_effect = [first_response, second_response]

    # Setup the structured output return
    mock_llm.with_structured_output.return_value = mock_structured_llm
    mock_llm.bind_tools.return_value = mock_llm_with_tools
    
    with patch("app.services.health_agent._get_llm", return_value=(mock_llm, mock_llm_with_tools, mock_structured_llm)):
        from app.services.health_agent import run_health_agent
        
        # Patch the repository functions that the tools call
        with patch("app.services.health_agent.user_repository.find_user_by_id", new_callable=AsyncMock) as mock_find_user, \
             patch("app.services.health_agent.medication_repository.find_medications_by_user", new_callable=AsyncMock) as mock_find_meds, \
             patch("app.services.health_agent.report_repository.find_reports_by_user", new_callable=AsyncMock) as mock_find_reports:
            
            mock_find_user.return_value = {"full_name": "Test User", "blood_type": "O+", "allergies": [], "medical_conditions": []}
            mock_find_meds.return_value = [{"name": "Aspirin", "dosage": "100mg", "frequency": "Once Daily", "instructions": None}]
            mock_find_reports.return_value = []
            
            result = await run_health_agent("test_user_id", "Tell me about my meds")
            
            assert result["answer"] == "Mocked health response: you are taking Aspirin."
            assert result["warning_level"] == "none"
            assert "get_active_medications" in result["sources_used"]

@pytest.mark.asyncio
async def test_extraction_agent_execution_mocked():
    """Verify the extraction agent logic runs and parses response using mock LLM."""
    from app.services.health_agent import ExtractionResponse, ExtractedMedicine
    
    mock_extracted_meds = [
        ExtractedMedicine(
            name="Amoxicillin",
            dosage="500mg",
            frequency="Thrice Daily",
            instructions="With food",
            is_duplicate=False,
            interaction_warning="None"
        )
    ]
    mock_extraction_response = ExtractionResponse(
        medicines=mock_extracted_meds,
        summary="Found 1 new medication."
    )
    
    # We mock _get_vision_llm to return mock LLM components
    mock_vision_llm = AsyncMock()
    mock_extraction_llm = AsyncMock()
    mock_extraction_llm.ainvoke.return_value = mock_extraction_response
    
    # Setup dummy download response
    mock_response = MagicMock()
    mock_response.status_code = 200
    mock_response.content = b"Image content mock"
    mock_response.headers = {"Content-Type": "image/jpeg"}
    
    # Setup the mock vision LLM return
    mock_vision_llm.ainvoke.return_value = AIMessage(content="Mock OCR transcription")
    
    with patch("app.services.health_agent._get_vision_llm", return_value=(mock_vision_llm, mock_extraction_llm)):
        from app.services.health_agent import run_extraction_agent
        
        # Patch the repository functions that verify_extraction_node calls
        with patch("app.services.health_agent.medication_repository.find_medications_by_user", new_callable=AsyncMock) as mock_find_meds, \
             patch("app.services.health_agent.user_repository.find_user_by_id", new_callable=AsyncMock) as mock_find_user:
            
            mock_find_user.return_value = {"full_name": "Test User", "blood_type": "O+", "allergies": [], "medical_conditions": []}
            mock_find_meds.return_value = []
            
            # Patch httpx client get
            with patch("httpx.AsyncClient.get", new_callable=AsyncMock) as mock_httpx_get:
                mock_httpx_get.return_value = mock_response
                
                result = await run_extraction_agent("test_user_id", "https://mock.url/report.jpg")
                
                assert len(result["medicines"]) == 1
                assert result["medicines"][0]["name"] == "Amoxicillin"
                assert result["summary"] == "Found 1 new medication."
