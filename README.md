# 🍎 맥줍줍 (MacJupJup)

갤럭시 S22를 **맥 앱 트렌드 수집 서버**로 삼는 단일 Android 앱.
macOS 앱(오픈소스/프리/유료)의 신규 출시·버전업·새 기능을 주기적으로 자동 수집해,
같은 네트워크의 맥 브라우저에서 **웹 포털**(타임라인·카테고리·버전 diff)로 보여줍니다.

- **패키지**: `com.borasarang.macjupjup` · 런처명: 맥줍줍
- **기본 포트**: 3000 (앱 설정에서 변경 가능)
- **포털**: `http://<S22 IP>:3000/`
- **형제 프로젝트**: [PlanJupJup](../PlanJupJup/) (알뜰요금줍줍, 동일 골격)

---

## ✨ 핵심 기능 (v1.0 목표)

| 기능 | 설명 |
|---|---|
| 🔄 자동 수집 | WorkManager 기반 3층 시드 (Setapp 월간 + 차트 24h + 발굴 6h) + 버전 폴링 24h |
| 🏷️ 3분류 | 오픈소스 / 프리 / 유료 + Setapp 10카테고리 + AI-Agent·MenuBar 태그 |
| 📈 버전 diff | 스토어·GitHub 버전 bump 감지 → 새 기능 요약 + 원문 링크 |
| 📊 트렌드 대시보드 | 카테고리별 신규 / AI 비중 / 릴리즈 주기 / 스타 속도 |
| 🔔 알림 센터 | 신규 앱·버전업 / 수집 완료·실패 알림 |

## 📚 문서

| 문서 | 위치 |
|---|---|
| 개발 계획서 | [`docs/plans/`](docs/plans/) |
| 작업 체크리스트 | [`docs/TODO.md`](docs/TODO.md) |
| UI/UX 설계 | [`docs/DESIGN.md`](docs/DESIGN.md) |
| API 명세 | [`docs/api/ENDPOINTS.md`](docs/api/ENDPOINTS.md) |
| 권한 사유서 | [`docs/PERMISSIONS.md`](docs/PERMISSIONS.md) |
| 변경 이력 | [`docs/CHANGELOG.md`](docs/CHANGELOG.md) |
| 사용자 메시지 | `error_message_ko.json` |

## 🗂️ 수집 소스 (v1.0)

| 소스 | 방식 | 주기 | 상태 |
|---|---|---|---|
| Setapp 카탈로그 | 공개 목록 월간 시드 + lookup 보완 (PAID 기본) | 720h | 활성 |
| Mac 차트 RSS | topfreemacapps (US) 진입·이탈 — 유료/매출 차트는 Apple 측 빈 배열 | 24h | 활성 |
| GitHub Search | topic:macos 윈도우 분할 | 6h | 활성 |
| GitHub Releases | ETag 버전 추적 | 24h | 활성 |
| Product Hunt | mac/developer-tools 토픽 + 키워드 필터 | 6h | 활성 |
| Show HN | mac 필터 + traction 점수 | 6h | 활성 |
| iTunes Lookup | 200 ID 묶음 버전 폴링 | 24h | 활성 |
| iTunes 이름 대조 | trackId 없는 앱 엄격 매칭 | 168h | 활성 |
| MacMenuBar | WordPress RSS (라이선스·스크린샷·방문링크) | 6h | 활성 |

## 🔨 빌드

요구: Android Studio 내장 JBR(JAVA_HOME 자동 설정), `~/Library/Android/sdk`.

```bash
./build_and_run.sh build          # assembleDebug + 디바이스 있으면 설치
./build_and_run.sh test           # 단위 테스트
./build_and_run.sh test full      # 단위 + connected 테스트
./build_and_run.sh lint           # Android Lint
./build_and_run.sh clean
```

## 🙏 만든 사람

[BoRaSaRang](https://github.com/BoraSarang) · 이메일: leeborasarang@gmail.com

> 이 앱은 공개 API·피드에서 수집한 맥 앱 정보를 탐색 목적으로 제공합니다.
> 가격·버전·기능 상세는 각 스토어·공식 채널에서 최종 확인하세요.
> 크랙·활성화툴 사이트는 수집하지 않습니다.
