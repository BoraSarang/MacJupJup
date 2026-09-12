# 세션 2026-09-10 — v1.5 마무리 + v1.6 수집처 제거 (android)

- 무엇을: 상세 HTML 소스 노출 수정 (포털 전체 stripHtml + 릴리즈노트 cleanNotes) + PH·HN·MMB 3종 제거 및 시작 시 purge.
- 플랫폼: Android 단일 (`com.borasarang.macjupjup`).
- 빌드+PERF+CACHE: 단위 57/57 (파서 테스트 5종 삭제·cleanNotes 1종 추가) · lint Error 0 · node 렌더 검증 외래태그 0 · perf/cache 영향 없음.
- 남은TODO: T-139/T-149/T-169 S22 실측 + DB wipe·재수집 (토큰 백업 선행) — 사용자 실행.
- 전달로그: 기존 DebugLogger 유지. 신규 error_code 없음.
- 문서갱신: PLAN_v1.5/v1.6 · TODO v1.5(T-140~143 완료)·v1.6(T-160·161 완료) · CHANGELOG v2.3.
- 큐상태: full/E2E 미실행 (smoke+unit만).
- 주의: 제거 3종 placeholer 판정은 잔재행 매칭용으로 유지. wipe 후 재수집 시 README 마크다운 원형·릴리즈노트 살균 적용됨.
