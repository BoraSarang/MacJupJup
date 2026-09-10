package com.borasarang.macjupjup.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.resume

/**
 * 다국어 → 한글 번역 (T-100: 소스 언어 Auto).
 * 기존 영문 고정이던 소스를 ML Kit 언어 감지로 자동 판별(영·중·일·러 등)하고,
 * 1순위 ML Kit 온디바이스(무료·외부 전송 없음), 실패 시 gtx(sl=auto) 폴백.
 * 불가 시 null (fail-open). 이미 한글 위주 텍스트는 스킵.
 */
object MacTranslator {

    private val langIdClient by lazy { LanguageIdentification.getClient() }

    private val translatorCache = mutableMapOf<String, Translator>()
    private val readyModels = mutableSetOf<String>()

    /** BCP-47 감지 → ML Kit 번역 코드. 미지원·불명 시 영어 폴백 */
    suspend fun identifySource(text: String): String {
        val tag = try {
            withTimeoutOrNull(10_000) {
                suspendCancellableCoroutine { cont ->
                    langIdClient.identifyLanguage(text.take(1000))
                        .addOnSuccessListener { cont.resume(it) }
                        .addOnFailureListener { cont.resume("und") }
                }
            }
        } catch (_: Exception) {
            null
        } ?: "und"
        if (tag == "und") return TranslateLanguage.ENGLISH
        return try {
            TranslateLanguage.fromLanguageTag(tag) ?: TranslateLanguage.ENGLISH
        } catch (_: Exception) {
            TranslateLanguage.ENGLISH
        }
    }

    @Synchronized
    private fun translatorFor(source: String): Translator {
        return translatorCache.getOrPut(source) {
            Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(source)
                    .setTargetLanguage(TranslateLanguage.KOREAN)
                    .build(),
            )
        }
    }

    /** 모델 준비 (20초 상한). 미터드망 대기 무한 방지 */
    suspend fun ensureModel(source: String, timeoutMs: Long = 20_000): Boolean {
        if (readyModels.contains(source)) return true
        val ok = try {
            withTimeoutOrNull(timeoutMs) {
                suspendCancellableCoroutine { cont ->
                    translatorFor(source)
                        .downloadModelIfNeeded(DownloadConditions.Builder().build())
                        .addOnSuccessListener { cont.resume(true) }
                        .addOnFailureListener { cont.resume(false) }
                }
            } ?: false
        } catch (e: Exception) {
            DebugLogger.d("번역", "모델 준비 예외 ($source): ${e.message}")
            false
        }
        if (ok) readyModels.add(source)
        else DebugLogger.d("번역", "ML Kit 모델 미준비 ($source) — gtx 폴백 사용")
        return ok
    }

    /** 어떤 언어든 → 한글. 자동 감지 + ML Kit 우선, gtx(sl=auto) 폴백 */
    suspend fun translateAutoToKo(text: String): String? {
        val t = text.trim()
        if (t.isBlank() || !needsTranslation(t)) return null
        val source = identifySource(t)
        if (source == TranslateLanguage.KOREAN) return null
        DebugLogger.i("번역", "[FEATURE] 자동번역 감지언어=$source ${t.take(40)}")
        if (ensureModel(source)) {
            try {
                val out: String? = suspendCancellableCoroutine { cont ->
                    translatorFor(source).translate(t.take(2000))
                        .addOnSuccessListener { cont.resume(it) }
                        .addOnFailureListener { e ->
                            DebugLogger.d("번역", "ML Kit 실패 ($source), 폴백: ${e.message}")
                            cont.resume(null)
                        }
                }
                if (!out.isNullOrBlank()) return out
            } catch (e: Exception) {
                DebugLogger.d("번역", "ML Kit 예외 ($source), 폴백: ${e.message}")
            }
        }
        return translateGtx(t)
    }

    /** 기존 영→한 진입점 (호환 유지, 내부는 자동 감지) */
    suspend fun translateEnToKo(text: String): String? = translateAutoToKo(text)

    /** gtx 폴백 (sl=auto, 키 불필요, 실패·429 시 null) */
    internal suspend fun translateGtx(text: String): String? {
        return try {
            val q = java.net.URLEncoder.encode(text.take(1500), "UTF-8")
            val url = "https://translate.googleapis.com/translate_a/single" +
                "?client=gtx&sl=auto&tl=ko&dt=t&q=$q"
            val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            try {
                conn.requestMethod = "GET"
                conn.connectTimeout = 15_000
                conn.readTimeout = 15_000
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) MacJupJup/1.1")
                if (conn.responseCode != 200) {
                    DebugLogger.d("번역", "gtx HTTP ${conn.responseCode}")
                    return null
                }
                val body = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                parseGtx(body)
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            DebugLogger.d("번역", "gtx 예외: ${e.message}")
            null
        }
    }

    /** [[["번역문","원문",...]]] → 번역문 결합 */
    internal fun parseGtx(body: String): String? {
        return try {
            val root = kotlinx.serialization.json.Json.parseToJsonElement(body).jsonArray
            val sentences = root[0].jsonArray
            val out = sentences.mapNotNull { s ->
                s.jsonArray.getOrNull(0)?.jsonPrimitive?.content
            }.joinToString("")
            out.ifBlank { null }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 번역 필요 판정. 한글 비중 50% 초과면 이미 한국어 → 스킵.
     * 영·중·일·러 등 그 외는 번역 대상 (기존 isEnglish는 ASCII만 통과시켜 중·러·일을 버렸음).
     */
    fun needsTranslation(text: String): Boolean {
        if (text.isBlank()) return false
        val letters = text.count { it.isLetter() }
        if (letters == 0) return false
        val ko = text.count { it in '가'..'힣' }
        return ko.toDouble() / letters <= 0.5
    }

    /** 한글 없음 + ASCII 문자 포함이면 영문으로 간주 (레거시, 호환 유지) */
    fun isEnglish(text: String): Boolean {
        if (text.contains(Regex("[가-힣]"))) return false
        return text.contains(Regex("[A-Za-z]"))
    }

    fun close() {
        try {
            translatorCache.values.forEach { runCatching { it.close() } }
            runCatching { langIdClient.close() }
        } catch (_: Exception) {
        } finally {
            translatorCache.clear()
            readyModels.clear()
        }
    }
}
