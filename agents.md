# 🤖 Integration Guide: LangGraph Health Agents

Hi Rohit,

The MediMind backend has been upgraded with a powerful **Agentic AI System** using LangGraph and Gemini. We are no longer using simple text-based chat; the app now uses intelligent agents that provide **structured JSON outputs**.

This guide explains how to connect your Flutter UI to these new agents.

---

## 🛰️ 1. Global Response Format
Every AI response from the backend is now a structured JSON object. Use a Class/model in Flutter to parse these reliably.

### A. Health Chat Agent (`POST /api/ai/chat`)
This powers the main chat window.
**Response Structure:**
```json
{
  "answer": "String - The assistant's reply.",
  "warning_level": "none | low | medium | high",
  "sources_used": ["List", "of", "strings"],
  "suggested_actions": ["List", "of", "bullet", "points"]
}
```
**🎨 UI Instructions:**
* **Color Coding**: Map `warning_level` to colors (none=Green, medium=Yellow, high=Red). Show a banner or icon next to the message.
* **Actions**: Render `suggested_actions` as a list of "Next Steps" or actionable chips at the bottom of the message.

### B. Medication Extraction Agent (`POST /api/ai/extract-medicines`)
Trigger this when a user uploads a prescription in the "Add Medication" flow.
**Response Structure:**
```json
{
  "medicines": [
    {
      "name": "String",
      "dosage": "String",
      "frequency": "String",
      "instructions": "String",
      "is_duplicate": true,
      "interaction_warning": "Warning text if dangerous or duplicate"
    }
  ],
  "summary": "String - Overall diagnosis of the document."
}
```
**🚧 Implementation Tip:**
* **Auto-Fill**: Use the `name`, `dosage`, and `frequency` to auto-populate the "Add Medication" form fields.
* **Safety Badges**: If `is_duplicate` is `true`, show a "Previously Added" badge. If `interaction_warning` is not null, show a high-visibility warning before they save the medication.

---

## 🔌 2. API Implementation Details

### File Uploads (Critical)
When calling `/api/reports/upload`, you get a `report_id`. You must use this ID to call the extraction agent.

```dart
// Flow for Extracting Medicines from a PDF/Image:
1. Upload file to /api/reports/upload -> get report_id
2. Call /api/ai/extract-medicines with { "report_id": report_id }
3. Receive structured JSON -> Populate UI form
```

### Authentication
Every request MUST include the JWT token:
`Authorization: Bearer <your_token>`

---

## 🛡️ 3. Safety Guardrails
These agents are **Grounded**. This means:
1. They *only* talk about data found in the tools (meds, profile, reports).
2. If an image is blurry, they return `"instructions": "IMAGE UNCLEAR"`. You should detect this string and ask the user to retake the photo.

## 📦 4. Recommended Flutter Packages
* `dio`: For network requests.
* `json_annotation` & `json_serializable`: To handle the structured responses cleanly.
* `flutter_markdown`: If you want to render the `answer` field with bold text or links.

Good luck with the integration! Let the backend team know if you need any adjustments to the JSON keys.
