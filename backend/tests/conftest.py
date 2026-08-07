import os
import pytest

# Ensure test credentials are set before importing app modules
os.environ["MONGODB_URL"] = "mongodb://localhost:27017/test_db"
os.environ["GROQ_API_KEY"] = "mock_groq_api_key_for_testing"
os.environ["GEMINI_API_KEY"] = "mock_gemini_api_key_for_testing"
os.environ["JWT_SECRET_KEY"] = "mock_secret_key_for_testing"
os.environ["CLOUDINARY_URL"] = "cloudinary://mock:mock@mock"

@pytest.fixture(scope="session")
def anyio_backend():
    return "asyncio"
