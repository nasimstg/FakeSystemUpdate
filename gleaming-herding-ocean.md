# Shareable Template Overhaul: 9 Templates with Full Dynamic Data

## Context

The current implementation has 6 shareable templates that were successfully implemented in the previous session. However, user testing revealed that the templates are missing critical dynamic data that makes them feel generic rather than personalized:

**Problems with current templates:**
- Victim name is not displayed prominently (or at all in some templates)
- Prankster name/signature is completely missing
- Time wasted is not shown as a hero stat (buried in secondary stats or missing)
- Exit interview data (reaction type, rating) is collected but barely used in templates
- No story/experience narrative text to make the share feel personal
- QR code is using a placeholder icon instead of the real frame.png from project root

**User requirements for overhaul:**
1. Add 3 NEW templates from `/shareableMockDesign/new/` (total 9 templates)
2. Collect prankster name in exit interview
3. Replace QR placeholder with frame.png across all templates
4. Show dynamic data properly in all templates following "Stack Strategy":
   - Bottom Layer: Background graphics (trophy, glitch lines, grid)
   - Middle Layer: Layout containers (dark boxes where text goes)
   - Top Layer: Dynamic text injection (victim name, prankster name, stats, story)

---

## Part 1: Extend PrankSessionData with Prankster Name

### File: `app/src/main/java/io/softexforge/fakesysupdate/PrankSessionData.kt`

**Add new constructor parameter:**
```kotlin
val pranksterName: String  // Collected in exit interview
```

**Add 3 new computed properties:**
```kotlin
// Observation log text for medical/diagnostic templates
val observationLog: String
    get() = when (reactionType) {
        "panicked" -> "Subject exhibited elevated cortisol levels. Rapid phone tapping detected. Diagnosis: Critical Gullibility."
        "stared" -> "Subject remained motionless for ${formattedDuration}. No vital signs of skepticism detected."
        "called_support" -> "Subject initiated emergency protocol. Attempted contact with external tech support."
        "hit_phone" -> "Subject displayed physical aggression toward device. Rage threshold exceeded."
        else -> "Subject behavior anomalous. Further observation required."
    }

// Achievement story text for trophy/gamer templates
val achievementStory: String
    get() {
        val adjective = when {
            durationMs > 180000 -> "legendary"  // >3min
            durationMs > 60000 -> "epic"        // >1min
            else -> "rare"
        }
        return "Executed a $adjective system update prank. Target fell for the fake loading screen and waited ${formattedDuration} before discovering the truth."
    }

// IQ drop percentage (satirical stat)
val iqDrop: Int
    get() = ((durationMs / 1000 / 10) + (prankRating * 5)).coerceIn(5, 99)
```

---

## Part 2: Update Exit Interview to Collect Prankster Name

### File: `app/src/main/res/layout/bottom_sheet_exit_interview.xml`

**Insert after victim name field (after line 60):**
```xml
<!-- Prankster Name -->
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/input_prankster_name"
    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="12dp"
    app:boxBackgroundColor="@color/charcoal_dark"
    app:boxCornerRadiusBottomEnd="12dp"
    app:boxCornerRadiusBottomStart="12dp"
    app:boxCornerRadiusTopEnd="12dp"
    app:boxCornerRadiusTopStart="12dp"
    app:boxStrokeColor="@color/electric_blue_30"
    app:hintTextColor="@color/slate_400">

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/edit_prankster_name"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="@string/exit_interview_prankster_hint"
        android:inputType="textPersonName"
        android:maxLength="30"
        android:textColor="@color/white"
        android:textSize="15sp" />

</com.google.android.material.textfield.TextInputLayout>
```

