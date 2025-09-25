# 토나리넷 となりネット - 백엔드

[![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=fff)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)

**데모 사이트: https://tn.thxx.xyz**

## 소개

**토나리넷**은 유학생 및 외국인 노동자를 위한 통합 관리 및 지원 플랫폼입니다. 이 프로젝트는 SMART Cloud IT Master 47기 교육과정에서 진행된 팀 프로젝트(4bits)의 백엔드 서버입니다.

대학 및 기업 관리자는 이 플랫폼을 통해 유학생과 외국인 노동자를 효율적으로 관리할 수 있으며, 사용자는 현지 생활 적응에 필요한 다양한 정보와 커뮤니티 기능을 제공받을 수 있습니다.

## 기술 스택

- **언어:** Java 17
- **프레임워크:** Spring Boot 3.5.4
- **데이터베이스:** MySQL
- **인증:** Spring Security, JWT, OAuth 2.0 (Google, Kakao, LINE)
- **API:** REST API with Swagger (springdoc-openapi)
- **실시간 통신:** WebSocket (with STOMP)
- **AI:** Spring AI (OpenAI GPT-5-mini)
- **ORM:** Spring Data JPA (Hibernate)
- **메일:** Spring Boot Starter Mail (OCI Email Delivery)
- **빌드:** Gradle

## 시작하기

### 환경변수 설정

이 프로젝트를 실행하기 위해서는 `.env` 파일을 프로젝트 루트에 생성하고 아래의 환경변수들을 설정해야 합니다.

```
MYSQL_URL=jdbc:mysql://<host>:<port>/<database>
MYSQL_USER=<username>
MYSQL_PASSWORD=<password>

UPLOAD_PATH=<upload_directory_path>

LINE_API_CLIENT_ID=...
LINE_API_CLIENT_SECRET=...
LINE_API_REDIRECT_URI=...

GOOGLE_API_CLIENT_ID=...
GOOGLE_API_CLIENT_SECRET=...
GOOGLE_API_REDIRECT_URI=...

KAKAO_CLIENT_ID=...
KAKAO_REDIRECT_URI=...
KAKAO_CLIENT_SECRET=...

JWT_SECRET_KEY=...

SPRING_MAIL_HOST=...
SPRING_MAIL_PORT=...
SPRING_MAIL_USERNAME=...
SPRING_MAIL_PASSWORD=...

OPENAI_API_KEY=...
GOOGLE_TRANS_API_KEY=...

SWAGGER_AUTH_USERNAME=...
SWAGGER_AUTH_PASSWORD=...
```

### 실행

프로젝트를 빌드하고 실행하는 명령어는 다음과 같습니다.

```bash
./gradlew bootRun
```

API 문서는 서버 실행 후 `http://localhost:8999/swagger-ui/index.html`에서 확인할 수 있습니다.

## 기술적 특징

- **REST API 및 Swagger 문서:** Springdoc-openapi를 활용하여 API를 명세하고 테스트할 수 있는 Swagger UI를 제공합니다.
- **인증 및 인가:** Spring Security와 JWT를 결합하여 Stateless한 인증/인가 시스템을 구축했습니다. 또한 OAuth 2.0을 통해 Google, Kakao, LINE 소셜 로그인을 지원합니다.
- **실시간 채팅:** WebSocket과 STOMP 프로토콜을 사용하여 사용자와 관리자 간의 실시간 채팅 기능을 구현했습니다.
- **AI 연동:** Spring AI를 통해 OpenAI의 언어 모델을 연동하여 AI 챗봇, 과제 추천 등 다양한 지능형 서비스를 제공합니다.
- **데이터베이스 관리:** Spring Data JPA와 JPQL을 사용하여 효율적으로 데이터를 관리하고 조작합니다.
- **이메일 서비스:** OCI (Oracle Cloud Infrastructure)의 Email Delivery 서비스를 연동하여 회원가입 인증, 비밀번호 재설정 등의 이메일을 발송합니다.



## 라이센스

이 프로젝트는 [GNU LGPLv3](LICENSE.md) 라이센스를 따릅니다.