package io.softexforge.fakesysupdate

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat

class UpdateCompleteActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_complete)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Show over lock screen (unpinning may trigger lock screen)
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

        enterImmersiveMode()

        val startTime = intent.getLongExtra(RevealActivity.EXTRA_START_TIME, 0L)
        val style = intent.getStringExtra(SetupActivity.EXTRA_STYLE) ?: "stock"
        val tapCount = intent.getIntExtra(SetupActivity.EXTRA_TAP_COUNT, 0)
        val exitMethod = intent.getStringExtra(SetupActivity.EXTRA_EXIT_METHOD) ?: "triple_tap"

        val layoutComplete = findViewById<LinearLayout>(R.id.layout_complete)
        val layoutOptimizing = findViewById<ConstraintLayout>(R.id.layout_optimizing)

        // Phase 1: "Update installed successfully" + "Restarting..." (2.5s)
        layoutComplete.visibility = View.VISIBLE
        layoutOptimizing.visibility = View.GONE

        // Phase 2: Black screen simulating reboot (1.5s)
        handler.postDelayed({
            layoutComplete.visibility = View.GONE
            layoutOptimizing.visibility = View.GONE
        }, 2500)

        // Phase 3: "Optimizing system..." with spinner (3s)
        handler.postDelayed({
            layoutOptimizing.visibility = View.VISIBLE
        }, 4000)

        // Phase 4: Navigate to reveal
        handler.postDelayed({
            val revealIntent = Intent(this, RevealActivity::class.java).apply {
                putExtra(RevealActivity.EXTRA_START_TIME, startTime)
                putExtra(SetupActivity.EXTRA_STYLE, style)
                putExtra(SetupActivity.EXTRA_TAP_COUNT, tapCount)
                putExtra(SetupActivity.EXTRA_EXIT_METHOD, exitMethod)
            }
            startActivity(revealIntent)
            finish()
        }, 7000)
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
