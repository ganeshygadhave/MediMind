# 🚀 Task Initialization: Flutter Migration for MediMind

Hi Rohit,

Your primary objective is to migrate the existing **MediMind Android App** (which currently uses native Kotlin, Jetpack Compose, and Hilt) over to a cross-platform **Flutter** codebase. 

The backend (Python FastAPI) is fully built, functional, and will **not** change. Your job is strictly to rebuild the UI and API integration layer in Flutter.

---

## 🏗️ 1. Project Overview & Architecture
MediMind is an AI-powered Health and Medication Assistant. It lets users track prescriptions, upload medical reports, and get AI-generated health metrics and summaries.

### Recommended Flutter Stack (Your Choice, but standard for this):
* **State Management**: Provider, Riverpod, or BLoC (Currently uses MVVM pattern with `ViewModel`).
* **Networking**: `dio` or `http` (Currently uses `Retrofit` & `OkHttp`).
* **Dependency Injection**: `get_it` or `riverpod` (Currently uses Hilt).
* **Local Storage**: `shared_preferences` or `flutter_secure_storage` for storing the JWT access token.

---

## 🎨 2. App Screens to Replicate in Flutter
You will need to construct Flutter screens matching the existing Android flow. You can look at `android/app/src/main/java/com/medrem/app/ui/screens/` to see how they are structured natively:

1. **Authentication:**
   - Login Screen
   - Register Screen (Collects email, password, full name, phone, etc.)
2. **Dashboard / Home:**
   - Displays daily consistency score, perfect streak days, and "Today's Progress" timeline.
3. **Medications:**
   - **Medications List:** View all active meds.
   - **Add Medication Screen:** Has a unique feature where clicking "Upload" allows the user to attach an image/PDF of a doctor's prescription. The app sends this to the backend, which runs AI to extract the dosage/frequency and auto-fills the form for the user!
4. **Reports:**
   - **Reports Library:** A screen to upload documents (PDF/jpg) and view existing uploaded health reports.
5. **Profile:**
   - Edit personal details and health constants (blood type, allergies).

---

## 🔌 3. Backend API Connection
The backend is a **FastAPI (Python)** server. All API calls expect the token in the `Authorization` header: `Bearer <token>`.

### Core Endpoint Groupings:
* **Auth**: `/api/auth/login` (Expects `username` and `password` as application/x-www-form-urlencoded), `/api/auth/register`
* **User/Profile**: `/api/user/profile` (GET and PUT)
* **Dashboard**: `/api/dashboard/stats`, `/api/dashboard/today`
* **Medications**: `/api/medications/` (CRUD operations)
* **Reports (File Uploads)**: `/api/reports/upload` 
  * *Watch Out!*: This is a `multipart/form-data` endpoint. You will need to use something like `Dio.FormData` or `http.MultipartRequest` in Flutter to attach physical files.
* **AI Feature**: `/api/ai/extract-medicines` (Pass it a `report_id` to get extracted medicine details).

*(You can view the full active endpoint list by running the backend and visiting `http://localhost:8000/docs` in your browser for the Swagger OpenAPI UI).*

---

## ⚠️ 4. Crucial Pitfalls to Avoid in Flutter
1. **File Picking/Uploading:** The current Android app uses `ActivityResultContracts.GetContent()` and copies files to a cache directory to get an absolute file path. In Flutter, use the `file_picker` package to grab documents/images, and ensure you are mapping the `File` object correctly to the Multipart upload POST request.
2. **Icons/Assets:** You can find the main app logo in the root of the repo as `medimind_app_logo.png`. Be sure to set up `flutter_launcher_icons` to keep the branding!
3. **Network Configurations:** By default, Android emulators talk to `10.0.2.2:8000` for localhost, and iOS simulators talk to `127.0.0.1:8000`. Set up a smart base URL string that toggles depending on `Platform.isAndroid`!

Best of luck! Let the team know if you hit any roadblocks while structuring the new Flutter project.
