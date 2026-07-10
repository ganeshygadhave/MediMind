"""
MedRem Backend — Application Configuration
Loads settings from environment variables / .env file.
"""

from pathlib import Path

from pydantic_settings import BaseSettings
from typing import Optional


BASE_DIR = Path(__file__).resolve().parent.parent


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""

    # MongoDB
    MONGODB_URL: str = "mongodb://localhost:27017"
    DATABASE_NAME: str = "medrem"

    # JWT Authentication
    JWT_SECRET: str = "medrem-dev-secret-change-in-production"
    JWT_ALGORITHM: str = "HS256"
    JWT_EXPIRATION_MINUTES: int = 1440  # 24 hours

    # Cloudinary
    CLOUDINARY_CLOUD_NAME: str = ""
    CLOUDINARY_API_KEY: str = ""
    CLOUDINARY_API_SECRET: str = ""

    # Google Gemini AI
    GEMINI_API_KEY: str = ""

    # Groq AI (Backup/Chat Fast Tier)
    GROQ_API_KEY: str = ""

    # Firebase
    FIREBASE_CREDENTIALS_PATH: str = "./firebase-service-account.json"

    # Server
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    DEBUG: bool = False

    class Config:
        env_file = str(BASE_DIR / ".env")
        env_file_encoding = "utf-8"


# Singleton settings instance
settings = Settings()
