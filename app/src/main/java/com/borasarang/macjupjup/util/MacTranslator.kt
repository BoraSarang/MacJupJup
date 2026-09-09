package com.borasarang.macjupjup.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.resume

/**
 * 영→한 번역.
 * 1순위 ML Kit 온디바이스(무료·외부 전송 없음), 모델 없으면 gtx 단일 요청 폴백.
 * 불가 시 null (fail-open). 한글 포함 텍스트는 스킵.
 */
object MacTranslator {

    @Volatile
    private var ready: Boolean = false

    private val options by lazy {
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.KOREAN)
            .build()
    }

    private val client by lazy { Translation.getClient(options) }

    /** 모델 준비 (20초 상한). 미터드망 대기 무한 방지 */
    suspend fun ensureModel(timeoutMs: Long = 20_000): Boolean {
        if (ready) return true
        val ok = try {
            withTimeoutOrNull(timeoutMs) {
                suspendCancellableCoroutine { cont ->
                    client.downloadModelIfNeeded(DownloadConditions.Builder().build())
                        .addOnSuccessListener { cont.resume(true) }
                        .addOnFailureListener { cont.resume(false) }
                }
            } ?: false
        } catch (e: Exception) {
            DebugLogger.d("번역", "모델 준비 예외: ${e.message}")
            false
        }
        ready = ok
        if (!ok) DebugLogger.d("번역", "ML Kit 모델 미준비 — gtx 폴백 사용")
        return ok
    }

    /** 영문 텍스트 → 한글. ML Kit 우선, 없으면 gtx 폴백 */
    suspend fun translateEnToKo(text: String): String? {
        val t = text.trim()
        if (t.isBlank() || !isEnglish(t)) return null
        if (ensureModel()) {
            try {
                val out: String? = suspendCancellableCoroutine { cont ->
                    client.translate(t.take(2000))
                        .addOnSuccessListener { cont.resume(it) }
                        .addOnFailureListener { e ->
                            DebugLogger.d("번역", "ML Kit 실패, 폴백: ${e.message}")
                            cont.resume(null)
                        }
                }
                if (!out.isNullOrBlank()) return out
            } catch (e: Exception) {
                DebugLogger.d("번역", "ML Kit 예외, 폴백: ${e.message}")
            }
        }
        return translateGtx(t)
    }

    /** gtx 단일 요청 폴백 (키 불필요, 실패·429 시 null) */
    internal suspend fun translateGtx(text: String): String? {
        return try {
            val q = java.net.URLEncoder.encode(text.take(1500), "UTF-8")
            val url = "https://translate.googleapis.com/translate_a/single" +
                "?client=gtx&sl=en&tl=ko&dt=t&q=$q"
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

    /** 한글 없음 + ASCII 문자 포함이면 영문으로 간주 */
    fun isEnglish(text: String): Boolean {
        if (text.contains(Regex("[가-힣]"))) return false
        return text.contains(Regex("[A-Za-z]"))
    }

    fun close() {
        try {
            client.close()
        } catch (_: Exception) {
        } finally {
            ready = false
        }
    }
}
