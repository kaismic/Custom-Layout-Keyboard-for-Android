package com.soobakjonmat.colemakbasedkeyboard.keyboard_language_layouts

import android.annotation.SuppressLint
import android.content.res.Resources
import android.util.TypedValue
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.size
import com.soobakjonmat.colemakbasedkeyboard.ColemakBasedKeyboard
import com.soobakjonmat.colemakbasedkeyboard.R
import java.util.Timer
import kotlin.concurrent.timerTask

class EnglishLayout(private val mainActivity: ColemakBasedKeyboard) {
    private val ctx = mainActivity.baseContext
    private val mainKeyboardView = mainActivity.mainKeyboardView
    private val resources: Resources = mainActivity.baseContext.resources
    private val rapidTextDeleteInterval = mainActivity.rapidTextDeleteInterval
    private val colorThemeMap = mainActivity.colorThemeMap
    private val delGestureMinDist = mainActivity.spacebarMinSlideDist

    private var capsLockMode = 0
    private val capsLockBtn = ImageButton(ctx)
    private val backspaceBtn = Button(ctx)

    private val capsLockMode0Image = ResourcesCompat.getDrawable(resources, R.drawable.caps_lock_mode_0, null)
    private val capsLockMode1Image = ResourcesCompat.getDrawable(resources, R.drawable.caps_lock_mode_1, null)
    private val capsLockMode2Image = ResourcesCompat.getDrawable(resources, R.drawable.caps_lock_mode_2, null)

    private var lastDownX = 0f
    private var lastDownLetter = ""

    private val row1Letters = listOf("q", "w", "f", "p", "g", "j", "l", "u", "y")
    private val row2Letters = listOf("a", "s", "d", "t", "r", "h", "e", "k", "i", "o")
    private val row3Letters = listOf("z", "x", "c", "v", "b", "n", "m")
    private val letterList = listOf(row1Letters, row2Letters, row3Letters)

    private val row1Btns: List<Button> = List(row1Letters.size) { Button(ctx) }
    private val row2Btns: List<Button> = List(row2Letters.size) { Button(ctx) }
    private val row3Btns: List<Button> = List(row3Letters.size) { Button(ctx) }
    private val btnList = listOf(row1Btns, row2Btns, row3Btns)

    private val row1: LinearLayout = LinearLayout(ctx)
    private val row2: LinearLayout = LinearLayout(ctx)
    private val row3: LinearLayout = LinearLayout(ctx)
    private val rowList = listOf(row1, row2, row3)


    /*
    sample xml codes
        <LinearLayout
        android:id="@+id/row_2"
        android:layout_width="match_parent"
        android:layout_height="@dimen/row_1_height"
        android:gravity="center"
        android:orientation="horizontal">
        </LinearLayout>

        <Button
        android:id="@+id/key_Q"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:layout_width="0dp"
        android:textAllCaps="false"
        android:padding="0dp"
        android:text="q"
        />
     */


    @SuppressLint("ClickableViewAccessibility")
    fun init() {
        // todo subtext with long click
        for (i in letterList.indices) {
            // set linear layout attributes
            rowList[i].layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.common_row_height)
            )
            rowList[i].orientation = LinearLayout.HORIZONTAL
            // create letter buttons and set attributes
            for (j in letterList[i].indices) {
                btnList[i][j].text = letterList[i][j]
                val param = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                btnList[i][j].layoutParams = param
                btnList[i][j].isAllCaps = false
                // set text size
                btnList[i][j].setTextSize(TypedValue.COMPLEX_UNIT_DIP, resources.getFloat(R.dimen.english_letter_text_size))

                btnList[i][j].setOnTouchListener { btn, motionEvent ->
                    if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                        lastDownX = motionEvent.rawX
                        lastDownLetter = (btn as Button).text.toString()
                    } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                        // on fling keyboard from right to left
                        if (lastDownX - motionEvent.rawX > delGestureMinDist) {
                            mainActivity.deleteWholeWord()
                        }
                        // on click
                        else
                        {
                            if (capsLockMode == 0) {
                                mainActivity.currentInputConnection.commitText(lastDownLetter, 1)
                            } else {
                                if (capsLockMode == 1) {
                                    setToLowercase()
                                    capsLockBtn.setImageDrawable(capsLockMode0Image)
                                    capsLockMode = 0
                                }
                                mainActivity.currentInputConnection.commitText(lastDownLetter.uppercase(), 1)
                            }
                        }
                    }
                    return@setOnTouchListener true
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
        row3.addView(capsLockBtn, 0)

        // set backspaceBtn attributes
        backspaceBtn.text = resources.getString(R.string.backspace_symbol)
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
                if (!backspaceBtn.isPressed || !mainActivity.deleteWholeWord()) {
                    this.cancel()
                }
            }, 0, rapidTextDeleteInterval)
            return@setOnLongClickListener true
        }

        row3.addView(backspaceBtn, row3.size)
    }

    fun insertLetterBtnsOnKeyboard() {
        for (i in rowList.size-1 downTo 0) {
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

    private fun setToLowercase() {
        for (i in letterList.indices) {
            for (j in letterList[i].indices) {
                btnList[i][j].isAllCaps = false
            }
        }
    }

    // todo use setBackgroundResource by making a drawable background button resource
    fun setColorTheme() { // todo set subtext colors
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
}