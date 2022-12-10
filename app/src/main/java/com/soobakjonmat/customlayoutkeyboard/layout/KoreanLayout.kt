package com.soobakjonmat.customlayoutkeyboard.layout

import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.TypefaceSpan
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.core.view.size
import com.soobakjonmat.customlayoutkeyboard.MainKeyboardService
import com.soobakjonmat.customlayoutkeyboard.HangulAssembler
import com.soobakjonmat.customlayoutkeyboard.R
import kotlin.collections.List

class KoreanLayout(mainKeyboardService: MainKeyboardService) : LanguageLayout(mainKeyboardService) {
    val hangulAssembler = HangulAssembler(mainKeyboardService)

    private val capsRow1Letters = listOf("ㅃ", "ㅉ", "ㄸ", "ㄲ", "ㅆ", "ㅛ", "ㅕ", "ㅑ", "ㅒ")
    private val capsRow2Letters = listOf("ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ", "ㅖ")
    private val capsRow3Letters = listOf("ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ")
    private val capsLetterList = listOf(capsRow1Letters, capsRow2Letters, capsRow3Letters)

    private val combinedCapsLetterList = List(capsLetterList.size) { mutableListOf<SpannableString>() }

    @SuppressLint("ClickableViewAccessibility")
    override fun init() {
        super.init()

        row1Letters = listOf("ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ")
        row2Letters = listOf("ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ", "ㅔ")
        row3Letters = listOf("ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ")
        letterList = listOf(row1Letters, row2Letters, row3Letters)
        rowList = List(letterList.size) { LinearLayout(mainKeyboardView.context) }
        combinedLetterList = List(letterList.size) { mutableListOf() }

        for (i in letterList.indices) {
            // add buttons to btnList
            btnList.add(List(letterList[i].size) { Button(ContextThemeWrapper(mainKeyboardService, R.style.Theme_LetterBtn)) })
            // set linear layout attributes
            rowList[i].layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            rowList[i].orientation = LinearLayout.HORIZONTAL
            // create letter buttons and set attributes
            for (j in letterList[i].indices) {
                val text = SpannableString(mainKeyboardService.subTextLetterList[i][j] + "\n" + letterList[i][j])
                val capsText = SpannableString(mainKeyboardService.subTextLetterList[i][j] + "\n" + capsLetterList[i][j])
                if (mainKeyboardService.subTextLetterList[i][j] != "") {
                    // set subtext size
                    text.setSpan(
                        ForegroundColorSpan(mainKeyboardService.subtextColor),
                        0,
                        1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    capsText.setSpan(
                        ForegroundColorSpan(mainKeyboardService.subtextColor),
                        0,
                        1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                // set text size
                text.setSpan(
                    RelativeSizeSpan(resources.getFloat(R.dimen.text_scale)),
                    text.length - 1,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                capsText.setSpan(
                    RelativeSizeSpan(resources.getFloat(R.dimen.text_scale)),
                    text.length - 1,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                // set font
                text.setSpan(
                    TypefaceSpan("Arial"),
                    0,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                combinedLetterList[i].add(text)
                combinedCapsLetterList[i].add(capsText)
                btnList[i][j].text = text
                btnList[i][j].layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                btnList[i][j].setPadding(0)

                val gestureDetector = GestureDetector(mainKeyboardService, KoreanGestureListener(i, j))
                btnList[i][j].setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                }

                // add buttons to linear layouts
                rowList[i].addView(btnList[i][j])
            }
        }

        capsLockBtn.setOnClickListener {
            mainKeyboardService.vibrate()
            when (capsLockMode) {
                0 -> {
                    setToUppercase()
                    capsLockMode = 1
                    capsLockBtn.setImageDrawable(capsLockMode1Image)
                }
                1 -> {
                    setToLowercase()
                    capsLockMode = 0
                    capsLockBtn.setImageDrawable(capsLockMode0Image)
                }
            }
        }
        rowList[rowList.size-1].addView(capsLockBtn, 0)

        // set backspaceBtn attributes
        backspaceBtn.setImageDrawable(mainKeyboardService.backspaceImage)
        backspaceBtn.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            resources.getFloat(R.dimen.backspace_weight)
        )
        backspaceBtn.setOnClickListener {
            mainKeyboardService.vibrate()
            if (mainKeyboardService.currentInputConnection.getSelectedText(0).isNullOrEmpty()) {
                // no selection, so delete previous character
                hangulAssembler.deleteText()
            } else {
                // delete the selection
                mainKeyboardService.currentInputConnection.commitText("", 1)
            }
        }
        rowList[rowList.size-1].addView(backspaceBtn, rowList[rowList.size-1].size)
    }

    override fun setToUppercase() {
        for (i in combinedCapsLetterList.indices) {
            for (j in combinedCapsLetterList[i].indices) {
                btnList[i][j].text = combinedCapsLetterList[i][j]
            }
        }
    }

    override fun setToLowercase() {
        for (i in combinedLetterList.indices) {
            for (j in combinedLetterList[i].indices) {
                btnList[i][j].text = combinedLetterList[i][j]
            }
        }
    }

    private inner class KoreanGestureListener(i: Int, j: Int) : LanguageGestureListener(i, j) {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            btnList[i][j].isPressed = false
            if (capsLockMode == 1) {
                setToLowercase()
                capsLockBtn.setImageDrawable(capsLockMode0Image)
                capsLockMode = 0
                hangulAssembler.commitText(capsLetterList[i][j])
            }
            else {
                hangulAssembler.commitText(letterList[i][j])
            }
            return true
        }
    }
}