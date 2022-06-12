package com.soobakjonmat.colemakbasedkeyboard

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.soobakjonmat.colemakbasedkeyboard.keyboard_language_layouts.*
import kotlin.math.absoluteValue

// todo vibration
class ColemakBasedKeyboard : InputMethodService() {
    lateinit var mainKeyboardView: LinearLayout
    private lateinit var englishLayout: EnglishLayout
    private lateinit var koreanLayout: KoreanLayout
    val rapidTextDeleteInterval: Long = 200 // in milliseconds
    val spacebarMinSlideDist = 120
    private val initColorTheme = "Dark"

    val colorThemeMap = mutableMapOf(
        "bg" to 0,
        "mainText" to 0,
        "commonBtnBg" to 0,
        "subText" to 0,
    )
    private var numberBtns = mutableListOf<Button>()
    private lateinit var specialKeyBtn: Button
    private lateinit var commaBtn: Button
    private lateinit var spacebarBtn: Button
    private lateinit var fullStopBtn: Button
    private lateinit var returnKeyBtn: Button

    private var mode = 1
    private var lastDownX = 0f
    /*
    init row 1 (numbers row) and row 5 (control row) by adding onClickListener and other listener

    when changing language
    swap row 2 ~ 4

    mode = 1
    keyboardModeDict =
    { 0: "special key layout"
      1: "english layout"
      2: "korean layout"
          }
    show keyboardModeDict[mode] to spacebar text
    if (space bar scrolled )
        measure scroll length
        intScLen =  int(scroll length)
        mode = (intScLen + mode) % someModularConstant
     */

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onCreateInputView(): View {
        mainKeyboardView = layoutInflater.inflate(R.layout.main_layout, null) as LinearLayout
        englishLayout = EnglishLayout(this)
        englishLayout.init()
        koreanLayout = KoreanLayout(this)
        koreanLayout.init()

        // initially insert english layout on default
        englishLayout.insertLetterBtnsOnKeyboard()

        // on click number buttons
        val numberLayout = mainKeyboardView.findViewById<LinearLayout>(R.id.number_row)
        for (i in 0 until numberLayout.childCount) {
            numberBtns.add(numberLayout.getChildAt(i) as Button)
            numberBtns[i].setOnClickListener {
                currentInputConnection.commitText(numberBtns[i].text.toString(), 1)
            }
        }

        // special key
        specialKeyBtn = mainKeyboardView.findViewById(R.id.special_key)
        specialKeyBtn.setOnClickListener {
            // todo change layout to special key layout
        }
        // comma
        commaBtn = mainKeyboardView.findViewById(R.id.comma)
        commaBtn.setOnClickListener {
            currentInputConnection.commitText(",", 1)
        }
        // spacebar
        // todo on scroll change language mode
        spacebarBtn = mainKeyboardView.findViewById(R.id.spacebar)
        spacebarBtn.setOnClickListener {

        }
        spacebarBtn.setOnTouchListener { _, motionEvent ->
            val action = motionEvent.action
            if (action == MotionEvent.ACTION_DOWN) {
                lastDownX = motionEvent.rawX
            } else if (action == MotionEvent.ACTION_MOVE) {
                // todo show transition between layout on top
            } else if (action == MotionEvent.ACTION_UP) {
                // on scroll keyboard
                if ((lastDownX - motionEvent.rawX).absoluteValue > spacebarMinSlideDist) {
                    when (mode) {
                        0 -> {
                            mode = 1
                        }
                        1 -> {
                            mode = 2
                        }
                        2 -> {
                            mode = 1
                        }
                    }
                    changeLayout()
                }
                // on click
                else {
                    currentInputConnection.commitText(" ", 1)
                }
            }
            return@setOnTouchListener true
        }

        // full stop
        fullStopBtn = mainKeyboardView.findViewById(R.id.full_stop)
        fullStopBtn.setOnClickListener {
            currentInputConnection.commitText(".", 1)
        }
        // return key
        returnKeyBtn = mainKeyboardView.findViewById(R.id.return_key)
        returnKeyBtn.setOnClickListener {
            currentInputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND)
        }