**Add 3 more template preview ImageViews (after preview_5 at line 260):**
```xml
<ImageView
    android:id="@+id/preview_6"
    android:layout_width="80dp"
    android:layout_height="140dp"
    android:layout_marginEnd="8dp"
    android:background="@color/charcoal_dark"
    android:contentDescription="Gamer Achievement"
    android:padding="2dp"
    android:scaleType="centerCrop"
    android:src="@drawable/preview_gamer_achievement" />

<ImageView
    android:id="@+id/preview_7"
    android:layout_width="80dp"
    android:layout_height="140dp"
    android:layout_marginEnd="8dp"
    android:background="@color/charcoal_dark"
    android:contentDescription="Kernel Panic"
    android:padding="2dp"
    android:scaleType="centerCrop"
    android:src="@drawable/preview_kernel_panic" />

<ImageView
    android:id="@+id/preview_8"
    android:layout_width="80dp"
    android:layout_height="140dp"
    android:background="@color/charcoal_dark"
    android:contentDescription="Vital Signs"
    android:padding="2dp"
    android:scaleType="centerCrop"
    android:src="@drawable/preview_vital_signs" />
```

### File: `app/src/main/java/io/softexforge/fakesysupdate/ExitInterviewBottomSheet.kt`

**Line 67: Add prankster name field reference:**
```kotlin
val editPrankster = view.findViewById<TextInputEditText>(R.id.edit_prankster_name)
```

**Line 91: Collect prankster name:**
```kotlin
val pranksterName = editPrankster.text?.toString()?.trim() ?: ""
```

**Line 95: Add to PrankSessionData constructor:**
```kotlin
val data = PrankSessionData(
    victimName = victimName,
    pranksterName = pranksterName,  // NEW
    reactionType = reactionType,
    ...
)
```

**Line 113: Update preview array for 9 templates:**
```kotlin
val previewIds = intArrayOf(
    R.id.preview_0, R.id.preview_1, R.id.preview_2,
    R.id.preview_3, R.id.preview_4, R.id.preview_5,
    R.id.preview_6, R.id.preview_7, R.id.preview_8  // NEW
)
```

---

## Part 3: Replace QR Placeholder with frame.png

### Copy frame.png to drawable

Use PowerShell to copy:
```powershell
Copy-Item "C:\Users\USER\AndroidStudioProjects\FakeSystemUpdate\frame.png" `
          "C:\Users\USER\AndroidStudioProjects\FakeSystemUpdate\app\src\main\res\drawable-xxhdpi\frame_qr_code.png"
```

### Update all template XMLs

In all 6 existing template files, find all occurrences of:
```xml
android:src="@drawable/ic_qr_placeholder"
```

Replace with:
```xml
android:src="@drawable/frame_qr_code"
```

Affected files:
- `template_share_stats_base.xml`
- `template_share_stats_variant.xml`
- `template_share_crash_base.xml`
- `template_share_crash_variant.xml`
- `template_share_trophy_base.xml`
- `template_share_trophy_variant.xml` (line 232)

---

## Part 4: Update Existing 6 Templates to Show Dynamic Data

All 6 existing templates need victim name, prankster signature, and time as hero stat.

### Critical Additions to Each Template XML:

**1. Prominent Victim Name Display** — Add near the header:
```xml
<TextView
    android:id="@+id/text_victim_name"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center"
    android:text="Target Name"
    android:textColor="@color/white"
    android:textSize="20sp"
    android:textStyle="bold" />
```

**2. Time Wasted as Hero Stat** — Replace buried time displays:
```xml
<TextView
    android:id="@+id/text_time_hero"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center"
    android:text="14:23"
    android:textColor="@color/electric_blue"
    android:textSize="48sp"
    android:textStyle="bold" />

<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center"
    android:text="TIME WASTED"
    android:textColor="#66FFFFFF"
    android:textSize="12sp"
    android:fontFamily="monospace" />
```

**3. Prankster Signature Footer** — Add near branding:
```xml
<LinearLayout
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="horizontal">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Pranked by: "
        android:textColor="#66FFFFFF"
        android:textSize="11sp" />

    <TextView
        android:id="@+id/text_prankster_signature"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Prankster"
        android:textColor="@color/electric_blue"
        android:textSize="11sp"
        android:textStyle="bold" />

