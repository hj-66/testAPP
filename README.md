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