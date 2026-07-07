package com.medrem.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Auth DTOs ───────────────────────────────────────────

data class LoginRequestDto(
    val email: String,
    val password: String,
    @SerializedName("fcm_token") val fcmToken: String? = null,
)

data class RegisterRequestDto(
    val email: String,
    val password: String,
    @SerializedName("full_name") val fullName: String,
    val phone: String? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    val gender: String? = null,
    @SerializedName("blood_type") val bloodType: String? = null,
    val allergies: List<String>? = null,
    @SerializedName("medical_conditions") val medicalConditions: List<Map<String, String>>? = null,
    @SerializedName("fcm_token") val fcmToken: String? = null,
)

data class AuthResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    val user: UserDto,
)

data class UserDto(
    val id: String,
    val email: String,
    @SerializedName("full_name") val fullName: String,
    val phone: String? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    val gender: String? = null,
    @SerializedName("blood_type") val bloodType: String? = null,
    val allergies: List<String> = emptyList(),
    @SerializedName("medical_conditions") val medicalConditions: List<Map<String, String>> = emptyList(),
    @SerializedName("emergency_contact") val emergencyContact: Map<String, String>? = null,
    @SerializedName("profile_image_url") val profileImageUrl: String? = null,
    @SerializedName("notification_settings") val notificationSettings: Map<String, Boolean> = emptyMap(),
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
)

data class MessageResponseDto(
    val message: String,
)

// ── Medication DTOs ─────────────────────────────────────

data class MedicationCreateDto(
    val name: String,
    val dosage: String,
    val frequency: String,
    @SerializedName("reminder_times") val reminderTimes: List<String>,
    @SerializedName("duration_type") val durationType: String = "permanent",
    @SerializedName("duration_start") val durationStart: String? = null,
    @SerializedName("duration_end") val durationEnd: String? = null,
    val instructions: String? = null,
    @SerializedName("is_prn") val isPrn: Boolean = false,
)

data class MedicationUpdateDto(
    val name: String? = null,
    val dosage: String? = null,
    val frequency: String? = null,
    @SerializedName("reminder_times") val reminderTimes: List<String>? = null,
    @SerializedName("duration_type") val durationType: String? = null,
    @SerializedName("is_prn") val isPrn: Boolean? = null,
    @SerializedName("is_active") val isActive: Boolean? = null,
)

data class MedicationDto(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    @SerializedName("reminder_times") val reminderTimes: List<String>,
    @SerializedName("duration_type") val durationType: String,
    @SerializedName("duration_start") val durationStart: String? = null,
    @SerializedName("duration_end") val durationEnd: String? = null,
    val instructions: String? = null,
    @SerializedName("is_prn") val isPrn: Boolean,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
)

// ── Report DTOs ─────────────────────────────────────────

data class ReportDto(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val title: String,
    @SerializedName("report_type") val reportType: String,
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("ai_summary") val aiSummary: String? = null,
    @SerializedName("extracted_data") val extractedData: Map<String, Any>? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
)

data class ReportActionDto(
    @SerializedName("report_id") val reportId: String,
)

data class ReportSummaryResponseDto(
    @SerializedName("report_id") val reportId: String,
    val summary: String,
)

data class ExtractMedicinesResponseDto(
    @SerializedName("report_id") val reportId: String,
    val medicines: List<ExtractedMedicineDto>,
    val summary: String? = null,
)

data class ExtractedMedicineDto(
    val name: String,
    val dosage: String,
    val frequency: String,
    val instructions: String? = null,
    @SerializedName("is_duplicate") val isDuplicate: Boolean = false,
    @SerializedName("interaction_warning") val interactionWarning: String? = null,
)

// ── Chat DTOs ───────────────────────────────────────────

data class ChatRequestDto(
    val message: String,
)

data class ChatMessageDto(
    val role: String,
    @SerializedName("answer") val content: String,
    @SerializedName("warning_level") val warningLevel: String? = "none",
    @SerializedName("sources_used") val sourcesUsed: List<String> = emptyList(),
    @SerializedName("suggested_actions") val suggestedActions: List<String> = emptyList(),
    val timestamp: String? = null,
)

// ── Dashboard DTOs ──────────────────────────────────────

data class DashboardStatsDto(
    @SerializedName("consistency_score") val consistencyScore: Double,
    @SerializedName("total_medications") val totalMedications: Int,
    @SerializedName("active_medications") val activeMedications: Int,
    @SerializedName("perfect_streak_days") val perfectStreakDays: Int,
    @SerializedName("avg_taken_time") val avgTakenTime: String?,
    @SerializedName("alerts_sent_today") val alertsSentToday: Int,
)

data class TodayProgressDto(
    @SerializedName("total_doses") val totalDoses: Int,
    val taken: Int,
    val missed: Int,
    val skipped: Int,
    val upcoming: Int,
    val doses: List<DoseDetailDto>,
)

data class DoseDetailDto(
    @SerializedName("medication_id") val medicationId: String,
    val name: String,
    val dosage: String,
    val time: String,
    val status: String,
)

data class RecentReportDto(
    val id: String,
    val title: String,
    val subtitle: String,
    @SerializedName("report_type") val reportType: String,
    @SerializedName("created_at") val createdAt: String,
)

data class DoseLogRequestDto(
    @SerializedName("medication_id") val medicationId: String,
    @SerializedName("scheduled_time") val scheduledTime: String,
    val action: String,
    val notes: String? = null,
)

data class DoseLogResponseDto(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("medication_id") val medicationId: String,
    @SerializedName("scheduled_time") val scheduledTime: String,
    val action: String,
    val timestamp: String,
)
