# kdt-shop

데브코스 부트캠프 Spring 쇼핑몰 실습 프로젝트입니다.

## 로컬 설정

`src/main/resources/application-example.yaml`을 `application.yaml`로 복사하고 로컬 DB 정보를 입력합니다. 실제 설정 파일과 로그는 Git에서 제외됩니다.

## 커밋

```bash
git add .
git commit -m "update"
git push
```

현재 PC에서는 커밋·푸시 시 Secret check가 자동 실행됩니다. 비밀번호·API 키는 로컬 설정이나 환경변수로 관리하세요. 자동 검사는 모든 개인정보나 비밀정보를 탐지한다고 보장하지 않습니다.

새로 복제한 환경에서는 Python 3와 Gitleaks를 설치하고 `sh scripts/install-hooks.sh`를 실행하세요. Git 훅은 복제만으로 활성화되지 않습니다.
