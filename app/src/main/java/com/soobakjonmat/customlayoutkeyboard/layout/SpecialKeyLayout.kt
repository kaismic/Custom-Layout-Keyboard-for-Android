package com.soobakjonmat.customlayoutkeyboard.layout

import android.annotation.SuppressLint
import android.content.res.Resources
import android.util.TypedValue

import android.view.MotionEvent
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.core.view.size
import com.soobakjonmat.customlayoutkeyboard.CustomLayoutKeyboard
import com.soobakjonmat.customlayoutkeyboard.R
import java.util.*
import kotlin.concurrent.timerTask

class SpecialKeyLayout(private val mainActivity: CustomLayoutKeyboard) {
    private val ctx = mainActivity.baseContext
    private val mainKeyboardView = mainActivity.mainKeyboardView
    private val resources: Resources = mainActivity.baseContext.resources
    private val rapidTextDeleteInterval = mainActivity.rapidTextDeleteInterval
    private val colorThemeMap = mainActivity.colorThemeMap
    private val gestureMinDist = mainActivity.gestureMinDist

    private val btnList = mutableListOf<List<Button>>()
    private val rowList = List(mainActivity.subTextLetterList.size) { LinearLayout(ctx) }
    private val backspaceBtn = Button(ctx)
    private var lastDownX = 0f

    @SuppressLint("ClickableViewAccessibility")
    fun init() {
        for (i in mainActivity.subTextLetterList.indices) {
            // add buttons to btnList
            btnList.add(List(mainActivity.subTextLetterList[i].size) { Button(ctx) })
            // set linear layout attributes
            rowList[i].layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            rowList[i].orientation = LinearLayout.HORIZONTAL
            // create letter buttons and set attributes
            for (j in mainActivity.subTextLetterList[i].indices) {
                btnList[i][j].text = mainActivity.subTextLetterList[i][j]
                btnList[i][j].setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getFloat(R.dimen.default_text_size))
                btnList[i][j].layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                btnList[i][j].isAllCaps = false
                btnList[i][j].setPadding(0)

                btnList[i][j].setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        lastDownX = event.rawX
                    }
                    if (event.action == MotionEvent.ACTION_UP) {
                        // on fling keyboard from right to left
                        if (lastDownX - event.rawX > gestureMinDist) {
                            mainActivity.deleteByWord(-1)
                        }
                        else if (event.rawX - lastDownX > gestureMinDist) {
                            mainActivity.deleteByWord(1)
                        } else {
                            mainActivity.currentInputConnection.commitText(mainActivity.subTextLetterList[i][j], 1)
                        }
                    }
                    return@setOnTouchListener true
                }
                // add buttons to linear layouts
                rowList[i].addView(btnList[i][j])
            }
        }
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

    fun setColor() {
        for (i in mainActivity.subTextLetterList.indices) {
            for (j in mainActivity.subTextLetterList[i].indices) {
                // letter buttons
                btnList[i][j].setTextColor(colorThemeMap.getValue("mainText"))
                btnList[i][j].setBackgroundColor(colorThemeMap.getValue("commonBtnBg"))
                // backspaceBtn
                backspaceBtn.setBackgroundColor(colorThemeMap.getValue("bg"))
                backspaceBtn.setTextColor(colorThemeMap.getValue("mainText"))
            }
        }
    }
}