        setColorTheme(initColorTheme)
        return mainKeyboardView
    }

    fun deleteWholeWord() {
        var count = 2
        var textBefore = ""
        if (this.currentInputConnection.getTextBeforeCursor(count, 0).isNullOrEmpty()) {
            return
        }
        while (this.currentInputConnection.getTextBeforeCursor(count, 0)?.get(0) != ' ' &&
            this.currentInputConnection.getTextBeforeCursor(count, 0)?.length != textBefore.length) {
            textBefore = this.currentInputConnection.getTextBeforeCursor(count, 0).toString()
            count++
        }
        this.currentInputConnection.deleteSurroundingText(count, 0)
    }

    private fun changeLayout() {
        this.currentInputConnection.finishComposingText()
        // delete middle rows
        for (i in 0 until 3) {
            mainKeyboardView.removeViewAt(1)
        }
        when (mode) {
            // special keys layout
            0 -> {
                // todo
            }
            // english layout
            1 -> {
                englishLayout.insertLetterBtnsOnKeyboard()
            }
            // korean layout
            2 -> {
                koreanLayout.insertLetterBtnsOnKeyboard()
            }
        }
    }

    private fun setColorTheme(theme: String) { // todo set subtext colors
        when (theme) {
            "Light" -> {
                colorThemeMap["bg"] = ContextCompat.getColor(baseContext, R.color.light_theme_bg)
                colorThemeMap["mainText"] =  ContextCompat.getColor(baseContext, R.color.black)
                colorThemeMap["commonBtnBg"] = ContextCompat.getColor(baseContext, R.color.white)
                colorThemeMap["subText"] = ContextCompat.getColor(baseContext, R.color.light_theme_subtext)
            }
            "Dark" -> {
                colorThemeMap["bg"] = ContextCompat.getColor(baseContext, R.color.dark_theme_bg)
                colorThemeMap["mainText"] = ContextCompat.getColor(baseContext, R.color.white)
                colorThemeMap["commonBtnBg"] = ContextCompat.getColor(baseContext, R.color.dark_theme_common_btn_bg)
                colorThemeMap["subText"] = ContextCompat.getColor(baseContext, R.color.dark_theme_subtext)
            }
        }

        // background
        mainKeyboardView.setBackgroundColor(colorThemeMap.getValue("bg"))
        // number buttons
        for (btn in numberBtns) {
            btn.setTextColor(colorThemeMap.getValue("mainText"))
            btn.setBackgroundColor(colorThemeMap.getValue("commonBtnBg"))
        }
        // control row
        specialKeyBtn.setTextColor(colorThemeMap.getValue("mainText"))
        specialKeyBtn.setBackgroundColor(colorThemeMap.getValue("bg"))
        commaBtn.setTextColor(colorThemeMap.getValue("mainText"))
        commaBtn.setBackgroundColor(colorThemeMap.getValue("bg"))
        spacebarBtn.setTextColor(colorThemeMap.getValue("mainText"))
        spacebarBtn.setBackgroundColor(colorThemeMap.getValue("commonBtnBg"))
        fullStopBtn.setTextColor(colorThemeMap.getValue("mainText"))
        fullStopBtn.setBackgroundColor(colorThemeMap.getValue("bg"))
        returnKeyBtn.setTextColor(colorThemeMap.getValue("mainText"))
        returnKeyBtn.setBackgroundColor(colorThemeMap.getValue("bg"))

        // language layouts
        englishLayout.setColorTheme()
        koreanLayout.setColorTheme()

        // caps lock image drawable
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.caps_lock_mode_0,
            null)?.setTint(colorThemeMap.getValue("mainText")
        )
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.caps_lock_mode_1,
            null)?.setTint(colorThemeMap.getValue("mainText")
        )
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.caps_lock_mode_2,
            null)?.setTint(colorThemeMap.getValue("mainText")
        )
    }
}
