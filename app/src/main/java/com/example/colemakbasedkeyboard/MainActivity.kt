package com.example.colemakbasedkeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button

class ColemakBasedKeyboard : InputMethodService(), View.OnClickListener {
    private var capsLockId : Int = 0
    private var backspaceId : Int = 0
    private var specialKeyId : Int = 0
    private var spacebarId : Int = 0
    private var returnKeyId : Int = 0

    override fun onCreateInputView(): View {
        val keyboardView: View = layoutInflater.inflate(R.layout.colemak_base_english, null)

        for (i in 0 until 9) {
            keyboardView.findViewById<View>(resources.getIdentifier("key_$i", "id", this.packageName)).setOnClickListener(this)
        }
        var letter = 'A'
        while (letter <= 'Z') {
            keyboardView.findViewById<View>(resources.getIdentifier("key_$letter", "id", this.packageName)).setOnClickListener(this)
            letter++
        }
        keyboardView.findViewById<View>(resources.getIdentifier("comma", "id", this.packageName)).setOnClickListener(this)
        keyboardView.findViewById<View>(resources.getIdentifier("full_stop", "id", this.packageName)).setOnClickListener(this)

        capsLockId = resources.getIdentifier("caps_lock", "id", this.packageName)
        keyboardView.findViewById<View>(capsLockId).setOnClickListener(this)
        backspaceId = resources.getIdentifier("backspace", "id", this.packageName)
        keyboardView.findViewById<View>(backspaceId).setOnClickListener(this)
        specialKeyId = resources.getIdentifier("special_key", "id", this.packageName)
        keyboardView.findViewById<View>(specialKeyId).setOnClickListener(this)
        spacebarId = resources.getIdentifier("spacebar", "id", this.packageName)
        keyboardView.findViewById<View>(spacebarId).setOnClickListener(this)
        returnKeyId = resources.getIdentifier("return_key", "id", this.packageName)
        keyboardView.findViewById<View>(returnKeyId).setOnClickListener(this)

        return keyboardView
    }

    override fun onClick(v: View) {
        //handle all the keyboard key clicks here
        val ic = this.currentInputConnection
        if (v is Button) {
            if (v.id == capsLockId) {
                //todo change to new layout or change key texts
                return
            }
            if (v.id == backspaceId) {
                ic.deleteSurroundingText(1, 0)
                return
            }
            if (v.id == specialKeyId) {
                //todo change to new layout
                return
            }
            if (v.id == spacebarId) {
                ic.commitText(" ", 1)
                return
            }
            if (v.id == returnKeyId) {
                ic.performEditorAction(EditorInfo.IME_ACTION_SEND)
                return
            }
            val text: String = v.text.toString()
            ic.commitText(text, 1)
        }
    }

    // todo on key hold and swipe
}

