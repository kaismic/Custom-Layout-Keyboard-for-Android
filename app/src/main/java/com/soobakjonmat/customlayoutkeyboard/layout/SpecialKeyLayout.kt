package com.soobakjonmat.customlayoutkeyboard.layout

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.core.view.size
import com.soobakjonmat.customlayoutkeyboard.MainKeyboardService
import com.soobakjonmat.customlayoutkeyboard.R
import java.util.*

class SpecialKeyLayout(mainKeyboardService: MainKeyboardService) : KeyboardLayout(mainKeyboardService) {
    @SuppressLint("ClickableViewAccessibility")
    override fun init() {
        super.init()

        rowList = List(mainKeyboardService.subTextLetterList.size) { LinearLayout(mainKeyboardView.context) }

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
                val gestureDetector = GestureDetector(mainKeyboardService, SpecialKeyGestureListener(i, j))
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
        rowList[rowList.size-1].addView(backspaceBtn, rowList[rowList.size-1].size)
    }

    private inner class SpecialKeyGestureListener(i: Int, j: Int) : KeyboardGestureListener(i, j) {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            super.onSingleTapUp(event)
            mainKeyboardService.currentInputConnection.commitText(mainKeyboardService.subTextLetterList[i][j], 1)
            return true
        }

        override fun onLongPress(event: MotionEvent) {

        }
    }
}