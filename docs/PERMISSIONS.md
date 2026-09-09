# PERMISSIONS.md — MacJupJup 권한 사용 사유서

> 패키지: `com.borasarang.macjupjup`

| 권한 | 용도 | 근거 |
|---|---|---|
| INTERNET | 수집 API 호출(GitHub/iTunes/PH/HN) + 내장 서버 소켓 | 핵심 기능 |
| ACCESS_NETWORK_STATE | WorkManager 네트워크 제약 + 연결 상태 확인 | 수집은 연결 시에만 실행 |
| FOREGROUND_SERVICE | 포그라운드 서비스 실행 | 서버 상시 구동 |
| FOREGROUND_SERVICE_DATA_SYNC | FGS 타입(dataSync) 선언 (Android 14+ 필수) | 주기 수집+서버가 데이터 동기화 성격 |
| POST_NOTIFICATIONS | 서버 실행 알림 + 수집 상태 알림 (Android 13+ 런타임) | FGS 알림 의무 |
| WAKE_LOCK | 수집·Watchdog 중 절전 방지(최소 보유) | 장시간 작업 안정성 |
| RECEIVE_BOOT_COMPLETED | 부팅 시 autoStart 설정이면 서버 자동 시작 | 무중단 운영 |
| REQUEST_IGNORE_BATTERY_OPTIMIZATIONS | 배터리 최적화 예외 **요청 인텐트용** (선언만, 자동 부여 아님) | Doze에서 WorkManager·FGS 제한 완화. 설정 화면에서 사용자가 직접 허용 |

## 수집 예의 (권한 외 준수 사항)

- 공식 API 우선, 요청 간격 1초 이상, User-Agent 명시, robots.txt 확인
- GitHub 토큰은 사용자 입력→DataStore 저장, 커밋·로그 금지(마스킹)
- 크랙·활성화툴 사이트는 수집 대상에서 제외
- 릴리즈노트 전문 복제 금지 — 요약 + 원문 링크만 저장

## 주의

- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`는 Play 정책상 남용 금지 — 본 앱은
  사용자 개입형 로컬 서버(화면 꺼짐 상태 장시간 동작)가 필요하므로 요청 사유가 명확함.
  설정 화면에 상태 표시 + 설명문 + 해제 경로를 함께 제공해 정책 준수.
- 위치·저장소·전화 권한은 사용하지 않음(요청 금지).
