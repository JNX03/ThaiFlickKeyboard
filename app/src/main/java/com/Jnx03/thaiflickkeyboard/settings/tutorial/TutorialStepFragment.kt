package com.Jnx03.thaiflickkeyboard.settings.tutorial

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.Jnx03.thaiflickkeyboard.R
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
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tvSubtitle = view.findViewById<TextView>(R.id.tv_subtitle)
        val tvDescription = view.findViewById<TextView>(R.id.tv_description)
        val practiceSection = view.findViewById<LinearLayout>(R.id.practice_section)

        when (stepIndex) {
            0 -> setupTapStep(animView, tvTitle, tvSubtitle, tvDescription)
            1 -> setupFlickStep(animView, tvTitle, tvSubtitle, tvDescription)
            2 -> setupVowelStep(animView, tvTitle, tvSubtitle, tvDescription)
            3 -> setupToneStep(animView, tvTitle, tvSubtitle, tvDescription)
            4 -> setupPracticeStep(animView, tvTitle, tvSubtitle, tvDescription, practiceSection, view)
        }
    }

    private fun setupTapStep(
        animView: TutorialAnimationView, title: TextView,
        subtitle: TextView, desc: TextView
    ) {
        animView.animationType = TutorialAnimationView.AnimationType.TAP
        animView.keyLabel = "ก"
        animView.flickLabels = mapOf("up" to "ค", "left" to "ข", "right" to "ฆ", "down" to "ฃ")

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
        animView.flickLabels = mapOf("up" to "ค", "left" to "ข", "right" to "ฆ", "down" to "ฃ")

        title.text = getString(R.string.tutorial_flick_title)
        subtitle.text = getString(R.string.tutorial_flick_subtitle)
        desc.text = getString(R.string.tutorial_flick_desc)
    }

    private fun setupVowelStep(
        animView: TutorialAnimationView, title: TextView,
        subtitle: TextView, desc: TextView
    ) {
        animView.animationType = TutorialAnimationView.AnimationType.FLICK_ALL
        animView.keyLabel = "เ"
        animView.flickLabels = mapOf("up" to "ไ", "left" to "แ", "right" to "ใ", "down" to "โ")

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
        animView.flickLabels = mapOf("up" to "้", "left" to "่", "right" to "", "down" to "๊")

        title.text = getString(R.string.tutorial_tone_title)
        subtitle.text = getString(R.string.tutorial_tone_subtitle)
        desc.text = getString(R.string.tutorial_tone_desc)
    }

    private fun setupPracticeStep(
        animView: TutorialAnimationView, title: TextView,
        subtitle: TextView, desc: TextView,
        practiceSection: LinearLayout, view: View
    ) {
        animView.animationType = TutorialAnimationView.AnimationType.TAP
        animView.keyLabel = "า"
        animView.flickLabels = mapOf("up" to "ุ", "left" to "ะ", "right" to "ู", "down" to "ำ")

        title.text = getString(R.string.tutorial_practice_title)
        subtitle.text = getString(R.string.tutorial_practice_subtitle)
        desc.text = getString(R.string.tutorial_practice_desc)

        practiceSection.visibility = View.VISIBLE
        currentWordIndex = 0

        val tvTarget = view.findViewById<TextView>(R.id.tv_target_word)
        val etPractice = view.findViewById<EditText>(R.id.et_practice)
        val tvResult = view.findViewById<TextView>(R.id.tv_result)

        tvTarget.text = PRACTICE_WORDS[currentWordIndex]

        etPractice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString()?.trim() ?: ""
                val target = PRACTICE_WORDS[currentWordIndex]

                if (input == target) {
                    tvResult.visibility = View.VISIBLE
                    tvResult.text = getString(R.string.tutorial_correct)
                    tvResult.setTextColor(requireContext().getColor(R.color.key_green))

                    // Advance to next word after a short delay
                    view.postDelayed({
                        if (!isAdded) return@postDelayed
                        currentWordIndex++
                        if (currentWordIndex < PRACTICE_WORDS.size) {
                            tvTarget.text = PRACTICE_WORDS[currentWordIndex]
                            etPractice.setText("")
                            tvResult.visibility = View.GONE
                        } else {
                            tvResult.text = getString(R.string.tutorial_complete)
                            tvResult.setTextColor(requireContext().getColor(R.color.key_indigo))
                            etPractice.isEnabled = false
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
