# Shift Alarm Assistant
### Developed by Emon Howlader

Shift Alarm Assistant is a native Android application designed to automatically synchronize authorized Gmail accounts and incoming SMS messages, identify shift rescheduling notifications, dynamically extract employee names, shift times, and dates, and schedule reliable alarms.

---

## 🌟 Key Features

1. **Dynamic Shift Time Extraction**:
   - Parses 12-hour (e.g. `12:00 PM - 8:00 PM`, `7:00 AM - 3:00 PM`) and 24-hour (e.g. `14:00 - 22:00`) formats.
   - Supports shifts with or without minutes (e.g. `6 AM - 2 PM`).
   - Automatically handles overnight shifts (e.g. `10:00 PM - 6:00 AM`).
   - Subject inspection with intelligent body fallback.
   - Flags discrepancies if email subject and body specify conflicting times.

2. **Dynamic Employee Name Detection**:
   - Dynamically extracts employee names from message greetings (`Dear [Name],`, `Hello [Name],`, `Employee: [Name]`).
   - Supports single-part and multi-part names (e.g., Emon, Rahim Ahmed, Sarah Jenkins).
   - Optional configurable employee name filter to prevent coworker schedules from triggering alarms.

3. **Dynamic Date Extraction & Weekday Validation**:
   - Supports `DD.MM.YYYY`, `DD/MM/YYYY`, `YYYY-MM-DD`, `October 12, 2026`, `12 October 2026`, etc.
   - Validates stated weekday against actual calendar dates (e.g., checks if 12.10.2026 is actually a Monday). Flags discrepancies if mismatched.
   - Validates against current device time; past shifts are marked as expired.

4. **Smart Pre-Shift Reminder Calculation**:
   - Default reminder: **2 hours before shift start** (`Alarm Time = Shift Start - Reminder Duration`).
   - **Reminder Timing Studio**: Presets from 5 minutes to 24 hours plus custom slider (0–168 hours, 0–59 minutes).
   - Multi-tier alert support (e.g., 4h preparation reminder + 2h main alarm + 30m departure call).
   - Real-time live date/time preview.

5. **Dual Alarm Integration**:
   - **Shift Alarm Assistant Internal Scheduler**: Uses Android `AlarmManager.setExactAndAllowWhileIdle()` to guarantee date-specific alarms with high-priority audio & vibration notifications.
   - **Phone Default Clock App Integration**: Dispatches `AlarmClock.ACTION_SET_ALARM` with pre-filled reminder time and label (`SHIFT REMINDER - [Date] - [Time]`).
   - Destination selector: Internal Scheduler, Phone Default Clock, or Both.

6. **Interactive RGB Water-Drop Animations**:
   - Liquid touch effect expanding from user touch coordinates.
   - Glowing circular wave edge blending electric cyan, electric blue, royal violet, and glowing magenta.
   - Respects user animation preferences with a toggle in Settings.

7. **100% Offline-First Persistence**:
   - Built with Android Room Database and Kotlin Coroutines.
   - `BootReceiver` recovers and reschedules all pending alarms after device reboot or timezone changes.

---

## 🛠 Tech Stack

- **Kotlin** & **Jetpack Compose** (Material 3)
- **Room Database** (KSP)
- **Android AlarmManager** & **BroadcastReceiver**
- **NotificationManager** (High-priority Alarm Channel)
- **Gmail API & Google OAuth 2.0 architecture**
- **JUnit 4** Unit Test Suite

---

## 🔐 Google Cloud OAuth 2.0 Setup (Gmail API)

To connect live Gmail synchronization in Google Cloud Console:
1. Open the [Google Cloud Console](https://console.cloud.google.com/).
2. Create or select a project (e.g., `Shift-Alarm-Assistant`).
3. Enable the **Gmail API** in APIs & Services > Library.
4. Configure the OAuth Consent Screen:
   - User Type: External
   - Add Scope: `https://www.googleapis.com/auth/gmail.readonly`
5. Create OAuth 2.0 Client ID:
   - Application type: Android
   - Package name: `com.aistudio.shiftalarm.ehowla`
   - SHA-1 fingerprint: Obtain from your debug or release keystore.
6. The app is pre-configured to query shift notifications using sender filters and keywords (`CuttingEdger-PayRol`, `Shift Re-scheduled`, etc.).

---

## 🏗 Build & Run Instructions

To compile the application in Android Studio:
```bash
gradle assembleDebug
```

To run unit tests:
```bash
gradle :app:testDebugUnitTest
```

---

## 👨‍💻 Credits

**Shift Alarm Assistant**  
Developed by **Emon Howlader**
