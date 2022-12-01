package com.soobakjonmat.customlayoutkeyboard.layout

import android.annotation.SuppressLint
import android.content.res.Resources
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.core.view.size
import com.soobakjonmat.customlayoutkeyboard.MainKeyboardService
import com.soobakjonmat.customlayoutkeyboard.R
import java.util.*
import kotlin.concurrent.timerTask

class SpecialKeyLayout(private val mainKeyboardService: MainKeyboardService) {
    private val mainKeyboardView = mainKeyboardService.mainKeyboardView
    private val resources: Resources = mainKeyboardService.baseContext.resources
    private val gestureMinDist = mainKeyboardService.gestureMinDist

    private val btnList = mutableListOf<List<Button>>()
    private val rowList = List(mainKeyboardService.subTextLetterList.size) { LinearLayout(mainKeyboardView.context) }
    private val backspaceBtn = ImageButton(ContextThemeWrapper(mainKeyboardService, R.style.Theme_ControlBtn))

    @SuppressLint("ClickableViewAccessibility")
    fun init() {
        for (i in mainKeyboardService.subTextLetterList.indices) {
            // add buttons to btnList
            btnList.add(List(mainKeyboardService.subTextLetterList[i].size) { Button(ContextThemeWrapper(mainKeyboardService, R.style.Theme_LetterBtn)) })
            // set linear layout attributes
            rowList[i].layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            rowList[i].orientation = LinearLayout.HORIZONTAL
            // create letter buttons and set attributes
            for (j in mainKeyboardService.subTextLetterList[i].indices) {
                btnList[i][j].text = mainKeyboardService.subTextLetterList[i][j]
                btnList[i][j].setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getFloat(R.dimen.default_text_size))
                btnList[i][j].layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                btnList[i][j].isAllCaps = false
                btnList[i][j].setPadding(0)
                val gestureDetector = GestureDetector(mainKeyboardService, GestureListener(i, j))
                btnList[i][j].setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                }
                // add buttons to linear layouts
                rowList[i].addView(btnList[i][j])
            }
        }
        // fill caps lock btn place
        val substBtn = Button(ContextThemeWrapper(mainKeyboardService, R.style.Theme_LetterBtn))
        substBtn.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            resources.getFloat(R.dimen.caps_lock_weight)
        )
        substBtn.setPadding(0)

        rowList[rowList.size-1].addView(substBtn, 0)
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
                mainKeyboardService.currentInputConnection.deleteSurroundingText(1, 0)
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
            }, 0, mainKeyboardService.rapidTextDeleteInterval)
            return@setOnLongClickListener true
        }

        rowList[rowList.size-1].addView(backspaceBtn, rowList[rowList.size-1].size)
    }

    fun insertLetterBtns() {
        for (i in rowList.size - 1 downTo 0) {
            mainKeyboardView.addView(rowList[i], 1)
        }
    }

    private inner class GestureListener(
        private val i: Int,
        private val j: Int
    ) : GestureDetector.OnGestureListener {

        override fun onDown(event: MotionEvent): Boolean {
            btnList[i][j].isPressed = true
            mainKeyboardService.vibrate()
            return true
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            btnList[i][j].isPressed = false
            mainKeyboardService.currentInputConnection.commitText(mainKeyboardService.subTextLetterList[i][j], 1)
            return true
        }

        override fun onLongPress(event: MotionEvent) {

        }

        override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
            if (p0.rawX - p1.rawX > gestureMinDist) {
                mainKeyboardService.deleteByWord(-1)
                return true
            }
            else if (p1.rawX - p0.rawX > gestureMinDist) {
                mainKeyboardService.deleteByWord(1)
                return true
            }
            return false
        }

        override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
            return true
        }

        override fun onShowPress(p0: MotionEvent) {

        }
    }
}