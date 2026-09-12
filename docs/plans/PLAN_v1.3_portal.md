# PLAN_v1.3_portal — Setapp 제거·뷰 메뉴·수집그래프·인사이트

> 상태: 완료(2026-09-10) · 사용자 결정: Setapp 수집처 완전 제거 + DB 삭제

## 1. Setapp 제거

- 코드: `SetappSeedCrawler`·`SetappParseTest` 삭제, 팩토리 분기·상수·시더 항목·`AppDao.getSetappWithoutIcon` 제거.
  `ITunesLookupPoller` 카테고리 유지 특례에서 `setapp_seed` 제외(차트만 유지).
- DB: 시작 시 1회 purge — 예약 취소 → 소스행 삭제 → 매핑 391건 삭제 →
  대표 앱 중 매핑 0건 고아 381건 삭제 → 로그 삭제. 타 출처 병합 앱은 유지.
- 실측: 소스 9개, TopSecret 등 Setapp 단독 앱 제거 확인.

## 2. 뷰별 메뉴 동작

- 증상: 공유 칩·정렬·검색이 타임라인만 새로고침 → 와치리스트·통계에서 무반응.
- 수정: `reloadCurrentView()` 라우팅. 와치리스트는 필터·정렬·검색 연동(100건 조회 후 버전업 선별).
  통계는 전역 대시보드라 칩 행 숨김 + 자체 기간 선택(7/14/30일).

## 3. 일별 수집량 + 인사이트

- `crawl_logs` 집계 DAO (`collectByDay`, 로컬타임존) → `GET /api/stats/collect?days=`
- `buildInsights` 순수 함수 + `GET /api/stats/insights` (수집왕·급증일·실패·번역·버전·편중)
- 포털: 인사이트 카드(KPI 바로 아래) + 수집처별 일별 수집량(소스 칩 선택·표) + 기간 선택.
- 구조: 기존 섹션(KPI·카테고리·라이선스) 우선 렌더 후 추가 섹션 독립 로드 —
  추가 API 실패가 기존 통계를 blank로 만들지 않음.

## 4. 검증

- 단위 45/45 · lint Error 0 · S22 실측(제거 로그·collect·insights·와치리스트 필터)
