<div align="center">
  <a href="https://github.com/scit4bits/tonarinet-be-springboot">
    <img src="https://raw.githubusercontent.com/s7s71/gemini-springboot-linter/main/assets/logo.png" alt="Logo" width="80" height="80">
  </a>

  <h3 align="center">Tonarinet Backend Server</h3>

  <p align="center">
    Backend server for the Tonarinet project, a community-based service platform.
    <br />
    <a href="https://github.com/scit4bits/tonarinet-be-springboot/issues">Report Bug</a>
    ·
    <a href="https://github.com/scit4bits/tonarinet-be-springboot/issues">Request Feature</a>
  </p>

<p align="center">
    <a href="./README.en.md">English</a>
    ·
    <a href="./README.ko.md">Korean</a>
    ·
    <a href="./README.ja.md">Japanese</a>
</p>
</div>

<details>
<summary>🏴󠁧󠁢󠁥󠁮󠁧󠁿 English</summary>

# Tonarinet Backend Server

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Description

This is the backend server for the Tonarinet project, built with Spring Boot. It provides the core functionalities for a community-based service platform, including user management, content sharing, real-time communication, and more.

## Features

-   **User Authentication:** Secure sign-up and sign-in using JWT (JSON Web Tokens) and OAuth2.
-   **RESTful API:** A comprehensive set of APIs for managing articles, comments, users, and other resources.
-   **Real-time Chat:** WebSocket-based real-time chat functionality.
-   **Content Management:** Features for creating, reading, updating, and deleting articles and posts.
-   **Task & Party Management:** Functionality for organizing and participating in tasks and events.
-   **AI Integration:** Utilizes AI for features like content recommendation.
-   **API Documentation:** API documentation generated with Swagger.

## Installation

### Prerequisites

-   Java 17
-   Gradle 8.x
-   MySQL

### Setup

1.  **Clone the repository**
    ```bash
    git clone https://github.com/scit4bits/tonarinet-be-springboot.git
    cd tonarinet-be-springboot
    ```

2.  **Database Setup**
    -   Create a MySQL database.
    -   Execute the `sql/tonarinet_db.sql` script to create the necessary tables.
    ```bash
    mysql -u [your_username] -p [your_database_name] < sql/tonarinet_db.sql
    ```

3.  **Configure Application**
    -   Open `src/main/resources/application.properties`.
    -   Update the database connection details (`spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`).
    -   Configure your JWT secret key, OAuth2 credentials, and any other external service settings.

## Usage

### Running the Application

You can run the application using the Gradle wrapper:

```bash
./gradlew bootRun
```

The server will start on `http://localhost:8080`.

### API Documentation

Once the application is running, you can access the Swagger UI for API documentation at:
`http://localhost:8080/swagger-ui.html`

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m '''Add some AmazingFeature'''`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## License

This project is licensed under the MIT License. See the `LICENSE` file for more information.

## Contact

Project Link: [https://github.com/scit4bits/tonarinet-be-springboot](https://github.com/scit4bits/tonarinet-be-springboot)

</details>

<details>
<summary>🇰🇷 한국어</summary>

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

</details>

<details>
<summary>🇯🇵 日本語</summary>

# Tonarinet バックエンドサーバー

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 説明

これはSpring Bootで構築されたTonarinetプロジェクトのバックエンドサーバーです。ユーザー管理、コンテンツ共有、リアルタイム通信など、コミュニティベースのサービスプラットフォームのコア機能を提供します。

## 主な機能

-   **ユーザー認証:** JWT (JSON Web Tokens) および OAuth2 を使用した安全なサインアップとサインイン。
-   **RESTful API:** 記事、コメント、ユーザー、その他のリソースを管理するための包括的なAPIセット。
-   **リアルタイムチャット:** WebSocketベースのリアルタイムチャット機能。
-   **コンテンツ管理:** 記事や投稿の作成、読み取り、更新、削除機能。
-   **タスク＆パーティー管理:** タスクやイベントの整理と参加機能。
-   **AI統合:** コンテンツ推薦などの機能にAIを活用。
-   **APIドキュментация:** Swaggerで生成されたAPIドキュメント。

## インストール

### 前提条件

-   Java 17
-   Gradle 8.x
-   MySQL

### セットアップ

1.  **リポジトリをクローン**
    ```bash
    git clone https://github.com/scit4bits/tonarinet-be-springboot.git
    cd tonarinet-be-springboot
    ```

2.  **データベースのセットアップ**
    -   MySQLデータベースを作成します。
    -   `sql/tonarinet_db.sql` スクリプトを実行して、必要なテーブルを作成します。
    ```bash
    mysql -u [ユーザー名] -p [データベース名] < sql/tonarinet_db.sql
    ```

3.  **アプリケーションの設定**
    -   `src/main/resources/application.properties` を開きます。
    -   データベース接続詳細 (`spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`) を更新します。
    -   JWTシークレットキー、OAuth2認証情報、およびその他の外部サービス設定を構成します。

## 使用法

### アプリケーションの実行

Gradleラッパーを使用してアプリケーションを実行できます。

```bash
./gradlew bootRun
```

サーバーは `http://localhost:8080` で起動します。

### APIドキュメント

アプリケーションが実行されたら、次のアドレスでAPIドキュメント用のSwagger UIにアクセスできます。
`http://localhost:8080/swagger-ui.html`

## 貢献

貢献は、オープンソースコミュニティを学び、ひらめき、創造するための素晴らしい場所にするものです。皆様の貢献を**心よりお待ちしております**。

1.  プロジェクトをフォークする
2.  機能ブランチを作成する (`git checkout -b feature/AmazingFeature`)
3.  変更をコミットする (`git commit -m '''Add some AmazingFeature'''`)
4.  ブランチにプッシュする (`git push origin feature/AmazingFeature`)
5.  プルリクエストを開く

## ライセンス

このプロジェクトはMITライセンスの下でライセンスされています。詳細については `LICENSE` ファイルを参照してください。

## 連絡先

プロジェクトリンク: [https://github.com/scit4bits/tonarinet-be-springboot](https://github.com/scit4bits/tonarinet-be-springboot)

</details>