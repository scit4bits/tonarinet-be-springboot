<details open>
<summary>한국어</summary>

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

</details>

<details>
<summary>English</summary>

# Tonarinet となりネット - Backend

[![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=fff)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)

**Demo Site: https://tn.thxx.xyz**

## Introduction

**Tonarinet** is an integrated management and support platform for international students and foreign workers. This project is the backend server for a team project (4bits) conducted in the SMART Cloud IT Master 47th course.

University and company administrators can efficiently manage international students and foreign workers through this platform, and users are provided with various information and community functions necessary for adapting to local life.

## Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.5.4
- **Database:** MySQL
- **Authentication:** Spring Security, JWT, OAuth 2.0 (Google, Kakao, LINE)
- **API:** REST API with Swagger (springdoc-openapi)
- **Real-time Communication:** WebSocket (with STOMP)
- **AI:** Spring AI (OpenAI GPT-5-mini)
- **ORM:** Spring Data JPA (Hibernate)
- **Mail:** Spring Boot Starter Mail (OCI Email Delivery)
- **Build:** Gradle

## Getting Started

### Environment Variables

To run this project, you need to create a `.env` file in the project root and set the following environment variables.

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

### Running the application

The command to build and run the project is as follows:

```bash
./gradlew bootRun
```

API documentation can be found at `http://localhost:8999/swagger-ui/index.html` after the server starts.

## Technical Features

- **REST API & Swagger Documentation:** Provides Swagger UI for API specification and testing using springdoc-openapi.
- **Authentication & Authorization:** Built a stateless authentication/authorization system by combining Spring Security and JWT. It also supports social logins for Google, Kakao, and LINE through OAuth 2.0.
- **Real-time Chat:** Implemented real-time chat functionality between users and administrators using WebSocket and STOMP protocol.
- **AI Integration:** Provides various intelligent services such as AI chatbot and task recommendations by integrating OpenAI's language model through Spring AI.
- **Database Management:** Efficiently manages and manipulates data using Spring Data JPA and JPQL.
- **Email Service:** Sends emails for membership authentication, password reset, etc. by linking with OCI (Oracle Cloud Infrastructure)'s Email Delivery service.



## License

This project is licensed under the [GNU LGPLv3](LICENSE.md) License.

</details>

<details>
<summary>日本語</summary>

# となりネット - バックエンド

[![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=fff)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)

**デモサイト: https://tn.thxx.xyz**

## 紹介

**となりネット**は、留学生や外国人労働者のための統合管理・支援プラットフォームです。このプロジェクトは、SMART Cloud IT Master 第47期のチームプロジェクト(4bits)のバックエンドサーバーです。

大学や企業の管理者は、このプラットフォームを通じて留学生や外国人労働者を効率的に管理でき、ユーザーは現地生活への適応に必要な様々な情報やコミュニティ機能を提供されます。

## 技術スタック

- **言語:** Java 17
- **フレームワーク:** Spring Boot 3.5.4
- **データベース:** MySQL
- **認証:** Spring Security, JWT, OAuth 2.0 (Google, Kakao, LINE)
- **API:** REST API with Swagger (springdoc-openapi)
- **リアルタイム通信:** WebSocket (with STOMP)
- **AI:** Spring AI (OpenAI GPT-5-mini)
- **ORM:** Spring Data JPA (Hibernate)
- **メール:** Spring Boot Starter Mail (OCI Email Delivery)
- **ビルド:** Gradle

##始め方

### 環境変数

このプロジェクトを実行するには、プロジェクトのルートに`.env`ファイルを作成し、次の環境変数を設定する必要があります。

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

### アプリケーションの実行

プロジェクトをビルドして実行するコマンドは次のとおりです。

```bash
./gradlew bootRun
```

APIドキュメントは、サーバーの起動後に`http://localhost:8999/swagger-ui/index.html`で確認できます。

## 技術的な特徴

- **REST APIとSwaggerドキュментаATION:** springdoc-openapiを使用してAPIの仕様とテストを行うためのSwagger UIを提供します。
- **認証と認可:** Spring SecurityとJWTを組み合わせて、ステートレスな認証/認可システムを構築しました。また、OAuth 2.0を介してGoogle、Kakao、LINEのソーシャルログインをサポートします。
- **リアルタイムチャット:** WebSocketとSTOMPプロトコルを使用して、ユーザーと管理者間のリアルタイムチャット機能を実装しました。
- **AI統合:** Spring AIを介してOpenAIの言語モデルを統合し、AIチャットボットやタスクの推奨など、さまざまなインテリジェントサービスを提供します。
- **データベース管理:** Spring Data JPAとJPQLを使用して、データを効率的に管理および操作します。
- **メールサービス:** OCI(Oracle Cloud Infrastructure)のEメール配信サービスと連携して、会員登録認証やパスワードリセットなどのメールを送信します。



## ライセンス

このプロジェクトは、[GNU LGPLv3](LICENSE.md)ライセンスの下でライセンスされています。

</details>