package com.soobakjonmat.customlayoutkeyboard.layout

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.Gravity
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

class SpecialKeyLayout(mainKeyboardService: MainKeyboardService) : KeyboardLayout(mainKeyboardService) {

    val subTextLetterList = mainKeyboardService.subTextLetterList
    private lateinit var topBtns: Array<Button>
    private lateinit var topRow: LinearLayout
    private val topPreviewPopupList = Array(mainKeyboardService.numBtnSubTexts.size) { PopupWindow(ContextThemeWrapper(mainKeyboardView.context, R.style.Theme_TransparentBackground)) }

    @SuppressLint("ClickableViewAccessibility")
    override fun init() {
        super.init()

        topRow = LinearLayout(mainKeyboardView.context)
        topRow.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        topRow.orientation = LinearLayout.HORIZONTAL
        topBtns = Array(mainKeyboardService.numBtnSubTexts.size) { Button(ContextThemeWrapper(mainKeyboardView.context, R.style.Theme_LetterBtn)) }
        for (i in mainKeyboardService.numBtnSubTexts.indices) {
            topBtns[i].layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
            topBtns[i].setPadding(0)

            val gestureDetector = GestureDetector(mainKeyboardService, TopRowGestureListener(i))
            topBtns[i].setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    topPreviewPopupList[i].dismiss()
                }
                gestureDetector.onTouchEvent(event)
            }
            topBtns[i].text = mainKeyboardService.numBtnSubTexts[i]

            topRow.addView(topBtns[i])

            topPreviewPopupList[i].isTouchable = false
            topPreviewPopupList[i].contentView = TextView(ContextThemeWrapper(mainKeyboardView.context, R.style.Theme_PreviewPopupTextView))
            (topPreviewPopupList[i].contentView as TextView).background = ResourcesCompat.getDrawable(resources, R.drawable.preview_popup_background, ContextThemeWrapper(mainKeyboardView.context, R.style.Theme_PreviewPopupTextView).theme)
            (topPreviewPopupList[i].contentView as TextView).text = mainKeyboardService.numBtnSubTexts[i]
            (topPreviewPopupList[i].contentView as TextView).elevation = 8f
            (topPreviewPopupList[i].contentView as TextView).setPadding(resources.getInteger(R.integer.english_preview_popup_text_padding), 0, 0, 0)
            (topPreviewPopupList[i].contentView as TextView).setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getFloat(R.dimen.preview_popup_text_size))
            topPreviewPopupList[i].setBackgroundDrawable(null)
        }

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

    override fun insertLayout() {
        for (i in rowList.size - 1 downTo 0) {
            mainKeyboardView.addView(rowList[i], 0)
        }
        mainKeyboardView.addView(topRow, 0)
    }

    private inner class TopRowGestureListener(val i: Int) : OnGestureListener {
        override fun onDown(p0: MotionEvent): Boolean {
            topBtns[i].isPressed = true
            val loc = IntArray(2)
            topBtns[i].getLocationInWindow(loc)
            topPreviewPopupList[i].showAtLocation(topBtns[i], Gravity.NO_GRAVITY, 0, 0)
            topPreviewPopupList[i].update(loc[0], loc[1]-128, 128, 128, false)
            mainKeyboardService.vibrate()
            return true
        }

        override fun onShowPress(p0: MotionEvent) {}
        override fun onLongPress(p0: MotionEvent) {}

        override fun onSingleTapUp(p0: MotionEvent): Boolean {
            topBtns[i].isPressed = false
            topPreviewPopupList[i].dismiss()
            mainKeyboardService.currentInputConnection.commitText(mainKeyboardService.numBtnSubTexts[i], 1)
            return true
        }

        override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
            topBtns[i].isPressed = false
            topPreviewPopupList[i].dismiss()
            return true
        }

        override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
            topBtns[i].isPressed = false
            topPreviewPopupList[i].dismiss()
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