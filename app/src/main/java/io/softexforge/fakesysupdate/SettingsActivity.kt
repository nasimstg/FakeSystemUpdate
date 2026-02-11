package io.softexforge.fakesysupdate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "prank_prefs"
        const val KEY_DEFAULT_STYLE = "default_style"
        const val KEY_DEFAULT_DURATION = "default_duration"
        const val KEY_DEFAULT_EXIT = "default_exit_method"
        const val KEY_FAKE_ERRORS = "fake_errors"
        const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide()
        title = ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        displayVersion()
        setupDefaultStyleDropdown()
        setupDefaultDurationSlider()
        setupDefaultExitDropdown()
        setupFakeErrorsToggle()
        setupHapticToggle()
        setupOsStyleDisplay()
        setupAboutLinks()
        setupRateApp()
        setupFeedback()
    }

    private fun displayVersion() {
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
        findViewById<TextView>(R.id.text_version).text = "v$versionName"
    }

    private fun setupDefaultStyleDropdown() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedStyle = prefs.getString(KEY_DEFAULT_STYLE, "stock") ?: "stock"

        val styles = resources.getStringArray(R.array.update_styles)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, styles)
        val dropdown = findViewById<AutoCompleteTextView>(R.id.dropdown_default_style)
        dropdown.setAdapter(adapter)

        val position = styleToPosition(savedStyle)
        dropdown.setText(styles[position], false)

        dropdown.setOnItemClickListener { _, _, pos, _ ->
            val style = positionToStyle(pos)
            prefs.edit().putString(KEY_DEFAULT_STYLE, style).apply()
            updateOsStyleDisplay(style)
        }
    }

    private fun setupDefaultDurationSlider() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedDuration = prefs.getInt(KEY_DEFAULT_DURATION, 30)

        val slider = findViewById<Slider>(R.id.slider_default_duration)
        val label = findViewById<TextView>(R.id.text_default_duration_value)

        slider.value = savedDuration.toFloat()
        label.text = getString(R.string.format_duration_short, savedDuration, 0)

        slider.addOnChangeListener { _, value, _ ->
            val duration = value.toInt()
            label.text = getString(R.string.format_duration_short, duration, 0)
            prefs.edit().putInt(KEY_DEFAULT_DURATION, duration).apply()
        }
    }

    private fun setupDefaultExitDropdown() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedExit = prefs.getString(KEY_DEFAULT_EXIT, "triple_tap") ?: "triple_tap"

        val methods = resources.getStringArray(R.array.exit_methods)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, methods)
        val dropdown = findViewById<AutoCompleteTextView>(R.id.dropdown_default_exit)
        dropdown.setAdapter(adapter)

        val position = when (savedExit) {
            "triple_tap" -> 0
            "long_press" -> 1
            "volume_up" -> 2
            "shake" -> 3
            else -> 0
        }
        dropdown.setText(methods[position], false)

        dropdown.setOnItemClickListener { _, _, pos, _ ->
            val exit = when (pos) {
                0 -> "triple_tap"
                1 -> "long_press"
                2 -> "volume_up"
                3 -> "shake"
                else -> "triple_tap"
            }
            prefs.edit().putString(KEY_DEFAULT_EXIT, exit).apply()
        }
    }

    private fun setupFakeErrorsToggle() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val switch = findViewById<SwitchMaterial>(R.id.switch_fake_errors)
        switch.isChecked = prefs.getBoolean(KEY_FAKE_ERRORS, false)
        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_FAKE_ERRORS, isChecked).apply()
        }
    }

    private fun setupHapticToggle() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val switch = findViewById<SwitchMaterial>(R.id.switch_haptic)
        switch.isChecked = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, isChecked).apply()
        }
    }

    private fun setupOsStyleDisplay() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedStyle = prefs.getString(KEY_DEFAULT_STYLE, "stock") ?: "stock"
        updateOsStyleDisplay(savedStyle)
    }

    private fun updateOsStyleDisplay(style: String) {
        val styles = resources.getStringArray(R.array.update_styles)
        val position = styleToPosition(style)
        findViewById<TextView>(R.id.text_os_style_value).text = styles[position]
    }

    private fun setupAboutLinks() {
        findViewById<LinearLayout>(R.id.row_privacy_policy).setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.row_terms).setOnClickListener {
            startActivity(Intent(this, TermsActivity::class.java))
        }
    }

    private fun setupRateApp() {
        findViewById<LinearLayout>(R.id.row_rate_app).setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
            } catch (_: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
            }
        }
    }

    private fun setupFeedback() {
        findViewById<LinearLayout>(R.id.row_feedback).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.feedback_url))))
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

    private fun positionToStyle(position: Int): String = when (position) {
        0 -> "samsung"
        1 -> "pixel"
        2 -> "xiaomi"
        3 -> "oneplus"
        4 -> "huawei"
        5 -> "stock"
        else -> "stock"
    }
}
