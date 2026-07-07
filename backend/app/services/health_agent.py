"""
MedRem Backend — Health Agent Service (LangGraph Edition)
Agentic AI system for personalized health Q&A using LangGraph and Gemini.
"""

import json
import os
from typing import Annotated, List, Literal, Optional, TypedDict, Union

from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import BaseMessage, HumanMessage, AIMessage, SystemMessage, ToolMessage
from langchain_core.tools import tool
from langgraph.graph import StateGraph, END
from langgraph.prebuilt import ToolNode
from langchain_groq import ChatGroq
from pydantic import BaseModel, Field

from app.config import settings
from app.repositories import medication_repository, report_repository, user_repository

# ── 1. Define Structured Output Schema ─────────────────────

class HealthResponse(BaseModel):
    """The final structured response format for the health assistant."""
    answer: str = Field(description="The main text of the response to the user.")
    warning_level: Literal["none", "low", "medium", "high"] = Field(
        description="Priority level based on potential health risks detected."
    )
    sources_used: List[str] = Field(
        description="List of data sources the agent queried (e.g., 'Medications', 'Reports')."
    )
    suggested_actions: List[str] = Field(
        description="Bullet points of what the user should do next."
    )

class ExtractedMedicine(BaseModel):
    """A single medication extracted from a document."""
    name: str
    dosage: str
    frequency: str
    instructions: str
    is_duplicate: bool = Field(description="True if the user is already taking this exact medicine.")
    interaction_warning: Optional[str] = Field(default=None, description="Warning if this medicine interacts with existing ones or allergies.")

class ExtractionResponse(BaseModel):
    """The structured result of a medication extraction process."""
    medicines: List[ExtractedMedicine]
    summary: str = Field(description="A brief summary of what was found and any concerns.")

# ── 2. Define State ────────────────────────────────────────

class AgentState(TypedDict):
    """The state of the graph, containing messages and the user ID."""
    messages: Annotated[List[BaseMessage], lambda x, y: x + y]
    user_id: str
    final_output: Union[HealthResponse, None]

# ── 3. Define Tools ────────────────────────────────────────

@tool
async def get_user_profile(user_id: str) -> str:
    """Fetch the patient's basic health profile (allergies, conditions, etc.)."""
    user = await user_repository.find_user_by_id(user_id)
    if not user:
        return "User profile not found."
    
    profile = {
        "full_name": user.get("full_name"),
        "blood_type": user.get("blood_type"),
        "allergies": user.get("allergies", []),
        "medical_conditions": user.get("medical_conditions", [])
    }
    return json.dumps(profile)

@tool
async def get_active_medications(user_id: str) -> str:
    """Fetch a list of medications the patient is currently taking."""
    meds = await medication_repository.find_medications_by_user(user_id, active_only=True)
    if not meds:
        return "Patient has no active medications."
    
    med_list = []
    for m in meds:
        med_list.append({
            "name": m.get("name"),
            "dosage": m.get("dosage"),
            "frequency": m.get("frequency"),
            "instructions": m.get("instructions")
        })
    return json.dumps(med_list)

@tool
async def get_recent_medical_reports(user_id: str) -> str:
    """Fetch summaries of the patient's recent medical lab reports or doctor prescriptions."""
    reports = await report_repository.find_reports_by_user(user_id, limit=5)
    if not reports:
        return "No recent medical reports found."
    
    report_list = []
    for r in reports:
        report_list.append({
            "title": r.get("title"),
            "type": r.get("report_type"),
            "summary": r.get("ai_summary", "No summary available."),
            "date": str(r.get("created_at"))
        })
    return json.dumps(report_list)

tools = [get_user_profile, get_active_medications, get_recent_medical_reports]
tool_node = ToolNode(tools)

# ── 4. Define Nodes ────────────────────────────────────────

# Ensure environment variable is set for LangChain
os.environ["GOOGLE_API_KEY"] = settings.GEMINI_API_KEY

# Lazy-init: LLM clients are created on first use to save memory at startup
_llm = None
_llm_with_tools = None
_structured_llm = None

def _get_llm():
    """Lazy-initialize the primary LLM."""
    global _llm, _llm_with_tools, _structured_llm
    if _llm is None:
        if settings.GROQ_API_KEY:
            _llm = ChatGroq(
                model="llama-3.3-70b-versatile",
                groq_api_key=settings.GROQ_API_KEY,
                temperature=0
            )
            print("AI Agent: Using Groq Engine")
        else:
            _llm = ChatGoogleGenerativeAI(
                model="gemini-2.0-flash",
                google_api_key=settings.GEMINI_API_KEY,
                temperature=0
            )
            print("AI Agent: Using Gemini Engine (Groq Key missing)")
        _llm_with_tools = _llm.bind_tools(tools)
        _structured_llm = _llm.with_structured_output(HealthResponse)
    return _llm, _llm_with_tools, _structured_llm

async def call_model(state: AgentState):
    """Node to let the LLM decide its next tool call or finish reasoning."""
    _, llm_with_tools, _ = _get_llm()
    system_prompt = (
        "You are the MedRem Health Assistant. YOUR KNOWLEDGE IS LIMITED TO THE TOOLS PROVIDED. "
        "1. ONLY answer based on data retrieved from 'get_user_profile', 'get_active_medications', or 'get_recent_medical_reports'. "
        "2. If the data is not found in the tools, explicitly state that you don't have that information. "
        "3. NEVER use general medical knowledge to assume things about THIS specific patient. "
        "4. Patient ID for tool calls: " + state["user_id"]
    )
    
    messages = [SystemMessage(content=system_prompt)] + state["messages"]
    response = await llm_with_tools.ainvoke(messages)
    return {"messages": [response]}

