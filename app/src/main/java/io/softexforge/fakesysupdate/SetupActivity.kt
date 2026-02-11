package io.softexforge.fakesysupdate

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class SetupActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
        const val EXTRA_KEEP_SCREEN_ON = "extra_keep_screen_on"
        const val EXTRA_STYLE = "extra_style"
        const val EXTRA_EXIT_METHOD = "extra_exit_method"
        const val EXTRA_APP_PINNING = "extra_app_pinning"
        const val EXTRA_TAP_COUNT = "extra_tap_count"
        const val WORK_TAG = "fake_update_scheduled"
    }

    private var selectedDuration = 30
    private var keepScreenOn = true
    private var selectedStyle = "stock"
    private var selectedExitMethod = "triple_tap"
    private var appPinningEnabled = true
    private var scheduleMode = "now"
    private var scheduledTimeMillis = 0L
    private var intervalHours = 4
    private var delayValue = 5
    private var delayUnit = "min" // "min", "hrs", "days"

    private var selectedDateMillis = 0L
    private var selectedHour = 12
    private var selectedMinute = 0

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                executeSchedule()
            } else {
                Toast.makeText(this, "Notification permission required for scheduling", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        supportActionBar?.hide()
        title = ""

        loadDefaults()
        displayDeviceInfo()
        setupSettingsButton()
        setupStyleDropdown()
        setupDurationSlider()
        setupScreenToggle()
        setupExitMethodDropdown()
        setupAppPinningToggle()
        setupScheduleMode()
        setupDelayPicker()
        setupDateTimePickers()
        setupIntervalSlider()
        setupCancelButton()
        setupStartButton()
        checkExistingSchedule()
    }

    private fun loadDefaults() {
        val prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE)
        val savedStyle = prefs.getString(SettingsActivity.KEY_DEFAULT_STYLE, null)
        if (savedStyle != null) {
            selectedStyle = savedStyle
        }
        val savedDuration = prefs.getInt(SettingsActivity.KEY_DEFAULT_DURATION, -1)
        if (savedDuration > 0) {
            selectedDuration = savedDuration
        }
        val savedExit = prefs.getString(SettingsActivity.KEY_DEFAULT_EXIT, null)
        if (savedExit != null) {
            selectedExitMethod = savedExit
        }
    }

    private fun setupSettingsButton() {
        findViewById<ImageButton>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun displayDeviceInfo() {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val androidVersion = Build.VERSION.RELEASE
        val sdkLevel = Build.VERSION.SDK_INT
        val brand = Build.BRAND.replaceFirstChar { it.uppercase() }
        val device = Build.DEVICE
        val osInfo = detectOsSkin()

        val info = buildString {
            append("$manufacturer $model\n")
            append("Android $androidVersion (API $sdkLevel)\n")
            if (osInfo != null) {
                append("$osInfo\n")
            }
            append("Brand: $brand | Device: $device")
        }

        findViewById<TextView>(R.id.text_device_info).text = info
    }

    private fun detectOsSkin(): String? {
        getSystemProperty("ro.mi.os.version.name")?.let { version ->
            return "HyperOS $version"
        }
        getSystemProperty("ro.miui.ui.version.name")?.let { version ->
            return "MIUI $version"
        }
        getSystemProperty("ro.build.version.oneui")?.let { raw ->
            val major = raw.take(1)
            val minor = if (raw.length > 1) raw.substring(1, 2) else "0"
            return "One UI $major.$minor"
        }
        getSystemProperty("ro.oxygen.version")?.let { version ->
            return "OxygenOS $version"
        }
        getSystemProperty("ro.build.version.oplusrom")?.let { version ->
            return "ColorOS $version"
        }
        getSystemProperty("ro.build.version.emui")?.let { raw ->
            val version = raw.removePrefix("EmotionUI_")
            return "EMUI $version"
        }
        getSystemProperty("ro.build.version.realmeui")?.let { version ->
            return "Realme UI $version"
        }
        getSystemProperty("ro.vivo.os.version")?.let { version ->
            return "OriginOS $version"
        }
        return null
    }

    private fun getSystemProperty(key: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop", key))
            val value = process.inputStream.bufferedReader().readLine()?.trim()
            process.waitFor()
            if (value.isNullOrBlank()) null else value
        } catch (_: Exception) {
            null
        }
    }

    private fun detectStyleFromDevice(): Pair<String, Int> {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("samsung") -> "samsung" to 0
            manufacturer.contains("google") -> "pixel" to 1
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> "xiaomi" to 2
            manufacturer.contains("oneplus") || manufacturer.contains("oppo") || manufacturer.contains("realme") -> "oneplus" to 3
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> "huawei" to 4
            else -> "stock" to 5
        }
    }

    private fun styleToPosition(style: String): Int = when (style) {
        "samsung" -> 0
        "pixel" -> 1
        "xiaomi" -> 2
        "oneplus" -> 3
        "huawei" -> 4
        "stock" -> 5
        else -> 5
    }

    // ==================== Style Dropdown with Brand Colors ====================

    private data class StyleItem(val brandName: String, val osSubtitle: String, val colorResId: Int)

    private inner class StyleDropdownAdapter(
        context: Context,
        private val items: List<StyleItem>
    ) : ArrayAdapter<StyleItem>(context, R.layout.item_dropdown_style, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createItemView(position, convertView, parent)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createItemView(position, convertView, parent)
        }

        private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_dropdown_style, parent, false)
            val item = items[position]

            val dot = view.findViewById<View>(R.id.dot_color)
            val dotBg = GradientDrawable()
            dotBg.shape = GradientDrawable.OVAL
            dotBg.setColor(ContextCompat.getColor(context, item.colorResId))
            dot.background = dotBg

            view.findViewById<TextView>(R.id.text_brand_name).text = item.brandName
            view.findViewById<TextView>(R.id.text_os_subtitle).text = item.osSubtitle

            return view
        }
    }

    private fun setupStyleDropdown() {
        val styleItems = listOf(
            StyleItem("Samsung", "One UI", R.color.samsung_ring_fill),
            StyleItem("Google Pixel", "Stock Android", R.color.pixel_blue),
            StyleItem("Xiaomi", "MIUI / HyperOS", R.color.xiaomi_orange),
            StyleItem("OnePlus", "OxygenOS", R.color.oneplus_green),
            StyleItem("Huawei", "EMUI", R.color.huawei_blue),
            StyleItem("Stock Android", "AOSP Recovery", R.color.stock_android_white)
        )

        val adapter = StyleDropdownAdapter(this, styleItems)
        val dropdown = findViewById<AutoCompleteTextView>(R.id.dropdown_style)
        dropdown.setAdapter(adapter)
        dropdown.setDropDownBackgroundDrawable(
            ContextCompat.getDrawable(this, R.drawable.bg_dropdown_popup)
        )

        // Use saved default; fall back to device detection if no saved preference
        val prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE)
        if (!prefs.contains(SettingsActivity.KEY_DEFAULT_STYLE)) {
            val (detectedStyle, _) = detectStyleFromDevice()
            selectedStyle = detectedStyle
        }
        val pos = styleToPosition(selectedStyle)
        dropdown.setText(styleItems[pos].brandName, false)

        dropdown.setOnItemClickListener { _, _, position, _ ->
            selectedStyle = when (position) {
                0 -> "samsung"
                1 -> "pixel"
                2 -> "xiaomi"
                3 -> "oneplus"
                4 -> "huawei"
                5 -> "stock"
                else -> "stock"
            }
            dropdown.setText(styleItems[position].brandName, false)
        }
    }

    // ==================== Exit Method Dropdown with Icons ====================

    private data class ExitMethodItem(val label: String, val iconResId: Int)

    private inner class ExitMethodAdapter(
        context: Context,
        private val items: List<ExitMethodItem>
    ) : ArrayAdapter<ExitMethodItem>(context, R.layout.item_dropdown_simple, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createItemView(position, convertView, parent)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createItemView(position, convertView, parent)
        }

        private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_dropdown_simple, parent, false)
            val item = items[position]

            val icon = view.findViewById<ImageView>(R.id.icon)
            icon.setImageResource(item.iconResId)
            icon.imageTintList = ContextCompat.getColorStateList(context, R.color.electric_blue)

            view.findViewById<TextView>(R.id.text_label).text = item.label

            return view
        }
    }

    private fun setupExitMethodDropdown() {
        val exitItems = listOf(
            ExitMethodItem("Triple Tap", R.drawable.ic_touch_app),
            ExitMethodItem("Long Press", R.drawable.ic_touch),
            ExitMethodItem("Volume Up (3x)", R.drawable.ic_volume_up),
            ExitMethodItem("Shake Device", R.drawable.ic_vibration)
        )

        val adapter = ExitMethodAdapter(this, exitItems)
        val dropdown = findViewById<AutoCompleteTextView>(R.id.dropdown_exit_method)
        dropdown.setAdapter(adapter)
        dropdown.setDropDownBackgroundDrawable(
            ContextCompat.getDrawable(this, R.drawable.bg_dropdown_popup)
        )

        val exitPosition = when (selectedExitMethod) {
            "triple_tap" -> 0
            "long_press" -> 1
            "volume_up" -> 2
            "shake" -> 3
            else -> 0
        }
        dropdown.setText(exitItems[exitPosition].label, false)

        dropdown.setOnItemClickListener { _, _, position, _ ->
            selectedExitMethod = when (position) {
                0 -> "triple_tap"
                1 -> "long_press"
                2 -> "volume_up"
                3 -> "shake"
                else -> "triple_tap"
            }
            dropdown.setText(exitItems[position].label, false)
        }
    }

    private fun formatDuration(minutes: Int): String {
        return getString(R.string.format_duration_short, minutes, 0)
    }

    private fun setupDurationSlider() {
        val slider = findViewById<Slider>(R.id.slider_duration)
        val label = findViewById<TextView>(R.id.text_duration_value)
        slider.value = selectedDuration.toFloat()
        label.text = formatDuration(selectedDuration)
        slider.addOnChangeListener { _, value, _ ->
            selectedDuration = value.toInt()
            label.text = formatDuration(selectedDuration)
        }
    }

    private fun setupScreenToggle() {
        val switch = findViewById<SwitchMaterial>(R.id.switch_screen_on)
        switch.isChecked = true
        switch.setOnCheckedChangeListener { _, isChecked ->
            keepScreenOn = isChecked
        }
    }

    private fun setupAppPinningToggle() {
        val switch = findViewById<SwitchMaterial>(R.id.switch_app_pinning)
        switch.isChecked = true
        switch.setOnCheckedChangeListener { _, isChecked ->
            appPinningEnabled = isChecked
        }
    }

    // ==================== Schedule Mode Toggle Group ====================

    private fun setupScheduleMode() {
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggle_schedule_mode)
        val groupDelay = findViewById<LinearLayout>(R.id.group_delay)
        val groupDatetime = findViewById<LinearLayout>(R.id.group_datetime)
        val groupInterval = findViewById<LinearLayout>(R.id.group_interval)
        val switchPinning = findViewById<SwitchMaterial>(R.id.switch_app_pinning)
        val btnStart = findViewById<MaterialButton>(R.id.btn_start_prank)

        val btnNow = findViewById<MaterialButton>(R.id.btn_mode_now)
        val btnDelay = findViewById<MaterialButton>(R.id.btn_mode_delay)
        val btnDatetime = findViewById<MaterialButton>(R.id.btn_mode_datetime)
        val btnInterval = findViewById<MaterialButton>(R.id.btn_mode_interval)

        val allModeButtons = listOf(btnNow, btnDelay, btnDatetime, btnInterval)
        val modeKeys = listOf("now", "delay", "datetime", "interval")

        fun updateModeUI() {
            groupDelay.visibility = if (scheduleMode == "delay") View.VISIBLE else View.GONE
            groupDatetime.visibility = if (scheduleMode == "datetime") View.VISIBLE else View.GONE
            groupInterval.visibility = if (scheduleMode == "interval") View.VISIBLE else View.GONE

            if (scheduleMode != "now") {
                switchPinning.isChecked = false
                switchPinning.isEnabled = false
                appPinningEnabled = false
                btnStart.text = getString(R.string.btn_schedule_prank)
            } else {
                switchPinning.isEnabled = true
                switchPinning.isChecked = true
                appPinningEnabled = true
                btnStart.text = getString(R.string.btn_start_prank)
            }

            // Update button backgrounds: selected gets electric_blue tint
            val selectedColor = ContextCompat.getColorStateList(this, R.color.electric_blue)
            val defaultColor = ContextCompat.getColorStateList(this, android.R.color.transparent)
            val selectedTextColor = ContextCompat.getColor(this, R.color.charcoal_dark)
            val defaultTextColor = ContextCompat.getColor(this, R.color.white)

            for (i in allModeButtons.indices) {
                val btn = allModeButtons[i]
                val isSelected = scheduleMode == modeKeys[i]
                btn.backgroundTintList = if (isSelected) selectedColor else defaultColor
                btn.setTextColor(if (isSelected) selectedTextColor else defaultTextColor)
                btn.iconTint = ContextCompat.getColorStateList(
                    this, if (isSelected) R.color.charcoal_dark else R.color.white
                )
            }
        }

        // Initial state
        updateModeUI()

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            scheduleMode = when (checkedId) {
                R.id.btn_mode_now -> "now"
                R.id.btn_mode_delay -> "delay"
                R.id.btn_mode_datetime -> "datetime"
                R.id.btn_mode_interval -> "interval"
                else -> "now"
            }
            updateModeUI()
        }
    }

    private fun setupDelayPicker() {
        val slider = findViewById<Slider>(R.id.slider_delay)
        val label = findViewById<TextView>(R.id.text_delay_value)
        val unitToggle = findViewById<MaterialButtonToggleGroup>(R.id.toggle_delay_unit)
        val btnMin = findViewById<MaterialButton>(R.id.btn_unit_min)
        val btnHrs = findViewById<MaterialButton>(R.id.btn_unit_hrs)
        val btnDays = findViewById<MaterialButton>(R.id.btn_unit_days)

        val unitButtons = listOf(btnMin, btnHrs, btnDays)
        val unitKeys = listOf("min", "hrs", "days")

        fun updateDelayLabel() {
            label.text = when (delayUnit) {
                "min" -> getString(R.string.format_delay_minutes, delayValue)
                "hrs" -> getString(R.string.format_delay_hours, delayValue)
                "days" -> getString(R.string.format_delay_days, delayValue)
                else -> getString(R.string.format_delay_minutes, delayValue)
            }
        }

        fun updateSliderRange() {
            when (delayUnit) {
                "min" -> { slider.valueFrom = 1f; slider.valueTo = 120f; slider.stepSize = 1f }
                "hrs" -> { slider.valueFrom = 1f; slider.valueTo = 48f; slider.stepSize = 1f }
                "days" -> { slider.valueFrom = 1f; slider.valueTo = 30f; slider.stepSize = 1f }
            }
            delayValue = slider.valueFrom.toInt()
            slider.value = slider.valueFrom
            updateDelayLabel()
        }

        fun updateUnitButtonColors() {
            val selectedColor = ContextCompat.getColorStateList(this, R.color.electric_blue)
            val defaultColor = ContextCompat.getColorStateList(this, android.R.color.transparent)
            val selectedTextColor = ContextCompat.getColor(this, R.color.charcoal_dark)
            val defaultTextColor = ContextCompat.getColor(this, R.color.white)
            for (i in unitButtons.indices) {
                val isSelected = delayUnit == unitKeys[i]
                unitButtons[i].backgroundTintList = if (isSelected) selectedColor else defaultColor
                unitButtons[i].setTextColor(if (isSelected) selectedTextColor else defaultTextColor)
            }
        }

        // Initial state
        updateSliderRange()
        updateUnitButtonColors()

        slider.addOnChangeListener { _, value, _ ->
            delayValue = value.toInt()
            updateDelayLabel()
        }

        unitToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            delayUnit = when (checkedId) {
                R.id.btn_unit_min -> "min"
                R.id.btn_unit_hrs -> "hrs"
                R.id.btn_unit_days -> "days"
                else -> "min"
            }
            updateSliderRange()
            updateUnitButtonColors()
        }
    }

    private fun setupDateTimePickers() {
        val btnDate = findViewById<MaterialButton>(R.id.btn_pick_date)
        val btnTime = findViewById<MaterialButton>(R.id.btn_pick_time)
        val textDateTime = findViewById<TextView>(R.id.text_scheduled_datetime)

        btnDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pick Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            picker.show(supportFragmentManager, "DATE_PICKER")
            picker.addOnPositiveButtonClickListener { dateMillis ->
                selectedDateMillis = dateMillis
                updateScheduledDisplay(textDateTime)
            }
        }

        btnTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Pick Time")
                .build()
            picker.show(supportFragmentManager, "TIME_PICKER")
            picker.addOnPositiveButtonClickListener {
                selectedHour = picker.hour
                selectedMinute = picker.minute
                updateScheduledDisplay(textDateTime)
            }
        }
    }

    private fun updateScheduledDisplay(textView: TextView) {
        if (selectedDateMillis == 0L) return

        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        scheduledTimeMillis = calendar.timeInMillis

        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        textView.text = getString(R.string.label_scheduled_for, formatter.format(calendar.time))
        textView.visibility = View.VISIBLE
    }

    private fun setupIntervalSlider() {
        val slider = findViewById<Slider>(R.id.slider_interval)
        val label = findViewById<TextView>(R.id.text_interval_value)
        slider.value = 4f
        label.text = getString(R.string.format_hours, 4)
        slider.addOnChangeListener { _, value, _ ->
            intervalHours = value.toInt()
            label.text = getString(R.string.format_hours, intervalHours)
        }
    }

    private fun setupCancelButton() {
        findViewById<MaterialButton>(R.id.btn_cancel_schedule).setOnClickListener {
            cancelScheduledWork()
        }
    }

    private fun setupStartButton() {
        findViewById<MaterialButton>(R.id.btn_start_prank).setOnClickListener {
            when (scheduleMode) {
                "now" -> launchNow()
                "delay", "datetime", "interval" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        executeSchedule()
                    }
                }
            }
        }
    }

    private fun launchNow() {
        val intent = Intent(this, FakeUpdateActivity::class.java).apply {
            putExtra(EXTRA_DURATION_MINUTES, selectedDuration)
            putExtra(EXTRA_KEEP_SCREEN_ON, keepScreenOn)
            putExtra(EXTRA_STYLE, selectedStyle)
            putExtra(EXTRA_EXIT_METHOD, selectedExitMethod)
            putExtra(EXTRA_APP_PINNING, appPinningEnabled)
        }
        startActivity(intent)
        finish()
    }

    private fun executeSchedule() {
        when (scheduleMode) {
            "delay" -> scheduleDelay()
            "datetime" -> scheduleOneTime()
            "interval" -> scheduleInterval()
        }
    }

    private fun scheduleOneTime() {
        val delayMillis = scheduledTimeMillis - System.currentTimeMillis()
        if (delayMillis <= 0) {
            Toast.makeText(this, R.string.error_past_time, Toast.LENGTH_SHORT).show()
            return
        }

        val request = OneTimeWorkRequestBuilder<FakeUpdateWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(buildWorkData())
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork("fake_update_onetime", ExistingWorkPolicy.REPLACE, request)

        Toast.makeText(this, R.string.toast_prank_scheduled, Toast.LENGTH_SHORT).show()
        checkExistingSchedule()
    }

    private fun scheduleDelay() {
        val delayMillis = when (delayUnit) {
            "min" -> delayValue.toLong() * 60 * 1000
            "hrs" -> delayValue.toLong() * 60 * 60 * 1000
            "days" -> delayValue.toLong() * 24 * 60 * 60 * 1000
            else -> delayValue.toLong() * 60 * 1000
        }

        val request = OneTimeWorkRequestBuilder<FakeUpdateWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(buildWorkData())
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork("fake_update_delay", ExistingWorkPolicy.REPLACE, request)

        val label = when (delayUnit) {
            "min" -> getString(R.string.format_delay_minutes, delayValue)
            "hrs" -> getString(R.string.format_delay_hours, delayValue)
            "days" -> getString(R.string.format_delay_days, delayValue)
            else -> getString(R.string.format_delay_minutes, delayValue)
        }
        Toast.makeText(this, "${getString(R.string.toast_prank_scheduled)} Starts in $label", Toast.LENGTH_SHORT).show()
        checkExistingSchedule()
    }

    private fun scheduleInterval() {
        val request = PeriodicWorkRequestBuilder<FakeUpdateWorker>(
            intervalHours.toLong(), TimeUnit.HOURS
        )
            .setInputData(buildWorkData())
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("fake_update_periodic", ExistingPeriodicWorkPolicy.REPLACE, request)

        Toast.makeText(this, R.string.toast_prank_scheduled, Toast.LENGTH_SHORT).show()
        checkExistingSchedule()
    }

    private fun buildWorkData(): Data {
        return Data.Builder()
            .putString("style", selectedStyle)
            .putInt("duration_minutes", selectedDuration)
            .putBoolean("keep_screen_on", keepScreenOn)
            .putString("exit_method", selectedExitMethod)
            .build()
    }

    private fun checkExistingSchedule() {
        val btnCancel = findViewById<MaterialButton>(R.id.btn_cancel_schedule)
        WorkManager.getInstance(this).getWorkInfosByTag(WORK_TAG).addListener({
            try {
                val workInfos = WorkManager.getInstance(this).getWorkInfosByTag(WORK_TAG).get()
                val hasActive = workInfos.any {
                    it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
                }
                runOnUiThread {
                    btnCancel.visibility = if (hasActive) View.VISIBLE else View.GONE
                }
            } catch (_: Exception) {}
        }, ContextCompat.getMainExecutor(this))
    }

    private fun cancelScheduledWork() {
        WorkManager.getInstance(this).cancelAllWorkByTag(WORK_TAG)
        Toast.makeText(this, R.string.toast_schedule_cancelled, Toast.LENGTH_SHORT).show()
        findViewById<MaterialButton>(R.id.btn_cancel_schedule).visibility = View.GONE
    }
}
