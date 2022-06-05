package com.soobakjonmat.colemakbasedkeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import com.soobakjonmat.colemakbasedkeyboard.keyboard_language_layouts.*

class ColemakBasedKeyboard : InputMethodService() {
    private lateinit var mainKeyboardView: LinearLayout
    private lateinit var englishLayout: EnglishLayout
    private var mode = 1
    /*
    init row 1 (numbers row) and row 5 (control row) by adding onClickListener and other listener

    when changing language
    swap row 2 ~ 4

    currentLayoutModeNum = 1
    keyboardModeDict =
    { 0: "special key layout"
      1: "english layout"
      2: "korean layout"
          }
    show keyboardModeDict[currentLayoutModeNum] to spacebar text
    if (space bar scrolled )
        measure scroll length
        intScLen =  int(scroll length)
        currentLayoutModeNum = (intScLen + currentLayoutModeNum) % someModularConstant
     */
    override fun onCreate() {
        super.onCreate()
        mainKeyboardView = layoutInflater.inflate(R.layout.main_layout, null) as LinearLayout
    }

    override fun onCreateInputView(): View {
        englishLayout = EnglishLayout(this, mainKeyboardView)
        englishLayout.init()

        // initially insert english layout on default
        englishLayout.insertRows()

        // on click number buttons
        for (i in 0 until 9) {
            mainKeyboardView.findViewById<Button>(resources.getIdentifier("key_$i", "id", this.packageName)).setOnClickListener {
                currentInputConnection.commitText(i.toString(), 1)

            }
        }
        // on click control key row
        // special key
        mainKeyboardView.findViewById<Button>(R.id.special_key).setOnClickListener {
            // todo change layout to special key layout
        }
        // comma
        mainKeyboardView.findViewById<Button>(R.id.comma).setOnClickListener {
            currentInputConnection.commitText(",", 1)
        }
        // spacebar
        mainKeyboardView.findViewById<Button>(R.id.spacebar).setOnClickListener {
            currentInputConnection.commitText(" ", 1)
        }
        // full stop
        mainKeyboardView.findViewById<Button>(R.id.full_stop).setOnClickListener {
            currentInputConnection.commitText(".", 1)
        }

        // return key
        mainKeyboardView.findViewById<Button>(R.id.return_key).setOnClickListener {
            currentInputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND)
        }
        return mainKeyboardView
    }
}

