# PLAN_v1.0_macjupjup — 맥줍줍(MacJupJup) 개발 계획서

> 플랫폼: Android 네이티브(Kotlin) 단일 앱 · 타겟: Galaxy S22(One UI 8.0 / Android 16)
> 패키지: `com.borasarang.macjupjup` · 런처명: 맥줍줍 · 정식명: 맥 앱 트렌드 레이더 맥줍줍
> 작성일: 2026-09-09 · 상태: 승인됨(사용자 확정 반영)
> 형제 프로젝트: PlanJupJup(알뜰요금줍줍) 골격 복제 — 데이터 이관 없음

## 1. 한 줄 설명

갤럭시 S22를 **맥 앱 트렌드 수집 서버**로 삼는 단일 Android 앱.
macOS 앱(오픈소스/프리/유료)의 신규 출시·버전업·새 기능을 주기 수집해,
같은 네트워크의 맥 브라우저에서 **웹 포털**(타임라인·카테고리·라이선스 필터·버전 diff)로 보여준다.
앱이 크롤러 + DB + 웹서버 역할을 모두 수행한다.

## 2. 확정 사항 (사용자 결정, 2026-09-09)

| # | 항목 | 결정 |
|---|---|---|
| 1 | 추적 대상 | macOS 앱만. 네이티브(Swift/ObjC) + Electron·Tauri 포함. iOS/Android/Win 제외 |
| 2 | 구분 | 1.오픈소스 2.프리 3.유료 3분류 고정 |
| 3 | 스토어 기준 | US (`country=us`) |
| 4 | 카테고리 | Setapp 10개로 시작 (아래 3장). AI Agent·메뉴바는 태그로 횡단 집계 |
| 5 | Watchlist | 3층: Setapp 베이스라인(~200, 월간) + 차트 핫나우(~200 중복제거, 24h) + 발굴층(유동, 6h) |
| 6 | GitHub 토큰 | 있음(`gh` 로그인 확인). 앱 설정 입력→DataStore 저장, 커밋 금지 |
| 7 | 크랙·활성화툴 | 수집 제외. macked.app은 크롤링하지 않고 카테고리 스타일 참고만 |
| 8 | UI 프레임워크 | View + ViewBinding + Material3 (네이티브는 상태·설정·디버그 최소, 주 화면은 웹 포털) |
| 9 | 포트·보관 | 기본 3000(변경 가능), 보관기간 30일/90일(기본 30일) |
| 10 | 생존성 | 배터리 최적화 예외 요청 + START_STICKY + BootReceiver + Watchdog |

## 3. 아키텍처 (PlanJupJup 패턴 재사용)

```
┌────────────── Android App (Kotlin, ViewBinding) ──────────────┐
│ [Foreground Service: HttpServerService, type=dataSync]         │
│   ├─ Ktor CIO 임베디드 서버 (0.0.0.0:3000)                     │
│   │    ├─ GET  /                  → 포털 HTML (assets/web)     │
│   │    ├─ GET  /api/apps          → 목록 (platform=macOS 고정/라이선스/카테고리/정렬/페이징) │
│   │    ├─ GET  /api/apps/:id      → 상세 + 버전 히스토리 + 출처 │
│   │    ├─ GET  /api/watchlist     → 시드 3층 상태              │
│   │    ├─ POST /api/sync          → 즉시 수집                  │
│   │    ├─ GET  /api/stats/*       → 트렌드 대시보드            │
│   │    └─ GET/POST /api/settings  → 설정 조회/저장 (토큰 포함) │
│   ├─ Watchdog 코루틴 (헬스체크+자동 재시작)                    │
│   └─ WorkManager (발굴 6h / 차트 24h / 베이스라인 월간 / 버전폴링 24h) │
│ [Room DB] apps / app_sources / sources / crawl_logs /          │
│           notifications / version_history                      │
│ [UI] Home(상태·주소·통계·수동수집) / SourceManage / Settings     │
│      (토큰 입력·포트·보관기간·배터리예외·자동시작·Watchdog주기)  │
│ [Receiver] BootReceiver (부팅 시 autoStart면 서버 자동 시작)    │
└──────────────────────────────────────────────────────────────┘
```

