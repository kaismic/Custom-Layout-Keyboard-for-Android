package com.soobakjonmat.customlayoutkeyboard.layout

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.soobakjonmat.customlayoutkeyboard.MainKeyboardService
import com.soobakjonmat.customlayoutkeyboard.R

abstract class LanguageLayout(mainKeyboardService: MainKeyboardService) : KeyboardLayout(mainKeyboardService) {
    protected val capsLockMode0Image = mainKeyboardService.capsLockMode0Image
    protected val capsLockMode1Image = mainKeyboardService.capsLockMode1Image
    protected val capsLockMode2Image = mainKeyboardService.capsLockMode2Image

    protected lateinit var letterList: List<Array<String>>
    protected val combinedLetterList = mutableListOf<Array<SpannableString>>()

    protected val capsLockBtn = ImageButton(ContextThemeWrapper(mainKeyboardService, R.style.Theme_ControlBtn))

    protected var capsLockMode = 0

    override fun init() {
        super.init()
        // set capsLockBtn attributes
        capsLockBtn.setImageDrawable(capsLockMode0Image)
        capsLockBtn.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            resources.getFloat(R.dimen.caps_lock_weight)
        )
    }

    fun updateSubtextColor() {
        for (i in letterList.indices) {
            for (j in letterList[i].indices) {
                if (mainKeyboardService.subTextLetterList[i][j].isNotEmpty()) {
                    combinedLetterList[i][j].setSpan(
                        ForegroundColorSpan(mainKeyboardService.subtextColor),
                        0,
                        1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    }

    override fun insertLayout() {
        for (i in rowList.size - 1 downTo 0) {
            mainKeyboardView.addView(rowList[i], 0)
        }
        mainKeyboardView.addView(mainKeyboardService.numberRow, 0)
    }

    protected abstract fun setToUppercase()

    protected abstract fun setToLowercase()

    abstract inner class LanguageGestureListener(i: Int, j: Int) : KeyboardGestureListener(i, j) {
        override fun onLongPress(event: MotionEvent) {
            (previewPopupList[i][j].contentView as TextView).text = mainKeyboardService.subTextLetterList[i][j]
            mainKeyboardService.vibrate()
            mainKeyboardService.resetAndFinishComposing()
            mainKeyboardService.currentInputConnection.commitText(mainKeyboardService.subTextLetterList[i][j], 1)
        }
    }
}