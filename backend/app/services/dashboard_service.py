"""
MedRem Backend — Dashboard Service
Business logic for dashboard statistics and medication tracking.
"""

from datetime import datetime, timedelta, timezone
from typing import Optional

from app.database import get_dose_logs_collection
from app.repositories import medication_repository, report_repository
from app.utils.helpers import serialize_doc


async def get_dashboard_stats(user_id: str) -> dict:
    """
    Calculate dashboard statistics:
    - Consistency score (last 30 days)
    - Active medications count
    - Perfect streak days
    - Average taken time
    - Alerts sent today
    """
    dose_logs = get_dose_logs_collection()

    # Date range: last 30 days
    now = datetime.now(timezone.utc)
    thirty_days_ago = now - timedelta(days=30)

    # Get all dose logs for the period
    logs = await dose_logs.find({
        "user_id": user_id,
        "timestamp": {"$gte": thirty_days_ago}
    }).to_list(length=1000)

    # Consistency score
    total_logs = len(logs)
    taken_logs = [l for l in logs if l.get("action") == "taken"]
    consistency_score = round((len(taken_logs) / total_logs * 100), 1) if total_logs > 0 else 100.0

    # Active medications
    active_count = await medication_repository.count_active_medications(user_id)

    # Perfect streak (consecutive days with all doses taken)
    streak = await _calculate_streak(user_id, dose_logs)

    # Average taken time
    avg_time = _calculate_avg_taken_time(taken_logs)

    # Alerts sent today
    today_start = now.replace(hour=0, minute=0, second=0, microsecond=0)
    today_alerts = await dose_logs.count_documents({
        "user_id": user_id,
        "timestamp": {"$gte": today_start},
        "action": {"$in": ["reminder_sent"]}
    })

    return {
        "consistency_score": consistency_score,
        "total_medications": active_count,
        "active_medications": active_count,
        "perfect_streak_days": streak,
        "avg_taken_time": avg_time,
        "alerts_sent_today": today_alerts,
    }


async def get_today_progress(user_id: str) -> dict:
    """
    Get today's medication progress: total doses, taken, missed, skipped, upcoming.
    """
    dose_logs = get_dose_logs_collection()
    now = datetime.now(timezone.utc)
    today_start = now.replace(hour=0, minute=0, second=0, microsecond=0)

    # Get active medications
    medications = await medication_repository.find_medications_by_user(user_id, active_only=True)

    # Calculate total expected doses today
    total_doses = 0
    doses_list = []
    for med in medications:
        if med.get("is_prn"):
            continue  # PRN meds don't count toward daily total
        reminder_times = med.get("reminder_times", [])
        total_doses += len(reminder_times)
        for time_str in reminder_times:
            doses_list.append({
                "medication_id": str(med["_id"]),
                "name": med["name"],
                "dosage": med["dosage"],
                "time": time_str,
                "status": "upcoming",
            })

    # Get today's logs
    today_logs = await dose_logs.find({
        "user_id": user_id,
        "timestamp": {"$gte": today_start}
    }).to_list(length=200)

    # Map logs to doses
    taken = 0
    missed = 0
    skipped = 0
    for log in today_logs:
        action = log.get("action", "")
        if action == "taken":
            taken += 1
        elif action == "missed":
            missed += 1
        elif action == "skipped":
            skipped += 1

        # Update status in doses_list
        for dose in doses_list:
            if (dose["medication_id"] == log.get("medication_id") and
                    dose["time"] == log.get("scheduled_time")):
                dose["status"] = action
                break

    upcoming = total_doses - taken - missed - skipped
    if upcoming < 0:
        upcoming = 0

    return {
        "total_doses": total_doses,
        "taken": taken,
        "missed": missed,
        "skipped": skipped,
        "upcoming": upcoming,
        "doses": doses_list,
    }


async def get_recent_reports_for_dashboard(user_id: str) -> list[dict]:
    """Get recent reports formatted for dashboard display."""
    reports = await report_repository.find_recent_reports(user_id, limit=3)
    result = []
    for report in reports:
        created = report.get("created_at")
        subtitle = ""
        if isinstance(created, datetime):
            subtitle = created.strftime("%b %d")

        result.append({
            "id": str(report["_id"]),
            "title": report.get("title", "Untitled Report"),
            "subtitle": subtitle,
            "report_type": report.get("report_type", "medical_record"),
            "created_at": created.isoformat() if isinstance(created, datetime) else str(created),
        })
    return result


async def log_dose_action(
    user_id: str,
    medication_id: str,
    scheduled_time: str,
    action: str,
    notes: Optional[str] = None
) -> dict:
    """
    Log a dose action (taken, skipped, remind_later).
    """
    dose_logs = get_dose_logs_collection()

    log_doc = {
        "user_id": user_id,
        "medication_id": medication_id,
        "scheduled_time": scheduled_time,
        "action": action,
        "notes": notes,
        "timestamp": datetime.now(timezone.utc),
    }

    result = await dose_logs.insert_one(log_doc)
    log_doc["_id"] = str(result.inserted_id)
    return serialize_doc(log_doc)


async def _calculate_streak(user_id: str, dose_logs_collection) -> int:
    """Calculate the number of consecutive days with 100% adherence."""
    now = datetime.now(timezone.utc)
    streak = 0

    for day_offset in range(1, 365):
        day_start = (now - timedelta(days=day_offset)).replace(
            hour=0, minute=0, second=0, microsecond=0
        )
        day_end = day_start + timedelta(days=1)

        day_logs = await dose_logs_collection.find({
            "user_id": user_id,
            "timestamp": {"$gte": day_start, "$lt": day_end}
        }).to_list(length=200)

        if not day_logs:
            break

        # Check if all logs are "taken"
        all_taken = all(log.get("action") == "taken" for log in day_logs)
        if all_taken:
            streak += 1
        else:
            break

    return streak


def _calculate_avg_taken_time(taken_logs: list) -> Optional[str]:
    """Calculate the average time doses are taken."""
    if not taken_logs:
        return None

    total_minutes = 0
    count = 0
    for log in taken_logs:
        ts = log.get("timestamp")
        if isinstance(ts, datetime):
            total_minutes += ts.hour * 60 + ts.minute
            count += 1

    if count == 0:
        return None

    avg_minutes = total_minutes // count
    hours = avg_minutes // 60
    minutes = avg_minutes % 60
    period = "AM" if hours < 12 else "PM"
    display_hours = hours if hours <= 12 else hours - 12
    if display_hours == 0:
        display_hours = 12

    return f"{display_hours}:{minutes:02d} {period}"
