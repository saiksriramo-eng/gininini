package app.ginininini.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import app.ginininini.R
import kotlin.math.abs

class BubbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onTapListener: (() -> Unit)? = null
    var onPositionChangedListener: ((x: Int, y: Int, isLeftEdge: Boolean) -> Unit)? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private val clickThreshold = 15f

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6212121") // Dark semi-translucent circle
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#40FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val iconDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_bubble)

    private val idleHandler = Handler(Looper.getMainLooper())
    private val idleRunnable = Runnable {
        animate().alpha(0.4f).setDuration(300).start()
    }

    init {
        resetIdleTimer()
    }

    fun resetIdleTimer() {
        idleHandler.removeCallbacks(idleRunnable)
        animate().alpha(1.0f).setDuration(150).start()
        idleHandler.postDelayed(idleRunnable, 3500)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = width / 2f
        canvas.drawCircle(radius, radius, radius - 2f, bgPaint)
        canvas.drawCircle(radius, radius, radius - 2f, borderPaint)

        iconDrawable?.let {
            val inset = (width * 0.22f).toInt()
            it.setBounds(inset, inset, width - inset, height - inset)
            it.draw(canvas)
        }
    }

    fun handleTouchEvent(event: MotionEvent, params: WindowManager.LayoutParams, windowManager: WindowManager): Boolean {
        resetIdleTimer()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - initialTouchX
                val dy = event.rawY - initialTouchY
                if (abs(dx) > clickThreshold || abs(dy) > clickThreshold) {
                    isDragging = true
                }
                if (isDragging) {
                    params.x = (initialX + dx).toInt()
                    params.y = (initialY + dy).toInt()
                    windowManager.updateViewLayout(this, params)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    onTapListener?.invoke()
                } else {
                    snapToEdge(params, windowManager)
                }
                return true
            }
        }
        return false
    }

    private fun snapToEdge(params: WindowManager.LayoutParams, windowManager: WindowManager) {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val currentX = params.x
        val targetX = if (currentX + width / 2 < screenWidth / 2) 0 else screenWidth - width
        val isLeft = targetX == 0

        val animator = ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 200
            addUpdateListener { anim ->
                params.x = anim.animatedValue as Int
                windowManager.updateViewLayout(this@BubbleView, params)
            }
        }
        animator.start()
        onPositionChangedListener?.invoke(targetX, params.y, isLeft)
    }
}
