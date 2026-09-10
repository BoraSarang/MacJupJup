# CHANGELOG — MacJupJup

> 형식: `## [vX.Y.Z] - YYYY-MM-DD` · platform 태그 + error_code + perf/cache 영향 기록

## [v2.0] - 2026-09-10

- 서버: JSON 헬퍼(receive/pathId/putIfNotNull) 전 라우트 적용 + seed 백그라운드·상태 API
- 서버: 포트변경 재시작 백그라운드화 + 에셋 IO 격리·로그
- DB: 싱글턴 단일화 + 파괴 폴백 전 백업 + 인덱스 v4 마이그레이션(데이터 보존 확인)
- 포털: 알림 페이지네이션 + 카드 키보드 진입 + aria-live
- 단위 54/54 · lint Error 0 · S22 실측
- 주의: connected 테스트는 기기 DB 초기화 (build_and_run.sh 경고 추가)

## [v1.9] - 2026-09-10

- 버그사냥: sync 무효ID 404 + 알림 재조회 50건 상한 + PH 폐쇄피드 2종 제거
- 단위 54/54 · lint Error 0 · 전수집 S22 실측(에러 0)

## [v1.8] - 2026-09-10

- isNew 정책 확정: 첫 발견 7일 경과 시 NEW 자동 해제(시작·보관정리 시) + 버전 bump 즉시 해제
- 단위 54/54 · connected 포함 full 그린 · lint Error 0 · S22 실측
- 주의: 현 환경 gtx 차단으로 번역 저류 중. 재설치 시 기기 DB 초기화됨(원인 조사 중)

## [v1.7] - 2026-09-10

- 리팩토링 R1-3: 크롤러 순회 템플릿(crawlEach) + 5종 이관 + 미사용 import 정리
- P0-3: 동명이앱 다수 후보 병합 중단 + 경고 로그
- 테스트: DAO connected 테스트 5종(S22 실DB) — UNIQUE 위반 테스트 결함 2건 수정
- 단위 53/53 · connected 포함 full 그린 · lint Error 0
- 주의: connected 테스트 실행 시 앱 재설치로 기기 DB 초기화됨 (재수집으로 복구)

## [v1.6] - 2026-09-10

- 리팩토링 B3: 일일요약 24h 주기화 + 와치리스트 서버필터(`bumped`)·페이징 + 타임라인 race 가드
- 리팩토링 B4: GitHub헤더·JsonSafe·parseFail·정규화·URL·제목/키워드 공용화 + postForm/dbl/별칭 삭제
- 리팩토링 B5: CommonCrawl·Sibling·정규화 테스트 (52/52)
- 단위 52/52 · lint Error 0 · S22 실측

## [v1.5] - 2026-09-10

- 리팩토링 P0-2·P1-1: saveApps 트랜잭션 원자화 + 배치 일괄 조회 + list 매핑 N+1 제거
- 소스별 워커 상호배제(SourceLocks) + 즉시수집 KEEP(연타 취소 방지) + 취소 로그
- 단위 45/45 · lint Error 0 · S22 실측(연타 1회 실행·데이터 보존)

## [v1.4] - 2026-09-10

- 리팩토링 P0-1·4·5: 번역 부분업데이트(COALESCE) + purge·cleanup 트랜잭션 + 버전이력·매핑 동반 삭제
- 저장 실패 에러코드 E-AND-DB-0402 부여
- 단위 45/45 · lint Error 0 · S22 실측(데이터 보존·워커 정상)

## [v1.3] - 2026-09-10

- Setapp 수집처 완전 제거(코드 삭제 + 시작 시 purge: 매핑 391·앱 381·로그) → 소스 9개
- 포털: 뷰별 메뉴 동작 수정(와치리스트 필터 연동, 통계 기간 선택 7/14/30일)
- 통계: 일별 수집량 그래프(`/api/stats/collect`) + 인사이트 카드(`/api/stats/insights`)
- 단위 45/45 · lint Error 0 · S22 실측

## [v1.2] - 2026-09-10

- 발견망: MAS 키워드 디스커버리 10번째 소스(24 키워드·24h) + 수동 시드 `POST /api/apps/seed`
- 상세표시: ⑥홈·Repo·스토어·출처 3단 분리 + ④세부설명(① 중복 해소) + 버전별 릴리즈 링크
- DB v3: `version_history.sourceUrl` (GitHub 릴리즈 페이지 / MAS 현재 페이지)
- MMB `github.com` repo 인식 + 수집원표기(MMB 포함) 중복 병합 + 이름대조 폴백 확대
- 번역 Auto: ML Kit 언어감지 + gtx `sl=auto` + `needsTranslation` (중·일·러 포괄)
- 알림: 수집 시작→완료·소요 표시 (네이티브 + 포털)
- 단위 45/45 · lint Error 0 · S22 실측 (MAS 1829건·SoundPaste 시드·Lyrimuse 홈버튼·버전링크 확인)

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
