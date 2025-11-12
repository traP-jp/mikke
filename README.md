# Mikke

友達特定 SNS

2025 冬ハッカソン イブ

## はじめに

このリポジトリでは開発体験を高めるために Git Hooks を利用しています
**必ず最初に 1 度だけ、以下の設定を行ってください**

### Git Hooks の初期設定

以下のコマンドを実行します

```bash
# Git Hooks を有効化
git config core.hooksPath .githooks

# フックの有効/無効を切り替える設定ファイルを作成
cp .githooks/example.config.env .githooks/config.env
```

`.githooks/config.env` を開いて、必要なフックを有効化します

```sh
export ENABLE_CLIENT_HOOKS=true # クライアントのHOOKSを有効化
export ENABLE_SERVER_HOOKS=true # サーバーのHOOKSを有効化
```

(Git Hooks の詳細は[CONTRIBUTING.md](.github/CONTRIBUTING.md#git-hooks)へ)

## プロジェクト構成

```
├── client/            # フロントエンド (React + Vite + TypeScript)
├── server/            # バックエンド (Kotlin + Ktor)
├── openapi/           # 手書き OpenAPI 定義 (サーバ生成 & API ドキュメント用)
├── .githooks/         # Git Hooks (lint / ktlint など)
├── .run/              # IntelliJ IDEA 用 Run Configuration
├── Dockerfile.server  # サーバ本番/汎用ビルド
```

## 開発環境の構築

### Client

クライアントは React + Vite で構築されています

#### 依存関係のインストール

```bash
cd client
npm ci
```

#### 開発サーバーの起動

```bash
npm run dev
```

### Server

サーバーは Kotlin + Gradle（Ktor, Koin, Exposed など）で構築されています

開発には Intellij IDEA を使用します

Intellij IDEA の起動構成から各種操作ができます

#### Dev Server

- 開発サーバーの起動

#### Dev Database

- 開発用データベースの起動 (Docker が必要です)

#### Build

- サーバーの Jar ファイルをビルド

#### Format

- ソースコードの自動整形

#### Generate Code

- openapi-generator や koin によるソースコードの生成
