package com.soobakjonmat.customlayoutkeyboard.layout

import android.annotation.SuppressLint
import android.content.res.Resources
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.core.view.size
import com.soobakjonmat.customlayoutkeyboard.MainKeyboardService
import com.soobakjonmat.customlayoutkeyboard.HangulAssembler
import com.soobakjonmat.customlayoutkeyboard.R
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.collections.List

class KoreanLayout(private val mainKeyboardService: MainKeyboardService) {
    private val mainKeyboardView = mainKeyboardService.mainKeyboardView
    private val resources: Resources = mainKeyboardService.baseContext.resources
    private val rapidTextDeleteInterval = mainKeyboardService.rapidTextDeleteInterval
    private val colorThemeMap = mainKeyboardService.colorThemeMap
    private val gestureMinDist = mainKeyboardService.gestureMinDist
    val hangulAssembler = HangulAssembler(mainKeyboardService)

    private val capsLockMode0Image = mainKeyboardService.capsLockMode0Image
    private val capsLockMode1Image = mainKeyboardService.capsLockMode1Image
    private val backspaceImage = mainKeyboardService.backspaceImage

    private val row1Letters = listOf("ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ")
    private val row2Letters = listOf("ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ", "ㅔ")
    private val row3Letters = listOf("ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ")
    private val letterList = listOf(row1Letters, row2Letters, row3Letters)

    private val combinedLetterList = List(letterList.size) { mutableListOf<SpannableString>() }

    private val capsRow1Letters = listOf("ㅃ", "ㅉ", "ㄸ", "ㄲ", "ㅆ", "ㅛ", "ㅕ", "ㅑ", "ㅒ")
    private val capsRow2Letters = listOf("ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ", "ㅖ")
    private val capsRow3Letters = listOf("ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ")
    private val capsLetterList = listOf(capsRow1Letters, capsRow2Letters, capsRow3Letters)

    private val combinedCapsLetterList = List(capsLetterList.size) { mutableListOf<SpannableString>() }

    private val btnList = mutableListOf<List<Button>>()

    private val rowList = List(letterList.size) { LinearLayout(mainKeyboardService) }

    private val capsLockBtn = ImageButton(mainKeyboardService)
    private val backspaceBtn = ImageButton(mainKeyboardService)

    private var lastDownX = 0f
    private var capsLockMode = 0

    @SuppressLint("ClickableViewAccessibility")
    fun init() {
        for (i in letterList.indices) {
            // add buttons to btnList
            btnList.add(List(letterList[i].size) { Button(mainKeyboardService) })
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
                    text.setSpan(
                        ForegroundColorSpan(colorThemeMap.getValue("subText")),
                        0,
                        1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    capsText.setSpan(
                        ForegroundColorSpan(colorThemeMap.getValue("subText")),
                        0,
                        1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
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
                combinedLetterList[i].add(text)
                combinedCapsLetterList[i].add(capsText)
                btnList[i][j].text = text
                btnList[i][j].layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                btnList[i][j].setPadding(0)

                val gestureDetector = GestureDetector(mainKeyboardService, SimpleGestureDetector(mainKeyboardService, this, i, j))
                btnList[i][j].setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                        // on fling keyboard from right to left
                        if (lastDownX - event.rawX > gestureMinDist) {
                            mainKeyboardService.deleteByWord(-1)
                            return@setOnTouchListener true
                        } else if (event.rawX - lastDownX > gestureMinDist) {
                            mainKeyboardService.deleteByWord(1)
                            return@setOnTouchListener true
                        }
                    }
                    return@setOnTouchListener gestureDetector.onTouchEvent(event)
                }

                // add buttons to linear layouts
                rowList[i].addView(btnList[i][j])
            }
        }
        // set capsLockBtn attributes
        capsLockBtn.setImageDrawable(capsLockMode0Image)
        capsLockBtn.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            resources.getFloat(R.dimen.caps_lock_weight)
        )
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
        backspaceBtn.setImageDrawable(backspaceImage)
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
        backspaceBtn.setOnLongClickListener {
            Timer().schedule(timerTask {
                if (!backspaceBtn.isPressed || !mainKeyboardService.deleteByWord(-1)) {
                    this.cancel()
                }
            }, 0, rapidTextDeleteInterval)
            return@setOnLongClickListener true
        }
        rowList[rowList.size-1].addView(backspaceBtn, rowList[rowList.size-1].size)
    }

    fun insertLetterBtns() {
        for (i in rowList.size - 1 downTo 0) {
            mainKeyboardView.addView(rowList[i], 1)
        }
    }

    private fun setToUppercase() {
        for (i in combinedCapsLetterList.indices) {
            for (j in combinedCapsLetterList[i].indices) {
                btnList[i][j].text = combinedCapsLetterList[i][j]
            }
        }
    }

    private fun setToLowercase() {
        for (i in combinedLetterList.indices) {
            for (j in combinedLetterList[i].indices) {
                btnList[i][j].text = combinedLetterList[i][j]
            }
        }
    }

    fun setColor() {
        for (i in letterList.indices) {
            for (j in letterList[i].indices) {
                // letter buttons
                btnList[i][j].setTextColor(colorThemeMap.getValue("mainText"))
                btnList[i][j].setBackgroundColor(colorThemeMap.getValue("commonBtnBg"))
                // capsLockBtn
                capsLockBtn.setBackgroundColor(colorThemeMap.getValue("bg"))
                // backspaceBtn
                backspaceBtn.setBackgroundColor(colorThemeMap.getValue("bg"))
            }
        }
    }

    private class SimpleGestureDetector(
        private val mainActivity: MainKeyboardService,
        private val layout: KoreanLayout,
        private val i: Int,
        private val j: Int
    ) : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(event: MotionEvent): Boolean {
            mainActivity.vibrate()
            layout.lastDownX = event.rawX
            return super.onDown(event)
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            if (layout.capsLockMode == 1) {
                layout.setToLowercase()
                layout.capsLockBtn.setImageDrawable(layout.capsLockMode0Image)
                layout.capsLockMode = 0
                layout.hangulAssembler.commitText(layout.capsLetterList[i][j])
            }
            else {
                layout.hangulAssembler.commitText(layout.letterList[i][j])
            }
            return super.onSingleTapUp(event)
        }

        override fun onLongPress(event: MotionEvent) {
            mainActivity.resetAndFinishComposing()
            mainActivity.currentInputConnection.commitText(mainActivity.subTextLetterList[i][j], 1)
            return super.onLongPress(event)
        }
    }
}