</LinearLayout>
```

---

## Part 5: Add 3 New Template Layouts

### New Template 1: `app/src/main/res/layout/template_share_gamer_achievement.xml`

**Design**: Gold/purple achievement card inspired by Xbox/PlayStation achievements

**Key elements:**
- "ACHIEVEMENT UNLOCKED" banner with gold background
- Large "MASTER TRICKSTER" title
- Victim name displayed as "Target: PlayerName"
- Achievement story text (using `data.achievementStory`)
- Stats grid: Time Wasted + XP Gained
- Reaction badge (color changes based on reaction type)
- QR code + prankster signature footer

**Layout structure:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout ...
    android:background="@drawable/bg_gamer_gradient"
    android:padding="32dp">

    <!-- ACHIEVEMENT UNLOCKED banner -->
    <!-- Achievement title -->
    <!-- Victim name (text_victim_name) -->
    <!-- Story text (text_achievement_story) -->
    <!-- Stats grid: Time + XP -->
    <!-- Reaction badge (text_reaction_badge) -->
    <!-- Footer: QR + prankster signature -->

</LinearLayout>
```

### New Template 2: `app/src/main/res/layout/template_share_kernel_panic.xml`

**Design**: Fake iOS kernel panic crash log with red text on black

**Key elements:**
- "panic(cpu 0 caller ...)" header in monospace red
- "GULLIBILITY_OVERFLOW_DETECTED" error message
- Victim tag as "@user panic at victim.c:line 42"
- Crash log backtrace in monospace
- Large time display as "FAKE SYSTEM UPDATE DURATION"
- "WASTED" stamp with skull emojis
- IQ drop stat
- QR code + operator signature footer

**Layout structure:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout ...
    android:background="@color/share_kernel_black"
    android:padding="28dp">

    <!-- Panic header -->
    <!-- Victim tag (text_victim_tag) -->
    <!-- Crash log lines -->
    <!-- Time hero stat (text_time_hero) -->
    <!-- WASTED stamp -->
    <!-- IQ drop stat (text_iq_drop) -->
    <!-- Footer: QR + operator signature -->

</LinearLayout>
```

### New Template 3: `app/src/main/res/layout/template_share_vital_signs.xml`

**Design**: Cyan medical HUD with diagnostic observation report

**Key elements:**
- "DIAGNOSTIC OBSERVATION REPORT" header with "ACTIVE" badge
- Patient ID showing victim name
- Vital signs grid: Confusion gauge + Exposure time
- Observation log text (using `data.observationLog`)
- Diagnosis row with "CRITICAL GULLIBILITY" warning
- QR code + observer signature footer

**Layout structure:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout ...
    android:background="@drawable/bg_medical_grid"
    android:padding="28dp">

    <!-- Medical header -->
    <!-- Patient ID (text_victim_id) -->
    <!-- Vital signs grid: Confusion % + Time -->
    <!-- Confusion gauge (image_confusion_gauge) -->
    <!-- Observation log (text_observation_log) -->
    <!-- Diagnosis row -->
    <!-- Footer: QR + observer signature -->

</LinearLayout>
```

---

## Part 6: Update ShareImageGenerator for 9 Templates

### File: `app/src/main/java/io/softexforge/fakesysupdate/ShareImageGenerator.kt`

**Line 20: Expand template array:**
```kotlin
private val templateLayouts = intArrayOf(
    R.layout.template_share_stats_base,
    R.layout.template_share_stats_variant,
    R.layout.template_share_crash_base,
    R.layout.template_share_crash_variant,
    R.layout.template_share_trophy_base,
    R.layout.template_share_trophy_variant,
    R.layout.template_share_gamer_achievement,     // NEW
    R.layout.template_share_kernel_panic,           // NEW
    R.layout.template_share_vital_signs             // NEW
)
```

