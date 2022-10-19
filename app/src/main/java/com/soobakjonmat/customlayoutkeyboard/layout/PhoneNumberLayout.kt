package com.soobakjonmat.customlayoutkeyboard.layout

import android.content.res.Resources
import android.view.ContextThemeWrapper
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.core.view.size
import com.soobakjonmat.customlayoutkeyboard.MainKeyboardService
import com.soobakjonmat.customlayoutkeyboard.R
import java.util.*

class PhoneNumberLayout (private val mainKeyboardService: MainKeyboardService) {
    private val mainKeyboardView = mainKeyboardService.mainKeyboardView
    private val resources: Resources = mainKeyboardService.baseContext.resources

    private val row1Letters = listOf("1", "2", "3", "(", ")")
    private val row2Letters = listOf("4", "5", "6", "+", "-")
    private val row3Letters = listOf("7", "8", "9", ".", /*delete*/)
    private val row4Letters = listOf("*", "0", "#", "⎵", /*enter*/)
    private val letterList = listOf(row1Letters, row2Letters, row3Letters, row4Letters)

    private val btnList = mutableListOf<List<Button>>()

    private val backspaceBtn = ImageButton(ContextThemeWrapper(mainKeyboardService, R.style.Theme_ControlBtn))
    val returnKeyBtn = ImageButton(ContextThemeWrapper(mainKeyboardService, R.style.Theme_ControlBtn))

    private val rowList = List(letterList.size) { LinearLayout(mainKeyboardView.context) }

    fun init() {
        for (i in letterList.indices) {
            // add buttons to btnList
            btnList.add(List(letterList[i].size) { Button(ContextThemeWrapper(mainKeyboardService, R.style.Theme_LetterBtn)) })
            // set linear layout attributes
            rowList[i].layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            for (j in letterList[i].indices) {
                btnList[i][j].text = letterList[i][j]
                btnList[i][j].layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                btnList[i][j].isAllCaps = false
                btnList[i][j].setPadding(0)
                btnList[i][j].setOnClickListener{
                    if (letterList[i][j] == "⎵") {
                        mainKeyboardService.currentInputConnection.commitText(" ", 1)
                        return@setOnClickListener
                    }
                    mainKeyboardService.currentInputConnection.commitText(letterList[i][j], 1)
                }
                // add buttons to linear layouts
                rowList[i].addView(btnList[i][j])
            }
        }

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
        rowList[rowList.size-2].addView(backspaceBtn, rowList[rowList.size-2].size)

        // return key
        returnKeyBtn.setOnClickListener {
            mainKeyboardService.vibrate()
            mainKeyboardService.currentInputConnection.performEditorAction(mainKeyboardService.currIMEOptions and EditorInfo.IME_MASK_ACTION)
        }
        rowList[rowList.size-1].addView(returnKeyBtn, rowList[rowList.size-1].size)
    }
}