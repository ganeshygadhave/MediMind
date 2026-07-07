"""
MedRem Backend — FCM Service
Firebase Cloud Messaging for push notifications.
"""

import os
from typing import Optional

from app.config import settings

# Firebase Admin SDK (initialized lazily)
_firebase_initialized = False


def _init_firebase():
    """Initialize Firebase Admin SDK from env var JSON or file path."""
    global _firebase_initialized
    if _firebase_initialized:
        return

    try:
        import json
        import firebase_admin
        from firebase_admin import credentials

        # Priority 1: Load from FIREBASE_CREDENTIALS_JSON env var (for cloud deploy on Render)
        cred_json_str = os.environ.get("FIREBASE_CREDENTIALS_JSON", "")
        if cred_json_str:
            cred_dict = json.loads(cred_json_str)
            cred = credentials.Certificate(cred_dict)
            firebase_admin.initialize_app(cred)
            _firebase_initialized = True
            print("Firebase Admin SDK initialized from environment variable.")
            return

        # Priority 2: Load from file path (for local development)
        cred_path = settings.FIREBASE_CREDENTIALS_PATH
        if os.path.exists(cred_path):
            cred = credentials.Certificate(cred_path)
            firebase_admin.initialize_app(cred)
            _firebase_initialized = True
            print("Firebase Admin SDK initialized from file.")
        else:
            print(f"Firebase credentials not found (checked env var + path '{cred_path}'). Push notifications disabled.")
    except Exception as e:
        print(f"Firebase initialization failed: {e}. Push notifications disabled.")


async def send_notification(
    fcm_token: str,
    title: str,
    body: str,
    data: Optional[dict] = None,
) -> bool:
    """
    Send a push notification to a specific device.

    Args:
        fcm_token: The device's FCM registration token
        title: Notification title
        body: Notification body text
        data: Optional data payload

    Returns:
        True if sent successfully, False otherwise
    """
    _init_firebase()

    if not _firebase_initialized:
        print("Firebase not initialized. Skipping notification.")
        return False

    try:
        from firebase_admin import messaging

        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body,
            ),
            data=data or {},
            token=fcm_token,
            android=messaging.AndroidConfig(
                priority="high",
                notification=messaging.AndroidNotification(
                    channel_id="medication_reminders",
                    icon="ic_notification",
                    color="#006B5E",
                ),
            ),
        )

        response = messaging.send(message)
        print(f"Notification sent: {response}")
        return True

    except Exception as e:
        print(f"Failed to send notification: {e}")
        return False


async def send_medication_reminder(
    fcm_token: str,
    medication_name: str,
    dosage: str,
    scheduled_time: str,
    medication_id: str,
) -> bool:
    """
    Send a medication reminder notification.

    Args:
        fcm_token: Device FCM token
        medication_name: Name of the medication
        dosage: Dosage amount
        scheduled_time: Scheduled time string
        medication_id: Medication ID for deep linking
    """
    return await send_notification(
        fcm_token=fcm_token,
        title=f"💊 Time for {medication_name}",
        body=f"Take {dosage} — Scheduled for {scheduled_time}",
        data={
            "type": "medication_reminder",
            "medication_id": medication_id,
            "scheduled_time": scheduled_time,
        },
    )
