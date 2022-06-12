package com.soobakjonmat.colemakbasedkeyboard

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.Normalizer

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    private fun printIt(string: String) {
        println(string)
        for (i in string.indices) {
            print(string.codePointAt(i).toChar() + " " + String.format("U+%04X ", string.codePointAt(i)) + " " + string.codePointAt(i).toChar().code)
            println()
            print(Normalizer.normalize(string.codePointAt(i).toChar().toString(), Normalizer.Form.NFC)+" ")
            print(Normalizer.normalize(string.codePointAt(i).toChar().toString(), Normalizer.Form.NFKC)+" ")
            print(Normalizer.normalize(string.codePointAt(i).toChar().toString(), Normalizer.Form.NFD)+" ")
            print(Normalizer.normalize(string.codePointAt(i).toChar().toString(), Normalizer.Form.NFKD)+" ")

            println()
        }
        println()
    }

    @Test
    fun useNormalizer() {
        val letter = "뵑"
//        val nfd = Normalizer.normalize(letter, Normalizer.Form.NFD)
//        printIt(nfd)

        printIt(Normalizer.normalize(letter, Normalizer.Form.NFC))
        printIt(Normalizer.normalize(letter, Normalizer.Form.NFKC))
        printIt(Normalizer.normalize(letter, Normalizer.Form.NFD))
        printIt(Normalizer.normalize(letter, Normalizer.Form.NFKD))


//        println('ㅂ'.code)
//        println('ㅚ'.code)
//        println('ㄺ'.code)

    }
}