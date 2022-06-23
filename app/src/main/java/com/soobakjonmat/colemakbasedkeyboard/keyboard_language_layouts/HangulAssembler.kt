package com.soobakjonmat.colemakbasedkeyboard.keyboard_language_layouts

import android.util.Log
import com.soobakjonmat.colemakbasedkeyboard.ColemakBasedKeyboard

class HangulAssembler(private val mainActivity: ColemakBasedKeyboard) {
    private var initConsFirst = 0.toChar()
    private var initConsSecond = 0.toChar()
    private var initConsIdx = -1
    private var medVowelFirst = 0.toChar()
    private var medVowelSecond = 0.toChar()
    private var medVowelIdx = -1
    private var finConsFirst = 0.toChar()
    private var finConsSecond = 0.toChar()
    private var finConsIdx = 0

    private val initConsList = listOf('ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ')
    private val medVowelList = listOf('ㅏ','ㅐ','ㅑ','ㅒ','ㅓ','ㅔ','ㅕ','ㅖ','ㅗ','ㅘ','ㅙ','ㅚ','ㅛ','ㅜ','ㅝ','ㅞ','ㅟ','ㅠ','ㅡ','ㅢ','ㅣ')
    private val finConsList = listOf(0.toChar(), 'ㄱ','ㄲ','ㄳ','ㄴ','ㄵ','ㄶ','ㄷ','ㄹ','ㄺ','ㄻ','ㄼ','ㄽ','ㄾ','ㄿ','ㅀ','ㅁ','ㅂ','ㅄ','ㅅ','ㅆ','ㅇ','ㅈ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ')

    private val combinableVowelMap = mapOf( // key idx on main list + list index + 1
        'ㅗ' to listOf('ㅏ','ㅐ','ㅣ'),
        'ㅜ' to listOf('ㅓ','ㅔ','ㅣ'),
        'ㅡ' to listOf('ㅣ')
    )
    private val combinableConsMap = mapOf(
//        'ㄱ' to listOf('ㅅ'), // idx of ㄳ at finConsList is 3
        'ㄴ' to listOf('ㅈ','ㅎ'), //  key idx on main list + list index + 1
        'ㄹ' to listOf('ㄱ','ㅁ','ㅂ','ㅅ','ㅌ','ㅍ','ㅎ'), //  key idx on main list + list index + 1
        'ㅂ' to listOf('ㅅ') //  key idx on main list + list index + 1
    )

    var cursorMovedBySystem = false

