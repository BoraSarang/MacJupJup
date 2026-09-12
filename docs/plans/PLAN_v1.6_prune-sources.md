# PLAN v1.6 — PH·HN·MMB 수집처 제거 (Android)

> 상태: 진행 중 (2026-09-10). 사용자 결정: 3종 전부 제거 + DB wipe 후 재수집은 사용자 실행.

## 1. 근거

- 3종 모두 `version` 상시 null → 버전 히스토리·Watchlist 업데이트·bump 알림에 영원히 미포함.
- 탈출구(이름대조 단일정확일치·형제병합)는 이론상 가능하나, 일반명사 제품명·비MAS 런칭·복수후보 중단으로 실질 불가.
- 추적 가능한 행은 MAS 행과 중복이라 정보 가치도 중복.

## 2. 변경 (T-110 Setapp 선례)

| # | 파일 | 작업 |
|---|---|---|
| 1 | `crawler/ph/`, `crawler/hn/`, `crawler/mmb/` | 디렉토리 삭제 |
| 2 | `InitialDataSeeder.kt` | 시드 3건 삭제 (9→6곳) |
| 3 | `CrawlerFactory.kt` | 분기·import 3건 삭제 |
| 4 | `Constants.kt` | SOURCE_/TYPE_ 6건 삭제 |
| 5 | `CrawlSource.kt` | 주석 나열 갱신 |
| 6 | `ITunesNameMatcher.kt` | placeholder 3건 삭제 + 주석 |
| 7 | `MacJupJupApplication.kt` | 시작 시 purge 3종 + 예약 취소 (Setapp 패턴) |
| 8 | 테스트 | Parse 3종 삭제, NameMatcher/Sibling/Dao/MergeUtils 수정 |

## 3. 검증·DoD

- 단위 green · lint Error 0 · PLAN+TODO+CHANGELOG+세션 로그.
- S22 실측 + DB wipe + 재수집은 사용자 실행 (토큰 백업 선행).
