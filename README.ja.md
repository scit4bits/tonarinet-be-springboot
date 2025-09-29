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