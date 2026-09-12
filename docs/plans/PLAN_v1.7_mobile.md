# PLAN v1.7 — 웹 포털 모바일 대응 (Android)

> 상태: 진행 중 (2026-09-12). 사용자 선택: 컴팩트+필터접기.

## 1. 배경

- 모바일(360px급) 접속 시 `header(sticky)`가 로고바+탭3+라이선스칩7+카테고리칩11 4단을 전부 sticky로 نگه 가져 화면 반을 가림.
- `header-content`가 nowrap + 버튼 4개(`한국어/총N개/번역/🔔`)라 좁은 폭에서 넘침.
- `@media(max-width:600px)`는 검색창·그리드만 대응, 터치영역 32~36px로 DESIGN 규정(44px) 미달.

## 2. 설계 (컴팩트+필터접기)

- 모바일(≤640px): `header-content`만 sticky(높이 ≤56px), 나머지(`top-nav+칩2줄`)는 `div#filterPanel`로 묶어 기본 접힘 + `☰ 필터` 버튼으로 펼침.
- 데스크탑(>640px): `filterToggle` 숨김, `filterPanel` 항상 펼침 — 기존 동작 유지.
- 칩·탭은 1줄 가로스크롤, 전부 최소 44px 터치. 모달은 모바일 bottom-sheet화.
- 스킬: `frontend-design` → `apple-design` → `emil-design-eng` → `web-design-guidelines` 순 적용.
  - apple: pointer-down 즉시 피드백, translucent 헤더, reduced-motion 대응.
  - emil: 200ms ease-out, 버튼 `:active scale(0.97)`, transform/opacity만 애니메이션.

## 3. 변경 파일

| 파일 | 변경 |
|---|---|
| `assets/web/index.html` | `top-nav+칩2줄` → `div#filterPanel` 감싸기 + `button#filterToggle` 추가(aria-controls/expanded) |
| `assets/web/style.css` | 640px 브레이크포인트: 헤더 컴팩트·칩 1줄 스크롤·44px 터치·모달 시트·수평 넘침 차단 |
| `assets/web/app.js` | `filterToggle` 토글 + `setView()` 표시 제어 `filterPanel` 단위로 변경 |
| `docs/DESIGN.md` | 3장 모바일 헤더 규칙 추가 |
| `docs/CHANGELOG.md` | v2.4 항목 |

## 4. 검증

- `./build_and_run.sh build` + `test unit` + `lint` Error 0.
- 크롬 디바이스 툴 360x740/390x844 눈확인: 첫 화면 카드 노출·접기/펼치기·탭·칩스크롤·검색100%·모달시트.
- S22 실기 full은 사용자 승인 후 (사용 중이면 에뮬만).

## 5. DoD

- [ ] PLAN+TODO(T-170~) · smoke+unit · build 성공 · 스크린샷(접힘/펼침) · DESIGN+CHANGELOG · 세션 로그
