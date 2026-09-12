# session-2026-09-10-android-v13 — v1.3 Setapp 제거·포털·수집그래프·인사이트

- 무엇: Setapp 코드+DB 완전 제거 / 뷰별 메뉴 동작 / 일별 수집량 그래프 / 인사이트
- 플랫폼: Android 단일, SM-S901N 실측
- 빌드: 단위 45/45 · lint Error 0 · assembleDebug 설치 Success
- 실측: purge 로그(매핑 391·앱 381·로그 1) · 소스 9개 · collect/insights 정상 ·
  와치리스트 필터 쿼리 정상 · Lyrimuse 1건 유지 · SoundPaste 유지
- 문서: PLAN_v1.3, TODO v1.3, ENDPOINTS(collect/insights), CHANGELOG v1.3
- 참고: 수집 로그 plansNew는 동시 실행 시 중복 집계 가능(휴리스틱). 번역은 제한망에서 대기 중
- 후속 수정: 통계 로드 분리(기존 우선 렌더 + 인사이트·수집량 독립 추가). 4개 API 200 확인 후 재배포
- 후속2: 인사이트를 KPI 아래로 이동, 수집량을 수집처별 선택+표로 변경. 재배포·API 확인
- v1.4 리팩토링 P0-1·4·5: updateKo COALESCE, purge/cleanup withTransaction + 동반삭제, saveApps E-AND-DB-0402. 45/45·lint0·실측(1837건 보존)
- v1.5 리팩토링 P0-2·P1-1: saveApps withTransaction+배치(getByIds/findByNamesLower/getByApps/insertAll), list 매핑 배치, SourceLocks, 즉시수집 KEEP. 45/45·lint0·실측(연타 1회완료)
- v1.6 리팩토링 B3·B4·B5: 일일요약 주기화, bumped 서버필터+와치리스트 페이징, race 가드, 공용 헬퍼 통합, 데드코드 삭제, 52/52·lint0·실측
- v1.7: R1-3 템플릿 5종 이관, P0-3 다수후보 병합중단, DAO connected 5종, 53/53+full 그린. connected 실행 시 기기DB 초기화 주의
- v1.8 isNew 정책: 7일경과 해제+시작시 적용, bump 즉시해제. 54/54+full 그린. 재설치 DB초기화 반복 발생(디버그 플로우, 조사 필요)
- v1.9 버그사냥: API 퍼징(무효ID 404 수정), 알림 N+1 상한, PH 폐쇄피드 제거, 전수집 에러 0. 54/54·lint0
- v2.0 감사잔재: 서버헬퍼·seed 백그라운드·DB싱글턴/백업·인덱스v4·포털페이징/키보드. 54/54·lint0·실측. 재설치wipe=connected탓 확정, 일반설치는 보존
- 마무리: feat/android-v1.2-v2.0 브랜치에 73파일 커밋 fb0f9e8 (push 안 함)
