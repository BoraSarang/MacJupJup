# session-2026-09-09-android — 맥줍줍 M2 발굴 크롤러

1. 무엇을: BaseCrawler·CrawlHttp·Factory·CategoryInfer + 크롤러 4종 + Worker·Scheduler + /api/sync 연동.
2. 플랫폼: Android 단일, JBR 빌드, S22 실기(adb forward) 수집 E2E.
3. 빌드+PERF+CACHE: 빌드 성공·설치 Success / 단위 18/18 / lint Error 0(W43) / ETag 미적용(M4 이후).
4. 남은TODO: M3 시드+버전추적(T-030~033: Setapp·차트·lookup). Factory else 분기 추가.
5. 전달로그: `[INFO] [FEATURE] 수집·스케줄` + `[ERROR] E-AND-CRAWL-0201` 0건(정상). M3 미지원 2종은 FAILED 격리됨.
6. 문서갱신: TODO T-020~026 체크, CHANGELOG v0.3.
7. 큐상태: 다음 = M3 SetappSeed·ChartRss·iTunesLookup 크롤러 + Factory 연결.
8. E2E: hn_show→Chalk 등 수집, github_search 무토큰 196개, OSS 필터·MenuBar 태그·main 최신 버전 확인.
