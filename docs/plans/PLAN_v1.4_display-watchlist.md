# PLAN v1.4 — 상세 표시·번역 개행·Watchlist 탭 분리 (Android)

> 상태: 진행 중 (2026-09-10). 사용자 제보 3건 대응.

## 1. 제보와 원인

### 1-1. Time Timer 소개·세부설명 번역이 한 줄로 표시
- 저장 단계는 개행 유지 (`ITunesLookupPoller.parseLookup` → `MaxTranslator` → `HttpServerService.appElement`).
- 표시 단계 문제:
  - `app.js detailHtml` 소개 발췌 `trim().slice(0,300)` — 양끝 개행 제거 + 300자 절단.
  - 카드 `.notes`는 `white-space:normal + line-clamp:2`라 목록 한 줄 표시는 정상.
  - 상세 `<p>`는 `pre-wrap`이라 `\n` 있으면 여러 줄 표시됨. 상세까지 한 줄이면 `descriptionKo` 자체에 `\n` 없음 → ML Kit 통번역 시 개행 collapse 유력.
- 대응: 번역 요청을 **줄 단위 분할 번역 후 `\n` 결합**으로 변경 + 개행 소실 행 자동 재번역.

### 1-2. 소개 원문보기 클릭 시 무반응 — 버그 확정
- `app.js` 소개 `<p>`에 `data-kotext` 없음 → 클릭 핸들러가 대상을 못 찾고 종료.
- 대응: 소개 `<p data-kotext>` 부여 + 전문 토글로 일관화 (300자 잘린 값 토글 폐지).

### 1-3. 4.3.4 같은 버전이 Watchlist에 업데이트로 표시
- `AppDao bumped = isNew=0 AND version NOT NULL` — 버전업 이력이 아니라 **정착앱 전체**.
- `needsEnrich` 보강·`trackId` 부여 등 `lastUpdatedAt` 터치면 최상단 노출 + 취소선(`prevVersion`) 없음.
- 문자열 미정규화 (`JsonSafe.str` 무트리밍, raw `!=` 비교 3곳) — 공백 차이로 오판 가능.
- 대응: 탭 분리 + 버전 비교 정규화.

## 2. Watchlist 정의 (사용자 선택: 탭 분리)

- `업데이트` 탭: `prevVersion IS NOT NULL` (실제 bump 증거) — 기본값.
- `전체 버전앱` 탭: 기존 조건 (`isNew=0 AND version NOT NULL`).
- DAO `updatedOnly` 파라미터 추가, 서버 `?updatedOnly=` 파싱, 웹 `state.watchMode` 토글.

## 3. 변경 파일

| 파일 | 변경 |
|---|---|
| `assets/web/app.js` | 소개 `data-kotext` + 전문 토글, `watchMode` 상태·토글 버튼 바인딩 |
| `assets/web/index.html` | Watchlist 섹션에 업데이트/전체 토글 버튼 + 문구 정정 |
| `util/MacTranslator.kt` | 줄 단위 분할 번역 (개행 보존), 단일 블록 추출 |
| `worker/TranslateWorker.kt` | 개행 소실 행 재번역 조건 |
| `data/db/dao/AppDao.kt` | `getUntranslated` 개행 복구 조건, `listFiltered/countFiltered` `updatedOnly` |
| `crawler/JsonSafe.kt` | `str()` trim (공백 버전 오판 제거) |
| `util/MergeUtils.kt` | `sameVersion()` 헬퍼 |
| `data/repository/AppRepository.kt` | 버전 비교 2곳 정규화 |
| `crawler/itunes/ITunesLookupPoller.kt` | 버전 비교 정규화 |
| `crawler/github/GitHubReleasesCrawler.kt` | 태그 비교 정규화 |
| `data/repository/Models.kt` | `AppFilter.updatedOnly` |
| `server/HttpServerService.kt` | `updatedOnly` 파싱 |

## 4. 검증

- 신규 단위 테스트: `parseGtx` 개행 결합, `sameVersion` 공백 판정.
- `./build_and_run.sh test unit` + `lint` (Error 0).
- 실기 확인: Time Timer 상세 여러 줄 표시 + 소개 원문보기 토글 + Watchlist 업데이트 탭에서 4.3.4 단일 행.
- full/E2E는 사용자 사용 중이므로 사전 확인 후.

## 5. DoD

- [ ] PLAN + TODO(T-130~) 등록
- [ ] DebugLogger 기존 경로 유지 (신규 로그 1개 이상: 번역 분할·재번역)
- [ ] smoke+unit 통과, lint Error 0
- [ ] CHANGELOG 기록, error_code 추가 없음 (신규 에러코드 불필요)
- [ ] 세션 로그 작성
