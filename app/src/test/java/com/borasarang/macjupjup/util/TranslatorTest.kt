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

    @Test
    fun `번역필요_중일러한글판정_T100`() {
        assertTrue(MacTranslator.needsTranslation("Hello world"))
        assertTrue(MacTranslator.needsTranslation("这是一款菜单栏应用"))
        assertTrue(MacTranslator.needsTranslation("メニューバーアプリです"))
        assertTrue(MacTranslator.needsTranslation("Это приложение для macOS"))
        assertFalse(MacTranslator.needsTranslation("안녕하세요 메뉴바 앱입니다"))
        assertFalse(MacTranslator.needsTranslation("123"))
        assertFalse(MacTranslator.needsTranslation("   "))
    }
}
