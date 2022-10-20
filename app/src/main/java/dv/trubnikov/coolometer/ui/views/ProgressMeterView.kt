package dv.trubnikov.coolometer.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.graphics.*
import android.os.VibrationEffect
import android.os.VibrationEffect.EFFECT_CLICK
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.View
import android.view.animation.BounceInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.withClip
import androidx.core.graphics.withRotation
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.tools.withMathCoordinates
import kotlin.math.abs
import kotlin.math.min


class ProgressMeterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val animator = ValueAnimator.ofFloat()
    private val borderPaint = Paint().apply {
        color = context.getColor(R.color.meter_border)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 30f
        isAntiAlias = true
    }
    private val shadowPaint = Paint().apply {
        color = context.getColor(R.color.meter_shadow)
        style = Paint.Style.FILL
        strokeWidth = 0f
        isAntiAlias = true
    }
    private val backgroundPaint = Paint().apply {
        color = context.getColor(R.color.meter_background)
        style = Paint.Style.FILL
        strokeWidth = 0f
        isAntiAlias = true
    }
    private val needlePaint = Paint().apply {
        color = context.getColor(R.color.meter_needle)
        style = Paint.Style.FILL
        strokeWidth = 25f
    }
    private val scalePaint = Paint().apply {
        color = context.getColor(R.color.meter_needle)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 20f
    }
    private val fillPaint = Paint().apply {
        color = context.getColor(R.color.meter_level_0)
        style = Paint.Style.STROKE
        strokeWidth = 60f
    }
    private val fillColors = IntArray(5) {
        val colorInt = when (it) {
            0 -> R.color.meter_level_0
            1 -> R.color.meter_level_1
            2 -> R.color.meter_level_2
            3 -> R.color.meter_level_3
            4 -> R.color.meter_level_4
            else -> R.color.meter_level_4
        }
        context.getColor(colorInt)
    }
    private val tempRect = Rect()
    private val rect = RectF()
    private val path = Path()

    private val scaleOffset = 200f
    private val degreeOffset = 25f
    private val bigTickCount = 5
    private val smallTickCount = 2
    private val progressPerSecond = 0.35f

    @FloatRange(from = 0.0, to = 1.0)
    var progress = 0.7f
        private set

    init {
        setupVibrationForAfterAnimation()
        animator.addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
    }

    fun setProgress(targetProgress: Float, animate: Boolean = false) {
        val correctedProgress = targetProgress.coerceIn(0f, 1f)
        if (correctedProgress == progress) {
            return
        }

        // w/o animation
        if (!animate) {
            progress = correctedProgress
            invalidate()
            return
        }

        // with animation
        animator.pause()
        animator.setFloatValues(progress, correctedProgress)
        val timeSeconds = abs(correctedProgress - progress) / progressPerSecond
        animator.duration = (1000 * timeSeconds).toLong()
        animator.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val resizeWidth = widthSpecMode != MeasureSpec.EXACTLY
        val resizeHeight = heightSpecMode != MeasureSpec.EXACTLY
        if (resizeWidth && measuredWidth > measuredHeight) {
            setMeasuredDimension(measuredHeight, measuredHeight)
            return
        }
        if (resizeHeight && measuredHeight > measuredWidth) {
            setMeasuredDimension(measuredWidth, measuredWidth)
            return
        }
    }

    override fun onDraw(canvas: Canvas) {
        val size = min(width, height) / 2
        canvas.withMathCoordinates(width, height) {
            canvas.withClip(-size, +size, +size, -size) {
                drawBackground(canvas)
                drawFilling(canvas)
                drawScale(canvas)
                drawNeedle(canvas)
            }
        }
    }

    private fun drawBackground(canvas: Canvas) {
        val shadowSize = 40f
        val dx = shadowSize / 8
        canvas.getClipBounds(rect)
        rect.inset(borderPaint.strokeWidth, borderPaint.strokeWidth)
        canvas.drawArc(rect, 0f, 180f, true, shadowPaint)
        // Background
        rect.inset(+dx, +shadowSize)
        canvas.drawArc(rect, 0f, 180f, true, backgroundPaint)
        // Border
        rect.inset(-dx, -shadowSize)
        rect.inset(-borderPaint.strokeWidth / 3, -borderPaint.strokeWidth / 4)
        path.rewind()
        path.addArc(rect, 0f, 180f)
        canvas.drawPath(path, borderPaint)
        canvas.drawLine(rect.left, rect.centerY(), rect.right, rect.centerY(), borderPaint)
    }

    private fun drawFilling(canvas: Canvas) {
        val bucketSize = 1f / (bigTickCount - 1)
        val uiBucketSize = (180f - 2 * degreeOffset) / (bigTickCount - 1)
        val fullBucketsCount = (progress / bucketSize).toInt()
        canvas.getClipBounds(rect)
        rect.inset(scaleOffset - fillPaint.strokeWidth / 2, scaleOffset - fillPaint.strokeWidth / 2)
        // Full buckets
        repeat(fullBucketsCount) { bucketNumber ->
            fillPaint.color = getColorForBucket(bucketNumber)
            val startAngle = (180f - degreeOffset) - uiBucketSize * bucketNumber
            canvas.drawArc(rect, startAngle, -uiBucketSize, false, fillPaint)
        }
        // Last incomplete bucket
        fillPaint.color = getColorForBucket(fullBucketsCount)
        val startAngle = (180f - degreeOffset) - uiBucketSize * fullBucketsCount
        val sweepAngle = (progress % bucketSize) * (180f - 2 * degreeOffset)
        canvas.drawArc(rect, startAngle, -sweepAngle, false, fillPaint)
    }

    private fun drawScale(canvas: Canvas) {
        canvas.getClipBounds(rect)
        rect.inset(scaleOffset, scaleOffset)
        canvas.drawArc(rect, 180f - degreeOffset, -180f + 2 * degreeOffset, false, scalePaint)
        val scaleHeight = rect.height() / 2
        val smallTickHeight = scaleHeight / 10
        val bigTickHeight = scaleHeight / 5
        val bigTickStep = (180f - 2 * degreeOffset) / (bigTickCount - 1)
        // Big ticks
        repeat(bigTickCount) { i ->
            canvas.withRotation(90 - degreeOffset - i * bigTickStep) {
                canvas.drawLine(0f, scaleHeight, 0f, scaleHeight + bigTickHeight, scalePaint)
            }
        }
        // Small ticks
        val totalSmallTickCount = (smallTickCount + 1) * (bigTickCount - 1) + 1
        val smallTickStep = (180f - 2 * degreeOffset) / (totalSmallTickCount - 1)
        scalePaint.strokeWidth /= smallTickCount
        repeat(totalSmallTickCount) { i ->
            canvas.withRotation(90 - degreeOffset - i * smallTickStep) {
                canvas.drawLine(0f, scaleHeight, 0f, scaleHeight + smallTickHeight, scalePaint)
            }
        }
        scalePaint.strokeWidth *= smallTickCount
    }

    private fun drawNeedle(canvas: Canvas) {
        val sweepAngle = (180f - 2 * degreeOffset) * progress
        val scaleHeight = rect.height() / 2
        canvas.withRotation(90f - degreeOffset - sweepAngle) {
            val needleWidth = 15f
            path.apply {
                rewind()
                moveTo(-needleWidth, 0f)
                lineTo(-needleWidth, scaleHeight * 0.9f)
                lineTo(0f, scaleHeight * 1.15f)
                lineTo(+needleWidth, scaleHeight * 0.9f)
                lineTo(+needleWidth, 0f)
                close()
            }
            canvas.drawPath(path, needlePaint)
        }
        canvas.drawCircle(0f, 0f, 40f, needlePaint)
    }

    @ColorInt
    private fun getColorForBucket(number: Int): Int {
        val bucketCount = bigTickCount - 1
        when (bucketCount) {
            2 -> when(number) {
                0 -> return fillColors[0]
                1 -> return fillColors[4]
            }
            3 -> when(number) {
                0 -> return fillColors[0]
                1 -> return fillColors[2]
                2 -> return fillColors[4]
            }
            4 -> when(number) {
                0 -> return fillColors[1]
                1 -> return fillColors[2]
                2 -> return fillColors[3]
                3 -> return fillColors[4]
            }
            5 -> when(number) {
                0 -> return fillColors[0]
                1 -> return fillColors[1]
                2 -> return fillColors[2]
                3 -> return fillColors[3]
                4 -> return fillColors[4]
            }
        }
        return Color.TRANSPARENT
    }

    private fun setupVibrationForAfterAnimation() {
        val vibrator = context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val clickVibration = VibrationEffect.createPredefined(EFFECT_CLICK)
        var vibrateIsDone = false
        animator.interpolator = BounceInterpolator()
        animator.addUpdateListener {
            // These conditions only for [BounceInterpolator]!
            if (!vibrateIsDone && 0.75f <= it.animatedFraction && it.animatedFraction <= 0.80f) {
                vibrateIsDone = true
                vibrator.defaultVibrator.vibrate(clickVibration)
            }
            if (vibrateIsDone && it.animatedFraction > 0.95f) {
                vibrateIsDone = false
            }
        }
    }

    private fun Canvas.getClipBounds(rect: RectF) {
        this.getClipBounds(tempRect)
        rect.set(tempRect)
    }

}