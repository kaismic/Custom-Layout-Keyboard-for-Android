package com.soobakjonmat.customlayoutkeyboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference


class KeyboardSetting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mySettingsFragment = MySettingsFragment()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SEND)
        registerReceiver(mySettingsFragment.MyReceiver(), filter)

        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra("Is Custom Layout Keyboard Created?", true)
        baseContext?.sendBroadcast(intent)

        setContentView(R.layout.settings_container)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, mySettingsFragment)
            .commit()
    }

    class MySettingsFragment : PreferenceFragmentCompat() {
        private lateinit var longClickDeleteSpeedPreference: SeekBarPreference
        private lateinit var keyboardHeightPreference: SeekBarPreference
        private val settingsKeyList = listOf("settings_long_click_delete_speed", "settings_keyboard_height")

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings, rootKey)
            longClickDeleteSpeedPreference = preferenceScreen.findPreference("long_click_delete_speed")!!

            keyboardHeightPreference = preferenceScreen.findPreference("keyboard_height")!!
            keyboardHeightPreference.max = (resources.displayMetrics.heightPixels*0.6).toInt()
            keyboardHeightPreference.value = (resources.displayMetrics.heightPixels*0.4).toInt()
            keyboardHeightPreference.min = (resources.displayMetrics.heightPixels*0.3).toInt()

            longClickDeleteSpeedPreference.setOnPreferenceChangeListener { _, newValue ->
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra("key", settingsKeyList[0])
                intent.putExtra("value", (newValue as Int).toLong())
                context?.sendBroadcast(intent)
                return@setOnPreferenceChangeListener true
            }
            keyboardHeightPreference.setOnPreferenceChangeListener { _, newValue ->
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra("key", settingsKeyList[1])
                intent.putExtra("value", newValue as Int)
                context?.sendBroadcast(intent)
                return@setOnPreferenceChangeListener true
            }
        }

        inner class MyReceiver : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (p1?.hasExtra("Custom Layout Keyboard Created") == true) {
                    longClickDeleteSpeedPreference.callChangeListener(longClickDeleteSpeedPreference.value)
                    keyboardHeightPreference.callChangeListener(keyboardHeightPreference.value)
                    this@MySettingsFragment.context?.unregisterReceiver(this)
                }
            }
        }
    }
}

// if possible and i have a lot of time: key layout change
