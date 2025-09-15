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
