package com.soobakjonmat.customlayoutkeyboard

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.VibrationEffect
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.TypedValue
import android.view.ContextThemeWrapper

import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.soobakjonmat.customlayoutkeyboard.layout.*
import kotlin.math.absoluteValue

class MainKeyboardService : InputMethodService() {
    lateinit var mainKeyboardView: LinearLayout
    private lateinit var englishLayout: EnglishLayout
    private lateinit var koreanLayout: KoreanLayout
    private lateinit var specialKeyLayout: SpecialKeyLayout
    val rapidTextDeleteInterval: Long = 200 // in milliseconds
    val gestureMinDist = 120

    private val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    private val numBtnSubTexts = listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")")
    private val combinedNums = mutableListOf<SpannableString>()

    private val subTextRow1Letters = listOf("", "", "`", "\\", "|", "[", "]", "{", "}")
    private val subTextRow2Letters = listOf("", "", "", "", "_", "~", ":", ";", "\"", "'")
    private val subTextRow3Letters = listOf("×", "÷", "=", "+", "-", "*", "/")
    val subTextLetterList = listOf(subTextRow1Letters, subTextRow2Letters, subTextRow3Letters)
    var subtextColor = 0

    private lateinit var numBtns: List<Button>
    private lateinit var specialKeyBtn: Button
    private lateinit var commaBtn: Button
    private lateinit var spacebarBtn: Button
    private lateinit var fullStopBtn: Button
    private lateinit var returnKeyBtn: ImageButton

    var capsLockMode0Image: VectorDrawableCompat? = null
    var capsLockMode1Image: VectorDrawableCompat? = null
    var capsLockMode2Image: VectorDrawableCompat? = null
    var backspaceImage: VectorDrawableCompat? = null
    private var returnKeyImageSearch: VectorDrawableCompat? = null
    private var returnKeyImageDone: VectorDrawableCompat? = null
    private var returnKeyImageForward: VectorDrawableCompat? = null
    private var returnKeyImageReturn: VectorDrawableCompat? = null
    private var returnKeyImageTab: VectorDrawableCompat? = null

    private val searchIconActionList = listOf(
        EditorInfo.IME_ACTION_SEARCH,
        EditorInfo.IME_ACTION_GO,
    )
    private val doneIconActionList = listOf(
        EditorInfo.IME_ACTION_DONE
    )
    private val returnIconActionList = listOf(
        EditorInfo.IME_ACTION_SEND
    )
    private val tabIconActionList = listOf(
        EditorInfo.IME_ACTION_NEXT
    )

    private var mode = 1
    private var lastDownSpacebarX = 0f
    private var lastCursorPos = 0

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onCreateInputView(): View {
        mainKeyboardView = layoutInflater.inflate(R.layout.main_keyboardview, null) as LinearLayout

        capsLockMode0Image = VectorDrawableCompat.create(resources, R.drawable.caps_lock_mode_0, null)
        capsLockMode1Image = VectorDrawableCompat.create(resources, R.drawable.caps_lock_mode_1, null)
        capsLockMode2Image = VectorDrawableCompat.create(resources, R.drawable.caps_lock_mode_2, null)
        backspaceImage = VectorDrawableCompat.create(resources, R.drawable.ic_outline_backspace_24, null)
        returnKeyImageSearch = VectorDrawableCompat.create(resources, R.drawable.ic_outline_search_24, null)
        returnKeyImageDone = VectorDrawableCompat.create(resources, R.drawable.ic_outline_done_24, null)
        returnKeyImageReturn = VectorDrawableCompat.create(resources, R.drawable.ic_outline_keyboard_return_24, null)
        returnKeyImageTab = VectorDrawableCompat.create(resources, R.drawable.ic_outline_keyboard_tab_24, null)
        returnKeyImageForward = VectorDrawableCompat.create(resources, R.drawable.ic_outline_arrow_forward_24, null)

        // number buttons
        val numberRow = mainKeyboardView.findViewById<LinearLayout>(R.id.number_row)

        numBtns = List(numbers.size) { Button(ContextThemeWrapper(this, R.style.Theme_LetterBtn)) }
        subtextColor = if (isDarkTheme()) {
            getColor(R.color.dark_theme_subtext)
        } else {
            getColor(R.color.light_theme_subtext)
        }

        for (i in numbers.indices) {
            numBtns[i].layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
            numBtns[i].setPadding(0)

            combinedNums.add(SpannableString(numBtnSubTexts[i] + "\n" + numbers[i]))

            if (numBtnSubTexts[i] != "") {
                combinedNums[i].setSpan(ForegroundColorSpan(subtextColor), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            combinedNums[i].setSpan(RelativeSizeSpan(resources.getFloat(R.dimen.text_scale)), combinedNums[i].length-1, combinedNums[i].length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            numBtns[i].text = combinedNums[i]
            numBtns[i].setOnLongClickListener {
                vibrate()
                resetAndFinishComposing()
                currentInputConnection.commitText(numBtnSubTexts[i], 1)
                return@setOnLongClickListener true
            }
            numBtns[i].setOnClickListener {
                vibrate()
                resetAndFinishComposing()
                currentInputConnection.commitText(numbers[i], 1)
            }
            numberRow.addView(numBtns[i])
        }

        // special key
        specialKeyBtn = mainKeyboardView.findViewById(R.id.special_key)
        specialKeyBtn.setOnClickListener {
            vibrate()
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
        spacebarBtn.setOnTouchListener { _, motionEvent ->
            val action = motionEvent.action
            if (action == MotionEvent.ACTION_DOWN) {
                vibrate()
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
            vibrate()
            resetAndFinishComposing()
            currentInputConnection.commitText(",", 1)
        }
        // full stop
        fullStopBtn = mainKeyboardView.findViewById(R.id.full_stop)
        fullStopBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getFloat(R.dimen.default_text_size))
        fullStopBtn.setOnClickListener {
            vibrate()
            resetAndFinishComposing()
            currentInputConnection.commitText(".", 1)
        }
        // return key
        returnKeyBtn = mainKeyboardView.findViewById(R.id.return_key)
        returnKeyBtn.setOnClickListener {
            vibrate()
            resetAndFinishComposing()
            currentInputConnection.performEditorAction(currentInputEditorInfo.actionId)
        }

        englishLayout = EnglishLayout(this)
        englishLayout.init()
        koreanLayout = KoreanLayout(this)
        koreanLayout.init()
        specialKeyLayout = SpecialKeyLayout(this)
        specialKeyLayout.init()
        // initially insert english layout on default
        englishLayout.insertLetterBtns()

        return mainKeyboardView
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        // change return key button image
        when (editorInfo?.actionId) {
            in searchIconActionList -> {
                returnKeyBtn.setImageDrawable(returnKeyImageSearch)
            }
            in doneIconActionList -> {
                returnKeyBtn.setImageDrawable(returnKeyImageDone)
            }
            in returnIconActionList -> {
                returnKeyBtn.setImageDrawable(returnKeyImageReturn)
            }
            in tabIconActionList -> {
                returnKeyBtn.setImageDrawable(returnKeyImageTab)
            }
            else -> {
                returnKeyBtn.setImageDrawable(returnKeyImageForward)
            }
        }
        // call onUpdateCursorAnchorInfo() whenever cursor/anchor position is changed
        currentInputConnection.requestCursorUpdates(InputConnection.CURSOR_UPDATE_MONITOR)
        // set subtext color according to whether dark mode is turned on or not
        updateSubtextColor()
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

    private fun isDarkTheme(): Boolean {
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            return true
        }
        return false
    }

    private fun updateSubtextColor() {
        subtextColor = if (isDarkTheme()) {
            if (subtextColor == getColor(R.color.dark_theme_subtext)) {
                return
            } else {
                getColor(R.color.dark_theme_subtext)
            }
        } else {
            if (subtextColor == getColor(R.color.dark_theme_subtext)) {
                getColor(R.color.light_theme_subtext)
            } else {
                return
            }
        }
        for (i in numbers.indices) {
            if (numBtnSubTexts[i] != "") {
                combinedNums[i].setSpan(
                    ForegroundColorSpan(subtextColor),
                    0,
                    1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            numBtns[i].text = combinedNums[i]
        }
        englishLayout.updateSubtextColor()
        koreanLayout.updateSubtextColor()
    }

    fun deleteByWord(direction: Int): Boolean {
        resetAndFinishComposing()
        vibrate()
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

    fun vibrate() {
        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
    }
}