## 4. 기술 스택 (고정 — PlanJupJup과 동일)

Kotlin 2.0 / View+ViewBinding+Material3 / Ktor 3.x CIO / Room 2.7.x /
WorkManager 2.9.x / Jsoup 1.22.x / 바닐라 포털 / DataStore /
Timber+DebugLogger / JUnit4+MockK, Espresso — 버전은 `gradle/libs.versions.toml` 단일 진실.

## 5. 데이터 모델 (Room)

- `apps`: id(정규화키 PK), platform('macOS' 고정), name, developer,
  license(OSS/FREE/PAID), price, currency(USD),
  category(10종), tags(CSV, `AI-Agent`·`MenuBar` 등),
  trackId?(스토어), repoFullName?(GitHub), homepageUrl?,
  version?, releaseNotesSummary?, stars?, primaryLanguage?, topicsJson?,
  firstSeenAt, lastUpdatedAt, isNew
- 인덱스: (name,developer) unique, license, category, trackId, repoFullName, lastUpdatedAt, isNew
- `version_history`: (appId, version) 복합PK, detectedAt, notesSummary?, source
- `app_sources`: (appId, sourceName) 복합PK, sourceUrl, fetchedAt
- `sources`: id PK, name, type(GITHUB_SEARCH/GITHUB_RELEASES/PH_FEED/HN_SHOW/SETAPP_SEED/CHART_RSS/ITUNES_LOOKUP),
  baseUrl, enabled, intervalHours, lastRunAt?, lastStatus, errorMessage?
- `crawl_logs` / `notifications`: PlanJupJup 스키마 재사용

### 중복 병합 규칙

- 키: `name + developer` 정규화(공백/특수문자 제거, 소문자화, 64자 절단)
- 스토어 trackId 또는 repo가 같으면 동일 앱으로 병합, 출처는 `app_sources` 전수 기록
- 라이선스 충돌 시: repo 존재 → OSS 우선, price>0 → PAID, 그 외 FREE. 수동 오버라이드 필드 별도

## 6. 크롤러 설계

- `BaseCrawler` 재사용: 설정 파싱 → 목록 수집 → 항목 파싱 → 트랜잭션 저장, 요청 간 1초+ 예의 대기, UA 명시, 실패 격리(`crawl_logs` + `Result.retry()`)
- GitHub: `GET /search/repositories?q=topic:macos+created:>date+stars:>20&sort=stars&per_page=100`
  (인증 시 분당 30회·시간당 5000회, 결과 상한 1000 → 7일 윈도우 분할).
  릴리즈: `GET /repos/{o}/{r}/releases?per_page=20`, ETag/`If-None-Match` 절약
- PH: `/feed` + `topics/mac/feed`·`topics/developer-tools/feed`, `mac|macos|menu bar` 키워드 필터, 링크만 저장
- HN: Show HN Algolia + `hnrss.org/show`, points+comments traction 점수
- Setapp: 공개 카탈로그 월간 시드(상세는 lookup 보완, 과도 재수집 금지)
- 차트: `topfreemacapps/toppAidmacapps/topgrossingmacapps` (US, limit=100, 키 불필요), 24h 진입·이탈 감지
- 버전 폴링: 전 시드 `lookup?id={200개 묶음}&entity=macSoftware` 일 1회
- 릴리즈노트 전문 복제 금지 — 요약 + 원문 링크만 저장

## 7. 생존성 설계 (PlanJupJup 패턴)

Manifest 9종 권한 + `HttpServerService`(START_STICKY, 최상단 startForeground,
FGS 시작 거부 catch) + `BootReceiver` + Watchdog(기본 60초) + 배터리예외 UI.
Android 15 dataSync 24h 누적 6h cap 유의 — 수집 Worker는 수분 내 종료 단기 실행,
상시 FGS는 서빙용으로만 유지.

## 8. API 명세 요약 (상세: docs/api/ENDPOINTS.md)

