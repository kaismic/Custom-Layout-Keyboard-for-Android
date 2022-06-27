package com.soobakjonmat.customlayoutkeyboard

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.soobakjonmat.customlayoutkeyboard.layout.*
import kotlin.math.absoluteValue

class CustomLayoutKeyboard : InputMethodService() {
    lateinit var mainKeyboardView: LinearLayout
    private lateinit var englishLayout: EnglishLayout
    private lateinit var koreanLayout: KoreanLayout
    private lateinit var specialKeyLayout: SpecialKeyLayout
    val rapidTextDeleteInterval: Long = 200 // in milliseconds
    val gestureMinDist = 120
    private val colorTheme = "dark"

    private val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    private val numBtnSubTexts = listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")")

    private val subTextRow1Letters = listOf("", "", "`", "\\", "|", "[", "]", "{", "}")
    private val subTextRow2Letters = listOf("", "", "", "", "_", "~", ":", ";", "\"", "'")
    private val subTextRow3Letters = listOf("×", "÷", "=", "+", "-", "*", "/")
    val subTextLetterList = listOf(subTextRow1Letters, subTextRow2Letters, subTextRow3Letters)

    val colorThemeMap = mutableMapOf(
        "bg" to 0,
        "mainText" to 0,
        "commonBtnBg" to 0,
        "subText" to 0,
    )

    private lateinit var numBtns: List<Button>
    private lateinit var specialKeyBtn: Button
    private lateinit var commaBtn: Button
    private lateinit var spacebarBtn: Button
    private lateinit var fullStopBtn: Button
    private lateinit var returnKeyBtn: Button

    var capsLockMode0Image: VectorDrawableCompat? = null
    var capsLockMode1Image: VectorDrawableCompat? = null
    var capsLockMode2Image: VectorDrawableCompat? = null

    private var mode = 1
    private var lastDownSpacebarX = 0f
    private var lastCursorPos = 0

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onCreateInputView(): View {
        // todo vibration
        // todo key touch sound
        mainKeyboardView = layoutInflater.inflate(R.layout.main_layout, null) as LinearLayout

        capsLockMode0Image = VectorDrawableCompat.create(resources, R.drawable.caps_lock_mode_0, null)
        capsLockMode1Image = VectorDrawableCompat.create(resources, R.drawable.caps_lock_mode_1, null)
        capsLockMode2Image = VectorDrawableCompat.create(resources, R.drawable.caps_lock_mode_2, null)

        initColorTheme(colorTheme)

        // number buttons
        val numberRow = mainKeyboardView.findViewById<LinearLayout>(R.id.number_row)
        numBtns = List(numbers.size) { Button(baseContext) }

        for (i in numbers.indices) {
            numBtns[i].layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
            numBtns[i].setPadding(0)

            val text = SpannableString(numBtnSubTexts[i] + "\n" + numbers[i])

            if (numBtnSubTexts[i] != "") {
                text.setSpan(ForegroundColorSpan(colorThemeMap.getValue("subText")), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            text.setSpan(RelativeSizeSpan(resources.getFloat(R.dimen.text_scale)), text.length-1, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            numBtns[i].text = text
            numBtns[i].setOnLongClickListener {
                resetAndFinishComposing()
                currentInputConnection.commitText(numBtnSubTexts[i], 1)
                return@setOnLongClickListener true
            }
            numBtns[i].setOnClickListener {
                resetAndFinishComposing()
                currentInputConnection.commitText(numbers[i], 1)
            }
            numberRow.addView(numBtns[i])
        }

        // special key
        specialKeyBtn = mainKeyboardView.findViewById(R.id.special_key)
        specialKeyBtn.setOnClickListener {
            if (mode != 0) {
                mode = 0
                changeLayout()
                specialKeyBtn.text = getString(R.string.special_key_text_english)
            } else {
                mode = 1
                changeLayout()
                specialKeyBtn.text = getString(R.string.special_key_text_special_key)
            }
        }
        // spacebar
        spacebarBtn = mainKeyboardView.findViewById(R.id.spacebar)
        spacebarBtn.setOnClickListener {

        }
        spacebarBtn.setOnTouchListener { _, motionEvent ->
            val action = motionEvent.action
            if (action == MotionEvent.ACTION_DOWN) {
                lastDownSpacebarX = motionEvent.rawX
            } else if (action == MotionEvent.ACTION_MOVE) {
                // todo show transition between layout on top. Use popup
            } else if (action == MotionEvent.ACTION_UP) {
                // on scroll keyboard
                if ((lastDownSpacebarX - motionEvent.rawX).absoluteValue > gestureMinDist) {
                    when (mode) {
                        0 -> {
                            specialKeyBtn.text = getString(R.string.special_key_text_special_key)
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
        // comma
        commaBtn = mainKeyboardView.findViewById(R.id.comma)
        commaBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getFloat(R.dimen.default_text_size))
        commaBtn.setOnClickListener {
            resetAndFinishComposing()
            currentInputConnection.commitText(",", 1)
        }
        // full stop
        fullStopBtn = mainKeyboardView.findViewById(R.id.full_stop)
        fullStopBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getFloat(R.dimen.default_text_size))
        fullStopBtn.setOnClickListener {
            resetAndFinishComposing()
            currentInputConnection.commitText(".", 1)
        }
        // return key
        returnKeyBtn = mainKeyboardView.findViewById(R.id.return_key)
        returnKeyBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getFloat(R.dimen.default_text_size))
        returnKeyBtn.setOnClickListener {
            resetAndFinishComposing()
            currentInputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND)
        }

        englishLayout = EnglishLayout(this)
        englishLayout.init()
        koreanLayout = KoreanLayout(this)
        koreanLayout.init()
        specialKeyLayout = SpecialKeyLayout(this)
        specialKeyLayout.init()
        // initially insert english layout on default
        englishLayout.insertLetterBtns()

        setColor()

        return mainKeyboardView
    }

    // todo set return key image according to EditorInfo
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        // call onUpdateCursorAnchorInfo() whenever cursor/anchor position is changed
        currentInputConnection.requestCursorUpdates(InputConnection.CURSOR_UPDATE_MONITOR)
    }

    override fun onUpdateCursorAnchorInfo(cursorAnchorInfo: CursorAnchorInfo) {
        val currPos = cursorAnchorInfo.selectionEnd
        if ((currPos != lastCursorPos + 1 || !koreanLayout.hangulAssembler.cursorMovedBySystem) && currPos != lastCursorPos) {
            resetAndFinishComposing()
        }
        lastCursorPos = currPos
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        resetAndFinishComposing()
    }

    fun deleteByWord(direction: Int): Boolean {
        var count = 2
        var textToDelete = ""
        when (direction) {
            -1 -> {
                if (currentInputConnection.getTextBeforeCursor(count, 0).isNullOrEmpty()) {
                    return false
                }
                while (currentInputConnection.getTextBeforeCursor(count, 0)?.first() != ' ' &&
                    currentInputConnection.getTextBeforeCursor(count, 0)?.length != textToDelete.length) {
                    textToDelete = currentInputConnection.getTextBeforeCursor(count, 0).toString()
                    count++
                }
                this.currentInputConnection.deleteSurroundingText(count, 0)
                return true
            }
            1 -> {

                if (currentInputConnection.getTextAfterCursor(count, 0).isNullOrEmpty()) {
                    return false
                }
                while (currentInputConnection.getTextAfterCursor(count, 0)?.last() != ' ' &&
                    currentInputConnection.getTextAfterCursor(count, 0)?.length != textToDelete.length) {
                    textToDelete = currentInputConnection.getTextAfterCursor(count, 0).toString()
                    count++
                }
                this.currentInputConnection.deleteSurroundingText(0, count)
                return true
            }
            else -> {
                throw Exception("Wrong deleteByWord parameter. Direction can be only either 1 or -1.")
            }
        }
    }

    private fun changeLayout() {
        resetAndFinishComposing()
        // delete middle rows
        for (i in 0 until mainKeyboardView.childCount-2) {
            mainKeyboardView.removeViewAt(1)
        }
        when (mode) {
            0 -> {
                specialKeyLayout.insertLetterBtns()
                spacebarBtn.text = getString(R.string.spacebar_text_special_key)
            }
            // english layout
            1 -> {
                englishLayout.insertLetterBtns()
                spacebarBtn.text = getString(R.string.spacebar_text_english)
            }
            // korean layout
            2 -> {
                koreanLayout.insertLetterBtns()
                spacebarBtn.text = getString(R.string.spacebar_text_korean)
            }
        }
    }

    fun resetAndFinishComposing() {
        koreanLayout.hangulAssembler.reset()
        currentInputConnection.finishComposingText()
    }

    private fun initColorTheme(colorTheme: String) {
        when (colorTheme) {
            "light" -> {
                this.
                colorThemeMap["bg"] = getColor(R.color.light_theme_bg)
                colorThemeMap["mainText"] =  getColor(R.color.black)
                colorThemeMap["commonBtnBg"] = getColor(R.color.white)
                colorThemeMap["subText"] = getColor(R.color.light_theme_subtext)
            }
            "dark" -> {
                colorThemeMap["bg"] = getColor(R.color.dark_theme_bg)
                colorThemeMap["mainText"] = getColor(R.color.white)
                colorThemeMap["commonBtnBg"] = getColor(R.color.dark_theme_common_btn_bg)
                colorThemeMap["subText"] = getColor(R.color.dark_theme_subtext)
            }
        }
    }

    // todo round edges, button onTouch onDown effect
    private fun setColor() {
        // background
        mainKeyboardView.setBackgroundColor(colorThemeMap.getValue("bg"))
        // number buttons
        for (btn in numBtns) {
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
        englishLayout.setColor()
        koreanLayout.setColor()
        specialKeyLayout.setColor()

        // caps lock image drawable
        capsLockMode0Image?.setTint(colorThemeMap.getValue("mainText"))
        capsLockMode1Image?.setTint(colorThemeMap.getValue("mainText"))
        capsLockMode2Image?.setTint(colorThemeMap.getValue("mainText"))
    }
}
