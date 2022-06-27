package com.soobakjonmat.customlayoutkeyboard.layout

import android.annotation.SuppressLint
import android.content.res.Resources
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.TypedValue
import android.view.GestureDetector

import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.core.view.size

import com.soobakjonmat.customlayoutkeyboard.CustomLayoutKeyboard
import com.soobakjonmat.customlayoutkeyboard.R
import java.util.Timer
import kotlin.concurrent.timerTask

class EnglishLayout(private val mainActivity: CustomLayoutKeyboard) {
    private val ctx = mainActivity.baseContext
    private val mainKeyboardView = mainActivity.mainKeyboardView
    private val resources: Resources = mainActivity.baseContext.resources
    private val rapidTextDeleteInterval = mainActivity.rapidTextDeleteInterval
    private val colorThemeMap = mainActivity.colorThemeMap
    private val gestureMinDist = mainActivity.gestureMinDist

    private val capsLockMode0Image = mainActivity.capsLockMode0Image
    private val capsLockMode1Image = mainActivity.capsLockMode1Image
    private val capsLockMode2Image = mainActivity.capsLockMode2Image

    private val row1Letters = listOf("q", "w", "f", "p", "g", "j", "l", "u", "y")
    private val row2Letters = listOf("a", "s", "d", "t", "r", "h", "e", "k", "i", "o")
    private val row3Letters = listOf("z", "x", "c", "v", "b", "n", "m")
    private val letterList = listOf(row1Letters, row2Letters, row3Letters)

    private val btnList = mutableListOf<List<Button>>()

    private val rowList = List(letterList.size) { LinearLayout(ctx) }

    private val capsLockBtn = ImageButton(ctx)
    private val backspaceBtn = Button(ctx)

    private var capsLockMode = 0
    private var lastDownX = 0f

    @SuppressLint("ClickableViewAccessibility")
    fun init() {
        for (i in letterList.indices) {
            // add buttons to btnList
            btnList.add(List(letterList[i].size) { Button(ctx) })
            // set linear layout attributes
            rowList[i].layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            rowList[i].orientation = LinearLayout.HORIZONTAL
            // create letter buttons and set attributes
            for (j in letterList[i].indices) {
                val text = SpannableString(mainActivity.subTextLetterList[i][j] + "\n" + letterList[i][j])
                if (mainActivity.subTextLetterList[i][j] != "") {
                    text.setSpan(
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
                btnList[i][j].text = text
                btnList[i][j].layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                btnList[i][j].isAllCaps = false
                btnList[i][j].setPadding(0)

                val gestureDetector = GestureDetector(ctx, SimpleGestureDetector(mainActivity, this, i, j))
                btnList[i][j].setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        // on fling keyboard from right to left
                        if (lastDownX - event.rawX > gestureMinDist) {
                            mainActivity.deleteByWord(-1)
                            return@setOnTouchListener true
                        }
                        else if (event.rawX - lastDownX > gestureMinDist) {
                            mainActivity.deleteByWord(1)
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
            when (capsLockMode) {
                0 -> {
                    setToUppercase()
                    capsLockMode = 1
                    capsLockBtn.setImageDrawable(capsLockMode1Image)
                }
                1 -> {
                    capsLockMode = 2
                    capsLockBtn.setImageDrawable(capsLockMode2Image)
                }
                2 -> {
                    setToLowercase()
                    capsLockMode = 0
                    capsLockBtn.setImageDrawable(capsLockMode0Image)
                }
            }
        }
        rowList[rowList.size-1].addView(capsLockBtn, 0)

        // set backspaceBtn attributes
        backspaceBtn.text = resources.getString(R.string.backspace_symbol)
        backspaceBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getFloat(R.dimen.default_text_size))
        backspaceBtn.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            resources.getFloat(R.dimen.backspace_weight)
        )
        backspaceBtn.setOnClickListener {
            if (mainActivity.currentInputConnection.getSelectedText(0).isNullOrEmpty()) {
                // no selection, so delete previous character
                mainActivity.currentInputConnection.deleteSurroundingText(1, 0)
            } else {
                // delete the selection
                mainActivity.currentInputConnection.commitText("", 1)
            }
        }
        backspaceBtn.setOnLongClickListener {
            Timer().schedule(timerTask {
                if (!backspaceBtn.isPressed || !mainActivity.deleteByWord(-1)) {
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
        for (i in letterList.indices) {
            for (j in letterList[i].indices) {
                btnList[i][j].isAllCaps = true
            }
        }
    }

    fun setToLowercase() {
        for (i in letterList.indices) {
            for (j in letterList[i].indices) {
                btnList[i][j].isAllCaps = false
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
                backspaceBtn.setTextColor(colorThemeMap.getValue("mainText"))
            }
        }
    }

    private class SimpleGestureDetector(
        private val mainActivity: CustomLayoutKeyboard,
        private val layout: EnglishLayout,
        private val i: Int,
        private val j: Int
        ) : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(event: MotionEvent): Boolean {
            layout.lastDownX = event.rawX
            return super.onDown(event)
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            if (layout.capsLockMode == 0) {
                mainActivity.currentInputConnection.commitText(layout.letterList[i][j], 1)
            } else {
                if (layout.capsLockMode == 1) {
                    layout.setToLowercase()
                    layout.capsLockBtn.setImageDrawable(layout.capsLockMode0Image)
                    layout.capsLockMode = 0
                }
                mainActivity.currentInputConnection.commitText(layout.letterList[i][j].uppercase(), 1)
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
