package io.softexforge.fakesysupdate

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class OnboardingAcceptFragment : Fragment() {

    private var onAcceptanceChanged: ((Boolean) -> Unit)? = null

    fun setOnAcceptanceChangedListener(listener: (Boolean) -> Unit) {
        onAcceptanceChanged = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_onboarding_accept, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Dynamic version
        view.findViewById<TextView>(R.id.text_version).text =
            getString(R.string.onboarding_version_format, BuildConfig.VERSION_NAME)

        val checkboxAgree = view.findViewById<CheckBox>(R.id.checkbox_agree)
        val textAgree = view.findViewById<TextView>(R.id.text_agree)

        // Build spannable with clickable Terms and Privacy links
        val fullText = getString(R.string.onboarding_accept_checkbox)
        val termsLabel = "Terms of Service"
        val privacyLabel = "Privacy Policy"
        val cyanColor = ContextCompat.getColor(requireContext(), R.color.electric_blue)

        val spannable = SpannableString(fullText)

        val termsStart = fullText.indexOf(termsLabel)
        if (termsStart >= 0) {
            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(Intent(requireContext(), TermsActivity::class.java))
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = cyanColor
                    ds.isUnderlineText = false
                }
            }, termsStart, termsStart + termsLabel.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val privacyStart = fullText.indexOf(privacyLabel)
        if (privacyStart >= 0) {
            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(Intent(requireContext(), PrivacyPolicyActivity::class.java))
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = cyanColor
                    ds.isUnderlineText = false
                }
            }, privacyStart, privacyStart + privacyLabel.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textAgree.text = spannable
        textAgree.movementMethod = LinkMovementMethod.getInstance()
        textAgree.highlightColor = Color.TRANSPARENT

        checkboxAgree.setOnCheckedChangeListener { _, isChecked ->
            onAcceptanceChanged?.invoke(isChecked)
        }
    }
}
