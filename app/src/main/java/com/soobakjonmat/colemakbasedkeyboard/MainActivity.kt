package com.soobakjonmat.colemakbasedkeyboard

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.soobakjonmat.colemakbasedkeyboard.keyboard_language_layouts.*
import kotlin.math.absoluteValue

class ColemakBasedKeyboard : InputMethodService() {
    lateinit var mainKeyboardView: LinearLayout
    private lateinit var englishLayout: EnglishLayout
    private lateinit var koreanLayout: KoreanLayout
    val rapidTextDeleteInterval: Long = 200 // in milliseconds
    val gestureMinDist = 120
    private val colorTheme = "dark"

    val colorThemeMap = mutableMapOf(
        "bg" to 0,
        "mainText" to 0,
        "commonBtnBg" to 0,
        "subText" to 0,
    )
    private val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    private val numBtnSubTexts = listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")")

    private val subTextRow1Letters = listOf("", "", "", "", "", "_", "~", "{", "}")
    private val subTextRow2Letters = listOf("", "", "", "", "\"", "'", ":", ";", "[", "]")
    private val subTextRow3Letters = listOf("", "", "=", "+", "-", "*", "/")
    val subTextLetterList = listOf(subTextRow1Letters, subTextRow2Letters, subTextRow3Letters)

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
     */

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onCreateInputView(): View {
        // todo vibration
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
            text.setSpan(RelativeSizeSpan(1.2f), text.length-1, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
            resetAndFinishComposing()
            // todo change layout to special key layout
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
                resetAndFinishComposing()
                // on scroll keyboard
                if ((lastDownSpacebarX - motionEvent.rawX).absoluteValue > gestureMinDist) {
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
        // comma
        commaBtn = mainKeyboardView.findViewById(R.id.comma)
        commaBtn.setOnClickListener {
            resetAndFinishComposing()
            currentInputConnection.commitText(",", 1)
        }
        // full stop
        fullStopBtn = mainKeyboardView.findViewById(R.id.full_stop)
        fullStopBtn.setOnClickListener {
            resetAndFinishComposing()
            currentInputConnection.commitText(".", 1)
        }
        // return key
        returnKeyBtn = mainKeyboardView.findViewById(R.id.return_key)
        returnKeyBtn.setOnClickListener {
            resetAndFinishComposing()
            currentInputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND)
        }

        englishLayout = EnglishLayout(this)
        englishLayout.init()
        koreanLayout = KoreanLayout(this)
        koreanLayout.init()
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
        // delete middle rows
        for (i in 0 until 3) {
            mainKeyboardView.removeViewAt(1)
        }
        when (mode) {
            // todo special keys layout
            0 -> {

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

        // caps lock image drawable
        capsLockMode0Image?.setTint(colorThemeMap.getValue("mainText"))
        capsLockMode1Image?.setTint(colorThemeMap.getValue("mainText"))
        capsLockMode2Image?.setTint(colorThemeMap.getValue("mainText"))
    }
}
