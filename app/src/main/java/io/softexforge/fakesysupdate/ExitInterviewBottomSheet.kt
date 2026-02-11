package io.softexforge.fakesysupdate

import android.content.Context
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
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText

class ExitInterviewBottomSheet : BottomSheetDialogFragment() {

    interface OnInterviewCompleteListener {
        fun onInterviewComplete(data: PrankSessionData)
    }

    private var listener: OnInterviewCompleteListener? = null
    private var selectedTemplateIndex = 0
    private var selectedMessageIndex = 0

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
        val chipGroupReaction = view.findViewById<ChipGroup>(R.id.chip_group_reaction)
        val sliderRating = view.findViewById<Slider>(R.id.slider_rating)
        val textRatingValue = view.findViewById<TextView>(R.id.text_rating_value)
        val chipGroupMessage = view.findViewById<ChipGroup>(R.id.chip_group_message)
        val btnGenerate = view.findViewById<MaterialButton>(R.id.btn_generate_share)

        // Default selection
        view.findViewById<Chip>(R.id.chip_panicked).isChecked = true

        // Rating skull display
        sliderRating.addOnChangeListener { _, value, _ ->
            val skulls = "\uD83D\uDC80".repeat(value.toInt())
            textRatingValue.text = skulls
        }

        // Setup template thumbnails selection
        setupTemplatePreviews(view)

        // Setup message chips
        setupMessageChips(chipGroupMessage)

        // Generate button
        btnGenerate.setOnClickListener {
            val victimName = editName.text?.toString()?.trim() ?: ""
            val reactionType = getSelectedReaction(chipGroupReaction)
            val rating = sliderRating.value.toInt()

            val data = PrankSessionData(
                victimName = victimName,
                reactionType = reactionType,
                prankRating = rating,
                templateIndex = selectedTemplateIndex,
                messageIndex = selectedMessageIndex,
                durationMs = durationMs,
                tapCount = tapCount,
                exitMethod = exitMethod,
                updateStyle = style
            )

            listener?.onInterviewComplete(data)
            dismiss()
        }
    }

    private fun setupTemplatePreviews(view: View) {
        val previewIds = intArrayOf(
            R.id.preview_0, R.id.preview_1, R.id.preview_2,
            R.id.preview_3, R.id.preview_4, R.id.preview_5
        )

        val previews = previewIds.map { view.findViewById<ImageView>(it) }

        // Set initial selection highlight
        updateTemplateSelection(previews, 0)

        previews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedTemplateIndex = index
                updateTemplateSelection(previews, index)
            }
        }
    }

    private fun updateTemplateSelection(previews: List<ImageView>, selectedIndex: Int) {
        previews.forEachIndexed { index, imageView ->
            if (index == selectedIndex) {
                imageView.setBackgroundColor(
                    requireContext().getColor(R.color.electric_blue)
                )
            } else {
                imageView.setBackgroundColor(
                    requireContext().getColor(R.color.charcoal_dark)
                )
            }
        }
    }

    private fun setupMessageChips(chipGroup: ChipGroup) {
        val messages = resources.getStringArray(R.array.share_message_templates)
        messages.forEachIndexed { index, message ->
            val chip = Chip(requireContext()).apply {
                text = message.take(40) + if (message.length > 40) "…" else ""
                isCheckable = true
                isChecked = index == 0
                setTextColor(requireContext().getColor(R.color.white))
                chipBackgroundColor =
                    requireContext().getColorStateList(R.color.charcoal_dark)
                chipStrokeColor =
                    requireContext().getColorStateList(R.color.electric_blue_30)
                chipStrokeWidth = resources.getDimension(R.dimen.dot_height)
                isCheckedIconVisible = false
                setOnClickListener {
                    selectedMessageIndex = index
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun getSelectedReaction(chipGroup: ChipGroup): String {
        return when (chipGroup.checkedChipId) {
            R.id.chip_panicked -> "panicked"
            R.id.chip_stared -> "stared"
            R.id.chip_called_support -> "called_support"
            R.id.chip_hit_phone -> "hit_phone"
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
