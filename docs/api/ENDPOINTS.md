# API 명세 — MacJupJup 내장 서버 (Ktor CIO, 기본 포트 3000)

> 베이스: `http://<S22 로컬 IP>:3000` · 응답: JSON · CORS: 같은 망 브라우저 허용
> M0 현재: 미구현. M1에서 아래 스펙대로 구현.

## GET /

- 웹 포털 정적 HTML (`assets/web/index.html`)

## GET /api/health

- 응답: `{"status":"ok","timestamp":1699999999999}`

## GET /api/apps

- 쿼리: `license`(OSS/FREE/PAID), `category`(10종 중 1),
  `tag`(AI-Agent/MenuBar), `q`(이름·개발사 검색),
  `sort`(newest/updated/stars/rating, 기본 newest),
  `page`(기본 1), `pageSize`(기본 50, 최대 100),
  `bumped`(true면 버전업만, 와치리스트용 v1.6)
- `platform`은 서버 고정 `macOS` (클라이언트 지정 불필요)
- 응답:
```json
{
  "apps": [
    {
      "id": "raycast-raycast",
      "name": "Raycast",
      "developer": "Raycast Technologies",
      "license": "FREE",
      "price": 0,
      "currency": "USD",
      "category": "생산성",
      "tags": "AI-Agent",
      "trackId": 591560477,
      "repoFullName": null,
      "version": "1.89.0",
      "prevVersion": "1.88.0",
      "releaseNotesSummary": "AI 채팅 개선…",
      "stars": null,
      "firstSeenAt": 1699999999999,
      "lastUpdatedAt": 1699900000000,
      "isNew": false,
      "sources": [
        {"sourceName": "MacMenuBar 신규", "sourceUrl": "https://macmenubar.com/lyrimuse/"}
      ]
    }
  ],
  "total": 128,
  "page": 1,
  "pageSize": 50
}
```

## GET /api/apps/{id}

- 성공 200: App + version_history(`sourceUrl` 포함, T-080) + sources + 상세 7섹션 필드. 없음 404: `{"error":"Not found"}`
- 상세 필드 매핑 (v1.2):
  - ①소개: 발췌 300자 / ④세부 설명: README 뒷부분·전문 (①과 중복 해소)
  - ⑥홈페이지: `homepageUrl` / Repo: `repoFullName`→GitHub URL / 스토어: `trackId`→현재 페이지
  - 출처: `sources[]` 목록 (발견처만, 둔갑 금지). MAS는 버전고정 불가라 현재 페이지로 명시

## POST /api/apps/seed

- 수동 시드 (T-061, 차트·키워드 밖 니치 앱 직접 등록).
  바디: `{"trackId":6471012328}` 또는 `{"name":"SoundPaste"}` (이름은 정규화 완전일치만)
- 응답: `{"seeded":1,"created":1,"updated":0}`. 없음 404 `{"seeded":0}`

## GET /api/watchlist

- 응답: 9개 소스 상태 (Setapp 제거됨, v1.3)
  `[{"id":"mas_discovery","name":"MAS 키워드 발견","type":"MAS_DISCOVERY","enabled":true,"intervalHours":24,"lastRunAt":ts,"lastStatus":"SUCCESS"}, ...]`

## POST /api/sync

- 바디: `{"sourceId": null}` (null이면 전체 활성 소스)
- 응답 202 Accepted (WorkManager에 위임, 비동기 실행)

## GET /api/stats

- 응답: `{"totalApps":412,"activeSources":7,"lastCollectedAt":ts}` (홈 3열 호환)

## GET /api/stats/trends

- 쿼리: `gran`(`daily`, 기본), `days`(기본 30)
- 카테고리별 신규·버전업 추이 + AI 비중 + 릴리즈 주기 + 스타 속도

## GET /api/stats/collect

- 일별 수집량 그래프용 (v1.3). 쿼리 `days`(1~30, 기본 14)
- 응답: `{"days":[{"day":"2026-09-10","found":1994,"new":1829,"updated":0,"runs":3,"bySource":[...]}],"total":{"found":N,"new":N,"updated":N}}`

## GET /api/stats/insights

- 트렌드 인사이트 카드 (v1.3). 응답: `{"insights":[{"icon":"🏆","title":"이번 주 수집왕","body":"..."}]}`
- 규칙: 수집왕 → 신규 급증일 → 실패 경고 → 번역 대기 → 버전 활동 → 카테고리 편중

## GET /api/notifications

- 쿼리: `type`(CRAWL_COMPLETE/NEW_APPS_FOUND/VERSION_BUMPED/CRAWL_FAILED_STREAK/CRAWL_SUMMARY),
  `isRead`(true/false), `page`(기본 1), `pageSize`(기본 20, 최대 100)
- 응답:
```json
{
  "notifications": [
    {"id": 1, "type": "CRAWL_COMPLETE", "summary": "수집 완료: ...", "createdAt": 1699999999999, "isRead": false}
  ],
  "total": 1, "page": 1, "pageSize": 20, "unreadCount": 1
}
```

## GET /api/notifications/{id}

- 알림 + 상세(detailJson 파싱): `{"notification":{...},"detail":{...}}`
- 없음 404.

## POST /api/notifications/{id}/read

- 해당 알림 읽음 처리. 응답 `{"updated":1}`.

## POST /api/notifications/read-all

- 전체 읽음 처리. 응답 `{"updated":N}`.

## DELETE /api/notifications/{id}

- 알림 삭제. 응답 `{"deleted":1}`.

## POST /api/notifications/cleanup

- 보관 기간 초과 알림 정리. 응답 `{"deleted":N}`.

## GET /api/notifications/unread-count

- 응답: `{"unreadCount":3}`

## GET /api/settings

- 응답: `{"port":3000,"retentionDays":30,"autoStart":true,"watchdogIntervalSec":60,"githubTokenSet":true,"notifCrawlComplete":true,"notifNewApp":true,"notifFailure":true}`
- 주의: 토큰 값은 절대 반환하지 않음. 설정 여부(boolean)만 반환

## POST /api/settings

- 바디(부분 허용): 위 필드 + `githubToken`(평문 수신 1회, DataStore 저장, 로그 마스킹)
- 포트 변경 시 서버 자동 재시작. 성공 200. 검증 실패 400 + `E-AND-VALID-...`

## 알림 API — 위 7종 (`/api/notifications*`) 참조

## 에러 응답 공통

- 형식: `{"error":"메시지"}` + HTTP 상태코드(400/404/500)
- 서버 내부 예외는 StatusPages에서 500으로 래핑, 로그에 `[ERROR] E-AND-SRV-...` 기록