**Line 50: Update populate routing:**
```kotlin
private fun populateTemplate(view: View, data: PrankSessionData) {
    when (data.templateIndex) {
        0 -> populateStatsBase(view, data)
        1 -> populateStatsVariant(view, data)
        2 -> populateCrashBase(view, data)
        3 -> populateCrashVariant(view, data)
        4 -> populateTrophyBase(view, data)
        5 -> populateTrophyVariant(view, data)
        6 -> populateGamerAchievement(view, data)   // NEW
        7 -> populateKernelPanic(view, data)        // NEW
        8 -> populateVitalSigns(view, data)         // NEW
    }
}
```

**Update ALL 6 existing populate methods** to inject victim name and prankster signature:

```kotlin
private fun populateStatsBase(view: View, data: PrankSessionData) {
    // Existing time/stats population...

    // NEW: Add victim name and prankster signature
    view.findViewById<TextView>(R.id.text_victim_name)?.text =
        data.victimName.ifEmpty { "Unknown Target" }
    view.findViewById<TextView>(R.id.text_prankster_signature)?.text =
        data.pranksterName.ifEmpty { "Anonymous" }
}
```

**Add 3 new populate methods:**

```kotlin
private fun populateGamerAchievement(view: View, data: PrankSessionData) {
    val victimDisplay = data.victimName.ifEmpty { "PlayerName" }
    view.findViewById<TextView>(R.id.text_victim_name).text = "Target: $victimDisplay"

    view.findViewById<TextView>(R.id.text_achievement_story).text = data.achievementStory

    view.findViewById<TextView>(R.id.text_time_hero).text = data.formattedDuration

    val xp = (data.durationMs / 1000 * 10).coerceIn(100, 9999)
    view.findViewById<TextView>(R.id.text_xp_gained).text = "+${xp} XP"

    // Reaction badge (dynamic color)
    val badge = view.findViewById<TextView>(R.id.text_reaction_badge)
    when (data.reactionType) {
        "panicked" -> {
            badge.text = "⚠ CRITICAL FAILURE"
            badge.setBackgroundResource(R.drawable.bg_badge_red)
        }
        "stared" -> {
            badge.text = "⏸ SYSTEM IDLE"
            badge.setBackgroundResource(R.drawable.bg_badge_blue)
        }
        "called_support" -> {
            badge.text = "☎ EMERGENCY PROTOCOL"
            badge.setBackgroundResource(R.drawable.bg_badge_orange)
        }
        "hit_phone" -> {
            badge.text = "💥 RAGE DETECTED"
            badge.setBackgroundResource(R.drawable.bg_badge_red)
        }
    }

    view.findViewById<TextView>(R.id.text_prankster_signature).text =
        data.pranksterName.ifEmpty { "Anonymous" }
}

private fun populateKernelPanic(view: View, data: PrankSessionData) {
    val victimTag = data.victimName.ifEmpty { "user" }
    view.findViewById<TextView>(R.id.text_victim_tag).text =
        "@$victimTag panic at victim.c:line 42"

    view.findViewById<TextView>(R.id.text_time_hero).text = data.formattedDurationClock

    view.findViewById<TextView>(R.id.text_iq_drop).text = "-${data.iqDrop}%"

    view.findViewById<TextView>(R.id.text_prankster_signature).text =
        data.pranksterName.ifEmpty { "root" }
}

private fun populateVitalSigns(view: View, data: PrankSessionData) {
    val patientId = data.victimName.ifEmpty { "UNKNOWN" }.uppercase()
    view.findViewById<TextView>(R.id.text_victim_id).text = "PATIENT ID: $patientId"

    view.findViewById<TextView>(R.id.text_confusion_percent).text = "${data.panicLevel}%"

    val gaugeView = view.findViewById<ImageView>(R.id.image_confusion_gauge)
    gaugeView.setImageDrawable(
        PanicGaugeDrawable(
            level = data.panicLevel,
            trackColor = Color.parseColor("#0D2E2E"),
            fillColor = Color.parseColor("#00FFFF"),
            glowColor = Color.parseColor("#4000FFFF")
        )
    )

    view.findViewById<TextView>(R.id.text_time_hero).text = data.formattedDurationClock

    view.findViewById<TextView>(R.id.text_observation_log).text = data.observationLog

    view.findViewById<TextView>(R.id.text_prankster_signature).text =
        if (data.pranksterName.isEmpty()) "Dr. Anonymous" else "Dr. ${data.pranksterName}"
}
```

