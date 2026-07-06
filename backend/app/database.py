"""
MedRem Backend — Database Connection
Async MongoDB connection using Motor driver.
"""

from motor.motor_asyncio import AsyncIOMotorClient
from app.config import settings

# MongoDB client and database instances
client: AsyncIOMotorClient = None
db = None


async def connect_to_database():
    """Initialize the MongoDB connection."""
    global client, db
    client = AsyncIOMotorClient(settings.MONGODB_URL)
    db = client[settings.DATABASE_NAME]

    # Create indexes for performance
    await db.users.create_index("email", unique=True)
    await db.medications.create_index("user_id")
    await db.reports.create_index("user_id")
    await db.chat_history.create_index("user_id")
    await db.dose_logs.create_index([("user_id", 1), ("medication_id", 1), ("scheduled_time", 1)])

    print(f"Connected to MongoDB: {settings.DATABASE_NAME}")


async def close_database_connection():
    """Close the MongoDB connection."""
    global client
    if client:
        client.close()
        print("MongoDB connection closed.")


def get_database():
    """Get the database instance."""
    return db


async def ping_database() -> bool:
    """Check whether MongoDB is reachable."""
    if db is None:
        return False

    await db.command("ping")
    return True


# Collection accessors
def get_users_collection():
    return db["users"]


def get_medications_collection():
    return db["medications"]


def get_reports_collection():
    return db["reports"]


def get_chat_history_collection():
    return db["chat_history"]


def get_dose_logs_collection():
    return db["dose_logs"]
