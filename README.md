# GitHub 새 프로젝트 연결 및 배포 정리

## 1. GitHub 새로운 프로젝트 연결하기

### 1-1. GitHub에서 새 Repository 생성

GitHub에서 새로운 Repository를 생성합니다.

예시:

```text
memo-app
```

---

### 1-2. 작업 공간 불러오기

로컬에서 작업 중인 프로젝트 폴더를 엽니다.

터미널을 프로젝트 최상위 폴더에서 실행합니다.

---

### 1-3. 기존 GitHub 주소 제거하기

기존에 연결된 GitHub 원격 저장소가 있다면 제거합니다.

```bash
git remote remove origin
```

현재 연결된 원격 저장소를 확인하려면 다음 명령어를 사용합니다.

```bash
git remote -v
```

---

### 1-4. Git 초기화하기

프로젝트 폴더에서 Git을 초기화합니다.

```bash
git init
```

---

### 1-5. 새로운 GitHub Repository 연결하기

새로 만든 GitHub Repository 주소를 연결합니다.

```bash
git remote add origin 새로운_프로젝트_주소
```

예시:

```bash
git remote add origin https://github.com/사용자명/프로젝트명.git
```

---

## 2. 프로젝트 커밋하기

### 2-1. 프로젝트 기본 세팅 후 변경사항 추가

```bash
git add .
```

---

### 2-2. 커밋하기

```bash
git commit -m "세팅"
```

또는 커밋 메시지를 영어 컨벤션으로 작성할 수도 있습니다.

```bash
git commit -m "chore: initial setup"
```

---

### 2-3. 브랜치 이름을 main으로 변경

```bash
git branch -M main
```

---

### 2-4. GitHub에 푸시하기

```bash
git push origin main
```

처음 푸시할 때는 아래 명령어를 사용해도 좋습니다.

```bash
git push -u origin main
```

---

## 3. Compose Desktop 앱 기준 macOS 배포 파일 만들기

### 3-1. 프로젝트 위치에서 터미널 실행

현재 프로젝트 폴더에서 터미널을 실행합니다.

---

### 3-2. 배포 파일 생성 명령어 실행

```bash
./gradlew packageDistributionForCurrentOS
```

---

### 3-3. 배포 파일 생성 위치

명령어 실행 후 다음 경로에 배포 파일이 생성됩니다.

```text
build/compose/binaries/main/dmg
```

macOS 기준으로 `.dmg` 설치 파일이 생성됩니다.

---

## 4. GitHub Release로 배포하기

## 4-1. 버전 태그 만들기

예를 들어 `v0.1.0` 버전을 배포한다고 하면 다음 명령어를 입력합니다.

```bash
git tag v0.1.0
```

---

### 4-2. 태그를 GitHub에 푸시하기

```bash
git push origin v0.1.0
```

---

### 4-3. GitHub Release 생성하기

GitHub Repository로 이동한 뒤 다음 순서대로 진행합니다.

1. Repository 메인 화면으로 이동
2. 오른쪽 또는 상단의 `Releases` 클릭
3. `Draft a new release` 클릭
4. Tag 선택: `v0.1.0`
5. Release title 입력: `v0.1.0`
6. 설명 작성
7. 배포 파일 업로드
8. Release 생성

---

## 5. 사용자가 다운로드하는 방법

사용자는 GitHub Repository에서 다음 경로로 이동하여 설치 파일을 다운로드할 수 있습니다.

```text
Release → Latest → Assets
```

`Assets` 영역에서 `.dmg` 파일을 다운로드하면 됩니다.

---

## 6. 버전 관리 형식

버전은 보통 다음 형식을 사용합니다.

```text
MAJOR.MINOR.PATCH
```

예시:

```text
0.1.0
1.0.0
1.2.3
```

---

## 7. 버전 번호 의미

| 구분 | 의미 | 예시 |
|---|---|---|
| MAJOR | 정식 배포, 큰 구조 변경, 호환성이 깨지는 변경 | `1.0.0` |
| MINOR | 작은 기능 추가, 기존 기능과 호환 가능 | `1.2.0` |
| PATCH | 버그 수정, 작은 개선 | `1.2.3` |

---

## 8. Gradle 버전과 Git 태그 맞추기

Git 태그와 Gradle의 앱 버전을 맞춰 관리하는 것이 좋습니다.

예를 들어 Git 태그가 `v1.0.0`이라면 Gradle 설정도 `1.0.0`으로 맞춥니다.

---

