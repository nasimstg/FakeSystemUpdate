# 🎭 Fake System Update

> **The ultimate Android prank app that simulates realistic system update screens**

[![Website](https://img.shields.io/badge/Website-fakesysupdate.softexforge.io-blue?style=for-the-badge)](https://fakesysupdate.softexforge.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android)](https://www.android.com/)

A high-fidelity prank application that simulates various manufacturer-specific system update screens with pixel-perfect accuracy. Perfect for harmless pranks on friends and family!

**🌐 Live Website:** [fakesysupdate.softexforge.io](https://fakesysupdate.softexforge.io/)

**👨‍💻 Created by:** [Nasim STG](https://www.nasimstg.dev) | [SoftexForge](https://www.softexforge.io)

---

## ✨ Features

### 🎨 Multiple Update Styles
Authentic recreations of update screens from major manufacturers:
- **Samsung One UI 7** - Complete with version info and security patch details
- **Google Pixel** - Clean, minimal stock Android update design
- **Xiaomi MIUI 16** - Feature-rich update interface with download progress
- **OnePlus OxygenOS 15** - Sleek update screen with build information
- **Huawei EMUI 14** - System optimization messaging
- **Stock Android** - Classic recovery-style update screen

### ⏰ Advanced Scheduling
- **Launch Now** - Immediate prank activation
- **Delay Timer** - Start after X minutes/hours/days
- **DateTime Schedule** - Launch at specific date and time
- **Interval Mode** - Repeat pranks every X hours

### 🎯 Smart Exit Methods
Multiple secret exit techniques to end the prank:
- Triple tap anywhere on screen
- Tap the four corners in sequence
- Shake device detection
- Long press power button (with dialog)
- Long press volume down button

### 🔒 Lockdown Features
- **Screen Pinning** - Makes it extremely difficult to exit
- **Keep Awake** - Prevents screen dimming during prank
- **Immersive Mode** - Hides system navigation bars
- **Full Screen Intent** - Launches over lock screen (for scheduled pranks)

### 📊 Prank Analytics & Sharing
- **Exit Interview** - Collect victim reactions and prank details
- **Shareable Receipts** - Generate stunning prank report images
- **Multiple Templates** - Gamer achievements, kernel panic, system diagnostics, medical reports
- **Reaction Tracking** - Record how victims responded (panicked, confused, laughed, etc.)
- **Time Tracking** - Shows how long the victim waited

### 🎭 Realistic Simulation
- **Non-linear Progress** - Progress bar moves unpredictably (fast then slow)
- **Haptic Feedback** - Occasional vibrations mimic system processing
- **Dynamic Updates** - Live text changes during simulation
- **Style-Specific Animations** - Each manufacturer style has unique animations

### 🛡️ Safety Features
- **Info Icon** - Appears after 15 seconds with exit instructions
- **No Data Collection** - Completely offline, no personal data collected
- **No System Changes** - Pure simulation, no actual file modifications

---

## 📱 Screenshots

*[Screenshots would go here showcasing different update styles and app screens]*

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer
- Android SDK 36
- Kotlin 2.3.10+
- Gradle 9.0.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/FakeSystemUpdate.git
   cd FakeSystemUpdate
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will automatically prompt to sync Gradle
   - Wait for dependencies to download

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click "Run" or press `Shift + F10`

### Building APK

**Debug Build:**
```bash
./gradlew assembleDebug
```

**Release Build:**
```bash
./gradlew assembleRelease
```

The APK will be generated in `app/build/outputs/apk/`

---

## 🏗️ Project Structure

```
FakeSystemUpdate/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/io/softexforge/fakesysupdate/
│   │   │   │   ├── OnboardingActivity.kt          # First-time user onboarding
│   │   │   │   ├── OnboardingWelcomeFragment.kt   # Welcome screen
│   │   │   │   ├── OnboardingHowItWorksFragment.kt # Tutorial
│   │   │   │   ├── OnboardingDisclaimerFragment.kt # Safety information
│   │   │   │   ├── OnboardingAcceptFragment.kt    # Terms acceptance
│   │   │   │   ├── SetupActivity.kt               # Main prank configuration
│   │   │   │   ├── FakeUpdateActivity.kt          # Update simulation engine
│   │   │   │   ├── UpdateCompleteActivity.kt      # Reboot transition screen
│   │   │   │   ├── RevealActivity.kt              # Prank reveal screen
│   │   │   │   ├── ExitInterviewBottomSheet.kt    # Share/reaction collection
│   │   │   │   ├── ShareImageGenerator.kt         # Receipt image generation
│   │   │   │   ├── SettingsActivity.kt            # App settings
│   │   │   │   ├── PrivacyPolicyActivity.kt       # Privacy policy viewer
│   │   │   │   ├── TermsActivity.kt               # Terms of service viewer
│   │   │   │   ├── FakeUpdateWorker.kt            # Background scheduling worker
│   │   │   │   ├── PrankSessionData.kt            # Prank data model
│   │   │   │   └── PanicGaugeDrawable.kt          # Custom gauge view
│   │   │   ├── res/
│   │   │   │   ├── layout/                        # XML layout files
│   │   │   │   ├── drawable/                      # Vector graphics & backgrounds
│   │   │   │   ├── values/                        # Strings, colors, themes
│   │   │   │   └── xml/                           # Preferences & file paths
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/                           # Instrumented tests
│   │   └── test/                                  # Unit tests
│   └── build.gradle.kts                           # Module build configuration
├── gradle/
│   └── libs.versions.toml                         # Version catalog
├── build.gradle.kts                               # Project build configuration
├── settings.gradle.kts                            # Gradle settings
├── README.md                                      # This file
├── DOCUMENTATION.md                               # Technical documentation
└── CONTRIBUTING.md                                # Contribution guidelines
```

---

## 🛠️ Technology Stack

- **Language:** Kotlin 2.3.10
- **Min SDK:** 24 (Android 7.0 Nougat)
- **Target SDK:** 36 (Android 15+)
- **Compile SDK:** 36

### Key Libraries
- **AndroidX Core KTX** 1.17.0 - Kotlin extensions
- **Material Design 3** 1.13.0 - UI components
- **ConstraintLayout** 2.2.1 - Flexible layouts
- **ViewPager2** 1.1.0 - Onboarding carousel
- **WorkManager** 2.11.1 - Background scheduling
- **SplashScreen** 1.0.1 - App startup experience

---

## 🎮 How It Works

1. **Onboarding Flow** - First-time users see tutorial and safety disclaimers
2. **Setup Configuration** - Choose update style, duration, scheduling, and exit method
3. **Hand Over Device** - Give phone to victim casually
4. **Prank Triggers** - Update screen launches at configured time
5. **Realistic Simulation** - Non-linear progress with haptic feedback
6. **Exit via Secret Method** - Prankster uses configured exit technique
7. **Reveal Screen** - Shows "You Got Pranked!" with time wasted
8. **Share Receipt** - Generate and share custom prank report image

---

## 📋 Permissions

The app requests the following permissions:

| Permission | Usage |
|------------|-------|
| `VIBRATE` | Haptic feedback during update simulation |
| `WAKE_LOCK` | Keep screen on during prank |
| `POST_NOTIFICATIONS` | Trigger scheduled pranks (Android 13+) |
| `USE_FULL_SCREEN_INTENT` | Launch prank screen over lock screen |

**No personal data is collected or transmitted.** All prank configurations are stored locally using SharedPreferences.

---

## 🔒 Privacy & Safety

- ✅ **No Data Collection** - Completely offline app
- ✅ **No File Modifications** - Pure visual simulation
- ✅ **No Analytics** - No tracking or telemetry
- ✅ **Clear Exit Methods** - Info icon appears after 15 seconds
- ✅ **Responsible Use** - Designed for harmless pranks only

---

## 📖 Documentation

- **[DOCUMENTATION.md](DOCUMENTATION.md)** - Detailed technical documentation
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contribution guidelines

---

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details on:
- Code style and conventions
- Development workflow
- Pull request process
- Issue reporting

---

## 📝 License

This project is proprietary software. All rights reserved.

**For entertainment purposes only.** Not affiliated with Google, Samsung, Xiaomi, OnePlus, Huawei, or any OS provider.

---

## 🌐 Links

- **Website:** [fakesysupdate.softexforge.io](https://fakesysupdate.softexforge.io/)
- **Creator:** [Nasim STG](https://www.nasimstg.dev)
- **Company:** [SoftexForge](https://www.softexforge.io)
- **Feedback:** [Submit Feedback](https://fakesysupdate.softexforge.io/feedback)
- **Community Pranks:** [View Pranks](https://fakesysupdate.softexforge.io/pranks)

---

## ⚠️ Disclaimer

This application is designed **purely for entertainment purposes**. It simulates system update screens and **does not**:
- Modify or delete any files
- Access personal data
- Make actual system changes
- Harm your device in any way

**Use responsibly.** Do not use this app to harass, bully, or cause distress. Reveal the prank immediately if the subject becomes visibly upset.

---

<p align="center">
  Made with 💙 by <a href="https://www.nasimstg.dev">Nasim STG</a> at <a href="https://www.softexforge.io">SoftexForge</a>
</p>
