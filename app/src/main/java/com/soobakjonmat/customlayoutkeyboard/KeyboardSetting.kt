package com.soobakjonmat.customlayoutkeyboard

import android.app.Activity
import android.os.Bundle

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