---

## Part 7: New Resources

### Colors: `app/src/main/res/values/colors.xml`

Add new color resources for the 3 new templates:

```xml
<!-- Gamer Achievement Theme -->
<color name="share_gamer_bg">#1A1028</color>
<color name="share_gamer_gold">#FFD700</color>
<color name="share_gamer_purple">#9D4EDD</color>
<color name="share_gamer_silver">#C0C0C0</color>

<!-- Kernel Panic Theme -->
<color name="share_kernel_black">#0A0A0A</color>
<color name="share_kernel_red">#FF3B30</color>

<!-- Vital Signs Medical Theme -->
<color name="share_medical_bg">#0D1F1F</color>
<color name="share_medical_cyan">#00FFFF</color>
```

### Strings: `app/src/main/res/values/strings.xml`

```xml
<!-- Exit Interview -->
<string name="exit_interview_prankster_hint">Your name (prankster)</string>

<!-- Gamer Template -->
<string name="share_achievement_unlocked">ACHIEVEMENT UNLOCKED</string>
<string name="share_master_trickster">MASTER TRICKSTER</string>

<!-- Kernel Template -->
<string name="share_kernel_panic">panic(cpu 0 caller 0xffffff8012ab62f0):</string>
<string name="share_gullibility_overflow">GULLIBILITY_OVERFLOW_DETECTED</string>

<!-- Medical Template -->
<string name="share_diagnostic_report">DIAGNOSTIC OBSERVATION REPORT</string>
<string name="share_patient_id">PATIENT ID:</string>
<string name="share_confusion_level">CONFUSION</string>
<string name="share_exposure_time">EXPOSURE TIME</string>
<string name="share_observation_log">OBSERVATION LOG:</string>
<string name="share_diagnosis">DIAGNOSIS:</string>
<string name="share_critical_gullibility">CRITICAL GULLIBILITY ⚠</string>
```

### Drawables

**1. Gamer gradient background:** `app/src/main/res/drawable/bg_gamer_gradient.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <gradient
        android:angle="135"
        android:endColor="#1A1028"
        android:startColor="#28104A"
        android:type="linear" />
</shape>
```

**2. Medical grid background:** `app/src/main/res/drawable/bg_medical_grid.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/share_medical_bg" />
</shape>
```

**3. Reaction badge backgrounds:**
- `bg_badge_red.xml` (red, 4dp corners)
- `bg_badge_blue.xml` (blue, 4dp corners)
- `bg_badge_orange.xml` (orange, 4dp corners)

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#FF3B30" />
    <corners android:radius="4dp" />
</shape>
```

### Copy Template Preview Thumbnails

Use PowerShell to copy 3 new preview images:

```powershell
# Gamer Achievement
Copy-Item "C:\Users\USER\AndroidStudioProjects\FakeSystemUpdate\shareableMockDesign\new\gamer_achievement_prank_card\screen.png" `
          "C:\Users\USER\AndroidStudioProjects\FakeSystemUpdate\app\src\main\res\drawable-xxhdpi\preview_gamer_achievement.png"

# Kernel Panic
Copy-Item "C:\Users\USER\AndroidStudioProjects\FakeSystemUpdate\shareableMockDesign\new\kernel_panic_crash_log\screen.png" `
          "C:\Users\USER\AndroidStudioProjects\FakeSystemUpdate\app\src\main\res\drawable-xxhdpi\preview_kernel_panic.png"