    fun commitText(letterStr: String) {
        Log.i("---", "commitText is called")
        val letterChar = letterStr.toCharArray()[0]
        cursorMovedBySystem = false
        // if 곯, 돣, 쵧, 핎, 즲, 잽, 춘, 봘, 욕, 칻, 웧, 오, 수, 브, 돼, 케, 뮈, ㄱ, ㅅ, ㅎ, ㅈ, ㅌ, ㅇ
        if (initConsFirst != 0.toChar()) {
            // if 곯, 돣, 쵧, 핎, 즲, 잽, 춘, 봘, 욕, 칻, 웧, 오, 수, 브, 돼, 케, 뮈
            if (medVowelFirst != 0.toChar()) {
                // if 곯, 돣, 쵧, 핎, 즲, 잽, 춘, 봘, 욕, 칻, 웧
                if (finConsFirst != 0.toChar()) {
                    // if 곯, 돣, 쵧, 핎, 즲
                    if (finConsSecond != 0.toChar()) {
                        // if char is a vowel
                        if (medVowelList.contains(letterChar)) {
                            val nextInitConsFirst = finConsSecond
                            finConsSecond = 0.toChar()
                            finConsIdx = finConsList.indexOf(finConsFirst)
                            mainActivity.currentInputConnection.commitText(getAssembledLetter(), 1)
                            finishAndCommitText(nextInitConsFirst, letterChar)
                        }
                        // if char is a consonant
                        else {
                            finishAndCommitText(letterChar)
                        }
                    }
                    // if 잽, 춘, 봘, 욕, 칻, 웧
                    else {
                        // if char is a vowel
                        if (medVowelList.contains(letterChar)) {
                            cursorMovedBySystem = true
                            val nextInitConsFirst = finConsFirst
                            finConsFirst = 0.toChar()
                            finConsIdx = 0
                            mainActivity.currentInputConnection.commitText(getAssembledLetter(), 1)
                            finishAndCommitText(nextInitConsFirst, letterChar)
                        }
                        // if char is a consonant
                        else {
                            // if final consonant first is combinable: 잽, 춘, 봘, 욕
                            if (combinableConsMap.containsKey(finConsFirst)) {
                                // if char is combinable based on final consonant first: 잽 + ㅅ, 춘 + ㅎ, 봘 + ㄱ, 욕 + ㅅ
                                if (combinableConsMap.getValue(finConsFirst).contains(letterChar)) {
                                    finConsSecond = letterChar
                                    // if final consonant first is ㄱ
                                    if (finConsFirst == 'ㄱ') {
                                        finConsIdx = 3
                                    }
                                    // if final consonant first is ㄴ, ㄹ, ㅂ
                                    else {
                                        finConsIdx += 1+combinableConsMap.getValue(finConsFirst).indexOf(letterChar)
                                    }
                                    mainActivity.currentInputConnection.setComposingText(getAssembledLetter(), 1)
                                }
                                // if char is not combinable based on final consonant first: 잽 + ㅌ, 춘 + ㅇ, 봘 + ㅋ, 욕 + ㄹ
                                else {
                                    finishAndCommitText(letterChar)
                                }
                            }
                            // if final consonant first is not combinable: 칻, 웧
                            else {
                                finishAndCommitText(letterChar)
                            }
                        }
                    }
                }
                // if 오, 수, 브, 돼, 케, 뮈
                else {
                    // if char is a vowel
                    if (medVowelList.contains(letterChar)) {
                        // if med vowel first is combinable: 오, 수, 브
                        if (combinableVowelMap.containsKey(medVowelFirst)) {
                            // if char is combinable based on med vowel first: 오 + ㅐ, 수 + ㅓ, 브 + ㅣ
                            if (combinableVowelMap.getValue(medVowelFirst).contains(letterChar)) {
                                medVowelSecond = letterChar
                                Log.i("index to plus", combinableVowelMap.getValue(medVowelFirst).indexOf(letterChar).toString())
                                medVowelIdx += 1+combinableVowelMap.getValue(medVowelFirst).indexOf(letterChar)
                                mainActivity.currentInputConnection.setComposingText(getAssembledLetter(), 1)
                            }
                            // if char is not combinable based on med vowel first: 오 + ㅖ, 수 + ㅏ, 브 + ㅠ
                            else {
                                finishAndCommitText(letterChar)
                            }
                        }
                        // if med vowel first is not combinable: 돼, 케, 뮈
                        else {
                            finishAndCommitText(letterChar)
                        }
                    }
                    // if char is a consonant
                    else {
                        finConsFirst = letterChar
                        finConsIdx = finConsList.indexOf(letterChar)
                        mainActivity.currentInputConnection.setComposingText(getAssembledLetter(), 1)
                    }
                }
            }
            // if 'ㄱ','ㄲ','ㄳ','ㄴ','ㄵ','ㄶ','ㄷ','ㄹ','ㄺ','ㄻ','ㄼ','ㄽ','ㄾ','ㄿ','ㅀ','ㅁ','ㅂ','ㅄ','ㅅ','ㅆ','ㅇ','ㅈ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
            else {
                // if char is a consonant
                if (initConsList.contains(letterChar)) {
                    // if consonant is combinable and not already combined: ㄱ, ㄴ, ㄹ, ㅂ
                    if (initConsSecond == 0.toChar() && combinableConsMap.containsKey(initConsFirst)) {
                        // if char is combinable based on init consonant first: ㄱ + ㅅ, ㄴ + ㅈ, ㄹ + ㅌ, ㅂ + ㅅ
                        if (combinableConsMap.getValue(initConsFirst).contains(letterChar)) {
                            initConsSecond = letterChar
                            val outputText: String
                            if (initConsFirst == 'ㄱ') {
                                outputText = "ㄳ"
                            } else {
                                outputText = finConsList[finConsList.indexOf(initConsFirst) + 1+combinableConsMap.getValue(initConsFirst).indexOf(initConsSecond)].toString()
                            }
                            mainActivity.currentInputConnection.setComposingText(outputText, 1)
                        }
                        // if char is not combinable based on init consonant first: ㄱ + ㅁ, ㄴ + ㅍ, ㄹ + ㅇ, ㅂ + ㅂ
                        else {
                            finishAndCommitText(letterChar)
                        }
                    }
                    // if consonant is not combinable: all consonants other than ㄱ, ㄴ, ㄹ, ㅂ
                    else {
                        finishAndCommitText(letterChar)
                    }
                }
                // if char is a vowel
                else {
                    // if 'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
                    if (initConsSecond == 0.toChar()) {
                        medVowelFirst = letterChar
                        medVowelIdx = medVowelList.indexOf(letterChar)
                        mainActivity.currentInputConnection.setComposingText(getAssembledLetter(), 1)
                    }
                    // if 'ㄳ','ㄵ','ㄶ','ㄺ','ㄻ','ㄼ','ㄽ','ㄾ','ㄿ','ㅀ','ㅄ'
                    else {
                        mainActivity.currentInputConnection.setComposingText(initConsFirst.toString(), 1)
                        finishAndCommitText(initConsSecond, letterChar)
                    }
                }
            }
        }
        // if  ㅏ, ㅑ, ㅛ, ㅗ, ㅜ, ㅡ, ㅚ, ㅞ, nothing
        else {
            // if nothing
            if (medVowelFirst == 0.toChar()) {
                finishAndCommitText(letterChar)
            }
            // if ㅏ, ㅑ, ㅛ, ㅗ, ㅜ, ㅡ, ㅚ, ㅞ
            else {
                // if ㅏ, ㅑ, ㅛ, ㅗ, ㅜ, ㅡ
                if (medVowelSecond == 0.toChar()) {
                    // if med vowel first is combinable: ㅗ, ㅜ, ㅡ
                    if (combinableVowelMap.containsKey(medVowelFirst)) {
                        // if char is combinable based on med vowel first
                        if (combinableVowelMap.getValue(medVowelFirst).contains(letterChar)) {
                            medVowelSecond = letterChar
                            medVowelIdx += 1+combinableVowelMap.getValue(medVowelFirst).indexOf(letterChar)
                            mainActivity.currentInputConnection.setComposingText(medVowelList[medVowelIdx].toString(), 1)
                        }
                        // if char is not combinable based on med vowel first
                        else {
                            finishAndCommitText(letterChar)
                        }
                    }
                    // if med vowel first is not combinable: ㅏ, ㅑ, ㅛ
                    else {
                        finishAndCommitText(letterChar)
                    }
                }
                // if ㅙ, ㅞ, ㅢ
                else {
                    finishAndCommitText(letterChar)
                }
            }
        }
    }