| Method | Path | 설명 |
|---|---|---|
| GET | `/` | 포털 HTML |
| GET | `/api/health` | 헬스체크 |
| GET | `/api/apps` | 목록 (license/category/tag/sort/page) |
| GET | `/api/apps/:id` | 상세 + 버전 히스토리 + 출처 |
| GET | `/api/watchlist` | 시드 3층 상태 |
| POST | `/api/sync` | 즉시 수집 (`{sourceId?}`) |
| GET | `/api/stats/*` | 트렌드 대시보드 |
| GET/POST | `/api/settings` | 설정 조회/저장 (githubToken 포함, GET 응답에서 마스킹) |

## 9. 웹 포털 UI (브라우저)

- 타임라인 탭(신규·버전업) / 카테고리 칩 10개 / 라이선스 칩(OSS·프리·유료) / 태그(AI-Agent·MenuBar)
- 카드: 이름·개발사·라이선스뱃지+카테고리+NEW / 버전(+이전 버전 취소선)
  / 새 기능 요약 / 스타·평점 / repo·스토어 출처뱃지(새탭)
- Watchlist diff 뷰: 버전 변경 앱의 이전→현재 diff 목록
- 통계 탭: 카테고리별 신규수, AI 비중, 릴리즈 주기, 스타 속도

## 10. 네이티브 UI (ViewBinding, 최소)

- 홈: 서버 상태 카드 + 주소 복사/QR + 통계 3개(수집 앱 수/활성 소스/마지막 수집) + 지금 수집하기
- 소스관리: 이름·타입·주기·상태색·토글·즉시실행
- 설정: GitHub 토큰 입력(마스킹 표시) + 포트 + 보관기간 + 자동시작 + 배터리예외 + Watchdog + 정리 버튼
- 알림 탭 + 디버그 로그 화면

## 11. 비기능 요구사항

- 배터리: FGS dataSync + 배터리예외 권고 + WorkManager 제약
- 예의: 공식 API 우선, 1초+ 간격, UA 명시, robots.txt 확인
- 보안: 로컬망 바인딩, usesCleartextTraffic 허용, 토큰 DataStore(평문 커밋 금지·로그 마스킹)
- 로그: `[INFO] [FEATURE] <기능명>` 진입, 실패 `[ERROR] E-AND-...` + 메시지 매핑

## 12. 에러코드 (상세: error_message_ko.json)

- 형식 `E-AND-{CATEGORY}-{NUM4}` — PlanJupJup 코드계 승계 + 앱도메인 추가
  (E-AND-CRAWL-0201 파싱 실패 → "앱 파싱에 실패했습니다"로 문구 일반화)

## 13. 마일스톤

1. M0 스캐폴드: Gradle·Manifest·Application·홈 플레이스홀더·CI (0.5일)
2. M1 DB+서버: Room + Ktor 정적서빙 + FGS + BootReceiver + Watchdog (1일)
3. M2 발굴 크롤러: GitHub Search/Releases + PH + HN (1.5일)
4. M3 시드+버전추적: Setapp 시드 + 차트 RSS + lookup 폴링 + version_history (1일)
5. M4 포털: 타임라인·필터·diff·통계 (1일)
6. M5 네이티브+알림+테스트+문서 마감 (1일)

## 14. 리스크

| 리스크 | 대응 |
|---|---|
| GitHub 레이트 초과 | 토큰 필수화 + ETag + 윈도우 분할 + 백오프 |
| 차트 피드 변경 | 파서 격리 + 실패 시 lookup 폴링으로 폴백 |
| Setapp 구조 변경 | 월간 저빈도 + 실패 격리(핵심 경로 아님) |
| Android 16 dataSync 제한 | Watchdog 재시작 + 단기 Worker |
| FGS 백그라운드 시작 제한 | startService 우선 + 예외 catch |

## 15. DoD

- `./gradlew assembleDebug` 성공, lint Error 0, 단위 테스트 통과
- 실기: 3층 수집 → 포털 카테고리·라이선스 필터 → 버전 bump diff 1건+ → repo 링크 이동
- 설정: 토큰 저장·포트변경·보관기간 반영·배터리예외 표시
- 문서: README·CHANGELOG·ENDPOINTS·PERMISSIONS·error_message_ko.json 갱신 + 세션 로그
