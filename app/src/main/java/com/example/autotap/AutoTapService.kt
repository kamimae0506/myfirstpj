package com.example.autotap

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * ユーザー補助サービス。
 * 有効になると画面に「照準」と「コントロールパネル」を重ねて表示し、
 * 開始ボタンが押されている間、照準の中心を一定間隔でタップする。
 */
class AutoTapService : AccessibilityService() {

    companion object {
        /** サービスが有効なときだけ非 null（MainActivity から状態確認に使う） */
        var instance: AutoTapService? = null
            private set

        /** 1回のタップで指を押している時間 */
        private const val TAP_DURATION_MS = 50L
    }

    private lateinit var windowManager: WindowManager
    private val handler = Handler(Looper.getMainLooper())

    private var targetView: TargetView? = null
    private lateinit var targetParams: WindowManager.LayoutParams
    private var panelView: View? = null
    private lateinit var toggleButton: Button
    private lateinit var infoText: TextView

    private var running = false
    private var tapCount = 0
    private var intervalMs = Settings.DEFAULT_INTERVAL_MS

    private val tapRunnable = object : Runnable {
        override fun run() {
            if (!running) return
            tapOnce()
            handler.postDelayed(this, intervalMs)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        intervalMs = Settings.getIntervalMs(this)
        showTarget()
        showPanel()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 画面の内容は使わない
    }

    override fun onInterrupt() {
        stopTapping()
    }

    override fun onDestroy() {
        stopTapping()
        targetView?.let { windowManager.removeView(it) }
        panelView?.let { windowManager.removeView(it) }
        targetView = null
        panelView = null
        instance = null
        super.onDestroy()
    }

    /** MainActivity で間隔が保存されたときに呼ばれる */
    fun onIntervalChanged() {
        intervalMs = Settings.getIntervalMs(this)
        updateInfo()
    }

    // ---- タップ処理 ----

    private fun tapOnce() {
        val view = targetView ?: return
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0] + view.width / 2f
        val y = location[1] + view.height / 2f

        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, TAP_DURATION_MS))
            .build()
        dispatchGesture(gesture, null, null)

        tapCount++
        updateInfo()
    }

    private fun startTapping() {
        if (running) return
        running = true
        tapCount = 0
        intervalMs = Settings.getIntervalMs(this)
        // 照準自体がタップを受け取らないよう、実行中はタッチを素通りさせる
        setTargetTouchable(false)
        toggleButton.setText(R.string.stop)
        handler.post(tapRunnable)
    }

    private fun stopTapping() {
        if (!running) return
        running = false
        handler.removeCallbacks(tapRunnable)
        setTargetTouchable(true)
        toggleButton.setText(R.string.start)
        updateInfo()
    }

    // ---- 画面表示 ----

    private fun overlayParams(): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun showTarget() {
        val size = dp(64)
        val metrics = resources.displayMetrics
        targetParams = overlayParams().apply {
            width = size
            height = size
            x = (metrics.widthPixels - size) / 2
            y = (metrics.heightPixels - size) / 2
        }
        val view = TargetView(this)
        enableDrag(handle = view, window = view, params = targetParams)
        windowManager.addView(view, targetParams)
        targetView = view
    }

    private fun showPanel() {
        val params = overlayParams().apply {
            x = dp(16)
            y = dp(120)
        }

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(Color.argb(220, 33, 33, 33))
            }
        }

        val dragHandle = TextView(this).apply {
            text = "≡"
            textSize = 24f
            setTextColor(Color.WHITE)
            setPadding(dp(4), 0, dp(8), 0)
        }
        toggleButton = Button(this).apply {
            setText(R.string.start)
            setOnClickListener { if (running) stopTapping() else startTapping() }
        }
        infoText = TextView(this).apply {
            setTextColor(Color.WHITE)
            setPadding(dp(8), 0, dp(4), 0)
        }

        panel.addView(dragHandle)
        panel.addView(toggleButton)
        panel.addView(infoText)
        enableDrag(handle = dragHandle, window = panel, params = params)

        windowManager.addView(panel, params)
        panelView = panel
        updateInfo()
    }

    private fun updateInfo() {
        if (!::infoText.isInitialized) return
        infoText.text = "${intervalMs}ms\n${tapCount}回"
    }

    private fun setTargetTouchable(touchable: Boolean) {
        val view = targetView ?: return
        targetParams.flags = if (touchable) {
            targetParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        } else {
            targetParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
        view.alpha = if (touchable) 1f else 0.6f
        windowManager.updateViewLayout(view, targetParams)
    }

    /** handle をドラッグすると window 全体が移動するようにする */
    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag(handle: View, window: View, params: WindowManager.LayoutParams) {
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        handle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (event.rawX - touchX).toInt()
                    params.y = startY + (event.rawY - touchY).toInt()
                    windowManager.updateViewLayout(window, params)
                }
            }
            true
        }
    }
}
