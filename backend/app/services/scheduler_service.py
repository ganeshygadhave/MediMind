from datetime import datetime, timezone
import asyncio
from app.repositories import medication_repository, user_repository
from app.services import fcm_service, dashboard_service

async def start_reminder_scheduler():
    """Starts the background loop to check for reminders every minute."""
    print("Reminder Scheduler started.")
    while True:
        try:
            await check_and_send_reminders()
        except Exception as e:
            print(f"Scheduler error: {e}")
        
        # Wait until the start of the next minute
        now = datetime.now()
        sleep_seconds = 60 - now.second
        await asyncio.sleep(sleep_seconds)

async def check_and_send_reminders():
    """Checks the DB for medications due at the current time."""
    now = datetime.now()
    current_time = now.strftime("%H:%M") # Format: "13:06"
    
    # 1. Find all medications that have THIS time in their schedule
    # Note: In a production app, we would use a more optimized query
    # but for this MVP, we fetch and filter.
    medications = await medication_repository.find_all_medications()
    
    for med in medications:
        if current_time in med.get("reminder_times", []):
            # 2. Get the user's FCM token
            user = await user_repository.find_user_by_id(med["user_id"])
            token = user.get("fcm_token")
            
            if (user and token):
                print(f"Sending automated reminder to {user['email']} for {med['name']} at {current_time}")
                print(f"Targeting Token: {token[:10]}...{token[-5:]}")
                
                await fcm_service.send_medication_reminder(
                    fcm_token=token,
                    medication_name=med["name"],
                    dosage=med["dosage"],
                    scheduled_time=current_time,
                    medication_id=str(med["_id"])
                )

                # Log the alert being sent for the dashboard stats
                await dashboard_service.log_dose_action(
                    user_id=str(user["_id"]),
                    medication_id=str(med["_id"]),
                    scheduled_time=current_time,
                    action="reminder_sent"
                )
            else:
                print(f"Skipping reminder for {user.get('email', 'unknown')}: No FCM token found. User must log in on mobile.")