    fun deleteText() {
        // if 곯, 돣, 쵧, 핎, 즲, 잽, 춘, 봘, 욕, 칻, 웧, 오, 수, 브, 돼, 케, 뮈, ㄱ, ㅅ, ㅎ, ㅈ, ㅌ, ㅇ
        if (initConsIdx != -1) {
            // if 곯, 돣, 쵧, 핎, 즲, 잽, 춘, 봘, 욕, 칻, 웧, 오, 수, 브, 돼, 케, 뮈
            if (medVowelIdx != -1) {
                // if 곯, 돣, 쵧, 핎, 즲, 잽, 춘, 봘, 욕, 칻, 웧
                if (finConsFirst != 0.toChar()) {
                    // 곯, 돣, 쵧, 핎, 즲
                    if (finConsSecond != 0.toChar()) {
                        finConsSecond = 0.toChar()
                        finConsIdx = finConsList.indexOf(finConsFirst)
                        mainActivity.currentInputConnection.setComposingText(getAssembledLetter(), 1)
                    }
                    // 잽, 춘, 봘, 욕, 칻, 웧
                    else {
                        finConsFirst = 0.toChar()
                        finConsIdx = 0
                        mainActivity.currentInputConnection.setComposingText(getAssembledLetter(), 1)
                    }
                }
                // if 오, 수, 브, 돼, 케, 뮈
                else {
                    // if med vowel second is null: 오, 수, 브, 케,
                    if (medVowelSecond == 0.toChar()) {
                        medVowelFirst = 0.toChar()
                        medVowelIdx = -1
                        mainActivity.currentInputConnection.setComposingText(initConsFirst.toString(), 1)
                    }
                    // if med vowel second is not null: 돼, 뮈
                    else {
                        medVowelSecond = 0.toChar()
                        medVowelIdx = medVowelList.indexOf(medVowelFirst)
                        mainActivity.currentInputConnection.setComposingText(getAssembledLetter(), 1)
                    }
                }
            }
            // if 'ㄱ','ㄲ','ㄳ','ㄴ','ㄵ','ㄶ','ㄷ','ㄹ','ㄺ','ㄻ','ㄼ','ㄽ','ㄾ','ㄿ','ㅀ','ㅁ','ㅂ','ㅄ','ㅅ','ㅆ','ㅇ','ㅈ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
            else {
                // if init consonant is not combined
                if (initConsSecond == 0.toChar()) {
                    reset()
                    mainActivity.currentInputConnection.setComposingText("", 1)
                }
                // if init consonant is combined
                else {
                    initConsSecond = 0.toChar()
                    mainActivity.currentInputConnection.setComposingText(initConsFirst.toString(), 1)
                }
            }
        }
        // if ㅏ, ㅑ, ㅛ, ㅗ, ㅜ, ㅡ, ㅚ, ㅞ, nothing
        else {
            // if nothing
            if (medVowelFirst == 0.toChar()) {
                mainActivity.currentInputConnection.deleteSurroundingText(1, 0)
            }
            // if ㅏ, ㅑ, ㅛ, ㅗ, ㅜ, ㅡ, ㅚ, ㅞ
            else {
                // if ㅏ, ㅑ, ㅛ, ㅗ, ㅜ, ㅡ
                if (medVowelSecond == 0.toChar()) {
                    reset()
                    mainActivity.currentInputConnection.setComposingText("", 1)
                }
                // if ㅙ, ㅞ, ㅢ
                else {
                    medVowelIdx = medVowelList.indexOf(medVowelFirst)
                    medVowelSecond = 0.toChar()
                    mainActivity.currentInputConnection.setComposingText(medVowelFirst.toString(), 1)
                }
            }
        }
    }

