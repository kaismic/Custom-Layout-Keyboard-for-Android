package com.soobakjonmat.customlayoutkeyboard.layout

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.core.view.size
import com.soobakjonmat.customlayoutkeyboard.MainKeyboardService
import com.soobakjonmat.customlayoutkeyboard.R
import java.util.*

class SpecialKeyLayout(mainKeyboardService: MainKeyboardService) : KeyboardLayout(mainKeyboardService) {

    val subTextLetterList = mainKeyboardService.subTextLetterList

    @SuppressLint("ClickableViewAccessibility")
    override fun init() {
        super.init()

        rowList = Array(subTextLetterList.size) { LinearLayout(mainKeyboardView.context) }

        for (i in subTextLetterList.indices) {
            // initialise btnList
            btnList.add(Array(subTextLetterList[i].size) { Button(ContextThemeWrapper(mainKeyboardView.context, R.style.Theme_LetterBtn)) })
            previewPopupList.add(Array(subTextLetterList[i].size) { PopupWindow(ContextThemeWrapper(mainKeyboardView.context, R.style.Theme_TransparentBackground)) })
            // set linear layout attributes
            rowList[i].layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            rowList[i].orientation = LinearLayout.HORIZONTAL
            // create letter buttons and set attributes
            for (j in subTextLetterList[i].indices) {
                btnList[i][j].text = subTextLetterList[i][j]
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
                    if (event.action == MotionEvent.ACTION_UP) {
                        previewPopupList[i][j].dismiss()
                    }
                    gestureDetector.onTouchEvent(event)
                }
                // add buttons to linear layouts
                rowList[i].addView(btnList[i][j])

                // key preview popup
                previewPopupList[i][j].isTouchable = false
                previewPopupList[i][j].contentView = TextView(ContextThemeWrapper(mainKeyboardView.context, R.style.Theme_PreviewPopupTextView))
                (previewPopupList[i][j].contentView as TextView).background = ResourcesCompat.getDrawable(resources, R.drawable.preview_popup_background, ContextThemeWrapper(mainKeyboardView.context, R.style.Theme_PreviewPopupTextView).theme)
                (previewPopupList[i][j].contentView as TextView).text = subTextLetterList[i][j]
                (previewPopupList[i][j].contentView as TextView).elevation = 8f
                (previewPopupList[i][j].contentView as TextView).setPadding(resources.getInteger(R.integer.english_preview_popup_text_padding), 0, 0, 0)
                (previewPopupList[i][j].contentView as TextView).setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getFloat(R.dimen.preview_popup_text_size))
                previewPopupList[i][j].setBackgroundDrawable(null)
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
            mainKeyboardService.currentInputConnection.commitText(subTextLetterList[i][j], 1)
            return true
        }

        override fun onLongPress(event: MotionEvent) {}
    }
}