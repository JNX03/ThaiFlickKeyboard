package com.Jnx03.thaiflickkeyboard.settings.tutorial

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.Jnx03.thaiflickkeyboard.R
import com.Jnx03.thaiflickkeyboard.view.KeyboardHeatmapView
import com.Jnx03.thaiflickkeyboard.view.TutorialAnimationView

class TutorialStepFragment : Fragment() {

    private var stepIndex = 0
    private var currentWordIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stepIndex = arguments?.getInt(ARG_STEP, 0) ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_tutorial_step, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animView = view.findViewById<TutorialAnimationView>(R.id.animation_view)
        val heatmapView = view.findViewById<KeyboardHeatmapView>(R.id.heatmap_view)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tvSubtitle = view.findViewById<TextView>(R.id.tv_subtitle)
        val tvDescription = view.findViewById<TextView>(R.id.tv_description)
        val practiceSection = view.findViewById<LinearLayout>(R.id.practice_section)

        val setupSection = view.findViewById<LinearLayout>(R.id.setup_section)

        when (stepIndex) {
            0 -> setupWelcomeStep(animView, tvTitle, tvSubtitle, tvDescription, setupSection, view)
            1 -> setupTapStep(animView, tvTitle, tvSubtitle, tvDescription)
            2 -> setupFlickStep(animView, tvTitle, tvSubtitle, tvDescription)
            3 -> setupLayoutStep(animView, heatmapView, tvTitle, tvSubtitle, tvDescription)
            4 -> setupVowelStep(animView, tvTitle, tvSubtitle, tvDescription)
            5 -> setupToneStep(animView, tvTitle, tvSubtitle, tvDescription)
            6 -> setupPracticeStep(animView, tvTitle, tvSubtitle, tvDescription, practiceSection, view)
        }
    }

    private fun setupWelcomeStep(
        animView: TutorialAnimationView, title: TextView,
        subtitle: TextView, desc: TextView,
        setupSection: LinearLayout, view: View
    ) {
        animView.animationType = TutorialAnimationView.AnimationType.TAP
        animView.keyLabel = "า"
        animView.flickLabels = mapOf("up" to "ร", "left" to "ง", "right" to "ั", "down" to "ต")

        title.text = getString(R.string.tutorial_welcome_title)
        subtitle.text = getString(R.string.tutorial_welcome_subtitle)
        desc.text = getString(R.string.tutorial_welcome_desc)

        setupSection.visibility = View.VISIBLE

        val btnEnable = view.findViewById<LinearLayout>(R.id.btn_enable_keyboard)
        val btnSelect = view.findViewById<LinearLayout>(R.id.btn_select_keyboard)
        val tvStatus = view.findViewById<TextView>(R.id.tv_enable_status)

        updateEnableStatus(tvStatus)

        btnEnable.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        btnSelect.setOnClickListener {
            val imm = requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }
    }

    private fun updateEnableStatus(tvStatus: TextView) {
        val imm = requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabled = imm.enabledInputMethodList.any {
            it.packageName == requireContext().packageName
        }
        if (enabled) {
            tvStatus.text = getString(R.string.tutorial_enabled)
            tvStatus.setTextColor(requireContext().getColor(R.color.key_green))
        } else {
            tvStatus.text = getString(R.string.tutorial_not_enabled)
            tvStatus.setTextColor(requireContext().getColor(R.color.key_amber))
        }
    }

    override fun onResume() {
        super.onResume()
        if (stepIndex == 0) {
            view?.findViewById<TextView>(R.id.tv_enable_status)?.let {
                updateEnableStatus(it)
            }
        }
    }

    private fun setupTapStep(
        animView: TutorialAnimationView, title: TextView,
        subtitle: TextView, desc: TextView
    ) {
        animView.animationType = TutorialAnimationView.AnimationType.TAP
        animView.keyLabel = "า"
        animView.flickLabels = mapOf("up" to "ร", "left" to "ง", "right" to "ั", "down" to "ต")

        title.text = getString(R.string.tutorial_tap_title)
        subtitle.text = getString(R.string.tutorial_tap_subtitle)
        desc.text = getString(R.string.tutorial_tap_desc)
    }

    private fun setupFlickStep(
        animView: TutorialAnimationView, title: TextView,
        subtitle: TextView, desc: TextView
    ) {
        animView.animationType = TutorialAnimationView.AnimationType.FLICK_ALL
        animView.keyLabel = "ก"
        animView.flickLabels = mapOf("up" to "เ", "left" to "ท", "right" to "ด", "down" to "พ")

        title.text = getString(R.string.tutorial_flick_title)
        subtitle.text = getString(R.string.tutorial_flick_subtitle)
        desc.text = getString(R.string.tutorial_flick_desc)
    }

    private fun setupLayoutStep(
        animView: TutorialAnimationView, heatmapView: KeyboardHeatmapView,
        title: TextView, subtitle: TextView, desc: TextView
    ) {
        animView.visibility = View.GONE
        heatmapView.visibility = View.VISIBLE

        title.text = getString(R.string.tutorial_layout_title)
        subtitle.text = getString(R.string.tutorial_layout_subtitle)
        desc.text = getString(R.string.tutorial_layout_desc)
    }

    private fun setupVowelStep(
        animView: TutorialAnimationView, title: TextView,
        subtitle: TextView, desc: TextView
    ) {
        animView.animationType = TutorialAnimationView.AnimationType.FLICK_ALL
        animView.keyLabel = "า"
        animView.flickLabels = mapOf("up" to "เ", "left" to "ั", "right" to "ี", "down" to "ะ")

        title.text = getString(R.string.tutorial_vowel_title)
        subtitle.text = getString(R.string.tutorial_vowel_subtitle)
        desc.text = getString(R.string.tutorial_vowel_desc)
    }

    private fun setupToneStep(
        animView: TutorialAnimationView, title: TextView,
        subtitle: TextView, desc: TextView
    ) {
        animView.animationType = TutorialAnimationView.AnimationType.FLICK_ALL
        animView.keyLabel = "Space"
        animView.flickLabels = mapOf("up" to "้", "left" to "่", "right" to "๊", "down" to "")

        title.text = getString(R.string.tutorial_tone_title)
        subtitle.text = getString(R.string.tutorial_tone_subtitle)
        desc.text = getString(R.string.tutorial_tone_desc)
    }

    private fun setHighlightChar(char: String) {
        requireContext().getSharedPreferences("tutorial_prefs", 0)
            .edit().putString("highlight_char", char).apply()
    }

    private fun clearHighlight() {
        requireContext().getSharedPreferences("tutorial_prefs", 0)
            .edit().putString("highlight_char", "").apply()
    }

    override fun onDestroyView() {
        if (stepIndex == 6) clearHighlight()
        super.onDestroyView()
    }

    private fun setupPracticeStep(
        animView: TutorialAnimationView, title: TextView,
        subtitle: TextView, desc: TextView,
        practiceSection: LinearLayout, view: View
    ) {
        animView.animationType = TutorialAnimationView.AnimationType.TAP
        animView.keyLabel = "น"
        animView.flickLabels = mapOf("up" to "อ", "left" to "ว", "right" to "ี", "down" to "ไ")

        title.text = getString(R.string.tutorial_practice_title)
        subtitle.text = getString(R.string.tutorial_practice_subtitle)
        desc.text = getString(R.string.tutorial_practice_desc)

        practiceSection.visibility = View.VISIBLE
        currentWordIndex = 0

        val tvTarget = view.findViewById<TextView>(R.id.tv_target_word)
        val etPractice = view.findViewById<EditText>(R.id.et_practice)
        val tvResult = view.findViewById<TextView>(R.id.tv_result)

        tvTarget.text = PRACTICE_WORDS[currentWordIndex]
        highlightNextChar("", PRACTICE_WORDS[currentWordIndex])

        etPractice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString()?.trim() ?: ""
                val target = PRACTICE_WORDS[currentWordIndex]

                highlightNextChar(input, target)

                if (input == target) {
                    clearHighlight()
                    tvResult.visibility = View.VISIBLE
                    tvResult.text = getString(R.string.tutorial_correct)
                    tvResult.setTextColor(requireContext().getColor(R.color.key_green))

                    view.postDelayed({
                        if (!isAdded) return@postDelayed
                        currentWordIndex++
                        if (currentWordIndex < PRACTICE_WORDS.size) {
                            tvTarget.text = PRACTICE_WORDS[currentWordIndex]
                            etPractice.setText("")
                            tvResult.visibility = View.GONE
                            highlightNextChar("", PRACTICE_WORDS[currentWordIndex])
                        } else {
                            tvResult.text = getString(R.string.tutorial_complete)
                            tvResult.setTextColor(requireContext().getColor(R.color.key_indigo))
                            etPractice.isEnabled = false
                            clearHighlight()
                        }
                    }, 800)
                } else if (target.startsWith(input) && input.isNotEmpty()) {
                    tvResult.visibility = View.VISIBLE
                    tvResult.text = getString(R.string.tutorial_keep_going)
                    tvResult.setTextColor(requireContext().getColor(R.color.key_amber))
                } else {
                    tvResult.visibility = View.GONE
                }
            }
        })
    }

    private fun highlightNextChar(input: String, target: String) {
        if (input.length < target.length) {
            setHighlightChar(target[input.length].toString())
        } else {
            clearHighlight()
        }
    }

    companion object {
        private const val ARG_STEP = "step"

        val PRACTICE_WORDS = listOf("สวัสดี", "ขอบคุณ", "ประเทศไทย", "กรุงเทพ", "ภาษาไทย")

        fun newInstance(step: Int): TutorialStepFragment {
            return TutorialStepFragment().apply {
                arguments = Bundle().apply { putInt(ARG_STEP, step) }
            }
        }
    }
}
