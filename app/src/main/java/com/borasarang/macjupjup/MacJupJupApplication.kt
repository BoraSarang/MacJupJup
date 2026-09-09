package com.borasarang.macjupjup

import android.app.Application
import com.borasarang.macjupjup.data.db.MacDatabase
import com.borasarang.macjupjup.data.preferences.PreferencesManager
import com.borasarang.macjupjup.data.repository.AppRepository
import com.borasarang.macjupjup.data.repository.NotificationRepository
import com.borasarang.macjupjup.data.repository.NotificationService
import com.borasarang.macjupjup.data.repository.SourceRepository
import com.borasarang.macjupjup.data.seed.InitialDataSeeder
import com.borasarang.macjupjup.server.HttpServerService
import com.borasarang.macjupjup.util.DebugLogger
import com.borasarang.macjupjup.worker.CrawlScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MacJupJupApplication : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: MacDatabase
        private set
    lateinit var appRepository: AppRepository
        private set
    lateinit var sourceRepository: SourceRepository
        private set
    lateinit var notificationService: NotificationService
        private set
    lateinit var notificationRepository: NotificationRepository
        private set
    lateinit var preferences: PreferencesManager
        private set
    lateinit var crawlScheduler: CrawlScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        DebugLogger.init()
        DebugLogger.i("앱", "맥줍줍 시작")

        database = try {
            MacDatabase.getInstance(this)
        } catch (e: Exception) {
            // 마이그레이션 실패 등 DB 열기 불가 → 재생성 폴백 (앱 벽돌 방지)
            DebugLogger.e("앱", "E-AND-DB-0403", "DB 열기 실패, 재생성: ${e.message}", e)
            MacDatabase.getInstanceFallback(this)
        }
        appRepository = AppRepository(database)
        sourceRepository = SourceRepository(database)
        preferences = PreferencesManager.getInstance(this)
        notificationService = NotificationService(database, preferences)
        notificationRepository = NotificationRepository(database)
        crawlScheduler = CrawlScheduler(this)

        appScope.launch(Dispatchers.IO) {
            InitialDataSeeder.seedIfEmpty(database)
            // v1.1 1회성 복구: 콤마 구분자로 깨진 CDN 스크린샷 정리 (다음 lookup/상세 수집에서 복원)
            try {
                val cleared = database.appDao().clearBrokenCdnScreenshots()
                if (cleared > 0) DebugLogger.i("복구", "깨진 스크린샷 정리 ${cleared}건")
            } catch (e: Exception) {
                DebugLogger.w("복구", "스크린샷 정리 스킵: ${e.message}")
            }
            crawlScheduler.scheduleAll(database)
            crawlScheduler.scheduleDailySummary()
            crawlScheduler.scheduleTranslate()
            val settings = preferences.getSettings()
            if (settings.autoStart) {
                DebugLogger.i("앱", "자동 시작 설정 켜짐 — 서버 시작")
                HttpServerService.start(this@MacJupJupApplication)
            } else {
                DebugLogger.i("앱", "자동 시작 꺼짐 — 서버 미시작")
            }
        }
    }

    companion object {
        lateinit var instance: MacJupJupApplication
            private set
    }
}
