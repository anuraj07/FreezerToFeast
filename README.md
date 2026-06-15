# Freezer-to-Feast: Fresh Harvest Meal Tracker

A premium, serverless Android application designed with a "Quiet Luxury" aesthetic. The app helps users track their kitchen parameters, personalize dietary choices, log daily meals, and leverage client-side Generative AI (`gemini-2.5-flash`) via the official Firebase Vertex AI SDK to suggest tailored recipes based on available ingredients.

---

## Architecture & Code Structure

The project strictly follows the **MVVM (Model-View-ViewModel)** architectural pattern, separating UI design from business logic and database management.

```
com.deep.freezertofeast
├── MainActivity.kt                 # Application entry point, NavHost navigation routing
├── MealIntelligenceRepository.kt   # Integrates Firebase Vertex AI client-side and Firestore logs
├── MealData.kt                     # Serializable meal output structure (recipe, ingredients, steps)
├── Resource.kt                     # State wrapper: Loading, Success, Error
├── ColorTokens.kt                  # Shared design system colors (PrimaryGreen, SecondaryYellow, etc.)
│
├── ui
│   ├── components
│   │   └── CommonComponents.kt     # Shared elements (e.g. solid green TopBar Composable)
│   │
│   ├── screens
│   │   ├── OnboardingScreen.kt     # Immersive left-aligned landing page with local high-res image
│   │   ├── LoginScreen.kt          # Full-screen "Enter the Kitchen" Google Auth page
│   │   ├── ProfileScreen.kt        # User profile customization (dietary focus and pantry staples)
│   │   └── JournalScreen.kt        # Dynamic daily meal slots and AI chef generator
│   │
│   └── viewmodels
│       ├── LoginViewModel.kt       # Authenticates users (Google Auth via Firebase Auth provider)
│       ├── ProfileViewModel.kt     # Loads and updates user preferences in Firestore
│       └── JournalViewModel.kt     # Generates recipes and listens to today's logged meal data
```

---

## Functionality Overview

### 1. Onboarding Screen
- Immersive fullscreen layout featuring a high-resolution local food composition image.
- Vertical gradient fade overlay matching the warm off-white background tint (`#F4F3EF`).
- Left-aligned typography and a primary entry button ("Begin your journey").

### 2. Login Screen ("Enter the Kitchen")
- Simplified authentication with a primary Google Sign-In trigger ("Enter the Kitchen").
- Integrated green header bar (`PrimaryGreen` `#3E4631` background and `SecondaryYellow` `#F9F4E0` typography).
- Handles back-navigation gracefully (pops backstack to onboarding screen rather than closing the app).
- Displays clear configuration alerts if Firebase Console actions require developer setup.

### 3. Nourishment Profile
- Customize dietary focus chips: `Balanced`, `Vegetarian`, `Vegan`, `Keto`, or `High Protein`.
- Quick-toggle pantry staples (oil, flour, rice, salt, ghee, etc.) stored directly under the user's profile document.

### 4. Daily Nourishment Journal & AI Chef
- Tracks daily nourishment across four meal slots: **Breakfast**, **Lunch**, **Dinner**, and **Snacks**.
- Caches and lists today's logged meals in real-time.
- For empty slots, enter available ingredients to query the client-side Vertex AI SDK. It will generate a custom recipe conforming to a strict JSON structure and automatically log the recipe in Firestore.

---

## Database Schema

The app stores data in Cloud Firestore under the following structure:

### User Preferences
- **Collection**: `users`
- **Document ID**: `{userId}`
- **Fields**:
  - `name`: String (default: "Nourished Explorer")
  - `dietaryFocus`: String (e.g., "Vegetarian")
  - `staples`: Array of Strings (e.g., `["Oil", "Atta", "Rice"]`)
  - `createdAt`: Timestamp

### Daily Meal Log
- **Collection**: `users/{userId}/daily_meals`
- **Document ID**: `{YYYY-MM-DD}` (strict 10-character date format, e.g., `2026-06-15`)
- **Fields**:
  - `breakfast` / `lunch` / `dinner` / `snacks`: Map containing:
    - `recipeName`: String
    - `prepTime`: Number (minutes)
    - `ingredientsUsed`: Array of Strings
    - `steps`: Array of Strings
  - `timestamp`: Server Timestamp (used for rule integrity)

---

## Firestore Security Rules

Secure Firestore rules are located in the [firestore.rules](firestore.rules) file. Key security validations include:
- Deny read/write on all documents by default.
- Authenticated read/write allowed strictly for the document owner: `request.auth.uid == userId`.
- Writing to the daily logs requires:
  1. The document ID strictly matches the 10-character `YYYY-MM-DD` date pattern: `^[0-9]{4}-[0-9]{2}-[0-9]{2}$`.
  2. The write payload contains a valid `timestamp` field set to the server-side timestamp value.
  3. Checks are written so that wildcard overrides do not bypass subcollection rules.

---

## Troubleshooting Firebase Configuration

If you click "Enter the Kitchen" and see the error: **"This operation is restricted to administrator only"**:

This is a configuration limitation on your Firebase Project settings. To resolve it:
1. Open your [Firebase Console](https://console.firebase.google.com/).
2. Select your project and navigate to **Authentication** (under Build).
3. Select the **Settings** tab at the top.
4. Click on **User actions** in the left sidebar.
5. Toggle ON **"Enable create (sign-up)"** (which allows new accounts to be registered from the client application).
6. Under the **Sign-in method** tab, ensure the **Anonymous** sign-in provider is enabled (used to verify connection in development).
7. Reload the application.

---

## Compiling & Running Locally

Compile the app using Gradle with Java 20:

```bash
# Verify compilation
JAVA_HOME=/Users/anuraj/Library/Java/JavaVirtualMachines/azul-20.0.1/Contents/Home ./gradlew compileDebugKotlin

# Build debug APK
JAVA_HOME=/Users/anuraj/Library/Java/JavaVirtualMachines/azul-20.0.1/Contents/Home ./gradlew assembleDebug
```
