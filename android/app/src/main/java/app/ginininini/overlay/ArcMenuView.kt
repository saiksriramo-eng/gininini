package app.ginininini.overlay

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import app.ginininini.R
import kotlin.math.cos
import kotlin.math.sin

class ArcMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    data class MenuItem(
        val id: String,
        val iconResId: Int,
        val label: String,
        val onClick: () -> Unit
    )

    var onDismissListener: (() -> Unit)? = null
    private val buttons = mutableListOf<ImageView>()

    init {
        setBackgroundColor(Color.parseColor("#4D000000")) // semi-transparent scrim
        setOnClickListener {
            onDismissListener?.invoke()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            onDismissListener?.invoke()
            return true
        }
        return super.onTouchEvent(event)
    }

    fun showButtons(
        items: List<MenuItem>,
        originX: Float,
        originY: Float,
        isLeftEdge: Boolean
    ) {
        removeAllViews()
        buttons.clear()

        val buttonSize = (48 * resources.displayMetrics.density).toInt()
        val radius = 120 * resources.displayMetrics.density

        // Sweep angles: if docked on left, fan from -70 to +70 deg; if right, 110 to 250 deg
        val startAngle = if (isLeftEdge) -60.0 else 120.0
        val endAngle = if (isLeftEdge) 60.0 else 240.0
        val angleStep = if (items.size > 1) (endAngle - startAngle) / (items.size - 1) else 0.0

        for ((index, item) in items.withIndex()) {
            val angleRad = Math.toRadians(startAngle + (index * angleStep))
            val targetX = (originX + radius * cos(angleRad)).toFloat()
            val targetY = (originY + radius * sin(angleRad)).toFloat()

            val btn = ImageView(context).apply {
                setImageResource(item.iconResId)
                setBackgroundResource(android.R.drawable.btn_default)
                background.setTint(Color.parseColor("#D9282828"))
                val pad = (10 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
                x = originX
                y = originY
                alpha = 0f
                scaleX = 0.2f
                scaleY = 0.2f

                setOnClickListener {
                    item.onClick()
                    onDismissListener?.invoke()
                }
            }

            val lp = LayoutParams(buttonSize, buttonSize, Gravity.TOP or Gravity.START)
            addView(btn, lp)
            buttons.add(btn)

            // Spring animate out
            animateWithSpring(btn, targetX, targetY)
        }
    }

    private fun animateWithSpring(view: ImageView, targetX: Float, targetY: Float) {
        view.animate().alpha(1.0f).setDuration(150).start()

        val springX = SpringAnimation(view, DynamicAnimation.X, targetX).apply {
            spring = SpringForce(targetX).apply {
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                stiffness = SpringForce.STIFFNESS_LOW
            }
        }

        val springY = SpringAnimation(view, DynamicAnimation.Y, targetY).apply {
            spring = SpringForce(targetY).apply {
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                stiffness = SpringForce.STIFFNESS_LOW
            }
        }

        val springScaleX = SpringAnimation(view, DynamicAnimation.SCALE_X, 1.0f)
        val springScaleY = SpringAnimation(view, DynamicAnimation.SCALE_Y, 1.0f)

        springX.start()
        springY.start()
        springScaleX.start()
        springScaleY.start()
    }
}
