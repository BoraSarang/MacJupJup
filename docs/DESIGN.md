# DESIGN.md — MacJupJup UI/UX 설계

> 네이티브: View + ViewBinding + Material3 Components (Compose 미사용).
> 원칙: 네이티브 화면은 최소(상태·설정·로그), 주 사용 화면은 웹 포털.

## 1. 네이티브 앱 정보 구조

```
MainActivity (BottomNavigation)
├─ 홈(HomeFragment) [M5]
│   ├─ 서버 상태 카드: [아이콘] 실행중/중지됨 + 주소 + [복사] [QR]
│   ├─ 통계 3열: 수집 앱 수 / 활성 소스 수 / 마지막 수집(상대시간)
│   └─ [지금 수집하기] 버튼 (실행 중 비활성화 + "수집 중..." 표시)
├─ 소스(SourceManageFragment) [M5]
│   └─ RecyclerView 행: 이름 / 타입·주기 / 최근로그 / 상태색 /
│       토글 / 주기 스피너(1h~월1회) / 즉시실행
│       상태색: SUCCESS=초록, FAILED=빨강, RUNNING=주황, NEVER_RUN=회색
├─ 알림(NotificationFragment) [M5]
│   └─ 요약(2줄) + 상대시간 + 삭제, 미읽음 볼드, 탭 → 상세 다이얼로그
└─ 설정(SettingsFragment) [M5]
    ├─ GitHub 토큰: 입력(마스킹 표시 `ghp_****1234`) + [저장] + 상태행
    ├─ 서버: 포트 입력 + [적용] + 상태행 + 자동시작 스위치
    ├─ 배터리 최적화 예외: 상태행 + [허용 요청]/[해제] + 안내문
    ├─ 데이터: 보관기간 라디오(30일/90일) + [오래된 데이터 정리]
    ├─ 알림: 수집 완료 / 신규 앱·버전업 / 수집 실패 3종 스위치 (기본 ON)
    ├─ Watchdog: 헬스체크 주기 입력(15~3600초) + [적용]
    └─ 디버그 로그: 최근 crawl_logs 50건 + 앱 정보(버전·제작자·문의)
```

M0 현재: 홈 플레이스홀더 1화면(상태 텍스트 + 버전 표기). M1부터 확장.

## 2. 네이티브 시각 규칙 (Material3)

- 테마: `Theme.Material3.DayNight.NoActionBar` 기반, 카드 radius 12dp, 패딩 16dp
- 상태 카드 배경: 실행중=`colorPrimaryContainer`, 중지=`colorErrorContainer`
- boolean 네이밍: isServerRunning, isCrawling, batteryUnrestricted
- 함수 동사 prefix: loadStats(), triggerManualCrawl(), copyAddress(), saveGithubToken()
- 진입 로그: `[INFO] [FEATURE] 홈`, `[INFO] [FEATURE] 수동수집` 등 화면·액션마다 1개 이상
- 토큰은 로그·화면·API 응답에서 항상 마스킹 (뒤 4자리만)

## 3. 웹 포털 레이아웃 (assets/web) [M4]

```
header (sticky)
├─ 로고 "🍎 맥줍줍" + 마지막업데이트 + [전체 N개] + [🔔 알림]
└─ 탭 내비(전환식): [타임라인] [카테고리 칩 10개(가로 스크롤)] [📊 통계] [👁 Watchlist]
main
├─ 라이선스 칩: [오픈소스] [프리] [유료] + 태그 칩: [AI-Agent] [MenuBar]
├─ section#timeline: 카드 그리드 (auto-fill minmax 280px, 모바일 1열)
│   카드: 이름·개발사 / 라이선스뱃지+카테고리+NEW / 버전(이전 취소선)
│         / 새 기능 요약 2줄 / 스타·평점 / repo·스토어 출처뱃지(새탭)
├─ section#watchlist: 버전 diff 목록 (이전→현재, 변경일, 요약, 링크)
├─ section#appDetail (카드 클릭 → 모달/전용 뷰, 7섹션 고정 순서)
│   ├─ ① 소개: description 발췌 + 더보기 (출처 표기)
│   ├─ ② 스크린샷: screenshotUrls 가로 캐러셀 (Apple CDN 직접 표시, 재호스팅 금지)
│   ├─ ③ 특징: 평점·평가수·연령등급·용량·지원언어·최소OS 하이라이트 + 태그
│   ├─ ④ 기능: description 본문 전체
│   ├─ ⑤ 새로운 기능: currentVersionReleaseDate + releaseNotes (버전별 히스토리와 연결)
│   ├─ ⑥ 홈페이지: sellerUrl/artistViewUrl 버튼
│   └─ ⑦ 다운로드 링크: trackViewUrl(App Store) + repoFullName(GitHub) 버튼
└─ section#stats: 카테고리별 신규수 / AI 비중 / 릴리즈 주기 / 스타 속도 (SVG 바닐라 차트)
폰(≤600px) 1열·버튼·칩 최소 44px 터치 영역
```

## 4. 웹 시각 규칙

- CSS 변수(--primary #0066FF 등), 카드 hover 상승 2px, 뱃지 pill 형태
- 라이선스 색: OSS=초록 / 프리=파랑 / 유료=주황
- 접근성: role=list/listitem, tablist, aria-label, 키보드 포커스 가능 버튼
- 빈 상태: "조건에 맞는 앱이 없습니다" + 초기화 유도

## 5. 화면 플로우

1. 앱 실행 → autoStart면 서버 자동 시작 → 알림 표시 → 홈에 주소 노출
2. 브라우저에서 주소 접속 → 타임라인 탐색 → 버전 diff 확인 → repo/스토어 새탭
3. 신규 앱·버전업 푸시 → 설정 수동 수집 또는 주기 대기
