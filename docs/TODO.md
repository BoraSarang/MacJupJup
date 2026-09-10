# TODO — MacJupJup v1.0 (Android)

> 규칙: 1커밋 1관심사, 완료 시 체크 + 세션 로그 갱신. 본 파일이 단일 진실.

## M0 스캐폴드 (PLAN_v1.0_macjupjup)

- [x] T-001 프로젝트 스캐폴딩: settings/root/app gradle, gradle.properties, 패키지 디렉토리
- [x] T-002 AndroidManifest.xml: 권한 9종 + MainActivity (서비스·리시버는 M1)
- [x] T-003 리소스: strings(맥줍줍)·themes(Material3)·colors·activity 레이아웃·mipmap
- [x] T-004 Application 클래스 + Timber + AppCategory 유틸(카테고리 10종·라이선스·태그) + 단위 테스트
- [x] T-005 build_and_run.sh + CI/Release 워크플로우 + 빌드 검증(assembleDebug)

## M1 DB + 서버 + 생존성

- [x] T-010 Room Entity 6종(apps/app_sources/sources/version_history/crawl_logs/notification_logs) + Converters(해당 없음, String CSV)
- [x] T-011 DAO 6종 + MacDatabase + Repository(App/Source) + Preferences(토큰키) + InitialDataSeeder(소스 7개)
- [x] T-012 Ktor CIO 서버: /api/health·apps·apps/:id(7섹션)·watchlist·sync(pending)·stats·settings + 정적 placeholder + CORS(동일망) + StatusPages
- [x] T-013 HttpServerService: START_STICKY + startForeground + onTaskRemoved + start()/stop() 헬퍼
- [x] T-014 BootReceiver + Watchdog(포트 헬스체크·자동재시작) + Application 와이어링(시드·autoStart)
- [x] T-015 빌드·단위 7/7·lint Error 0·S22 실기 E2E(health/stats/watchlist/settings/portal) + 세션 로그

## M2 발굴 크롤러

- [x] T-020 BaseCrawler + CrawlHttp(헤더·304) + CrawlerFactory + AppCrawler + CategoryInfer + AppSourceMappingHelper
- [x] T-021 GitHubSearchCrawler(topic 3종·sort=updated) + 픽스처 테스트
- [x] T-022 GitHubReleasesCrawler(태그 비교·bump draft) + 파싱 테스트
- [x] T-023 ProductHuntFeedCrawler(피드 3종·mac 키워드 필터) + Atom 픽스처 테스트
- [x] T-024 HnShowCrawler(Algolia 2쿼리·traction 하한) + 픽스처 테스트
- [x] T-025 CrawlWorker + CrawlScheduler(개별주기·즉시실행) + /api/sync 연동 + Application 와이어링
- [x] T-026 빌드·단위 18/18·lint Error 0·S22 실기 E2E + CHANGELOG·세션 로그

## M3 시드 + 버전 추적

- [x] T-030 SetappSeedCrawler(SSR 카드 파싱·Mac 필터·월간·PAID 기본) + 픽스처 테스트
- [x] T-031 ChartRssCrawler(무료 차트 100·Apple 카테고리 매핑) + 픽스처 테스트 — 유료/매출 차트는 Apple 측 빈 배열이라 제외
- [x] T-032 iTunesLookupPoller(200 ID 묶음·버전 bump·상세 보완) + 픽스처 테스트 + AppDao.getAppsWithTrackId
- [x] T-033 Factory 7종 연결 + /api/sources/:id/toggle 추가 + CategoryInfer 단어경계·Setapp 태그라인 정제
- [x] T-034 빌드·단위 23/23·lint Error 0·S22 실기 E2E + CHANGELOG·세션 로그

## M4 웹 포털

- [x] T-040 assets/web 실물: 타임라인·칩(라이선스4·태그2·카테고리10)·검색·정렬·그리드·페이지네이션·상세 7섹션 모달
- [x] T-041 Watchlist diff 뷰 + /api/stats/trends(집계 쿼리 7종) + 통계 대시보드(KPI·카테고리·라이선스)
- [x] T-042 빌드·단위 23/23·lint Error 0·S22 실기 + 브라우저 렌더 E2E + CHANGELOG·세션 로그

## M5 네이티브 + 마감

