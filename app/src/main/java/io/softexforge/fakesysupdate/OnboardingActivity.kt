package io.softexforge.fakesysupdate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val PAGE_COUNT = 4
        private const val PAGE_WELCOME = 0
        private const val PAGE_HOW = 1
        private const val PAGE_DISCLAIMER = 2
        private const val PAGE_ACCEPT = 3
    }

    private lateinit var viewPager: ViewPager2
    private lateinit var btnAction: MaterialButton
    private lateinit var btnSkip: TextView
    private lateinit var dotContainer: LinearLayout
    private lateinit var dots: List<View>
    private lateinit var termsLink: TextView

    private var termsAccepted = false
    private var acceptFragment: OnboardingAcceptFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Skip onboarding if already completed
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)) {
            launchSetup()
            return
        }

        setContentView(R.layout.activity_onboarding)
        supportActionBar?.hide()
        title = ""

        viewPager = findViewById(R.id.view_pager)
        btnAction = findViewById(R.id.btn_action)
        btnSkip = findViewById(R.id.btn_skip)
        dotContainer = findViewById(R.id.dot_container)
        termsLink = findViewById(R.id.text_terms_link)
        dots = listOf(
            findViewById(R.id.dot_0),
            findViewById(R.id.dot_1),
            findViewById(R.id.dot_2)
        )

        setupViewPager()
        setupButtons()
        updatePageUI(PAGE_WELCOME)
    }

    private fun setupViewPager() {
        acceptFragment = OnboardingAcceptFragment().also {
            it.setOnAcceptanceChangedListener { accepted ->
                termsAccepted = accepted
                if (viewPager.currentItem == PAGE_ACCEPT) {
                    btnAction.isEnabled = accepted
                    btnAction.alpha = if (accepted) 1f else 0.4f
                }
            }
        }

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = PAGE_COUNT
            override fun createFragment(position: Int): Fragment = when (position) {
                0 -> OnboardingWelcomeFragment()
                1 -> OnboardingHowItWorksFragment()
                2 -> OnboardingDisclaimerFragment()
                3 -> acceptFragment!!
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageUI(position)
            }
        })
    }

    private fun setupButtons() {
        btnAction.setOnClickListener {
            val current = viewPager.currentItem
            if (current < PAGE_COUNT - 1) {
                viewPager.currentItem = current + 1
            } else {
                completeOnboarding()
            }
        }

        btnSkip.setOnClickListener {
            viewPager.currentItem = PAGE_ACCEPT
        }
    }

    private fun updatePageUI(position: Int) {
        updateDots(position)
        updateButton(position)
        updateSkipVisibility(position)
        updateTermsLink(position)
    }

    private fun updateDots(position: Int) {
        // Dots visible on pages 0-2 (welcome, how, disclaimer), hidden on accept
        when (position) {
            PAGE_WELCOME -> {
                // Welcome: no dots at top (design shows dots centered in content, but we approximate)
                dotContainer.visibility = View.GONE
            }
            PAGE_HOW, PAGE_DISCLAIMER -> {
                dotContainer.visibility = View.VISIBLE
                val dotIndex = position // 0=welcome, 1=how, 2=disclaimer maps to dot 0,1,2
                dots.forEachIndexed { index, dot ->
                    if (index == dotIndex) {
                        dot.setBackgroundResource(R.drawable.bg_dot_pill_active)
                        dot.layoutParams = (dot.layoutParams as LinearLayout.LayoutParams).apply {
                            width = resources.getDimensionPixelSize(R.dimen.dot_active_width)
                            height = resources.getDimensionPixelSize(R.dimen.dot_height)
                        }
                    } else {
                        dot.setBackgroundResource(R.drawable.bg_dot_pill_inactive)
                        dot.layoutParams = (dot.layoutParams as LinearLayout.LayoutParams).apply {
                            width = resources.getDimensionPixelSize(R.dimen.dot_inactive_size)
                            height = resources.getDimensionPixelSize(R.dimen.dot_inactive_size)
                        }
                    }
                }
            }
            PAGE_ACCEPT -> {
                dotContainer.visibility = View.GONE
            }
        }
    }

    private fun updateButton(position: Int) {
        when (position) {
            PAGE_WELCOME -> {
                btnAction.text = getString(R.string.onboarding_btn_start)
                btnAction.setIconResource(R.drawable.ic_play_arrow)
                btnAction.isEnabled = true
                btnAction.alpha = 1f
            }
            PAGE_HOW -> {
                btnAction.text = getString(R.string.onboarding_btn_next_step)
                btnAction.setIconResource(R.drawable.ic_play_arrow)
                btnAction.isEnabled = true
                btnAction.alpha = 1f
            }
            PAGE_DISCLAIMER -> {
                btnAction.text = getString(R.string.onboarding_btn_understand)
                btnAction.setIconResource(R.drawable.ic_play_arrow)
                btnAction.isEnabled = true
                btnAction.alpha = 1f
            }
            PAGE_ACCEPT -> {
                btnAction.text = getString(R.string.onboarding_btn_initialize)
                btnAction.setIconResource(R.drawable.ic_play_arrow)
                btnAction.isEnabled = termsAccepted
                btnAction.alpha = if (termsAccepted) 1f else 0.4f
            }
        }
    }

    private fun updateSkipVisibility(position: Int) {
        // Skip only on how-it-works page (design shows it there)
        btnSkip.visibility = if (position == PAGE_HOW) View.VISIBLE else View.GONE
    }

    private fun updateTermsLink(position: Int) {
        // Terms link only on welcome page
        termsLink.visibility = if (position == PAGE_WELCOME) View.VISIBLE else View.GONE
    }

    private fun completeOnboarding() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
        launchSetup()
    }

    private fun launchSetup() {
        startActivity(Intent(this, SetupActivity::class.java))
        finish()
    }
}
