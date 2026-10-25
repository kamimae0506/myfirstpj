# オートタップ（Android）

画面の指定した位置を、一定間隔で自動タップする Android アプリです。
ユーザー補助（AccessibilityService）の `dispatchGesture()` を使うので、root 化は不要で、他のアプリの上でもタップできます。

## 使い方

1. アプリを開き、タップ間隔（ミリ秒）を入力して「保存」
2. 「ユーザー補助設定を開く」→「オートタップ」をオン
3. 画面に赤い照準と黒いコントロールパネルが表示される
4. 照準をドラッグしてタップしたい場所へ移動（パネルは「≡」をドラッグで移動）
5. 「開始」で自動タップ開始、「停止」で停止
6. 使い終わったらユーザー補助設定でオフにすると表示が消えます

- 実行中、照準はタッチを素通りさせる（半透明になる）ので、照準の下にあるものがタップされます
- 最短間隔は 100 ミリ秒です

## APK の入手

GitHub に push すると GitHub Actions がビルドします。
リポジトリの **Actions** タブ → 最新の「Build APK」→ 下部の **Artifacts** から `autotap-debug-apk` をダウンロードし、中の `app-debug.apk` をスマホでインストールしてください。

Android Studio で開いて「Run」してもビルド・インストールできます。

### Android 13 以降で「制限付き設定」と表示される場合

ストア以外から入れたアプリは、ユーザー補助を最初はオンにできません。
設定 → アプリ → オートタップ → 右上の「︙」→「制限付き設定を許可」をしてから、もう一度オンにしてください。

## ファイル構成

| ファイル | 役割 |
| --- | --- |
| `app/src/main/java/com/example/autotap/AutoTapService.kt` | 照準・パネルの表示とタップ処理 |
| `app/src/main/java/com/example/autotap/MainActivity.kt` | 間隔の設定画面 |
| `app/src/main/java/com/example/autotap/TargetView.kt` | 照準の描画 |
| `app/src/main/java/com/example/autotap/Settings.kt` | 設定の保存 |
| `app/src/main/res/xml/accessibility_service_config.xml` | ジェスチャー送信の許可設定 |

## 注意

ゲームやサービスによっては自動操作が利用規約で禁止されています。利用先のルールを確認して使ってください。
