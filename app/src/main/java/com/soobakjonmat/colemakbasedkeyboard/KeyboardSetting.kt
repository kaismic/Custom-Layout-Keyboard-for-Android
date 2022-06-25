package com.soobakjonmat.colemakbasedkeyboard

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView

class KeyboardSetting : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)
    }
    /* todo settings:
    long click rapid input interval
    dark/light mode
    vibration
    if possible and have a lot of time: key layout change

     */
}
