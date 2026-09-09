package com.borasarang.macjupjup.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslatorTest {

    @Test
    fun `gtx응답_결합`() {
        val body = """[[["안녕하세요","Hello",null,null,1]],null,"en"]"""
        assertEquals("안녕하세요", MacTranslator.parseGtx(body))
    }

    @Test
    fun `gtx응답_불량_null`() {
        assertNull(MacTranslator.parseGtx("not json"))
        assertNull(MacTranslator.parseGtx("[]"))
    }

    @Test
    fun `영문판정_한글포함제외`() {
        assertTrue(MacTranslator.isEnglish("Hello world"))
        assertFalse(MacTranslator.isEnglish("안녕하세요"))
        assertFalse(MacTranslator.isEnglish("123"))
    }
}
