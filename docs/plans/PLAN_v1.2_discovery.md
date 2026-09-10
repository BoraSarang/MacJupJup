# PLAN_v1.2_discovery — 발견망·상세표시·버전링크·알림시간 보강

> 상태: 진행 중(사용자 승인) · 작성일: 2026-09-10
> 배경: 실기기(SM-S901N, 795건) 실측 — DockDuck/SoundPaste/TabLinker 0건, TopSecret Setapp 1건(홈 None),
> Lyrimuse MMB `homepageUrl` 저장됨·포털 미표시, FetchBar 홈 정상저장·미표시,
> 알림 detail에 시작→완료 존재·UI 미표시. `macked.app`은 크랙 사이트라 수집처 제외(PLAN_v1.0 7번).

## 1. 소스 의미 정의 (전 크롤러 공통)

| 칸 | 정의 | 원천 |
|---|---|---|
| 출처 | 발견한 곳 | `AppSourceMapping.sourceUrl` (MMB 포스트, PH 포스트, HN 스레드, Setapp 페이지) |
| 홈페이지 | 공식 사이트 | MMB Visit 링크, GitHub API `homepage`, Setapp은 없음(비워둠) |
| 다운로드/Repo/스토어 | 실행물 위치 | `repoFullName`→GitHub URL, `trackId`→MAS URL. 직접 dmg/zip 저장 안 함 |
| 버전별 링크 | 해당 버전 릴리즈 페이지 | GitHub `releases html_url`을 `version_history.sourceUrl`에 기록. MAS는 버전고정 불가라 현재 스토어 페이지로 명시 |

## 2. 작업 목록

- T-060 MAS 키워드 디스커버리(신규): `itunes/search?entity=macSoftware` 키워드 분할(24개, 24h).
  SoundPaste/TabLinker류 차트 밖 앱 발견용. 상세는 기존 Lookup 보완.
- T-070 포털 ⑥⑦ 분리: 홈페이지 버튼 + Repo/스토어 버튼 + 출처 목록 3단.
  `sources[]` 스캔 대신 `app.repoFullName/trackId/homepageUrl` 직접 사용.
- T-071 MMB: `guessRepo`를 `github.com/{owner}/{repo}`형도 인식 + 플레이스홀더 개발사 중복 병합.
  Lyrimuse 2건 중복(`MacMenuBar` vs `Khalil`) 해소.
- T-072 GitHub: `homepage` 빈값이면 공란 유지(repo로 둔갑 금지) 규칙·테스트 고정.
- T-073 ④기능→④세부설명: ①소개와 동일 원천 중복 해소. README 뒷부분·lookup 전문의 긴 쪽 표시.
- T-080 버전 링크(DB v2→v3): `version_history.sourceUrl` 추가. GitHub bump→릴리즈 URL,
  Lookup bump→trackViewUrl(스토어 현재 페이지 표기). API·포털 버전 테이블에 링크 열.
- T-090 알림 시간: 수집완료에 시작→완료·소요 표시(네이티브+포털). 신규/버전업은 발견 시각만.
- T-100 번역 Auto: `MacTranslator` 소스 고정(EN) → ML Kit 언어감지 + gtx `sl=auto`.
  `needsTranslation`(한글 50% 초과만 스킵)으로 중·일·러 포괄. `mlkit:language-id` 추가.

## 3. 검증

- 단위 테스트(파서·마이그레이션·매처) + `./build_and_run.sh test` + `lint`
- S22 실측: Lyrimuse 홈 버튼, FetchBar Repo 버튼, TopSecret 출처, 알림 시간, MAS 발견 1건 이상
- `error_message_ko.json` 변경 없음(신규 에러코드 없음). CHANGELOG에 v1.2 항목 기록.
