package io.softexforge.fakesysupdate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ExitInterviewBottomSheet : BottomSheetDialogFragment() {

    interface OnInterviewCompleteListener {
        fun onInterviewComplete(data: PrankSessionData)
    }

    private var listener: OnInterviewCompleteListener? = null
    private var selectedTemplateIndex = 0
    private var selectedMessageIndex = 0 // Default message index

    companion object {
        private const val ARG_DURATION_MS = "duration_ms"
        private const val ARG_TAP_COUNT = "tap_count"
        private const val ARG_EXIT_METHOD = "exit_method"
        private const val ARG_STYLE = "style"

        fun newInstance(
            durationMs: Long,
            tapCount: Int,
            exitMethod: String,
            style: String
        ): ExitInterviewBottomSheet {
            return ExitInterviewBottomSheet().apply {
                arguments = Bundle().apply {
                    putLong(ARG_DURATION_MS, durationMs)
                    putInt(ARG_TAP_COUNT, tapCount)
                    putString(ARG_EXIT_METHOD, exitMethod)
                    putString(ARG_STYLE, style)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_exit_interview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val durationMs = arguments?.getLong(ARG_DURATION_MS, 0L) ?: 0L
        val tapCount = arguments?.getInt(ARG_TAP_COUNT, 0) ?: 0
        val exitMethod = arguments?.getString(ARG_EXIT_METHOD) ?: "triple_tap"
        val style = arguments?.getString(ARG_STYLE) ?: "stock"

        val editName = view.findViewById<TextInputEditText>(R.id.edit_victim_name)
        val editPrankster = view.findViewById<TextInputEditText>(R.id.edit_prankster_name)
        val chipGroupReaction = view.findViewById<ChipGroup>(R.id.chip_group_reaction)
        val sliderRating = view.findViewById<Slider>(R.id.slider_rating)
        val textRatingValue = view.findViewById<TextView>(R.id.text_rating_value)
        val chipGroupMessage = view.findViewById<ChipGroup>(R.id.chip_group_message)
        val layoutCustomMessage = view.findViewById<TextInputLayout>(R.id.layout_custom_message)
        val editCustomMessage = view.findViewById<TextInputEditText>(R.id.edit_custom_message)
        val btnGenerate = view.findViewById<FloatingActionButton>(R.id.btn_generate)
        val btnSharePrankWeb = view.findViewById<MaterialButton>(R.id.btn_share_prank_web)

        // Default selection
        view.findViewById<Chip>(R.id.chip_panicked).isChecked = true

        // Update rating display on slider change
        sliderRating.addOnChangeListener { _, value, _ ->
            textRatingValue.text = "${value.toInt()}%"
        }

        // Setup template thumbnails selection
        setupTemplatePreviews(view)

        // Setup message chips with data population
        setupMessageChips(chipGroupMessage, layoutCustomMessage, editName)

        // Add TextWatcher to regenerate chips when victim name changes
        editName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                // Regenerate chips with updated name
                chipGroupMessage.removeAllViews()
                setupMessageChips(chipGroupMessage, layoutCustomMessage, editName)
            }
        })

        // Website promotion button
        btnSharePrankWeb.setOnClickListener {
            val url = getString(R.string.pranks_community_url)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        // Generate button
        btnGenerate.setOnClickListener {
            val victimName = editName.text?.toString()?.trim() ?: ""
            val pranksterName = editPrankster.text?.toString()?.trim() ?: ""
            val reactionType = getSelectedReaction(chipGroupReaction)
            val rating = sliderRating.value.toInt()
            
            // Get custom message if provided, otherwise use selected template
            val customMsg = editCustomMessage.text?.toString()?.trim()
            val messageIdx = if (!customMsg.isNullOrEmpty()) -1 else selectedMessageIndex

            val data = PrankSessionData(
                victimName = victimName,
                pranksterName = pranksterName,
                reactionType = reactionType,
                prankRating = rating,
                templateIndex = selectedTemplateIndex,
                messageIndex = messageIdx,
                durationMs = durationMs,
                tapCount = tapCount,
                exitMethod = exitMethod,
                updateStyle = style,
                customMessage = customMsg
            )

            listener?.onInterviewComplete(data)
            dismiss()
        }
    }

    private fun setupTemplatePreviews(view: View) {
        val previewIds = intArrayOf(
            R.id.preview_0, R.id.preview_1, R.id.preview_2,
            R.id.preview_3, R.id.preview_4, R.id.preview_5,
            R.id.preview_6, R.id.preview_7, R.id.preview_8
        )

        val templateLayouts = intArrayOf(
            R.layout.template_share_stats_base,
            R.layout.template_share_stats_variant,
            R.layout.template_share_crash_base,
            R.layout.template_share_crash_variant,
            R.layout.template_share_trophy_base,
            R.layout.template_share_trophy_variant,
            R.layout.template_share_gamer_achievement,
            R.layout.template_share_kernel_panic,
            R.layout.template_share_vital_signs
        )

        val previews = previewIds.map { view.findViewById<ImageView>(it) }
        val durationMs = arguments?.getLong(ARG_DURATION_MS) ?: 120000L

        // Generate preview bitmaps with mock data
        val mockData = PrankSessionData(
            victimName = "Preview",
            pranksterName = "Demo",
            reactionType = "panicked",
            prankRating = 75,
            templateIndex = 0,
            messageIndex = 0,
            durationMs = durationMs,
            tapCount = 15,
            exitMethod = "triple_tap",
            updateStyle = "stock"
        )

        val generator = ShareImageGenerator(requireContext())

        // Generate previews asynchronously to avoid blocking UI
        previews.forEachIndexed { index, imageView ->
            // Set placeholder while loading
            imageView.setBackgroundColor(
                requireContext().getColor(R.color.ei_surface_darker)
            )
            
            imageView.post {
                try {
                    // Generate bitmap off the critical path
                    val previewBitmap = generator.generate(
                        mockData.copy(templateIndex = index)
                    )
                    // Scale down for preview (fixed size) 512 704
                    val targetWidth = 500
                    val targetHeight = 800
                    val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(
                        previewBitmap,
                        targetWidth,
                        targetHeight,
                        true
                    )
                    imageView.setImageBitmap(scaledBitmap)
                    imageView.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    previewBitmap.recycle() // Free original bitmap
                } catch (e: Exception) {
                    // Fallback to colored background
                    android.util.Log.e("ExitInterview", "Failed to generate preview $index", e)
                }
            }

            imageView.setOnClickListener {
                selectedTemplateIndex = index
                updateTemplateSelection(previews, index)
            }
        }

        // Set initial selection highlight
        updateTemplateSelection(previews, 0)
    }

    private fun updateTemplateSelection(previews: List<ImageView>, selectedIndex: Int) {
        previews.forEachIndexed { index, imageView ->
            if (index == selectedIndex) {
                imageView.setPadding(4, 4, 4, 4)
                imageView.setBackgroundColor(
                    requireContext().getColor(R.color.ei_primary)
                )
            } else {
                imageView.setPadding(0, 0, 0, 0)
                imageView.setBackgroundColor(
                    requireContext().getColor(android.R.color.transparent)
                )
            }
        }
    }

    private fun setupMessageChips(
        chipGroup: ChipGroup,
        customLayout: TextInputLayout,
        nameField: TextInputEditText
    ) {
        val messages = resources.getStringArray(R.array.share_message_templates)
        val victimName = nameField.text?.toString()?.ifEmpty { "Victim" } ?: "Victim"
        val durationMs = arguments?.getLong(ARG_DURATION_MS) ?: 120000L
        val duration = (durationMs / 1000 / 60).toString() + "m"
        
        // Add predefined message chips with populated data
        messages.forEachIndexed { index, messageTemplate ->
            // Replace placeholders with actual data
            val populatedMessage = messageTemplate
                .replace("%1\$s", victimName)
                .replace("%2\$s", duration)
            
            val chip = Chip(requireContext()).apply {
                text = populatedMessage.take(40) + if (populatedMessage.length > 40) "…" else ""
                isCheckable = true
                isChecked = index == 0
                setTextColor(requireContext().getColor(android.R.color.white))
                setChipBackgroundColorResource(R.color.chip_background_color)
                setChipStrokeColorResource(R.color.ei_border)
                chipStrokeWidth = 2f
                isCheckedIconVisible = false
                setOnClickListener {
                    selectedMessageIndex = index
                    customLayout.visibility = View.GONE
                }
            }
            chipGroup.addView(chip)
        }
        
        // Add "Custom Message" chip
        val customChip = Chip(requireContext()).apply {
            text = getString(R.string.chip_custom_message)
            isCheckable = true
            setTextColor(requireContext().getColor(android.R.color.white))
            setChipBackgroundColorResource(R.color.chip_background_color)
            setChipStrokeColorResource(R.color.ei_border)
            chipStrokeWidth = 2f
            isCheckedIconVisible = false
            setOnClickListener {
                customLayout.visibility = View.VISIBLE
            }
        }
        chipGroup.addView(customChip)
    }

    private fun getSelectedReaction(chipGroup: ChipGroup): String {
        return when (chipGroup.checkedChipId) {
            R.id.chip_panicked -> "panicked"
            R.id.chip_stared -> "stared"
            R.id.chip_called_it -> "called_support"
            R.id.chip_laughed -> "laughed"
            R.id.chip_rebooted -> "hit_phone"
            else -> "panicked"
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnInterviewCompleteListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
