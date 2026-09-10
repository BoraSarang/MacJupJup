# 세션 2026-09-10 — T-150 번역 처리율 상향 (android)

- 무엇을: 번역 주기 6h→3h·회당 30→100건 + 기존 예약 REPLACE 교체 + 포털 🌐 번역 수동 버튼.
- 플랫폼: Android 단일 (`com.borasarang.macjupjup`).
- 빌드+PERF+CACHE: 단위 61/61 · lint Error 0 · JS node --check 통과 · 일 처리율 120→800건.
- 남은TODO: T-139/T-149 S22 실측 (포털 렌더·토글·Watchlist·번역 버튼) — 사용자 확인 후.
- 전달로그: 스케줄 로그 "번역 워커 예약 3시간마다" 변경. 신규 error_code 없음.
- 문서갱신: TODO T-150 완료 · CHANGELOG v2.2 · 인사이트 문구 3시간 갱신.
- 큐상태: full/E2E 미실행 (smoke+unit만).
- 주의: gtx 차단 환경이면 실행만 늘고 실패 반복 — 로그에서 gtx HTTP 코드 확인 필요.