- [x] T-050 홈(상태·주소·통계·수동수집)·소스(토글·주기·즉시실행)·설정(토큰·포트·배터리·보관·Watchdog·알림토글·로그)·알림 탭
- [x] T-051 알림 생성(수집완료·신규·버전업·실패연속·일일요약) + 서버 알림 API 7종 + DailySummaryWorker
- [x] T-052 단위 23/23·Espresso 2/2(SM-S901N)·lint Error 0·S22 E2E + README·CHANGELOG·ENDPOINTS 마감

## v1.1 정보 보강 + 번역 (진행 중)
- [x] DB v2: 컬럼 11종(아이콘·전체설명·한글·스토어 메타·GitHub 신호) + MIGRATION_1_2
- [x] 파서 보강: lookup(아이콘·전체설명·노트·판매자·용량·OS·등급) + GitHub(포크·이슈·라이선스·아바타) + README(상위 10·2000자)
- [x] 저장 병합 규칙(보강분 보호·라이선스 우선·태그 합집합·카테고리 유지)
- [x] 이름 대조 매처(엄격+단일후보 폴백) + 주간 소스 + Setapp 상세 보강(설명·아이콘·스크린샷)
- [x] 스크린샷 구분자 수정(콤마→개행) + 깨진 CDN 정리 1회성 복구
- [x] ML Kit + gtx 폴백 번역 워커(6h·30건) + 설정 토글 + POST /api/translate
- [x] macmenubar.com RSS 수집처 (라이선스·스크린샷·방문링크·github.io repo 추정)
- [x] 포털: 아이콘 카드·한글 기본+원문 토글·풍부한 특징·알림 목록
- [x] 단위 41/41·lint Error 0·S22 E2E + 문서

## v1.2 발견망·상세표시·버전링크·알림시간 (PLAN_v1.2_discovery, 완료 2026-09-10)
- [x] T-060 MAS 키워드 디스커버리(신규 소스, 24h): search 키워드 분할 + 시드 + 팩토리
- [x] T-061 수동 시드 POST /api/apps/seed (trackId·이름)
- [x] T-070 포털 ⑥⑦ 분리: 홈페이지·Repo/스토어·출처 3단 렌더
- [x] T-071 MMB guessRepo 확장 + 플레이스홀더 중복 병합 + 테스트
- [x] T-072 GitHub homepage 빈값 규칙 고정(테스트)
- [x] T-073 ④기능→④세부설명 분리
- [x] T-080 버전 릴리즈페이지 링크(DB v2→v3: version_history.sourceUrl + API + 포털)
- [x] T-090 알림 시작→완료·소요 표시(네이티브 + 포털)
- [x] T-100 번역 소스언어 Auto(중·일·러): ML Kit 언어감지 + gtx sl=auto + needsTranslation 게이트
- [x] T-099 빌드·단위 45/45·lint Error 0·S22 실측 검증 + CHANGELOG·세션 로그

## v1.3 Setapp 제거·포털 동작·수집그래프·인사이트 (PLAN_v1.3_portal, 완료 2026-09-10)
- [x] T-110 Setapp 완전 제거(코드+시작시 purge: 매핑 391·앱 381·로그) + 소스 9개
- [x] T-111 뷰별 메뉴 동작: reloadCurrentView + 와치리스트 필터 연동 + 통계 칩 숨김·기간 선택
- [x] T-112 일별 수집량 API(collectByDay) + 그래프 + 인사이트 API·카드
- [x] T-119 빌드·단위 45/45·lint Error 0·S22 실측 + 문서

## v2.0 감사 미처리분 (완료 2026-09-10)
- [x] T-120 서버 JSON 헬퍼 + seed 백그라운드·상태 + 재시작 백그라운드화
- [x] T-121 DB 싱글턴 정리 + 파괴 폴백 백업 + 인덱스 v4
- [x] T-122 포털 알림페이징·키보드·aria
- [x] T-129 빌드·단위 54/54·lint Error 0·S22 실측 + 문서

## 완료 기준 (DoD 요약)

- [x] lint Error 0, 단위/UI 테스트 통과
- [x] 실기: 수집→포털 필터→버전 diff→repo 링크 이동
- [x] 설정 반영(토큰·포트·보관기간·배터리예외) 확인 후 세션 로그 작성
