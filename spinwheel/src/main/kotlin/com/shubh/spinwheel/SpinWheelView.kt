package com.shubh.spinwheel

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.min
import kotlin.random.Random

/**
 * A "spin the wheel" view. Add it in XML or code, call [setSegments], then [spin].
 *
 * ```
 * wheel.setSegments(listOf(WheelSegment("10", "10 pts", Color.RED), ...))
 * wheel.onSpinEnd = { segment -> showReward(segment) }
 * button.setOnClickListener { wheel.spin() }            // random, weighted
 * wheel.spinTo("10")                                    // or: result decided by your server
 * ```
 */
class SpinWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    /** Called on the main thread once the wheel stops, with the winning segment. */
    var onSpinEnd: ((WheelSegment) -> Unit)? = null

    /** Called when a spin begins. */
    var onSpinStart: (() -> Unit)? = null

    var spinDurationMs: Long = 5000L
    var extraRotations: Int = 5

    val isSpinning: Boolean get() = animator?.isRunning == true

    private var segments: List<WheelSegment> = emptyList()
    private var rotationAngle = 0f // degrees, clockwise
    private var animator: ValueAnimator? = null

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.WHITE }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#D32F2F") }
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val oval = RectF()
    private val pointer = Path()

    init {
        var textColor = Color.WHITE
        var textSizePx = sp(14f)
        var borderColor = Color.parseColor("#FFC107")
        var borderWidth = dp(6f)
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.SpinWheelView, defStyleAttr, 0)
            try {
                textColor = a.getColor(R.styleable.SpinWheelView_swTextColor, textColor)
                textSizePx = a.getDimension(R.styleable.SpinWheelView_swTextSizeSp, textSizePx)
                borderColor = a.getColor(R.styleable.SpinWheelView_swBorderColor, borderColor)
                borderWidth = a.getDimension(R.styleable.SpinWheelView_swBorderWidth, borderWidth)
                spinDurationMs = a.getInt(R.styleable.SpinWheelView_swSpinDurationMs, spinDurationMs.toInt()).toLong()
            } finally {
                a.recycle()
            }
        }
        textPaint.color = textColor
        textPaint.textSize = textSizePx
        borderPaint.color = borderColor
        borderPaint.strokeWidth = borderWidth
        dividerPaint.strokeWidth = dp(1.5f)
    }

    fun setSegments(list: List<WheelSegment>) {
        require(list.size >= 2) { "Need at least 2 segments" }
        segments = list.toList()
        invalidate()
    }

    /** Spin and land on a random segment chosen by [WheelSegment.weight]. */
    fun spin() {
        check(segments.isNotEmpty()) { "Call setSegments() first" }
        spinToIndex(pickWeighted())
    }

    /** Spin and land on the segment with this id (e.g. a result decided by your backend). */
    fun spinTo(segmentId: String) {
        val idx = segments.indexOfFirst { it.id == segmentId }
        require(idx >= 0) { "No segment with id '$segmentId'" }
        spinToIndex(idx)
    }

    fun spinToIndex(index: Int) {
        check(segments.isNotEmpty()) { "Call setSegments() first" }
        require(index in segments.indices) { "index out of range" }
        if (isSpinning) return

        val sweep = 360f / segments.size
        // Angle (in wheel's own frame, from the pointer at top) we want under the pointer:
        val jitter = (Random.nextFloat() - 0.5f) * sweep * 0.7f
        val wanted = index * sweep + sweep / 2f + jitter
        // Wheel rotated R clockwise puts wheel-angle (-R) under the pointer.
        val targetMod = (((-wanted) % 360f) + 360f) % 360f
        val currentMod = ((rotationAngle % 360f) + 360f) % 360f
        val delta = ((targetMod - currentMod) % 360f + 360f) % 360f
        val end = rotationAngle + extraRotations * 360f + delta

        onSpinStart?.invoke()
        animator = ValueAnimator.ofFloat(rotationAngle, end).apply {
            duration = spinDurationMs
            interpolator = DecelerateInterpolator(1.6f)
            addUpdateListener { rotationAngle = it.animatedValue as Float; invalidate() }
            addListener(object : AnimatorListenerAdapter() {
                private var cancelled = false
                override fun onAnimationCancel(animation: Animator) { cancelled = true }
                override fun onAnimationEnd(animation: Animator) {
                    if (!cancelled) onSpinEnd?.invoke(segments[index])
                }
            })
            start()
        }
    }

    private fun pickWeighted(): Int {
        val total = segments.sumOf { it.weight }
        require(total > 0.0) { "At least one segment needs weight > 0" }
        var r = Random.nextDouble() * total
        segments.forEachIndexed { i, s ->
            r -= s.weight
            if (r < 0) return i
        }
        return segments.indexOfLast { it.weight > 0 }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = resolveSize(dp(300f).toInt(), widthMeasureSpec)
        val h = resolveSize(dp(300f).toInt(), heightMeasureSpec)
        val s = min(w, h)
        setMeasuredDimension(s, s)
    }

    override fun onDraw(canvas: Canvas) {
        if (segments.isEmpty()) return
        val cx = width / 2f
        val cy = height / 2f
        val bw = borderPaint.strokeWidth
        val topInset = dp(14f) // room for pointer
        val radius = min(cx, cy) - bw / 2f - topInset / 2f
        val wy = cy + topInset / 2f
        oval.set(cx - radius, wy - radius, cx + radius, wy + radius)
        val sweep = 360f / segments.size

        canvas.save()
        canvas.rotate(rotationAngle, cx, wy)
        segments.forEachIndexed { i, seg ->
            val start = -90f + i * sweep
            arcPaint.color = seg.color
            canvas.drawArc(oval, start, sweep, true, arcPaint)
            canvas.drawArc(oval, start, sweep, true, dividerPaint)

            canvas.save()
            canvas.rotate(start + sweep / 2f, cx, wy)
            val maxWidth = radius * 0.62f
            val text = TextUtils.ellipsize(seg.label, textPaint, maxWidth, TextUtils.TruncateAt.END).toString()
            canvas.drawText(text, cx + radius * 0.58f, wy + textPaint.textSize / 3f, textPaint)
            canvas.restore()
        }
        canvas.restore()

        canvas.drawCircle(cx, wy, radius, borderPaint)
        canvas.drawCircle(cx, wy, dp(14f), hubPaint)

        // Pointer at the top, pointing down into the wheel.
        val pw = dp(14f)
        pointer.reset()
        pointer.moveTo(cx - pw, 0f)
        pointer.lineTo(cx + pw, 0f)
        pointer.lineTo(cx, wy - radius + bw)
        pointer.close()
        canvas.drawPath(pointer, pointerPaint)
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }

    private fun dp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics)
    private fun sp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, resources.displayMetrics)
}