# Vital Signs
Copy-Item "C:\Users\USER\AndroidStudioProjects\FakeSystemUpdate\shareableMockDesign\new\vital_signs_prank_diagnostic\screen.png" `
          "C:\Users\USER\AndroidStudioProjects\FakeSystemUpdate\app\src\main\res\drawable-xxhdpi\preview_vital_signs.png"
```

---

## Files Summary

### New Files (10):
| File | Type |
|------|------|
| `template_share_gamer_achievement.xml` | Template layout |
| `template_share_kernel_panic.xml` | Template layout |
| `template_share_vital_signs.xml` | Template layout |
| `bg_gamer_gradient.xml` | Background drawable |
| `bg_medical_grid.xml` | Background drawable |
| `bg_badge_red.xml` | Badge drawable |
| `bg_badge_blue.xml` | Badge drawable |
| `bg_badge_orange.xml` | Badge drawable |
| `frame_qr_code.png` | QR code image (copied from frame.png) |
| `preview_*.png` (x3) | Template thumbnails |

### Modified Files (14):
| File | Changes |
|------|---------|
| `PrankSessionData.kt` | Add `pranksterName` field + 3 computed properties |
| `ExitInterviewBottomSheet.kt` | Collect prankster name, expand to 9 previews |
| `bottom_sheet_exit_interview.xml` | Add prankster field, 3 preview ImageViews |
| `ShareImageGenerator.kt` | Expand to 9 templates, add 3 populate methods, update existing 6 |
| `template_share_stats_base.xml` | Add victim name, prankster signature, replace QR |
| `template_share_stats_variant.xml` | Add victim name, prankster signature, replace QR |
| `template_share_crash_base.xml` | Add victim name, prankster signature, replace QR |
| `template_share_crash_variant.xml` | Add victim name, prankster signature, replace QR |
| `template_share_trophy_base.xml` | Add victim name, prankster signature, replace QR |
| `template_share_trophy_variant.xml` | Add victim name, prankster signature, replace QR |
| `values/colors.xml` | Add 7 new colors |
| `values/strings.xml` | Add prankster hint + template strings |

---

## Implementation Order

1. Copy frame.png to drawable-xxhdpi/frame_qr_code.png
2. Copy 3 new template preview PNGs
3. Add new color resources
4. Add new string resources
5. Create background drawables (gradients, badges)
6. Update PrankSessionData (add pranksterName + 3 computed properties)
7. Update bottom_sheet_exit_interview.xml (prankster field + 3 previews)
8. Update ExitInterviewBottomSheet.kt (collect prankster, handle 9 templates)
9. Update all 6 existing template XMLs (victim name, prankster signature, QR replacement)
10. Create 3 new template XMLs (gamer, kernel, vital signs)
11. Update ShareImageGenerator (expand to 9 templates, add 3 populate methods, update existing 6)
12. Build and test with `gradlew.bat assembleDebug`

---

## Verification

1. `gradlew.bat assembleDebug` builds successfully with no errors
2. Exit interview shows prankster name field
3. Exit interview shows all 9 template previews in scrollable row
4. Each template displays:
   - ✅ Victim name prominently
   - ✅ Prankster signature in footer
   - ✅ Time wasted as hero stat (large, centered, or prominent)
   - ✅ Reaction-based dynamic content (badges, log text)
   - ✅ Real QR code from frame.png
5. New templates match mock designs:
   - Gamer Achievement: Gold/purple with achievement banner
   - Kernel Panic: Red/black crash log style
   - Vital Signs: Cyan medical HUD with observation log
6. All computed properties work:
   - `observationLog` shows different text based on reaction
   - `achievementStory` shows appropriate adjective based on duration
   - `iqDrop` calculates correctly
7. Generated images are 1080px width PNGs
8. Share intent works with all 9 templates
