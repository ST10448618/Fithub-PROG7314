<p align="center">
  <img src="https://readme-typing-svg.herokuapp.com/?color=212C99&size=45&center=true&vCenter=true&width=1000&lines=FITHUB;Your+All-in-One+Fitness+Companion" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-212C99?style=flat-square&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Architecture-MVVM-6237FF?style=flat-square" />
  <img src="https://img.shields.io/badge/Auth-Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/Database-Firestore%20%2B%20Room-374AFF?style=flat-square" />
  <img src="https://img.shields.io/badge/API-OpenFoodFacts-212C99?style=flat-square" />
  <img src="https://img.shields.io/badge/ML-TensorFlow%20Lite-FF6F00?style=flat-square&logo=tensorflow&logoColor=white" />
  <img src="https://img.shields.io/badge/Scanner-ML%20Kit-4285F4?style=flat-square&logo=google&logoColor=white" />
  <img src="https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white" />
  <img src="https://img.shields.io/badge/Min%20SDK-API%2024-CCD1F8?style=flat-square" />
</p>

---

## Demonstration Video

<p align="center">
  <a href="https://www.youtube.com/watch?v=YOUR_VIDEO_ID">
    <img src="https://img.shields.io/badge/YouTube-Watch%20Demo-212C99?style=for-the-badge&logo=youtube&logoColor=white"/>
  </a>
</p>

> Click the badge above to watch a full walkthrough of the FitHub application, covering all core and innovative features.

---

## Main Project Link

<p align="center">
  <a href="https://github.com/EMKNDW/prog7314-g2-2026-prog7314-2026-poe-st10448618.git">
    <img src="https://img.shields.io/badge/GitHub-Repository-212C99?style=for-the-badge&logo=github&logoColor=white" />
  </a>
</p>

---

