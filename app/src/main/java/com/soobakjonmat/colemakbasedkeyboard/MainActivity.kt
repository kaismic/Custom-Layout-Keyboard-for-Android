package com.soobakjonmat.colemakbasedkeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button

class ColemakBasedKeyboard : InputMethodService(), View.OnClickListener, View.OnLongClickListener {
    private var capsLockId : Int = 0
    private var backspaceId : Int = 0
    private var specialKeyId : Int = 0
    private var spacebarId : Int = 0
    private var returnKeyId : Int = 0

    private var letterBtns : ArrayList<Button> = ArrayList(26)

    override fun onCreateInputView(): View {
        val keyboardView: View = layoutInflater.inflate(R.layout.english_lowercase, null)

        // On Click
        for (i in 0 until 9) {
            keyboardView.findViewById<View>(resources.getIdentifier("key_$i", "id", this.packageName)).setOnClickListener(this)
        }
        var letter = 'A'
        var btn : Button
        while (letter <= 'Z') {
            btn = keyboardView.findViewById(resources.getIdentifier("key_$letter", "id", this.packageName))
            btn.setOnClickListener(this)
            letterBtns.add(btn)
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

        // On Long Click todo

        // On Swipe todo

        return keyboardView
    }

    override fun onClick(v: View) {
        //handle all the keyboard key clicks here
        val ic = this.currentInputConnection
        if (v is Button) {
            if (v.id == capsLockId) {
                if (letterBtns[0].text.toString() == "a") {
                    for (btn in letterBtns) {
                        btn.text = btn.text.toString().uppercase()
                    }
                } else {
                    for (btn in letterBtns) {
                        btn.text = btn.text.toString().lowercase()
                    }
                }
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

    override fun onLongClick(v: View?): Boolean {
        TODO("Not yet implemented")
    }

    // todo on key swipe
}

