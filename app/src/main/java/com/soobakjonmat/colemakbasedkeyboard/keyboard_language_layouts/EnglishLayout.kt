package com.soobakjonmat.colemakbasedkeyboard.keyboard_language_layouts

import android.content.res.Resources
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.core.view.size
import com.soobakjonmat.colemakbasedkeyboard.ColemakBasedKeyboard
import com.soobakjonmat.colemakbasedkeyboard.R

class EnglishLayout(
    private val mainActivity: ColemakBasedKeyboard,
    private val mainKeyboardView: LinearLayout) {
    private val ctx = mainActivity.baseContext
    private val resources: Resources = mainActivity.baseContext.resources
    private var capsLockMode = 0

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

    fun init() {
        for (i in letterList.indices) {
            // set linear layout attributes
            rowList[i].layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.common_row_height)
            )
            rowList[i].orientation = LinearLayout.HORIZONTAL
            for (j in letterList[i].indices) {
                // create letter buttons and set attributes
                btnList[i][j].text = letterList[i][j]
                btnList[i][j].layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f
                )
                btnList[i][j].setPadding(0)
                btnList[i][j].isAllCaps = false

                // todo set longClickListener
                // add buttons to linear layouts
                rowList[i].addView(btnList[i][j])
            }
        }
        val capsLockBtn = ImageButton(ctx)
        capsLockBtn.setImageDrawable(ResourcesCompat.getDrawable(
            resources,
            R.drawable.caps_lock_mode_0,
            null))
        capsLockBtn.layoutParams = LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getFloat(R.dimen.caps_lock_weight)
        )
        capsLockBtn.setPadding(0)
        capsLockBtn.setOnClickListener {
            when (capsLockMode) {
                0 -> {
                    setToUppercase()
                    capsLockMode = 1
                    capsLockBtn.setImageDrawable(ResourcesCompat.getDrawable(resources,
                        R.drawable.caps_lock_mode_1,
                        null))
                }
                1 -> {
                    capsLockMode = 2
                    capsLockBtn.setImageDrawable(ResourcesCompat.getDrawable(resources,
                        R.drawable.caps_lock_mode_2,
                        null))
                }
                2 -> {
                    setToLowercase()
                    capsLockMode = 0
                    capsLockBtn.setImageDrawable(ResourcesCompat.getDrawable(resources,
                        R.drawable.caps_lock_mode_0,
                        null))
                }
            }
        }
        row3.addView(capsLockBtn, 0)
        val backspaceBtn = Button(ctx)
        backspaceBtn.text = resources.getString(R.string.backspace_symbol)
        backspaceBtn.layoutParams = LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getFloat(R.dimen.backspace_weight)
        )
        backspaceBtn.setPadding(0)
        backspaceBtn.setOnClickListener {
            if (TextUtils.isEmpty(mainActivity.currentInputConnection.getSelectedText(0))) {
                // no selection, so delete previous character
                mainActivity.currentInputConnection.deleteSurroundingText(1, 0)
            } else {
                // delete the selection
                mainActivity.currentInputConnection.commitText("", 1)
            }
        }
        // todo backspace onLongClickListener delete each word separated by space
        row3.addView(backspaceBtn, row3.size)

        // set letter buttons onClickListener because capsLockBtn is created after letter buttons
        for (i in letterList.indices) {
            for (j in letterList[i].indices) {
                btnList[i][j].setOnClickListener {
                    if (capsLockMode == 0) {
                        mainActivity.currentInputConnection.commitText(letterList[i][j], 1)
                    } else {
                        if (capsLockMode == 1) {
                            setToLowercase()
                            capsLockBtn.setImageDrawable(ResourcesCompat.getDrawable(resources,
                                R.drawable.caps_lock_mode_0,
                                null))
                            capsLockMode = 0
                        }
                        mainActivity.currentInputConnection.commitText(letterList[i][j].uppercase(), 1)
                    }
                }
            }
        }

    }

    fun insertRows() {
        for (i in rowList.size-1 downTo 0) {
            mainKeyboardView.addView(rowList[i], 1)
        }
    }

    private fun setToUppercase() {
        for (i in letterList.indices) {
            for (j in letterList[i].indices) {
                btnList[i][j].text = letterList[i][j].uppercase()
            }
        }
    }

    private fun setToLowercase() {
        for (i in letterList.indices) {
            for (j in letterList[i].indices) {
                btnList[i][j].text = letterList[i][j]
            }
        }
    }
}