    private fun getAssembledLetter(): String {
        return (initConsIdx*588 + medVowelIdx*28 + finConsIdx + 44032).toChar().toString()
    }

    private fun finishAndCommitText(letter: Char) {
        Log.i("---", "finishAndCommitText is called")
        cursorMovedBySystem = true
        mainActivity.currentInputConnection.finishComposingText()
        reset()
        if (initConsList.contains(letter)) {
            initConsFirst = letter
            initConsIdx = initConsList.indexOf(letter)
        } else {
            medVowelFirst = letter
            medVowelIdx = medVowelList.indexOf(letter)
        }
        mainActivity.currentInputConnection.setComposingText(letter.toString(), 1)
    }

    private fun finishAndCommitText(nextInitConsFirst: Char, nextMedVowelFirst: Char) {
        cursorMovedBySystem = true
        mainActivity.currentInputConnection.finishComposingText()
        reset()
        initConsFirst = nextInitConsFirst
        initConsIdx = initConsList.indexOf(initConsFirst)
        medVowelFirst = nextMedVowelFirst
        medVowelIdx = medVowelList.indexOf(nextMedVowelFirst)
        mainActivity.currentInputConnection.setComposingText(getAssembledLetter(), 1)
    }

    fun reset() {
        initConsFirst = 0.toChar()
        initConsSecond = 0.toChar()
        initConsIdx = -1
        medVowelFirst = 0.toChar()
        medVowelSecond = 0.toChar()
        medVowelIdx = -1
        finConsFirst = 0.toChar()
        finConsSecond = 0.toChar()
        finConsIdx = 0
    }
}