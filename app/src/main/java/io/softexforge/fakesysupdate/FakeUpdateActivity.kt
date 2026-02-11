package io.softexforge.fakesysupdate

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import kotlin.math.sqrt

class FakeUpdateActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var percentText: TextView

    private var timer: CountDownTimer? = null
    private var totalDurationMs: Long = 30 * 60 * 1000L
    private var currentProgress = 0

    private var exitMethod = "triple_tap"
    private var isExiting = false
    private var appPinningEnabled = true
    private var prankStartTime = 0L
    private var currentStyle = "stock"

    // Total tap count for share stats
    private var totalTapCount = 0

    // Triple tap detection
    private var tapCount = 0
    private var lastTapTime = 0L

    // Long press detection
    private var longPressDownTime = 0L
    private val longPressDuration = 3000L

    // Volume up detection
    private var volumeUpCount = 0
    private var lastVolumeUpTime = 0L

    // Shake detection
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var shakeCount = 0
    private var lastShakeTime = 0L
    private var lastAccelX = 0f
    private var lastAccelY = 0f
    private var lastAccelZ = 0f
    private var accelInitialized = false

    // Screen-off receiver to counter power button
    private var screenOffReceiver: BroadcastReceiver? = null
    private var wakeLock: PowerManager.WakeLock? = null

    // Periodic immersive mode re-entry
    private val immersiveHandler = Handler(Looper.getMainLooper())
    private val immersiveRunnable = object : Runnable {
        override fun run() {
            enterImmersiveMode()
            immersiveHandler.postDelayed(this, 2000)
        }
    }

    // Edge blocking dimensions (computed once in onCreate)
    private var topEdge = 0
    private var bottomEdge = 0
    private var sideEdge = 0
    private var screenHeight = 0
    private var screenWidth = 0

    private val shakeListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!accelInitialized) {
                lastAccelX = event.values[0]
                lastAccelY = event.values[1]
                lastAccelZ = event.values[2]
                accelInitialized = true
                return
            }

            val deltaX = event.values[0] - lastAccelX
            val deltaY = event.values[1] - lastAccelY
            val deltaZ = event.values[2] - lastAccelZ

            lastAccelX = event.values[0]
            lastAccelY = event.values[1]
            lastAccelZ = event.values[2]

            val acceleration = sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble())
            if (acceleration > 12) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime > 500) {
                    shakeCount++
                    lastShakeTime = now
                    if (shakeCount >= 3) {
                        shakeCount = 0
                        navigateToReveal()
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentStyle = intent.getStringExtra(SetupActivity.EXTRA_STYLE) ?: "stock"
        prankStartTime = System.currentTimeMillis()
        val layoutRes = when (currentStyle) {
            "samsung" -> R.layout.activity_update_samsung
            "pixel" -> R.layout.activity_update_pixel
            "xiaomi" -> R.layout.activity_update_xiaomi
            "oneplus" -> R.layout.activity_update_oneplus
            "huawei" -> R.layout.activity_update_huawei
            else -> R.layout.activity_fake_update
        }
        setContentView(layoutRes)

        if (currentStyle == "huawei") {
            window.statusBarColor = getColor(R.color.huawei_light_bg)
            window.navigationBarColor = getColor(R.color.huawei_light_bg)
        }

        // Xiaomi: hide interactive buttons (Stop / Reboot Now)
        if (currentStyle == "xiaomi") {
            findViewById<View?>(R.id.buttons_row)?.visibility = View.GONE
        }

        // Samsung: set dynamic time and battery from actual device
        if (currentStyle == "samsung") {
            setupSamsungDynamicInfo()
        }

        exitMethod = intent.getStringExtra(SetupActivity.EXTRA_EXIT_METHOD) ?: "triple_tap"
        appPinningEnabled = intent.getBooleanExtra(SetupActivity.EXTRA_APP_PINNING, true)
        val durationMinutes = intent.getIntExtra(SetupActivity.EXTRA_DURATION_MINUTES, 30)
        val keepScreenOn = intent.getBooleanExtra(SetupActivity.EXTRA_KEEP_SCREEN_ON, true)
        totalDurationMs = durationMinutes * 60 * 1000L

        progressBar = findViewById(R.id.progress_update)
        percentText = findViewById(R.id.text_percent)

        if (keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        computeEdgeDimensions()
        setupLockdown()
        enterImmersiveMode()
        if (appPinningEnabled) {
            startScreenPinning()
        }
        startProgressTimer()
        startStyleAnimations()

        if (exitMethod == "shake") {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            accelerometer?.let {
                sensorManager?.registerListener(shakeListener, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    private fun startScreenPinning() {
        // Screen pinning locks the app at the OS level:
        // - Notification panel completely blocked
        // - Home, Recent apps, back gesture all disabled
        // - Navigation bar hidden
        // - Status bar hidden
        // The system shows a brief "Screen pinned" toast
        try {
            startLockTask()
        } catch (_: Exception) {
            // Fallback: edge blocking + immersive mode still active
        }
    }

    private fun stopScreenPinning() {
        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) {
                stopLockTask()
            }
        } catch (_: Exception) {}
    }

    private fun computeEdgeDimensions() {
        val dm = resources.displayMetrics
        val density = dm.density

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            val size = android.graphics.Point()
            @Suppress("DEPRECATION")
            display.getRealSize(size)
            screenWidth = size.x
            screenHeight = size.y
        }

        val statusBarResId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarH = if (statusBarResId > 0) resources.getDimensionPixelSize(statusBarResId) else (48 * density).toInt()
        topEdge = statusBarH + (16 * density).toInt()

        val navBarResId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navBarH = if (navBarResId > 0) resources.getDimensionPixelSize(navBarResId) else (48 * density).toInt()
        bottomEdge = navBarH + (16 * density).toInt()

        sideEdge = (24 * density).toInt()
    }

    private fun setupLockdown() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        screenOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF && !isExiting) {
                    wakeScreen()
                }
            }
        }
        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                if (insets.isVisible(WindowInsets.Type.statusBars()) ||
                    insets.isVisible(WindowInsets.Type.navigationBars())) {
                    immersiveHandler.postDelayed({ enterImmersiveMode() }, 100)
                }
                view.onApplyWindowInsets(insets)
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    immersiveHandler.postDelayed({ enterImmersiveMode() }, 100)
                }
            }
        }

        immersiveHandler.postDelayed(immersiveRunnable, 2000)
    }

    @Suppress("DEPRECATION")
    private fun wakeScreen() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                or PowerManager.ACQUIRE_CAUSES_WAKEUP
                or PowerManager.ON_AFTER_RELEASE,
            "FakeUpdate:ScreenOn"
        )
        wakeLock?.acquire(3000)
    }

    private fun enterImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        }
    }

    private fun startProgressTimer() {
        val tickInterval = 1000L
        timer = object : CountDownTimer(totalDurationMs, tickInterval) {
            var elapsed = 0L

            override fun onTick(millisUntilFinished: Long) {
                elapsed += tickInterval
                val fraction = elapsed.toDouble() / totalDurationMs.toDouble()
                currentProgress = computeNonLinearProgress(fraction)
                updateUI(currentProgress)

                if (elapsed % 45000 < tickInterval) {
                    triggerHaptic()
                }
            }

            override fun onFinish() {
                currentProgress = 100
                updateUI(100)
                navigateToReveal()
            }
        }.start()
    }

    private fun updateUI(progress: Int) {
        progressBar.progress = progress
        // Xiaomi uses raw number without % symbol (separate % TextView)
        if (currentStyle == "xiaomi") {
            percentText.text = progress.toString()
            updateXiaomiDownloadText(progress)
        } else {
            percentText.text = getString(R.string.format_percent, progress)
        }
    }

    private fun updateXiaomiDownloadText(progress: Int) {
        val totalSizeGb = 5.2
        val downloadedGb = totalSizeGb * progress / 100.0
        val downloadText = findViewById<TextView?>(R.id.text_download_size)
        downloadText?.text = String.format("%.1f GB / %.1f GB", downloadedGb, totalSizeGb)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) totalTapCount++
        // Process exit method gestures first (these work from ANY position)
        when (exitMethod) {
            "triple_tap" -> {
                if (ev.action == MotionEvent.ACTION_DOWN) {
                    val now = System.currentTimeMillis()
                    if (now - lastTapTime > 600) {
                        tapCount = 1
                    } else {
                        tapCount++
                    }
                    lastTapTime = now
                    if (tapCount >= 3) {
                        tapCount = 0
                        navigateToReveal()
                    }
                }
            }
            "long_press" -> {
                when (ev.action) {
                    MotionEvent.ACTION_DOWN -> {
                        longPressDownTime = System.currentTimeMillis()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (System.currentTimeMillis() - longPressDownTime >= longPressDuration) {
                            navigateToReveal()
                        }
                        longPressDownTime = 0L
                    }
                }
            }
        }

        // Block all edge touches (fallback if screen pinning unavailable)
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val x = ev.rawX.toInt()
            val y = ev.rawY.toInt()
            if (y < topEdge ||
                y > screenHeight - bottomEdge ||
                x < sideEdge ||
                x > screenWidth - sideEdge) {
                return true
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (exitMethod == "volume_up" && keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            val now = System.currentTimeMillis()
            if (now - lastVolumeUpTime > 1000) {
                volumeUpCount = 1
            } else {
                volumeUpCount++
            }
            lastVolumeUpTime = now
            if (volumeUpCount >= 3) {
                volumeUpCount = 0
                navigateToReveal()
            }
            return true
        }

        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_MUTE,
            KeyEvent.KEYCODE_CAMERA,
            KeyEvent.KEYCODE_SEARCH,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_APP_SWITCH -> true
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_MUTE,
            KeyEvent.KEYCODE_CAMERA,
            KeyEvent.KEYCODE_SEARCH,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_APP_SWITCH -> true
            else -> super.onKeyUp(keyCode, event)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Toast.makeText(this, R.string.update_home_disabled, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        enterImmersiveMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (isExiting) return
        if (hasFocus) {
            enterImmersiveMode()
        } else {
            immersiveHandler.postDelayed({ if (!isExiting) enterImmersiveMode() }, 200)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isExiting) return
        val bringBackIntent = Intent(this, FakeUpdateActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(bringBackIntent)
    }

    private fun triggerHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
    }

    private fun setupSamsungDynamicInfo() {
        // Set current time in status bar
        val timeFormat = java.text.SimpleDateFormat("h:mm", java.util.Locale.getDefault())
        findViewById<TextView?>(R.id.status_bar)?.let { bar ->
            val timeView = (bar as? android.view.ViewGroup)?.getChildAt(0) as? TextView
            timeView?.text = timeFormat.format(java.util.Date())
        }
        // Set actual battery level
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, 84) ?: 84
        val scale = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryPct = (level * 100 / scale)
        findViewById<TextView?>(R.id.status_bar)?.let { bar ->
            val batteryView = (bar as? android.view.ViewGroup)?.getChildAt(1) as? TextView
            batteryView?.text = "$batteryPct%"
        }
    }

    private fun startStyleAnimations() {
        when (currentStyle) {
            "pixel" -> {
                // Rotate the update icon continuously
                findViewById<ImageView?>(R.id.icon_update)?.let { icon ->
                    ObjectAnimator.ofFloat(icon, "rotation", 0f, 360f).apply {
                        duration = 3000
                        repeatCount = ObjectAnimator.INFINITE
                        interpolator = LinearInterpolator()
                        start()
                    }
                }
            }
            "stock" -> {
                // Rotate the gear icon slowly
                findViewById<ImageView?>(R.id.icon_gear)?.let { gear ->
                    ObjectAnimator.ofFloat(gear, "rotation", 0f, 360f).apply {
                        duration = 8000
                        repeatCount = ObjectAnimator.INFINITE
                        interpolator = LinearInterpolator()
                        start()
                    }
                }
            }
            "huawei" -> {
                // Pulse the updating text
                findViewById<TextView?>(R.id.text_update_status)?.let { text ->
                    ObjectAnimator.ofFloat(text, "alpha", 1f, 0.4f, 1f).apply {
                        duration = 2000
                        repeatCount = ObjectAnimator.INFINITE
                        start()
                    }
                }
            }
            "oneplus" -> {
                // Pulse the brand text
                findViewById<TextView?>(R.id.text_brand)?.let { brand ->
                    ObjectAnimator.ofFloat(brand, "alpha", 1f, 0.5f, 1f).apply {
                        duration = 4000
                        repeatCount = ObjectAnimator.INFINITE
                        start()
                    }
                }
            }
        }
    }

    private fun navigateToReveal() {
        if (isExiting) return
        isExiting = true

        // Stop all lockdown mechanisms before navigating
        timer?.cancel()
        sensorManager?.unregisterListener(shakeListener)
        immersiveHandler.removeCallbacks(immersiveRunnable)
        try {
            screenOffReceiver?.let { unregisterReceiver(it) }
            screenOffReceiver = null
        } catch (_: Exception) {}

        stopScreenPinning()
        val completeIntent = Intent(this, UpdateCompleteActivity::class.java).apply {
            putExtra(RevealActivity.EXTRA_START_TIME, prankStartTime)
            putExtra(SetupActivity.EXTRA_STYLE, currentStyle)
            putExtra(SetupActivity.EXTRA_TAP_COUNT, totalTapCount)
            putExtra(SetupActivity.EXTRA_EXIT_METHOD, exitMethod)
        }
        startActivity(completeIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        sensorManager?.unregisterListener(shakeListener)
        immersiveHandler.removeCallbacks(immersiveRunnable)
        stopScreenPinning()
        try {
            screenOffReceiver?.let { unregisterReceiver(it) }
        } catch (_: Exception) {}
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
    }

    companion object {
        fun computeNonLinearProgress(fraction: Double): Int {
            val progress = when {
                fraction < 0.05 -> {
                    fraction / 0.05 * 4.0
                }
                fraction < 0.30 -> {
                    4.0 + ((fraction - 0.05) / 0.25 * 8.0)
                }
                fraction < 0.35 -> {
                    12.0 + ((fraction - 0.30) / 0.05 * 18.0)
                }
                fraction < 0.55 -> {
                    30.0 + ((fraction - 0.35) / 0.20 * 15.0)
                }
                fraction < 0.60 -> {
                    45.0
                }
                fraction < 0.70 -> {
                    45.0 + ((fraction - 0.60) / 0.10 * 20.0)
                }
                fraction < 0.85 -> {
                    65.0 + ((fraction - 0.70) / 0.15 * 15.0)
                }
                fraction < 0.90 -> {
                    80.0
                }
                fraction < 0.95 -> {
                    80.0 + ((fraction - 0.90) / 0.05 * 15.0)
                }
                else -> {
                    95.0 + ((fraction - 0.95) / 0.05 * 4.0)
                }
            }
            return progress.toInt().coerceIn(0, 99)
        }
    }
}
