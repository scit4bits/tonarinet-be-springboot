# Tonarinet 백엔드 서버

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 설명

이것은 Spring Boot로 구축된 Tonarinet 프로젝트의 백엔드 서버입니다. 사용자 관리, 콘텐츠 공유, 실시간 통신 등 커뮤니티 기반 서비스 플랫폼의 핵심 기능을 제공합니다.

## 주요 기능

-   **사용자 인증:** JWT (JSON Web Tokens) 및 OAuth2를 사용한 안전한 회원가입 및 로그인.
-   **RESTful API:** 게시글, 댓글, 사용자 및 기타 리소스를 관리하기 위한 포괄적인 API 세트.
-   **실시간 채팅:** WebSocket 기반의 실시간 채팅 기능.
-   **콘텐츠 관리:** 게시글 및 포스트의 생성, 조회, 수정, 삭제 기능.
-   **태스크 & 파티 관리:** 태스크 및 이벤트 구성 및 참여 기능.
-   **AI 통합:** 콘텐츠 추천과 같은 기능에 AI 활용.
-   **API 문서화:** Swagger를 통해 생성된 API 문서.

## 설치

### 요구 사항

-   Java 17
-   Gradle 8.x
-   MySQL

### 설정

1.  **리포지토리 클론**
    ```bash
    git clone https://github.com/scit4bits/tonarinet-be-springboot.git
    cd tonarinet-be-springboot
    ```

2.  **데이터베이스 설정**
    -   MySQL 데이터베이스를 생성합니다.
    -   `sql/tonarinet_db.sql` 스크립트를 실행하여 필요한 테이블을 생성합니다.
    ```bash
    mysql -u [사용자명] -p [데이터베이스명] < sql/tonarinet_db.sql
    ```

3.  **애플리케이션 구성**
    -   `src/main/resources/application.properties` 파일을 엽니다.
    -   데이터베이스 연결 정보(`spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`)를 업데이트합니다.
    -   JWT 비밀 키, OAuth2 자격 증명 및 기타 외부 서비스 설정을 구성합니다.

## 사용법

### 애플리케이션 실행

Gradle 래퍼를 사용하여 애플리케이션을 실행할 수 있습니다.

```bash
./gradlew bootRun
```

서버는 `http://localhost:8080`에서 시작됩니다.

### API 문서

애플리케이션이 실행되면 다음 주소에서 API 문서를 위한 Swagger UI에 액세스할 수 있습니다.
`http://localhost:8080/swagger-ui.html`

## 기여

기여는 오픈 소스 커뮤니티를 배우고, 영감을 주고, 창조하는 놀라운 공간으로 만듭니다. 여러분의 모든 기여는 **매우 감사합니다**.

1.  프로젝트 포크
2.  기능 브랜치 생성 (`git checkout -b feature/AmazingFeature`)
3.  변경 사항 커밋 (`git commit -m '''Add some AmazingFeature'''`)
4.  브랜치에 푸시 (`git push origin feature/AmazingFeature`)
5.  풀 리퀘스트 열기

## 라이선스

이 프로젝트는 MIT 라이선스에 따라 라이선스가 부여됩니다. 자세한 내용은 `LICENSE` 파일을 참조하십시오.

## 연락처

프로젝트 링크: [https://github.com/scit4bits/tonarinet-be-springboot](https://github.com/scit4bits/tonarinet-be-springboot)
