package com.example.autotap

import android.content.Context

/** アプリの設定（タップ間隔）を保存・読み込みする */
object Settings {
    private const val PREFS = "settings"
    private const val KEY_INTERVAL_MS = "interval_ms"

    const val DEFAULT_INTERVAL_MS = 1000L

    /** タップ自体に TAP_DURATION_MS かかるので、それより短い間隔は許可しない */
    const val MIN_INTERVAL_MS = 100L

    fun getIntervalMs(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_INTERVAL_MS, DEFAULT_INTERVAL_MS)

    fun setIntervalMs(context: Context, value: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_INTERVAL_MS, value.coerceAtLeast(MIN_INTERVAL_MS))
            .apply()
    }
}
