# CHANGELOG — MacJupJup

> 형식: `## [vX.Y.Z] - YYYY-MM-DD` · platform 태그 + error_code + perf/cache 영향 기록

## [v1.1] - 2026-09-09

- 정보 보강: DB v2 11컬럼 + lookup 전체상세 + GitHub 신호 + README + Setapp 상세
- 저장 병합 규칙 (재수집이 보강분 지우지 않음)
- 이름 대조 매처(엄격+단일후보) + macmenubar.com RSS 9번째 수집처
- 스크린샷 구분자 수정 + 깨진 CDN 1회성 복구
- ML Kit + gtx 폴백 번역 (한국어 기본·원문 토글·설정 on/off)
- 포털: 아이콘 카드·한글 설명·풍부한 특징·알림 목록
- 목록 대표 출처 포함 + 카드 출처 뱃지(새탭) + 버전 없음 폴백(포착일·빈 히스토리 문구)
- 단위 41/41 · lint Error 0 · S22 E2E (730개·한글·아이콘 렌더 확인)

## [v1.0] - 2026-09-09

- M5 네이티브 4탭: 홈·소스관리(주기·즉시실행)·설정(토큰 마스킹·배터리예외·로그)·알림 센터
- 알림 생성 5종 + 서버 알림 API 7종 + 일일 요약 워커(09:00)
- 실기 E2E: 알림 자동 생성(CRAWL_COMPLETE·NEW_APPS_FOUND) 확인
- 단위 23/23 · Espresso 2/2 (SM-S901N) · lint Error 0
- v1.0 DoD 전부 충족

## [v0.5] - 2026-09-09

- M4 포털 실물: 타임라인·칩 16개·검색·정렬·페이지네이션·상세 7섹션 모달·Watchlist·통계
- /api/stats/trends 집계 7종 (카테고리·라이선스·7일 신규/업데이트/버전bump·AI/MenuBar)
- 브라우저 렌더 E2E: 657개 카드·상세 스크린샷·통계·Watchlist 확인
- 단위 23/23 · lint Error 0 (Warning 43)

## [v0.4] - 2026-09-09

- M3 시드+버전추적: Setapp(SSR·Mac 필터·PAID 기본) + 차트 RSS + lookup 폴링
- Apple 유료/매출 차트 빈 배열 확인 → 무료 차트만 수집, 유료는 Setapp으로 커버
- CategoryInfer 단어 경계(3자 이하) + Setapp 태그라인 정제 + /api/sources/:id/toggle
- 실기 E2E: 7종 SUCCESS·657개·PAID 391·lookup 보완(버전+스크린샷 50/50)
- 단위 23/23 · lint Error 0 (Warning 43)

## [v0.3] - 2026-09-09

- M2 발굴 크롤러 4종: GitHub Search/Releases + PH 피드 + Show HN
- CategoryInfer(10종·AI-Agent/MenuBar 태그) + MergeUtils + 토큰 마스킹
- CrawlWorker(FGS dataSync) + CrawlScheduler(개별주기·즉시실행·20초 분산) + /api/sync 연동
- 실기 E2E: hn/ph/github 수집 SUCCESS → 196개 앱, OSS 필터·태그·버전 흐름 확인
- M3 예정 소스는 FAILED 격리(차트·lookup·setapp STAGED) — 전체 중단 없음
- 단위 18/18 · lint Error 0 (Warning 43)

## [v0.2] - 2026-09-09

- M1 DB+서버: Room 6종(apps/app_sources/sources/version_history/crawl_logs/notification_logs) + DAO 6종 + MacDatabase
- AppRepository(병합·버전 히스토리 기록·정리) + SourceRepository + Preferences(githubToken 키, 마스킹)
- InitialDataSeeder 소스 7개 (setapp_seed만 STAGED 비활성)
- Ktor 서버 8종 라우트 + placeholder 포털 + FGS(dataSync) + BootReceiver + Watchdog
- 실기 E2E: health ok·stats(0/6)·watchlist 7종·settings 검증·토큰 마스킹·portal 200
- 단위 7/7 · lint Error 0 (Warning 44)

## [v0.1] - 2026-09-09

- M0 스캐폴드: Gradle(AGP 9.3.1·Kotlin 2.0)·Manifest(권한 9종)·Application+Timber·홈 플레이스홀더
- 문서 우선: PLAN_v1.0_macjupjup·TODO·DESIGN·ENDPOINTS·PERMISSIONS·error_message_ko.json
- 카테고리 10종 + 라이선스 3분류 유틸(AppCategory) + 단위 테스트
- build_and_run.sh + CI/Release 워크플로우
