package com.example.colemakbasedkeyboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ColemakBasedKeyboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.colemak_base_english)
    }
}

/*
class ColemakBasedKeyboard : InputMethodService(), View.OnClickListener {
    override fun onCreateInputView(): View {
        val keyboardView: View = layoutInflater.inflate(R.layout.key_layout, null)

        val btnA: Button = myKeyboardView.findViewById(R.id.btnA)
        btnA.setOnClickListener(this)
        val btnB: Button = myKeyboardView.findViewById(R.id.btnB)
        btnB.setOnClickListener(this) // ... etc.
        //ADD ALL THE OTHER LISTENERS HERE FOR ALL THE KEYS

        return myKeyboardView
    }

    override fun onClick(v: View) {
        //handle all the keyboard key clicks here
        val ic = currentInputConnection
        if (v is Button) {
            val clickedKeyText: String = (v as Button).getText().toString()
            ic.commitText(clickedKeyText, 1)
        }
    }
}
 */