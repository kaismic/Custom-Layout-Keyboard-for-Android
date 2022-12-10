package com.soobakjonmat.customlayoutkeyboard.layout

import android.content.res.Resources
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import com.soobakjonmat.customlayoutkeyboard.MainKeyboardService
import com.soobakjonmat.customlayoutkeyboard.R
import java.util.*
import kotlin.concurrent.timerTask

abstract class KeyboardLayout(protected val mainKeyboardService: MainKeyboardService) {
    protected val mainKeyboardView = mainKeyboardService.mainKeyboardView
    protected val resources: Resources = mainKeyboardService.baseContext.resources
    protected val gestureMinDist = mainKeyboardService.gestureMinDist

    protected val btnList = mutableListOf<List<Button>>()
    protected lateinit var rowList: List<LinearLayout>
    protected val backspaceBtn = ImageButton(ContextThemeWrapper(mainKeyboardService, R.style.Theme_ControlBtn))

    open fun init() {
        // set backspaceBtn behaviour when long clicked
        backspaceBtn.setOnLongClickListener {
            Timer().schedule(timerTask {
                if (!backspaceBtn.isPressed || !mainKeyboardService.deleteByWord(-1)) {
                    this.cancel()
                }
            }, 0, mainKeyboardService.rapidTextDeleteInterval)
            return@setOnLongClickListener true
        }
    }

    fun insertLetterBtns() {
        for (i in rowList.size - 1 downTo 0) {
            mainKeyboardView.addView(rowList[i], 1)
        }
    }

    abstract inner class KeyboardGestureListener(
        protected val i: Int,
        protected val j: Int
    ) : GestureDetector.OnGestureListener {

        override fun onDown(event: MotionEvent): Boolean {
            btnList[i][j].isPressed = true
            mainKeyboardService.vibrate()
            return true
        }

        override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
            btnList[i][j].isPressed = false
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
            btnList[i][j].isPressed = false
            return true
        }

        override fun onShowPress(p0: MotionEvent) {
            // todo keyboard button popup when pressed and on long click change text on the popup
        }
    }
}