## Quick Start

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Koala or newer)
- [JDK 17](https://adoptium.net/temurin/releases/?version=17)
- Android SDK 24+ (target 36)
- A Firebase project with **Auth**, **Firestore** and **Storage** enabled

### Installation

**Clone and open the project**

```bash
git clone https://github.com/ST10448618/Fithub-PROG7314.git
cd Fithub-PROG7314
```

**Add your Firebase configuration**

- Download `google-services.json` from your Firebase project
- Place it at: `app/google-services.json`

**Build and run**

```bash
./gradlew assembleDebug
```

Open the project in Android Studio → run on device or emulator running **Android 7.0 (API 24)** or newer.

### Optional — Enable On-Device Food Recognition

The camera recognition feature uses a TensorFlow Lite model. Without it, the app falls back to manual entry — **nothing crashes**.

1. Train a model at [Teachable Machine](https://teachablemachine.withgoogle.com/)
2. Export as **TensorFlow Lite → Floating point**
3. Place in `app/src/main/assets/`:
   - `model_unquant.tflite` → `food_model.tflite`
   - `labels.txt` → `food_labels.txt`

---

## 1. System Overview

FitHub is a native Android fitness companion application built with **Kotlin** and **Jetpack Compose**. It is designed around a single connected product cycle rather than a collection of disconnected dashboards.

The user establishes their profile, sets goals (weight, nutrition, workouts), and receives actionable targets. Daily actions — logging meals and completing workouts — are recorded in a Journal. Progress screens then measure performance against those targets, and consistent activity earns rewards. Every part of the app feeds the next.

The system supports a single authenticated user per device session, with all user data stored locally in Room and synced to Firebase Firestore under the user's authenticated UID. External data sources include the OpenFoodFacts API (food nutrition), Google ML Kit (barcode scanning), and TensorFlow Lite (on-device food recognition).

Security is a core requirement because FitHub processes sensitive information including authentication credentials, personal details (age, weight, height), health records, and nutrition/workout history. The system uses multiple security controls throughout the application rather than relying on a single mechanism.

---

## 2. System Architecture

### 2.1 Layered MVVM Architecture

FitHub follows a layered MVVM architecture. The Compose UI observes state from ViewModels via `StateFlow`. ViewModels coordinate with domain repositories and pure calculator objects. Repositories access data through Room, Firestore, or external APIs. This separation improves maintainability and testability — functionality can be developed and tested without placing all responsibilities in a single component.

### 2.2 System Components

The main components of FitHub are:

- **Jetpack Compose UI**: Provides all user-facing screens, reusable components, custom charts, and overlays.
- **ViewModels**: Hold UI state, expose it via StateFlow, and handle user events. Lifecycle-aware via `collectAsStateWithLifecycle()`.
- **Domain Calculators**: Pure Kotlin objects implementing the calorie engine (Mifflin–St Jeor), macro distribution, workout estimation, weight progress, and nutrition aggregation.
- **Repository Layer**: Abstracts all data sources behind interfaces. The UI never knows whether data comes from Room, Firestore, or OpenFoodFacts.
- **Room Database**: Local persistence for user profiles, weight entries, goals, food logs, workout plans, workout sessions, achievements, and rewards.
- **Firebase Firestore**: Cloud sync, scoped per authenticated user ID via security rules.
- **Firebase Authentication**: Email/password registration and login; supports biometric re-authentication via Android's system `BiometricPrompt`.
- **Retrofit + OkHttp**: HTTP client for OpenFoodFacts API.
- **Google ML Kit**: Barcode scanning via CameraX.
- **TensorFlow Lite**: On-device food recognition from camera images.
- **WorkManager**: Scheduled checkpoint reminders.
- **ServiceLocator**: Manual dependency injection — simple, zero build complexity.

### 2.3 App Architecture

The app uses a layered structure. A user action in the Compose UI triggers a ViewModel event. The ViewModel coordinates the operation — validating input, calling domain calculators, and invoking repositories. Repositories write to Room immediately (offline-first) and push to Firestore in the background. The UI observes StateFlows and re-renders automatically.

This separation gives each layer a clear responsibility. It also supports future development because additional functionality can be introduced without placing all application logic directly inside composables.

### 2.4 System Boundaries

The main FitHub app boundary contains the Compose UI, ViewModels, domain calculators, repositories, Room database, and Firebase SDKs.

External services include Firebase (Auth, Firestore, Storage), OpenFoodFacts API, and Google ML Kit. These services support the application but are not part of the core application code.

HTTPS/TLS forms the secure communication boundary between the app and external services. Local persistence (Room) operates entirely offline — no network is required for reading or writing user records.

---

## 3. Request Flow

A typical request begins when the user interacts with a Compose screen. The screen dispatches an event to its ViewModel. The ViewModel validates the request and coordinates the operation.

For operations that need stored data, the ViewModel calls a repository method. The repository first writes to Room (offline-first). It then pushes to Firestore in the background via a suspend function with `await()` on the Firebase Task. If the network is unavailable, the local write succeeds and the cloud push is retried on next sync.

For food data, the repository hits the OpenFoodFacts API through Retrofit. Responses are mapped from DTOs to domain models and cached in Room for subsequent offline access. If the API call fails, cached results are used as a fallback.

For workout sessions, the ViewModel uses the `WorkoutEstimator` calculator to determine estimated activity and duration from the seeded exercise dataset. When the user completes a session, the ViewModel creates a single `WorkoutSession` record. Dashboard, Journal, Progress, and Rewards all derive their updated values from that single record — no manual propagation needed.

The result flows back to the ViewModel as updated StateFlow, which triggers Compose recomposition. The UI reflects the new state.

This flow ensures that business logic is centralised, data sources are abstracted, and security checks (Firebase Auth state, Firestore rules) occur before any protected resource is accessed.

---

## 4. Authentication and Data Security

### 4.1 Registration

During onboarding, the user supplies their username, email, password, and physical details (age, gender, height, weight, activity level). All input is validated on the client and re-validated server-side by Firestore security rules.

The backend uses Firebase Authentication, which handles password hashing (bcrypt under the hood) and secure credential storage. FitHub never stores plaintext passwords — not in Room, not in Firestore, not in logs.

After successful registration, FitHub creates a `UserProfile` document in Firestore at `users/{uid}` and mirrors it locally in Room. Both writes are scoped to the authenticated user's UID.

### 4.2 Login

Login submits credentials to Firebase Auth via HTTPS. On success, Firebase returns a signed ID token. FitHub extracts the UID from the token and uses it as the scope for every subsequent Room and Firestore operation.

All error messages are mapped to friendly, non-revealing text — for example, "Incorrect email or password" is returned for both invalid-email and invalid-password cases, so attackers cannot enumerate valid accounts.

### 4.3 Session Management

Firebase Auth persists the session across app restarts. On app launch, if a valid session exists, the user is taken directly to the Dashboard. If biometric login is enabled, the user can re-authenticate using Android's system `BiometricPrompt` — the app never sees or stores fingerprint data, only the success/failure result.

Firestore security rules enforce that a user can only read or write documents under their own `users/{uid}` path. Even if an attacker obtained another user's UID, the rules would block access.

### 4.4 Protected Requests

Every Firestore read or write is authenticated via the ID token. There is no anonymous access to user data.

For the OpenFoodFacts API (which requires no authentication but does require a descriptive User-Agent), FitHub sends `FitHub/1.0 (Android; contact@fithub.app)` on every request, following their published API guidelines.

The distinction between authentication and authorisation is enforced by Firestore rules: authentication proves *who* the user is; authorisation rules prove *what* they're allowed to touch.

---

## 5. Security Architecture

### 5.1 Password Security

Passwords are hashed by Firebase Authentication using bcrypt with a secure cost factor. Plaintext passwords are never stored anywhere in the app or its local database. Even if a device were compromised, Room would contain no usable credentials.

Firebase also enforces password policy (minimum 6 characters) at the API level.

### 5.2 Input Validation

All user input is treated as untrusted and validated before processing.

- **Onboarding fields** — required, type-checked, range-checked (age 1–120, height 80–250 cm, weight 20–400 kg).
- **Food quantities** — clamped 1–20.
- **Goal values** — validated against sensible ranges.
- **Meal allocations** — must sum exactly to the daily calorie target.

Validation happens on the ViewModel before any write. Malformed or incomplete input is rejected before reaching Room or Firestore.

### 5.3 Data Sanitisation

User-generated content (workout names, notes) is trimmed and length-limited. The app does not render raw HTML anywhere — all text goes through Compose `Text` composables, which are safe by construction.

For external data from OpenFoodFacts (community-contributed, potentially malformed), FitHub applies defensive mapping: missing fields become null, malformed numeric values are coerced safely, and unknown categories fall back to "Other". The UI handles all optional fields gracefully — no crash on missing data.

### 5.4 HTTPS/TLS

All communication with Firebase and OpenFoodFacts uses HTTPS/TLS 1.2 or higher. Firebase SDKs enforce this automatically. Retrofit is configured with `okHttpClient` using the platform's default TLS stack.

Room is fully local, so no encryption-in-transit is required for user records — but Android's built-in filesystem encryption (FBE, on by default since Android 10) protects the database at rest.

### 5.5 Firestore Security Rules

Firestore rules enforce that a user can only access documents under their own path.

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      match /{subcollection=**}/{document=**} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

This prevents a user from reading or writing another user's profile, weight entries, food logs, workout plans, workout sessions, or rewards — even if they know the UID.

### 5.6 Ownership Checks

Beyond Firestore rules, FitHub's repository layer always scopes operations to the current authenticated UID. `SessionManager.currentUserId` reads directly from `FirebaseAuth.getInstance().currentUser?.uid` — there is no hard-coded UID anywhere in the app.

Every repository method accepts the UID as an explicit parameter, so there's no ambiguity about whose data is being operated on.

### 5.7 Biometric Authentication

FitHub uses Android's system `BiometricPrompt` — the app never stores, transmits, or receives fingerprint data. The system handles the credential; the app receives only the boolean success/failure result.

Biometric login is opt-in (enabled during onboarding or later in Settings) and stores only a preference flag — not any credential material.

### 5.8 Secure Error Handling

API and Firebase errors are mapped to user-friendly messages before display. Internal details (stack traces, Firestore paths, Firebase error codes) are never shown to the user.

Examples:
- "Incorrect email or password" (not "no such user" vs "wrong password")
- "Network error — check your connection"
- "Please enter a valid email address"

Firebase exceptions are caught and logged locally with minimal detail. No sensitive information appears in Android Logcat.

### 5.9 Data Ownership and Integrity

The `FoodLog` model snapshots nutritional values at the time of logging. If OpenFoodFacts later updates a product, historical food logs keep their original values. This preserves data integrity and prevents retroactive corruption of user history.

Workout sessions similarly preserve their own state — exercises, reps, and estimated activity are frozen at completion. Even if a workout plan is edited later, past sessions keep their original content.

---

## 6. Security Rationale

The security controls used by FitHub were selected according to the risks associated with processing authentication, personal details, and health data.

| Security Control | Purpose |
|---|---|
| Firebase Authentication | Manages credentials, hashes passwords with bcrypt |
| Firestore security rules | Enforces per-user data isolation |
| UID-scoped repositories | Prevents any cross-user data access from app code |
| Input validation | Rejects malformed or out-of-range values |
| HTTPS/TLS | Protects data in transit |
| Biometric via system prompt | Re-authentication without storing biometric data |
| Room local persistence | Ensures offline-first operation; no network dependency for reads |
| Defensive API mapping | Handles missing/malformed OpenFoodFacts fields gracefully |
| Immutable food/workout snapshots | Preserves historical accuracy regardless of future changes |
| Friendly error messages | Avoids leaking implementation details |

These controls provide defence in depth. Multiple security mechanisms operate at different stages of a request, meaning the system does not depend on a single control.

---

## 7. DevSecOps / CI-CD Overview

Security and testing are integrated into the development lifecycle through a GitHub Actions CI/CD pipeline. Rather than treating verification as a final step before submission, FitHub integrates build, test, and lint checks throughout development.

The pipeline performs:

- **Automated builds** — `./gradlew assembleDebug` on every push
- **Automated unit tests** — `./gradlew testDebugUnitTest` covering calculators and aggregation logic
- **Static analysis** — `./gradlew lintDebug` to catch potential issues
- **Artifact uploads** — APK, test reports, and lint reports are saved for review

Only when the required checks succeed can a Pull Request be merged into `main`. This is enforced by GitHub branch protection — pull requests require at least one approval and a passing CI run before merge.

The pipeline therefore supports the DevSecOps approach by integrating development, testing, and verification into a continuous process. It reduces reliance on manual checks and encourages quality to be considered throughout the software development lifecycle.

Both workflow files are located in `.github/workflows/` and run automatically on:
- Every push to `main` or `feature/**` branches
- Every pull request targeting `main`
- Manual trigger via `workflow_dispatch`

---

<p align="center">
  <strong>PROG7314 · Programming 3D · Group 6 · 2026</strong>
</p>
