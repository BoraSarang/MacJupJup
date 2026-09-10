package com.borasarang.macjupjup.util

/** 앱 중복 병합용 정규화 키 생성. PLAN 5장 규칙 */
object MergeUtils {
    /**
     * name + developer 정규화: 공백/특수문자 제거, 소문자화, 64자 절단.
     * 동일 키 다출처 수집 시 1개 App으로 병합.
     */
    fun generateId(name: String, developer: String): String {
        return "${normalizeName(developer)}-${normalizeName(name)}".trim('-').take(128)
    }

    /** 이름·개발사 정규화 단일 진실 (R1-11). 크롤러·서버에서 재사용 */
    fun normalizeName(s: String): String {
        return s.lowercase()
            .replace("[^a-z0-9가-힣]".toRegex(), "")
            .take(64)
    }
}
