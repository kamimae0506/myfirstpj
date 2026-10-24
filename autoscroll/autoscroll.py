#!/usr/bin/env python3
"""ADB で Android 端末の画面を一定間隔で上スワイプし、次の動画へ進める。

使い方:
    python autoscroll.py                 # 15 秒ごとにスワイプ
    python autoscroll.py --interval 30   # 30 秒ごと
    python autoscroll.py --count 20      # 20 回で終了
Ctrl+C で停止。
"""
import argparse
import re
import shutil
import subprocess
import sys
import time

PACKAGE = "com.zhiliaoapp.musically.go"  # TikTok Lite


def adb(*args, serial=None):
    cmd = ["adb"] + (["-s", serial] if serial else []) + list(args)
    return subprocess.run(cmd, capture_output=True, text=True, check=True).stdout


def screen_size(serial):
    out = adb("shell", "wm", "size", serial=serial)
    # "Override size" があればそちらを優先
    sizes = re.findall(r"(\d+)x(\d+)", out)
    if not sizes:
        sys.exit(f"画面サイズを取得できません: {out!r}")
    w, h = sizes[-1]
    return int(w), int(h)


def swipe_up(w, h, duration_ms, serial):
    x = w // 2
    adb("shell", "input", "swipe", str(x), str(int(h * 0.75)),
        str(x), str(int(h * 0.25)), str(duration_ms), serial=serial)


def main():
    p = argparse.ArgumentParser(description="TikTok Lite オートスクロール (ADB)")
    p.add_argument("--interval", type=float, default=15, help="スワイプ間隔(秒)")
    p.add_argument("--count", type=int, default=0, help="スワイプ回数 (0 = 無制限)")
    p.add_argument("--duration", type=int, default=300, help="スワイプ時間(ms)")
    p.add_argument("--serial", help="複数端末接続時のデバイスシリアル")
    p.add_argument("--launch", action="store_true", help="開始前に TikTok Lite を起動")
    a = p.parse_args()

    if not shutil.which("adb"):
        sys.exit("adb が見つかりません。Android SDK Platform-Tools をインストールしてください。")

    if a.launch:
        adb("shell", "monkey", "-p", PACKAGE, "-c",
            "android.intent.category.LAUNCHER", "1", serial=a.serial)
        time.sleep(5)

    w, h = screen_size(a.serial)
    print(f"画面 {w}x{h} / {a.interval} 秒ごとにスワイプ (Ctrl+C で停止)")

    n = 0
    try:
        while a.count == 0 or n < a.count:
            time.sleep(a.interval)
            swipe_up(w, h, a.duration, a.serial)
            n += 1
            print(f"[{time.strftime('%H:%M:%S')}] スワイプ {n} 回目")
    except KeyboardInterrupt:
        pass
    print(f"終了 (合計 {n} 回)")


if __name__ == "__main__":
    main()
