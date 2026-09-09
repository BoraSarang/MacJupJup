package com.borasarang.macjupjup.util

/** 앱 중복 병합용 정규화 키 생성. PLAN 5장 규칙 */
object MergeUtils {
    /**
     * name + developer 정규화: 공백/특수문자 제거, 소문자화, 64자 절단.
     * 동일 키 다출처 수집 시 1개 App으로 병합.
     */
    fun generateId(name: String, developer: String): String {
        val norm = { s: String ->
            s.lowercase()
                .replace("[^a-z0-9가-힣]".toRegex(), "")
                .take(64)
        }
        return "${norm(developer)}-${norm(name)}".trim('-').take(128)
    }
}
