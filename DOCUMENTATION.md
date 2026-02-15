# 📚 Technical Documentation

> **Detailed technical documentation for the Fake System Update Android application**

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Application Flow](#application-flow)
3. [Core Components](#core-components)
4. [Update Style System](#update-style-system)
5. [Scheduling System](#scheduling-system)
6. [Exit Methods](#exit-methods)
7. [Share & Receipt System](#share--receipt-system)
8. [Data Models](#data-models)
9. [Permissions & Security](#permissions--security)
10. [Key Algorithms](#key-algorithms)

---

## Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    FakeSystemUpdate App                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐   │
│  │  Onboarding  │──▶│    Setup     │──▶│   Scheduler  │   │
│  │   Module     │   │   Module     │   │   (Worker)   │   │
│  └──────────────┘   └──────────────┘   └──────┬───────┘   │
│                                                 │           │
│                                                 ▼           │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐   │
│  │   Reveal     │◀──│  Complete    │◀──│    Update    │   │
│  │   Module     │   │  Transition  │   │  Simulation  │   │
│  └──────┬───────┘   └──────────────┘   └──────────────┘   │
│         │                                                   │
│         ▼                                                   │
│  ┌──────────────┐   ┌──────────────┐                      │
│  │    Share     │   │   Settings   │                      │
│  │  Generator   │   │   Module     │                      │
│  └──────────────┘   └──────────────┘                      │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack

- **Language:** Kotlin 2.3.10
- **Build System:** Gradle 9.0.0 with Kotlin DSL
- **Android SDK:** Min 24 / Target 36
- **Architecture Pattern:** Activity-based with lifecycle-aware components
- **Background Processing:** WorkManager 2.11.1
- **UI Framework:** Material Design 3 (1.13.0)

### Package Structure

```
io.softexforge.fakesysupdate/
├── Activities (UI Controllers)
│   ├── OnboardingActivity.kt
│   ├── SetupActivity.kt
│   ├── FakeUpdateActivity.kt
│   ├── UpdateCompleteActivity.kt
│   ├── RevealActivity.kt
│   ├── SettingsActivity.kt
│   ├── PrivacyPolicyActivity.kt
│   └── TermsActivity.kt
├── Fragments (Onboarding Steps)
│   ├── OnboardingWelcomeFragment.kt
│   ├── OnboardingHowItWorksFragment.kt
│   ├── OnboardingDisclaimerFragment.kt
│   └── OnboardingAcceptFragment.kt
├── Components
│   ├── ExitInterviewBottomSheet.kt
│   ├── ShareImageGenerator.kt
│   └── PanicGaugeDrawable.kt
├── Workers
│   └── FakeUpdateWorker.kt
└── Models
    └── PrankSessionData.kt
```

---

## Application Flow

### Complete User Journey

```
┌─────────────────┐
│  App Launch     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐    First Time?
│  Splash Screen  │────────Yes──────┐
└────────┬────────┘                 │
         │                          ▼
         No                ┌─────────────────┐
         │                 │   Onboarding    │
         │                 │  (4 Fragments)  │
         │                 └────────┬────────┘
         │                          │
         ▼                          │
┌─────────────────┐◀────────────────┘
│  Setup Screen   │
│  (Configure)    │
└────────┬────────┘
         │
         ▼
    ┌────────┐
    │ Mode?  │
    └───┬────┘
        │
  ┌─────┼──────┬──────────┐
  │     │      │          │
  Now  Delay DateTime  Interval
  │     │      │          │
  └─────┼──────┴──────────┘
        │
        ▼
┌─────────────────┐
│ Fake Update     │
│  Simulation     │
└────────┬────────┘
         │
         ▼
    ┌────────┐
    │ Exit   │
    │Method? │
    └───┬────┘
        │
        ▼
┌─────────────────┐
│ Update Complete │
│   (Reboot)      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Reveal Screen  │
│ "You Got Pranked"│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Exit Interview  │
│  (Optional)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Share Receipt   │
│   (Optional)    │
└─────────────────┘
```

### State Transitions

| From State | To State | Trigger |
|-----------|----------|---------|
| Splash | Onboarding | First launch |
| Splash | Setup | Returning user |
| Onboarding | Setup | Complete onboarding |
| Setup | FakeUpdate | Launch Now |
| Setup | Background | Schedule later |
| Background | FakeUpdate | Scheduled time reached |
| FakeUpdate | UpdateComplete | Duration elapsed |
| UpdateComplete | Reveal | Transition complete |
| Reveal | Setup | New prank / Close |
| Reveal | ExitInterview | Share button |
| ExitInterview | ShareIntent | Generate receipt |

---

## Core Components

### 1. OnboardingActivity

**Purpose:** First-time user experience with tutorial and legal acceptance.

**Components:**
- `ViewPager2` with 4 fragments
- Progress indicator dots
- Skip functionality
- Terms acceptance checkbox

**Fragments:**
1. **WelcomeFragment** - Animated welcome screen
2. **HowItWorksFragment** - 3-step tutorial (Choose style → Set timer → Watch panic)
3. **DisclaimerFragment** - Safety guidelines (4 key points)
4. **AcceptFragment** - Terms acceptance with detailed summary

**Key Features:**
- Prevents app usage without terms acceptance
- Stores acceptance status in SharedPreferences
- Beautiful animations and transitions
- Clear safety messaging

---

### 2. SetupActivity

**Purpose:** Main configuration screen for prank customization.

**Configuration Options:**

#### Update Style Selection
Custom dropdown adapter with visual previews:
- Samsung One UI 7
- Google Pixel
- Xiaomi MIUI 16
- OnePlus OxygenOS 15
- Huawei EMUI 14
- Stock Android Recovery

**Implementation:**
```kotlin
class StyleDropdownAdapter : BaseAdapter {
    // Custom adapter with icon + label
    // Detects device manufacturer and auto-selects
}
```

#### Duration Slider
- Range: 0 (Instant) to 60 minutes (Eternal)
- Non-linear scale for better UX
- Real-time preview of selected duration

#### Launch Mode Chips
- **Now** - Immediate launch
- **Delay** - Start after X time
- **DateTime** - Schedule for specific moment
- **Interval** - Repeat every X hours

#### Secret Exit Method
- Triple Tap
- Four Corners
- Shake Device
- Long Press Power
- Long Press Volume Down

#### Advanced Options
- **Keep Screen Awake** - Prevents screen dimming
- **Screen Pinning** - Makes exit extremely difficult

**Device Detection:**
```kotlin
private fun detectOsSkin(): String {
    val manufacturer = Build.MANUFACTURER.lowercase()
    val brand = Build.BRAND.lowercase()
    return when {
        manufacturer.contains("samsung") -> "samsung"
        manufacturer.contains("xiaomi") || brand.contains("redmi") -> "xiaomi"
        manufacturer.contains("oneplus") -> "oneplus"
        manufacturer.contains("huawei") || manufacturer.contains("honor") -> "huawei"
        manufacturer.contains("google") -> "pixel"
        else -> "stock"
    }
}
```

---

### 3. FakeUpdateActivity

**Purpose:** The core simulation engine that displays the fake update screen.

**Key Responsibilities:**
- Display manufacturer-specific update UI
- Animate non-linear progress bar
- Handle exit methods detection
- Trigger haptic feedback
- Maintain screen wake lock
- Prevent user escape attempts

**Layout Strategy:**
Each manufacturer has a dedicated layout XML:
- `activity_update_samsung.xml`
- `activity_update_pixel.xml`
- `activity_update_xiaomi.xml`
- `activity_update_oneplus.xml`
- `activity_update_huawei.xml`
- `activity_fake_update.xml` (stock)

**Progress Algorithm:**
```kotlin
companion object {
    fun computeNonLinearProgress(fraction: Double): Int {
        // Non-linear stall points for realism
        return when {
            fraction < 0.05 -> (fraction / 0.05 * 4).toInt()
            fraction < 0.15 -> 4 + ((fraction - 0.05) / 0.10 * 1).toInt()
            fraction < 0.35 -> 5 + ((fraction - 0.15) / 0.20 * 5).toInt()
            // ... more stall zones
            else -> min(99, (fraction * 100).toInt())
        }
    }
}
```

**Exit Method Detection:**

| Method | Implementation |
|--------|---------------|
| Triple Tap | Track touch events with timestamp validation |
| Four Corners | Monitor touch coordinates in specific zones |
| Shake | SensorManager listening to accelerometer |
| Long Press Power | Override `onKeyDown` with duration check |
| Long Press Volume | Override `onKeyDown` for VOLUME_DOWN |

**Lockdown Features:**
```kotlin
private fun setupLockdown() {
    // Immersive mode (hide nav bars)
    enterImmersiveMode()
    
    // Broadcast receiver for home button attempts
    registerReceiver(homeButtonReceiver, IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    
    // Screen pinning (if enabled)
    if (appPinningEnabled) startScreenPinning()
    
    // Wake lock
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}
```

**Safety Compliance:**
After 15 seconds, an info icon (ℹ️) appears that users can tap for exit instructions:
```kotlin
private fun showInfoIconDelayed() {
    handler.postDelayed({
        findViewById<ImageButton>(R.id.btn_info)?.visibility = View.VISIBLE
    }, 15000) // 15 seconds
}
```

---

### 4. UpdateCompleteActivity

**Purpose:** Transition screen simulating device reboot.

**Features:**
- Short 2-3 second "Restarting..." animation
- Manufacturer-specific reboot screens
- Automatic transition to Reveal screen
- Completes the illusion of a real update

---

### 5. RevealActivity

**Purpose:** The big reveal - "You Got Pranked!"

**Display Elements:**
- Large emoji (😉) with floating animation
- Bold "YOU GOT PRANKED!" text
- Time wasted display (e.g., "5m 32s wasted")
- Tap count statistics
- Action buttons

**Actions:**
1. **Share Reaction** - Opens exit interview
2. **New Prank** - Return to setup
3. **View Pranks** - Opens community website
4. **Close** - Exit to setup

**Floating Animation:**
```kotlin
private fun startFloatingAnimation() {
    val emoji = findViewById<TextView>(R.id.text_emoji)
    val translateY = PropertyValuesHolder.ofFloat("translationY", 0f, -40f, 0f)
    ObjectAnimator.ofPropertyValuesHolder(emoji, translateY).apply {
        duration = 6000
        repeatCount = ObjectAnimator.INFINITE
        interpolator = AccelerateDecelerateInterpolator()
        start()
    }
}
```

---

### 6. ExitInterviewBottomSheet

**Purpose:** Collect prank details and victim reactions for shareable receipt.

**Input Fields:**
- **Victim Name** (TARGET_ID)
- **Prankster Name** (OPERATOR_ID)
- **Reaction Chips:**
  - Panicked 😱
  - Stared Silently 😶
  - Called IT 📞
  - Laughed 😂
  - Rebooted 🔄

**Prank Rating:**
- Slider from "MINOR GLITCH" to "TOTAL OUTAGE"
- Visual panic gauge indicator

**Receipt Template Selection:**
- Diagnostic Report (technical)
- System Failure (error log style)
- Gamer Achievement
- Kernel Panic
- Medical Observation Report

**Share Message Templates:**
Pre-written messages with variable substitution:
- "Just pranked %1$s for %2$s with a fake system update! 😂"
- "CRITICAL ERROR: %1$s fell for a %2$s system glitch"
- And more...

---

### 7. ShareImageGenerator

**Purpose:** Generate stunning PNG receipts for social sharing.

**Template System:**
Each template is a layout XML rendered to bitmap:
- `template_share_stats_base.xml`
- `template_share_crash_variant.xml`
- `template_share_gamer_achievement.xml`
- `template_share_kernel_panic.xml`
- `template_share_vital_signs.xml`

**Generation Process:**
```kotlin
fun generate(data: PrankSessionData): Bitmap {
    // 1. Inflate XML layout
    val view = inflater.inflate(templateLayout, null)
    
    // 2. Populate data
    view.findViewById<TextView>(R.id.victim_name).text = data.victimName
    view.findViewById<TextView>(R.id.duration).text = data.formattedDuration
    
    // 3. Measure and layout
    view.measure(widthSpec, heightSpec)
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    
    // 4. Draw to bitmap
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    
    return bitmap
}
```

**Branding:**
All receipts include:
- FAKE_SYS_UPDATE branding
- Website URL (fakesysupdate.softexforge.io)
- Optional creator attribution

---

## Update Style System

### Style Characteristics

Each update style recreates the authentic look and feel of manufacturer update screens:

#### 1. Samsung One UI 7
- **Colors:** Dark theme with cyan accents (#00D9FF)
- **Fonts:** Samsung One (fallback: Roboto)
- **Layout:** Centered logo, version badge, progress bar
- **Animations:** Pulsing logo effect
- **Dynamic Elements:**
  - Build number (G998BXXU5FVA3)
  - Security patch date
  - One UI 7.0 branding

#### 2. Google Pixel
- **Colors:** Pure black background, white text
- **Fonts:** Google Sans (fallback: Roboto)
- **Layout:** Minimal, centered progress
- **Animations:** Smooth indeterminate progress
- **Dynamic Elements:**
  - Clean "Installing system update..." text
  - Simple percentage display

#### 3. Xiaomi MIUI 16
- **Colors:** Orange accents (#FF6900), dark background
- **Fonts:** Mi Sans (fallback: Roboto)
- **Layout:** Header with branding, download progress
- **Animations:** Scanning effect, progress bar
- **Dynamic Elements:**
  - Download size (3.8 GB / 5.2 GB)
  - "What's New" section
  - Stop button (non-functional)
  - MIUI 16.0.2.0 version

#### 4. OnePlus OxygenOS 15
- **Colors:** Red accent (#FF0000), dark theme
- **Fonts:** OnePlus Sans (fallback: Roboto)
- **Layout:** Clean minimal design
- **Animations:** Circular progress indicator
- **Dynamic Elements:**
  - Build number (IN2025_15.1.0.216)
  - "Protected by Play Protect" badge

#### 5. Huawei EMUI 14
- **Colors:** Blue accents (#0057FF), gradient background
- **Fonts:** Huawei Sans (fallback: Roboto)
- **Layout:** Centered with system optimization message
- **Animations:** Circular progress with glow
- **Dynamic Elements:**
  - EMUI 14.0.0.162 version
  - Optimization messaging

#### 6. Stock Android (Recovery Mode)
- **Colors:** Black background, cyan text (#00FFFF)
- **Fonts:** Monospace (Courier)
- **Layout:** Terminal-style recovery screen
- **Animations:** Blinking cursor effect
- **Dynamic Elements:**
  - "ANDROID SYSTEM RECOVERY <3e>"
  - Build number display

---

## Scheduling System

### WorkManager Integration

The app uses Android's WorkManager for reliable background scheduling:

```kotlin
class FakeUpdateWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        // Extract configuration from input data
        val style = inputData.getString("style") ?: "stock"
        val duration = inputData.getInt("duration", 10)
        val exitMethod = inputData.getString("exit_method") ?: "triple_tap"
        
        // Launch FakeUpdateActivity
        val intent = Intent(applicationContext, FakeUpdateActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(SetupActivity.EXTRA_STYLE, style)
            putExtra(SetupActivity.EXTRA_DURATION, duration)
            putExtra(SetupActivity.EXTRA_EXIT_METHOD, exitMethod)
            // ... more extras
        }
        applicationContext.startActivity(intent)
        
        return Result.success()
    }
}
```

### Schedule Modes

#### 1. Launch Now
- Immediate: `startActivity(intent)`
- No WorkManager involvement

#### 2. Delay Timer
```kotlin
private fun scheduleDelay() {
    val delayMinutes = calculateDelayInMinutes()
    
    val request = OneTimeWorkRequestBuilder<FakeUpdateWorker>()
        .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
        .setInputData(buildWorkData())
        .addTag("prank_delay")
        .build()
    
    WorkManager.getInstance(this).enqueue(request)
}
```

#### 3. DateTime Scheduled
```kotlin
private fun scheduleOneTime() {
    val selectedTime = calculateSelectedTimestamp()
    val currentTime = System.currentTimeMillis()
    val delayMs = selectedTime - currentTime
    
    if (delayMs <= 0) {
        Toast.makeText(this, R.string.error_past_time, Toast.LENGTH_SHORT).show()
        return
    }
    
    val request = OneTimeWorkRequestBuilder<FakeUpdateWorker>()
        .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
        .setInputData(buildWorkData())
        .addTag("prank_scheduled")
        .build()
    
    WorkManager.getInstance(this).enqueue(request)
}
```

#### 4. Interval Mode
```kotlin
private fun scheduleInterval() {
    val intervalHours = sliderInterval.value.toLong()
    
    val request = PeriodicWorkRequestBuilder<FakeUpdateWorker>(
        intervalHours, TimeUnit.HOURS
    )
        .setInputData(buildWorkData())
        .addTag("prank_interval")
        .build()
    
    WorkManager.getInstance(this).enqueue(request)
}
```

### Cancellation
```kotlin
private fun cancelScheduledWork() {
    WorkManager.getInstance(this).cancelAllWorkByTag("prank_delay")
    WorkManager.getInstance(this).cancelAllWorkByTag("prank_scheduled")
    WorkManager.getInstance(this).cancelAllWorkByTag("prank_interval")
}
```

---

## Exit Methods

### Implementation Details

#### 1. Triple Tap
```kotlin
private var tapCount = 0
private var lastTapTime = 0L

override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    if (exitMethod == "triple_tap" && ev.action == MotionEvent.ACTION_DOWN) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime < 500) { // Within 500ms
            tapCount++
            if (tapCount >= 3) {
                navigateToReveal()
                return true
            }
        } else {
            tapCount = 1
        }
        lastTapTime = currentTime
        totalTapCount++
    }
    return super.dispatchTouchEvent(ev)
}
```

#### 2. Four Corners
```kotlin
private val corners = mutableSetOf<Int>()

override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    if (exitMethod == "four_corners" && ev.action == MotionEvent.ACTION_DOWN) {
        val x = ev.x
        val y = ev.y
        
        val corner = when {
            x < edgeX && y < edgeY -> 0 // Top-left
            x > screenWidth - edgeX && y < edgeY -> 1 // Top-right
            x < edgeX && y > screenHeight - edgeY -> 2 // Bottom-left
            x > screenWidth - edgeX && y > screenHeight - edgeY -> 3 // Bottom-right
            else -> -1
        }
        
        if (corner >= 0) {
            corners.add(corner)
            if (corners.size == 4) {
                navigateToReveal()
                return true
            }
        }
    }
    return super.dispatchTouchEvent(ev)
}
```

#### 3. Shake Detection
```kotlin
private val shakeListener = object : SensorEventListener {
    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
        
        if (acceleration > 15f) { // Threshold for shake
            navigateToReveal()
        }
    }
}

sensorManager?.registerListener(
    shakeListener,
    accelerometer,
    SensorManager.SENSOR_DELAY_UI
)
```

#### 4. Long Press Power Button
```kotlin
private var powerDownTime = 0L

override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (exitMethod == "long_press_power" && keyCode == KeyEvent.KEYCODE_POWER) {
        powerDownTime = System.currentTimeMillis()
        return true
    }
    return super.onKeyDown(keyCode, event)
}

override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
    if (exitMethod == "long_press_power" && keyCode == KeyEvent.KEYCODE_POWER) {
        val pressDuration = System.currentTimeMillis() - powerDownTime
        if (pressDuration >= 2000) { // 2 seconds
            navigateToReveal()
            return true
        }
    }
    return super.onKeyUp(keyCode, event)
}
```

#### 5. Long Press Volume Down
Similar implementation to power button but using `KeyEvent.KEYCODE_VOLUME_DOWN`.

---

## Share & Receipt System

### Data Collection Flow

```
User Exits Prank
      │
      ▼
RevealActivity
      │
      ▼
"Share Reaction" Button
      │
      ▼
ExitInterviewBottomSheet
      │
      ├─ Victim Name Input
      ├─ Prankster Name Input
      ├─ Reaction Selection (Chips)
      ├─ Prank Rating Slider
      ├─ Template Selection
      └─ Message Selection
      │
      ▼
"Generate & Share" Button
      │
      ▼
ShareImageGenerator
      │
      ├─ Inflate Template XML
      ├─ Populate with Data
      ├─ Render to Bitmap
      └─ Save to Cache
      │
      ▼
Share Intent (Image + Text)
```

### Template Variants

**1. Diagnostic Report (Stats Base)**
```
┌─────────────────────────────────────┐
│  DIAGNOSTIC DATA OUTPUT //          │
│                                     │
│  WASTED TIME: 5m 32s                │
│  TARGET: John Doe                   │
│  REACTION LOG: Panicked             │
│  PANIC LEVEL: ████████░░ CRITICAL   │
│                                     │
│  FAKE_SYS_UPDATE CERTIFIED          │
│  fakesysupdate.softexforge.io       │
└─────────────────────────────────────┘
```

**2. Gamer Achievement**
```
┌─────────────────────────────────────┐
│   🏆 ACHIEVEMENT UNLOCKED            │
│                                     │
│      MASTER TRICKSTER               │
│                                     │
│  You stared at a frozen screen      │
│  for 5m 32s without throwing        │
│  your phone!                        │
│                                     │
│  XP GAINED: +9999                   │
│  RAGE LEVEL: EXTREME                │
└─────────────────────────────────────┘
```

**3. Kernel Panic**
```
┌─────────────────────────────────────┐
│  panic(cpu 0 caller 0xffffff80):    │
│  GULLIBILITY_OVERFLOW_DETECTED      │
│                                     │
│  Backtrace:                         │
│  0xffffff80 trust.exe               │
│  0xffffff81 common_sense.dll        │
│                                     │
│  Victim: John Doe                   │
│  Duration: 332 seconds              │
└─────────────────────────────────────┘
```

**4. Medical Report**
```
┌─────────────────────────────────────┐
│  DIAGNOSTIC OBSERVATION REPORT      │
│                                     │
│  PATIENT ID: John Doe               │
│  EXPOSURE TIME: 5m 32s              │
│                                     │
│  OBSERVATION LOG:                   │
│  • User initiated panic protocol    │
│  • Pulse elevated                   │
│                                     │
│  DIAGNOSIS: CRITICAL GULLIBILITY ⚠  │
└─────────────────────────────────────┘
```

---

## Data Models

### PrankSessionData

```kotlin
data class PrankSessionData(
    val victimName: String,
    val pranksterName: String,
    val durationMs: Long,
    val formattedDuration: String,
    val tapCount: Int,
    val exitMethod: String,
    val style: String,
    val reactions: List<String>,
    val prankRating: Int,
    val templateIndex: Int,
    val messageIndex: Int
) : Parcelable
```

### SharedPreferences Keys

```kotlin
// User preferences
const val PREF_ONBOARDING_COMPLETE = "onboarding_complete"
const val PREF_DEFAULT_STYLE = "default_style"
const val PREF_DEFAULT_DURATION = "default_duration"
const val PREF_DEFAULT_EXIT = "default_exit_method"
const val PREF_KEEP_SCREEN_ON_DEFAULT = "keep_screen_on_default"
const val PREF_APP_PINNING_DEFAULT = "app_pinning_default"
```

---

## Permissions & Security

### Required Permissions

```xml
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
```

### Permission Usage

| Permission | Usage | Runtime? |
|-----------|-------|---------|
| VIBRATE | Haptic feedback during simulation | No |
| WAKE_LOCK | Keep screen on during prank | No |
| POST_NOTIFICATIONS | Scheduled prank triggers (Android 13+) | Yes |
| USE_FULL_SCREEN_INTENT | Launch over lock screen | No |

### Security Considerations

**Data Privacy:**
- ✅ No network requests
- ✅ No analytics/tracking
- ✅ No personal data collection
- ✅ All data stored locally via SharedPreferences
- ✅ No cloud storage or backups

**Google Play Compliance:**
- Info icon appears after 15 seconds
- Clear exit instructions available
- No deceptive practices
- Entertainment purpose clearly stated
- Privacy policy and ToS included

---

## Key Algorithms

### Non-Linear Progress Calculation

The progress bar doesn't move linearly - it stalls at realistic points to maximize the prank effect:

```kotlin
fun computeNonLinearProgress(fraction: Double): Int {
    return when {
        // Quick start (0-5% in first 5% of time)
        fraction < 0.05 -> (fraction / 0.05 * 4).toInt()
        
        // First stall (4-5% over 10% of time)
        fraction < 0.15 -> 4 + ((fraction - 0.05) / 0.10 * 1).toInt()
        
        // Moderate progress (5-10% over 20% of time)
        fraction < 0.35 -> 5 + ((fraction - 0.15) / 0.20 * 5).toInt()
        
        // Second stall (10-15% over 15% of time)
        fraction < 0.50 -> 10 + ((fraction - 0.35) / 0.15 * 5).toInt()
        
        // Steady progress (15-45% over 25% of time)
        fraction < 0.75 -> 15 + ((fraction - 0.50) / 0.25 * 30).toInt()
        
        // Final approach (45-99% over remaining time)
        else -> min(99, 45 + ((fraction - 0.75) / 0.25 * 54).toInt())
    }
}
```

**Visualization:**
```
Progress
   100 |                                    ─────
       |                              ─────
    75 |                        ─────
       |                  ─────
    50 |            ─────
       |      ─────
    25 |  ───
       |──
     0 |___________________________________________
       0%          25%         50%        75%   100%
                        Time Fraction
```

### Haptic Feedback Pattern

```kotlin
private fun triggerHaptic() {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    50, // Duration in ms
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }
}

// Triggered every 10-30 seconds randomly during simulation
```

---

## Build Configuration

### Gradle Files

**Project-level `build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
}
```

**Module-level `app/build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.softexforge.fakesysupdate"
    compileSdk = 36
    
    defaultConfig {
        applicationId = "io.softexforge.fakesysupdate"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.core.splashscreen)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

**Version Catalog (`gradle/libs.versions.toml`):**
```toml
[versions]
agp = "9.0.0"
kotlin = "2.3.10"
coreKtx = "1.17.0"
material = "1.13.0"
work = "2.11.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
```

---

## Testing

### Unit Tests
Located in `app/src/test/java/io/softexforge/fakesysupdate/`

**Example Test:**
```kotlin
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    
    @Test
    fun nonLinearProgress_startsLow() {
        val progress = FakeUpdateActivity.computeNonLinearProgress(0.05)
        assertTrue(progress < 10)
    }
}
```

### Instrumented Tests
Located in `app/src/androidTest/java/io/softexforge/fakesysupdate/`

---

## Debugging & Logging

### Debug BuildConfig
```kotlin
if (BuildConfig.DEBUG) {
    Log.d("FakeUpdate", "Current progress: $currentProgress%")
    Log.d("FakeUpdate", "Exit method: $exitMethod")
}
```

### Common Debug Points
- Progress calculation
- Exit method detection
- WorkManager scheduling
- Bitmap generation

---

## Performance Considerations

### Memory Management
- Bitmaps are recycled after sharing: `bitmap.recycle()`
- Views are properly destroyed in `onDestroy()`
- Sensor listeners unregistered when not needed

### Battery Optimization
- WakeLock released when prank ends
- WorkManager respects Doze mode
- Minimal background processing

---

## Future Enhancements

Potential features for future versions:
- Windows 11 update screen style
- iOS 18 update screen style
- Customizable progress speed
- Sound effects (typing, beeping)
- Multiple language support
- Dark mode themes for all styles
- Video recording of victim reactions
- Cloud sync for prank statistics

---

## Resources & Links

- **Official Website:** https://fakesysupdate.softexforge.io/
- **Creator:** https://www.nasimstg.dev
- **Company:** https://www.softexforge.io
- **Material Design Guidelines:** https://m3.material.io/
- **WorkManager Documentation:** https://developer.android.com/topic/libraries/architecture/workmanager

---

**Last Updated:** February 2026

© 2026 SoftexForge - All Rights Reserved
