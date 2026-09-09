# AGENTS.local.md — MacJupJup 프로젝트 특화 규칙

> 공통 가이드는 `~/.config/opencode/AGENTS.md` 참조. 여기에는 변경 항목만 기록.

- **적용 플랫폼 확정**: Android 단일 (`com.borasarang.macjupjup`). 추적 대상은 macOS 앱이지만 빌드·테스트는 Android만.
- **phisical 디바이스**: Galaxy S22 실기 E2E (사용자 사용 중이면 headless·smoke/unit만, full은 사전 확인).
- **수집 예의 고정**: 공식 API 우선, 요청 간격 1초+, UA 명시, robots.txt 확인.
  크랙·활성화툴 사이트 수집 금지. 릴리즈노트 전문 복제 금지(요약+링크만).
- **시크릿**: GitHub 토큰은 설정 화면 입력→DataStore만. 커밋·로그 금지(마스킹).
- **버전 고정**: `gradle/libs.versions.toml` 단일 진실 (AGP 9.3.1·Ktor 3.5.2·Room 2.7.0·Work 2.9.0).
- **빌드**: `./build_and_run.sh` 경유만. JBR 자동 JAVA_HOME (`swift build` 해당 없음).
