package com.soobakjonmat.customlayoutkeyboard

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.preference.PreferenceFragmentCompat

class KeyboardSetting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_container)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, MySettingsFragment())
            .commit()

        title = getString(R.string.app_name) + " Setting"
    }
    // todo solve the problem of text not showing when in light color theme

    class MySettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings, rootKey)


        }
    }
}

/* todo settings:
long click rapid input interval
dark/light mode
vibration
if possible and have a lot of time: key layout change

 */