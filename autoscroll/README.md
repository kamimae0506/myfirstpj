# TikTok Lite オートスクロール

ADB 経由で Android 端末を一定間隔で上スワイプし、TikTok Lite の動画を自動で次へ送るスクリプトです。

## 準備

1. PC に [Android SDK Platform-Tools](https://developer.android.com/tools/releases/platform-tools)（`adb`）と Python 3 をインストール
2. 端末で「開発者向けオプション」→「USB デバッグ」を ON
3. USB で接続し、`adb devices` で端末が `device` と表示されることを確認

## 実行

```sh
python autoscroll.py --launch            # TikTok Lite を起動して 15 秒ごとにスワイプ
python autoscroll.py --interval 30       # 30 秒ごと
python autoscroll.py --count 50          # 50 回で終了
python autoscroll.py --serial XXXX       # 複数端末接続時
```

Ctrl+C で停止します。

## 注意

自動操作は TikTok の利用規約に反する可能性があります。特にリワード（ポイント）目的での自動視聴は不正とみなされ、
ポイント没収やアカウント停止の対象になることがあります。自己責任で使ってください。
