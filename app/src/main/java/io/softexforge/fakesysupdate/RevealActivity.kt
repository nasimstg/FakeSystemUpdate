package io.softexforge.fakesysupdate

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton

class RevealActivity : AppCompatActivity(), ExitInterviewBottomSheet.OnInterviewCompleteListener {

    companion object {
        const val EXTRA_START_TIME = "extra_start_time"
    }

    // Prank session data for sharing
    private var elapsedMs: Long = 0L
    private var totalTapCount: Int = 0
    private var exitMethod: String = "triple_tap"
    private var updateStyle: String = "stock"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reveal)
        supportActionBar?.hide()
        title = ""

        // Close button
        findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
            navigateToSetup()
        }

        // Buttons
        findViewById<MaterialButton>(R.id.btn_share).setOnClickListener {
            showExitInterview()
        }

        findViewById<MaterialButton>(R.id.btn_new_prank).setOnClickListener {
            navigateToSetup()
        }

        // Read prank session data
        totalTapCount = intent.getIntExtra(SetupActivity.EXTRA_TAP_COUNT, 0)
        exitMethod = intent.getStringExtra(SetupActivity.EXTRA_EXIT_METHOD) ?: "triple_tap"
        updateStyle = intent.getStringExtra(SetupActivity.EXTRA_STYLE) ?: "stock"

        // Duration tracking
        val startTime = intent.getLongExtra(EXTRA_START_TIME, 0L)
        if (startTime > 0) {
            elapsedMs = System.currentTimeMillis() - startTime
            val totalSeconds = (elapsedMs / 1000).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val timeFormatted = "${minutes}m ${seconds}s"
            val timeText = getString(R.string.reveal_time_wasted, timeFormatted)
            findViewById<TextView>(R.id.text_time_wasted).text = timeText
        }

        // Variant footer based on selected style
        val styleKeys = listOf("samsung", "pixel", "xiaomi", "oneplus", "huawei", "stock")
        val variantNumber = (styleKeys.indexOf(updateStyle) + 1).coerceAtLeast(1)
        findViewById<TextView>(R.id.text_variant).text =
            getString(R.string.reveal_variant, variantNumber, styleKeys.size)

        // Floating emoji animation
        startFloatingAnimation()
    }

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

    private fun showExitInterview() {
        val sheet = ExitInterviewBottomSheet.newInstance(
            durationMs = elapsedMs,
            tapCount = totalTapCount,
            exitMethod = exitMethod,
            style = updateStyle
        )
        sheet.show(supportFragmentManager, "exit_interview")
    }

    override fun onInterviewComplete(data: PrankSessionData) {
        val generator = ShareImageGenerator(this)
        val bitmap = generator.generate(data)
        val file = generator.saveBitmapToCache(bitmap)

        val uri = FileProvider.getUriForFile(
            this, "${packageName}.fileprovider", file
        )

        // Build share text from selected template
        val messages = resources.getStringArray(R.array.share_message_templates)
        val messageTemplate = messages[data.messageIndex.coerceIn(0, messages.size - 1)]
        val victimName = data.victimName.ifEmpty { "someone" }
        val shareText = messageTemplate
            .replace("%1\$s", victimName)
            .replace("%2\$s", data.formattedDuration)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, null))

        bitmap.recycle()
    }

    private fun navigateToSetup() {
        val intent = Intent(this, SetupActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        navigateToSetup()
    }
}
