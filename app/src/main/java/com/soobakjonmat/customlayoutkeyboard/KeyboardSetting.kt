package com.soobakjonmat.customlayoutkeyboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat

class KeyboardSetting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mySettingsFragment = MySettingsFragment()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SEND)
        registerReceiver(mySettingsFragment.MyReceiver(), filter)

        setContentView(R.layout.settings_container)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, mySettingsFragment)
            .commit()
    }

    class MySettingsFragment : PreferenceFragmentCompat() {
        private lateinit var vibrationSwitchPreference: SwitchPreferenceCompat
        private lateinit var longClickDeleteSpeedPreference: SeekBarPreference
        private lateinit var keyboardHeightPreference: SeekBarPreference
        private val settingsKeyList = listOf("settings_vibration", "settings_long_click_delete_speed", "settings_keyboard_height")

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings, rootKey)
            vibrationSwitchPreference = preferenceScreen.findPreference("vibration")!!
            longClickDeleteSpeedPreference = preferenceScreen.findPreference("long_click_delete_speed")!!
            keyboardHeightPreference = preferenceScreen.findPreference("keyboard_height")!!

            keyboardHeightPreference.max = (resources.displayMetrics.heightPixels*0.6).toInt()
            keyboardHeightPreference.value = (resources.displayMetrics.heightPixels*0.3).toInt()
            keyboardHeightPreference.min = (resources.displayMetrics.heightPixels*0.3).toInt()

            vibrationSwitchPreference.setOnPreferenceChangeListener { _, newValue ->
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra("key", settingsKeyList[0])
                intent.putExtra("value", newValue as Boolean)
                context?.sendBroadcast(intent)
                return@setOnPreferenceChangeListener true
            }
            longClickDeleteSpeedPreference.setOnPreferenceChangeListener { _, newValue ->
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra("key", settingsKeyList[1])
                intent.putExtra("value", (newValue as Int).toLong())
                context?.sendBroadcast(intent)
                return@setOnPreferenceChangeListener true
            }
            keyboardHeightPreference.setOnPreferenceChangeListener { _, newValue ->
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra("key", settingsKeyList[2])
                intent.putExtra("value", newValue as Int)
                context?.sendBroadcast(intent)
                return@setOnPreferenceChangeListener true
            }
        }

        inner class MyReceiver : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (p1?.hasExtra("Custom Layout Keyboard Created") == true) {
                    vibrationSwitchPreference.callChangeListener(vibrationSwitchPreference.isChecked)
                    longClickDeleteSpeedPreference.callChangeListener(longClickDeleteSpeedPreference.value)
                    keyboardHeightPreference.callChangeListener(keyboardHeightPreference.value)
                }
            }
        }
    }
}

/* todo settings:

if possible and have a lot of time: key layout change

 */