### 8-1. build.gradle.kts 수정 예시

```kotlin
nativeDistributions {
    targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
    packageName = "Memo_App_MacOS"
    packageVersion = "1.0.0"
}
```

---

### 8-2. 프로젝트 버전 설정 예시

```kotlin
version = "1.0.0"
```

---

## 9. CHANGELOG.md 관리하기

프로젝트 루트 경로에 `CHANGELOG.md` 파일을 만들어 버전별 변경사항을 기록합니다.

---

### 9-1. CHANGELOG.md 예시

```markdown
# Changelog

## [0.2.0] - 2026-04-30

### Added

- 메모 검색 기능 추가
- 즐겨찾기 기능 추가

### Changed

- 메모 목록 UI 개선

### Fixed

- 앱 종료 후 데이터가 저장되지 않던 문제 수정

## [0.1.0] - 2026-04-20

### Added

- 첫 번째 배포
- 메모 작성 기능
- 메모 삭제 기능
```

---

# 10. 실제 배포 루틴

매번 배포할 때는 아래 순서로 진행하면 됩니다.

예시로 `0.2.0` 버전을 배포한다고 가정합니다.

---

## 10-1. 코드 수정 완료 후 상태 확인

```bash
git status
```

변경된 파일이 있는지 확인합니다.

---

## 10-2. 버전 수정

`build.gradle.kts`에서 프로젝트 버전을 수정합니다.

```kotlin
version = "0.2.0"
```

Compose Desktop 앱이라면 `nativeDistributions` 안의 `packageVersion`도 수정합니다.

```kotlin
nativeDistributions {
    packageVersion = "0.2.0"
}
```

---

## 10-3. CHANGELOG.md 수정

```markdown
## [0.2.0] - 2026-04-30

### Added

- 검색 기능 추가

### Fixed

- 저장 오류 수정
```

---

## 10-4. 테스트와 빌드

일반 빌드 테스트를 먼저 실행합니다.

```bash
./gradlew clean build
```

Compose Desktop 배포 파일을 만들 경우 다음 명령어를 실행합니다.

```bash
./gradlew clean packageDistributionForCurrentOS
```

일반 JVM 앱이라면 다음 명령어를 사용할 수 있습니다.

```bash
./gradlew clean distZip
```

---

## 10-5. 커밋하기

```bash
git add .
git commit -m "chore: release v0.2.0"
```

---

## 10-6. 태그 생성하기

```bash
git tag v0.2.0
```

---

## 10-7. GitHub에 푸시하기

```bash
git push origin main
git push origin v0.2.0
```

---

## 10-8. GitHub Release 생성하기

GitHub에서 `v0.2.0` Release를 만들고 빌드 파일을 업로드합니다.

업로드할 파일 위치 예시:

```text
build/compose/binaries/main/dmg
```

---

# 11. 전체 배포 명령어 요약

```bash
git status

# 버전 수정 후
./gradlew clean build
./gradlew clean packageDistributionForCurrentOS

git add .
git commit -m "chore: release v0.2.0"

git tag v0.2.0

git push origin main
git push origin v0.2.0
```

---

# 12. 최종 정리

GitHub 배포 흐름은 다음과 같습니다.

```text
GitHub Repository 생성
→ 로컬 프로젝트 Git 초기화
→ 원격 저장소 연결
→ 커밋
→ GitHub에 푸시
→ Gradle로 배포 파일 생성
→ Git 태그 생성
→ GitHub Release 생성
→ 배포 파일 업로드
```

---

# 13. GitHub Actions로 자동 빌드하기

나중에는 GitHub Actions를 이용해서 `push`나 `pull request`가 발생할 때 자동으로 빌드와 테스트를 실행할 수 있습니다.

GitHub Actions를 사용하면 다음과 같은 작업을 자동화할 수 있습니다.

- 코드가 GitHub에 올라올 때 자동 빌드
- 테스트 자동 실행
- 빌드 실패 여부 확인
- 실행 결과 파일을 artifact로 업로드
- 배포 전 코드 상태 검증

---

## 13-1. GitHub Actions 워크플로 파일 생성

프로젝트 루트 경로에 다음 폴더와 파일을 생성합니다.

```text
.github/workflows/ci.yml
```

폴더 구조는 다음과 같습니다.

```text
프로젝트/
├── .github/
│   └── workflows/
│       └── ci.yml
├── build.gradle.kts
├── gradlew
└── src/
```

---

## 13-2. ci.yml 작성하기

