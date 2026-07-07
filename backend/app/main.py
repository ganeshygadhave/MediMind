"""
MedRem Backend — FastAPI Application Entry Point
AI-Powered Medication Reminder & Health Assistant API
"""

from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.database import connect_to_database, close_database_connection, ping_database
from app.routes import auth, user, medications, reports, ai, dashboard, reminders


# ── Lifespan (startup/shutdown) ──────────────────────────

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Handle application startup and shutdown events."""
    # Startup
    print("Starting MedRem Backend...")
    await connect_to_database()
    
    # Start the automated reminder scheduler
    import asyncio
    from app.services.scheduler_service import start_reminder_scheduler
    asyncio.create_task(start_reminder_scheduler())
    
    print("MedRem Backend is ready!")
    yield
    # Shutdown
    await close_database_connection()
    print("MedRem Backend shut down.")


# ── Create FastAPI App ───────────────────────────────────

app = FastAPI(
    title="MedRem API",
    description="AI-Powered Medication Reminder & Health Assistant — Backend API",
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)

# ── CORS Middleware ──────────────────────────────────────

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Restrict in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Register Routers ────────────────────────────────────

app.include_router(auth.router)
app.include_router(user.router)
app.include_router(medications.router)
app.include_router(reports.router)
app.include_router(ai.router)
app.include_router(dashboard.router)
app.include_router(reminders.router)


# ── Root Endpoint ────────────────────────────────────────

@app.get("/", tags=["Health"])
async def root():
    """Health check endpoint."""
    return {
        "name": "MedRem API",
        "version": "1.0.0",
        "status": "healthy",
        "description": "AI-Powered Medication Reminder & Health Assistant",
    }


@app.get("/health", tags=["Health"])
async def health_check():
    """Detailed health check."""
    database_status = "connected" if await ping_database() else "disconnected"

    return {
        "status": "healthy",
        "database": database_status,
        "api": "operational",
    }