async def finalize_response(state: AgentState):
    """Node that forces the LLM to follow the structured HealthResponse schema."""
    _, _, structured_llm = _get_llm()
    prompt = (
        "Based on the conversation and tools results found above, provide a FINAL structured response. "
        "If you detect a drug interaction, set warning_level to 'high'."
    )
    # Give the LLM the entire conversation to summarize into JSON
    response = await structured_llm.ainvoke(state["messages"] + [HumanMessage(content=prompt)])
    return {"final_output": response}

def should_continue(state: AgentState):
    """Router to determine if we go to tools or finalize."""
    last_message = state["messages"][-1]
    if last_message.tool_calls:
        return "tools"
    return "finalize"

# ── 5. Build the Graph (Lazy) ──────────────────────────────
# Compiled graphs are cached after first call to avoid startup memory cost.

_app = None

def _get_app():
    """Lazy-compile the health agent graph."""
    global _app
    if _app is None:
        workflow = StateGraph(AgentState)
        workflow.add_node("agent", call_model)
        workflow.add_node("tools", tool_node)
        workflow.add_node("finalize", finalize_response)
        workflow.set_entry_point("agent")
        workflow.add_conditional_edges("agent", should_continue, {"tools": "tools", "finalize": "finalize"})
        workflow.add_edge("tools", "agent")
        workflow.add_edge("finalize", END)
        _app = workflow.compile()
    return _app

# ── 6. Entry Point ─────────────────────────────────────────

async def run_health_agent(user_id: str, message: str) -> dict:
    """Runs the LangGraph health assistant and returns structured JSON."""
    initial_state = {
        "messages": [HumanMessage(content=message)],
        "user_id": user_id,
        "final_output": None
    }
    
    result = await _get_app().ainvoke(initial_state)
    output = result["final_output"]
    
    # Return as dict for consistency with existing service
    if isinstance(output, HealthResponse):
        return output.dict()
    
    return {
        "answer": "Error in generating response.",
        "warning_level": "medium",
        "sources_used": [],
        "suggested_actions": ["Retry"]
    }

# ── 7. Extraction Agent ────────────────────────────────────

from typing import Optional

class ExtractionState(TypedDict):
    messages: Annotated[List[BaseMessage], lambda x, y: x + y]
    user_id: str
    report_url: str
    extracted_data: Union[ExtractionResponse, None]

# Lazy-init: vision LLM created on first extraction call
_vision_llm = None
_extraction_llm = None

def _get_vision_llm():
    """Lazy-initialize the Gemini vision LLM."""
    global _vision_llm, _extraction_llm
    if _vision_llm is None:
        _vision_llm = ChatGoogleGenerativeAI(
            model="gemini-2.0-flash",
            google_api_key=settings.GEMINI_API_KEY,
            temperature=0
        )
        _extraction_llm = _vision_llm.with_structured_output(ExtractionResponse)
    return _vision_llm, _extraction_llm

async def extraction_node(state: ExtractionState):
    """Node that extracts meds from a document URL using Gemini Vision."""
    vision_llm, _ = _get_vision_llm()
    prompt = (
        "STRICT GROUNDING RULE: You are a medical transcriber. "
        "ONLY extract what is EXPLICITLY visible in the provided document image. "
        "DO NOT guess medications. DO NOT infer information not present in the image. "
        "If the image is blurry or unclear, set the 'instructions' field to 'IMAGE UNCLEAR'."
    )
    
    # We include the URL in the message for Gemini to fetch/see
    message = HumanMessage(content=[
        {"type": "text", "text": f"{prompt}\nDocument URL: {state['report_url']}"},
        {"type": "image_url", "image_url": {"url": state['report_url']}}
    ])
    
    response = await vision_llm.ainvoke([message])
    return {"messages": [response]}

async def verify_extraction_node(state: ExtractionState):
    """Node that cross-references extracted meds with user's existing history."""
    _, extraction_llm = _get_vision_llm()
    
    med_context = await get_active_medications.ainvoke(state["user_id"])
    profile_context = await get_user_profile.ainvoke(state["user_id"])
    
    final_prompt = (
        f"CONTEXT:\nMedications: {med_context}\nProfile: {profile_context}\n\n"
        f"EXTRACTED FROM DOC: {state['messages'][-1].content}\n\n"
        "Generate the FINAL ExtractionResponse JSON."
    )
    
    response = await extraction_llm.ainvoke(final_prompt)
    return {"extracted_data": response}

_extract_app = None

def _get_extract_app():
    """Lazy-compile the extraction agent graph."""
    global _extract_app
    if _extract_app is None:
        extract_workflow = StateGraph(ExtractionState)
        extract_workflow.add_node("extract", extraction_node)
        extract_workflow.add_node("verify", verify_extraction_node)
        extract_workflow.set_entry_point("extract")
        extract_workflow.add_edge("extract", "verify")
        extract_workflow.add_edge("verify", END)
        _extract_app = extract_workflow.compile()
    return _extract_app

async def run_extraction_agent(user_id: str, report_url: str) -> dict:
    """Entry point for the extraction agent."""
    initial_state = {
        "messages": [],
        "user_id": user_id,
        "report_url": report_url,
        "extracted_data": None
    }
    
    result = await _get_extract_app().ainvoke(initial_state)
    output = result["extracted_data"]
    
    if isinstance(output, ExtractionResponse):
        return output.dict()
    
    return {"medicines": [], "summary": "Failed to extract data."}
