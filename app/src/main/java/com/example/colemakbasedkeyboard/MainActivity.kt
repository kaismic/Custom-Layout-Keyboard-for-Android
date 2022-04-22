package com.example.colemakbasedkeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button

//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle

//class ColemakBasedKeyboard : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.colemak_base_english)
//    }
//}

class ColemakBasedKeyboard : InputMethodService(), View.OnClickListener {
    override fun onCreateInputView(): View {
        val keyboardView: View = layoutInflater.inflate(R.layout.colemak_base_english, null)

        for (i in 0 until 9) {
            val id = resources.getIdentifier("key_$i", "id", this.packageName)
            keyboardView.findViewById<View>(id).setOnClickListener(this)
        }

        var letter = 'A'
        while (letter <= 'Z') {
            val id = resources.getIdentifier("key_$letter", "id", this.packageName)
            keyboardView.findViewById<View>(id).setOnClickListener(this)
            letter++
        }

        return keyboardView
    }

    override fun onClick(v: View) {
        //handle all the keyboard key clicks here
        val ic = this.currentInputConnection
        if (v is Button) {
            val clickedKeyText: String = v.text.toString()
            ic.commitText(clickedKeyText, 1)
        }
    }
}
