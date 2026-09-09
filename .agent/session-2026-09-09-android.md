# session-2026-09-09-android — 맥줍줍 M0 스캐폴드

1. 무엇을: MacJupJup 신규 생성. 문서 우선(PLAN_v1.0·TODO·DESIGN·ENDPOINTS·PERMISSIONS·CHANGELOG·에러메시지·README·AGENTS.local.md) 후 M0 스캐폴드.
2. 플랫폼: Android 단일 (`com.borasarang.macjupjup`), JBR 빌드, S22 실기 설치 확인.
3. 빌드+PERF+CACHE: assembleDebug 24s 성공·APK 설치 Success / 단위 4/4 통과 / lint Error 0(Warning 47, M0 기본값) / 캐시 미적용(신규).
4. 남은 TODO: M1 DB+서버(T-010~014)부터. 상세 7섹션 스펙은 DESIGN·ENDPOINTS 반영済.
5. 전달로그: `[INFO] [FEATURE] 앱 시작·홈 플레이스홀더` 2건. ERROR 0.
6. 문서갱신: docs 7종 + README + CHANGELOG v0.1. TODO T-001~005 체크.
7. 큐상태: 다음 = M1 Room 6종 + Ktor 서버 + FGS + BootReceiver + Watchdog.
8. E2E: 실기 설치까지만. 포털·수집 E2E는 M4/M5에서.
