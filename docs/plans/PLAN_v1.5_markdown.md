# PLAN v1.5 — GitHub 설명 마크다운 렌더링 (Android)

> 상태: 진행 중 (2026-09-10). 사용자 선택: 마크다운 렌더링.

## 1. 배경

- GitHub 설명 원본은 README 마크다운이나, 수집 단계(`cleanReadme`)에서 기호를 벗겨 평문 저장 중.
- 포털은 `esc()` 평문 표시라 제목·목록·코드·링크 구조가 사라짐.
- 기존 저장분(벗겨진 평문)은 렌더러에서 단락으로 정상 표시 → 마이그레이션 불필요.

## 2. 설계 (보수적 렌더러 — 평문 오렌더 방지)

- 렌더 지원: 코드펜스/인라인코드/제목(#)/굵게(**)/링크·이미지→링크/목록(-,*,1.)/인용(>)/구분선/단락.
- 미지원(의도): 단일 `*`·`_` 강조 — App Store 평문의 snake_case·수식 오렌더 방지.
- XSS: 전체 escape 선행 + href `https?://` 화이트리스트 + 수집 단계 raw HTML 제거 이중 방어.

## 3. 변경 파일

| 파일 | 변경 |
|---|---|
| `crawler/github/GitHubSearchCrawler.kt` | `cleanReadme` 마크다운 보존·raw HTML 살균 (`·` 변환 폐지) |
| `util/MacTranslator.kt` | 코드펜스 스킵 + 줄 앞머리 마커 분리 + 링크 URL·인라인코드 미번역 (`splitMdLine` 순수함수) |
| `assets/web/app.js` | `md()` 렌더러 + `stripMd()` 카드 발췌 + 토글 메모리 재렌더 |
| `assets/web/style.css` | `.detail-sec` 마크다운 요소 스타일 (h4·ul·code·pre·blockquote·a) |
| 테스트 | `ReadmeCleanTest` 갱신, `TranslatorTest` 분할 헬퍼 케이스 |

## 4. 검증

- 단위 56+α · lint Error 0 · node(있으면) 렌더러 스모크.
- S22 실측: GitHub 앱 상세 마크다운 표시 + 토글 + 카드 발췌.

## 5. DoD

- [ ] PLAN + TODO(T-140~) · DebugLogger 기존 유지 · smoke+unit · CHANGELOG · 세션 로그