`ci.yml` 파일에 아래 내용을 작성합니다.

```yaml
name: CI

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout source code
        uses: actions/checkout@v5

      - name: Set up JDK
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 21

      - name: Grant execute permission for Gradle
        run: chmod +x gradlew

      - name: Build with Gradle
        run: ./gradlew clean build
```

---

## 13-3. 동작 방식

위 설정을 추가하면 다음 상황에서 자동으로 빌드가 실행됩니다.

```text
main 브랜치에 push 할 때
main 브랜치로 pull request를 만들 때
```

즉, GitHub에 코드를 올릴 때마다 프로젝트가 정상적으로 빌드되는지 자동으로 확인할 수 있습니다.

---

## 13-4. GitHub Actions 실행 결과 확인 방법

GitHub Repository에서 다음 경로로 이동합니다.

```text
Repository → Actions
```

여기에서 실행된 워크플로 결과를 확인할 수 있습니다.

빌드가 성공하면 초록색 체크 표시가 나타나고, 실패하면 빨간색 X 표시가 나타납니다.

실패한 경우에는 로그를 확인해서 어떤 단계에서 문제가 발생했는지 확인할 수 있습니다.

---

# 14. 배포 전 체크리스트

배포 전에 아래 항목을 항상 확인합니다.

```markdown
- [ ] 앱이 정상 실행되는가?
- [ ] `./gradlew clean build`가 성공하는가?
- [ ] 앱 버전을 올렸는가?
- [ ] `CHANGELOG.md`를 수정했는가?
- [ ] `README.md`의 다운로드 설명이 맞는가?
- [ ] 불필요한 `build/`, `.gradle/`, `.idea/` 폴더가 올라가지 않았는가?
- [ ] API Key, 비밀번호, 토큰 같은 비밀 정보가 GitHub에 올라가지 않았는가?
- [ ] Git tag를 만들었는가?
- [ ] GitHub Release에 실행 파일을 첨부했는가?
```

---

## 14-1. 반드시 올리면 안 되는 파일과 정보

GitHub에는 다음과 같은 정보가 올라가면 안 됩니다.

```text
API Key
DB 비밀번호
Access Token
Refresh Token
개인 인증서
.env 파일
local.properties
keystore 파일
비공개 서버 주소
```

특히 API Key, DB 비밀번호, 토큰은 절대 GitHub에 올리면 안 됩니다.

---

## 14-2. .gitignore 확인하기

불필요한 파일이 GitHub에 올라가지 않도록 `.gitignore`를 확인합니다.

예시:

```gitignore
# Gradle
.gradle/
build/

# IntelliJ
.idea/
*.iml

# macOS
.DS_Store

# 환경 변수
.env
.env.local

# Kotlin / Local config
local.properties

# 인증 관련 파일
*.jks
*.keystore
```

---

## 14-3. 실수로 비밀 정보를 올렸을 때

만약 API Key, 비밀번호, 토큰을 실수로 GitHub에 올렸다면 단순히 파일을 삭제하고 다시 커밋하는 것만으로는 부족합니다.

Git 기록에 남아 있을 수 있기 때문입니다.

이 경우에는 다음 조치를 해야 합니다.

1. 노출된 API Key나 토큰을 즉시 폐기
2. 새 API Key나 토큰 발급
3. GitHub Repository에서 해당 파일 삭제
4. 필요하다면 Git 기록에서 민감 정보 제거
5. `.gitignore`에 해당 파일 추가

---

# 15. 최종 배포 흐름 정리

전체 배포 흐름은 다음과 같습니다.

```text
코드 수정
→ 앱 실행 확인
→ ./gradlew clean build 실행
→ 버전 수정
→ CHANGELOG.md 수정
→ git add .
→ git commit
→ git tag 생성
→ GitHub에 push
→ GitHub Actions 빌드 확인
→ 배포 파일 생성
→ GitHub Release 생성
→ 실행 파일 업로드
```

---

# 16. 최종 명령어 예시

예를 들어 `v0.2.0` 버전을 배포한다면 다음 순서로 진행합니다.

```bash
git status

./gradlew clean build
./gradlew clean packageDistributionForCurrentOS

git add .
git commit -m "chore: release v0.2.0"

git tag v0.2.0

git push origin main
git push origin v0.2.0
```

GitHub에 푸시한 뒤에는 반드시 `Actions` 탭에서 빌드가 성공했는지 확인합니다.

그 다음 `Releases`에서 새 Release를 만들고 배포 파일을 업로드합니다.