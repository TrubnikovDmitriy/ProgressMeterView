package dv.trubnikov.coolometer.ui.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.graphics.*
import android.os.VibrationEffect
import android.os.VibrationEffect.EFFECT_CLICK
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.withClip
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.tools.setTextSizeForHeight
import dv.trubnikov.coolometer.tools.withMathCoordinates
import kotlin.math.abs
import kotlin.math.min

class ProgressMeterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    fun interface OvershootListener {
        fun onOvershoot(forward: Boolean)
    }

    private val vibrator = context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
    private val progressAnimator = ObjectAnimator.ofInt(
        this, "progress", 0
    )
    private val totalProgressAnimator = ObjectAnimator.ofInt(
        this, "totalProgress", 0
    )
    private val overshootAnimator = ValueAnimator.ofInt()
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
    private val digitPaint = Paint().apply {
        color = context.getColor(R.color.meter_score)
        typeface = context.resources.getFont(R.font.digital_font)
        textSize = context.resources.getDimension(R.dimen.meter_digital_number_size)
        isFakeBoldText = true
    }
    private val drawRect = RectF()
    private val textRect = Rect()
    private val rect = RectF()
    private val path = Path()

    private val dp = context.resources.getDimension(R.dimen.one_dp)
    private val textPadding = 16 * dp
    private var nippleRadius = 12 * dp
    private val widthHeightRatio = 3f / 2f
    private val scaleOffset = 200f
    private val degreeOffset = 25f
    private val bigTickCount = 5
    private val smallTickCount = 2
    private val progressPerSecond = 0.35f
    private val bounceInterpolatorThreshold = 0.75f

    var overshootListener: OvershootListener? = null

    var maxProgress: Int = 1000
        set(value) {
            field = value
            invalidate()
        }

    var progress: Int = 700
        set(value) {
            field = value
            invalidate()
        }

    var totalProgress: Int = progress
        set(value) {
            field = value
            invalidate()
        }

    init {
        setupVibrationForAfterAnimation()
        totalProgressAnimator.interpolator = LinearInterpolator()
    }

    /**
     * Add [value] to current [progress] and [totalProgress]
     *
     * @param value how much progress needs to be added
     * @param animate is need to animate the change
     *
     * @return true if the change was applied, false otherwise
     */
    fun addProgress(value: Int, animate: Boolean = false): Boolean {
        return addProgress(value, animate, force = false)
    }

    private fun addProgress(value: Int, animate: Boolean = false, force: Boolean = false): Boolean {
        if (value == 0) return true
        if (progressAnimator.isRunning && !force) return false
        if (totalProgressAnimator.isRunning && !force) return false

        val newProgress = progress + value
        val normalizeProgress = newProgress.coerceIn(0, maxProgress)

        // w/o animation
        if (!animate) {
            progress = newProgress % maxProgress
            totalProgress += value
            return true
        }

        if (progress == 0 && value < 0) {
            backwardOvershootAnimation(value)
            return true
        }
        if (progress == maxProgress && value > 0) {
            forwardOvershootAnimation(value)
            return true
        }

        // with animation
        if (newProgress < 0) {
            val listener = object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animator: Animator) {
                    progressAnimator.removeListener(this)
                    addProgress(newProgress, animate = true, force = true)
                }
            }
            progressAnimator.addListener(listener)
        }
        if (newProgress > maxProgress) {
            val listener = object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animator: Animator) {
                    progressAnimator.removeListener(this)
                    addProgress(newProgress - maxProgress, animate = true, force = true)
                }
            }
            progressAnimator.addListener(listener)
        }
        if (newProgress < 0 || maxProgress < newProgress) {
            progressAnimator.interpolator = AccelerateInterpolator()
        } else {
            progressAnimator.interpolator = BounceInterpolator()
        }

        val timeSeconds = abs(normalizeProgress - progress).toFloat() / maxProgress / progressPerSecond
        val timeMilliseconds = (1000 * timeSeconds).toLong()

        progressAnimator.setIntValues(progress, normalizeProgress)
        progressAnimator.duration = timeMilliseconds
        progressAnimator.start()

        totalProgressAnimator.setIntValues(totalProgress, totalProgress + normalizeProgress - progress)
        totalProgressAnimator.duration = timeMilliseconds
        totalProgressAnimator.start()

        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val desiredHeight = if (heightSpecMode == MeasureSpec.AT_MOST) {
            min(measuredHeight, heightSize)
        } else {
            measuredHeight
        }
        val desiredWidth = if (widthSpecMode == MeasureSpec.AT_MOST) {
            min(measuredWidth, widthSize)
        } else {
            measuredWidth
        }
        val resizeWidth = widthSpecMode != MeasureSpec.EXACTLY
        val resizeHeight = heightSpecMode != MeasureSpec.EXACTLY
        if (resizeWidth && desiredWidth > desiredHeight * widthHeightRatio) {
            setMeasuredDimension((desiredHeight * widthHeightRatio).toInt(), desiredHeight)
            return
        }
        if (resizeHeight && desiredHeight * widthHeightRatio > desiredWidth) {
            setMeasuredDimension(desiredWidth, (desiredWidth / widthHeightRatio).toInt())
            return
        }
        if (resizeHeight && resizeWidth) {
            val minWidth = minOf(desiredWidth.toFloat(), desiredHeight * widthHeightRatio)
            setMeasuredDimension(minWidth.toInt(), (minWidth / widthHeightRatio).toInt())
            return
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // It defines sizes that depend on the size of this view
        val desiredHeight = minOf(width / widthHeightRatio, height * widthHeightRatio)
        val heightForScoreboard = desiredHeight - width / 2 - borderPaint.strokeWidth * 2 - textPadding
        digitPaint.setTextSizeForHeight(heightForScoreboard, "0")
        nippleRadius = width / 30f
    }

    override fun onDraw(canvas: Canvas) {
        val halfWidth = width / 2f
        val desiredHeight = minOf(width / widthHeightRatio, height * widthHeightRatio)
        val dy = (desiredHeight - width) / 2f
        drawRect.set(-halfWidth, -halfWidth, +halfWidth, +halfWidth)
        drawRect.offset(0f, dy)
        canvas.withMathCoordinates(width, height) {
            canvas.withClip(-halfWidth, +halfWidth, +halfWidth, -halfWidth) {
                drawScoreboard(canvas)
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
        rect.set(drawRect)
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
        val normalizeProgress = normalizeProgress
        val fullBucketsCount = if (normalizeProgress > 0) {
            (normalizeProgress / bucketSize).toInt()
        } else {
            -1
        }
        rect.set(drawRect)
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
        val sweepAngle = (normalizeProgress % bucketSize) * (180f - 2 * degreeOffset)
        canvas.drawArc(rect, startAngle, -sweepAngle, false, fillPaint)
    }

    private fun drawScale(canvas: Canvas) {
        rect.set(drawRect)
        rect.inset(scaleOffset, scaleOffset)
        canvas.drawArc(rect, 180f - degreeOffset, -180f + 2 * degreeOffset, false, scalePaint)
        val scaleHeight = rect.height() / 2
        val smallTickHeight = scaleHeight / 10
        val bigTickHeight = scaleHeight / 5
        val bigTickStep = (180f - 2 * degreeOffset) / (bigTickCount - 1)
        // Big ticks
        repeat(bigTickCount) { i ->
            canvas.withRotation(
                90 - degreeOffset - i * bigTickStep,
                rect.centerX(),
                rect.centerY()
            ) {
                canvas.drawLine(
                    0f,
                    rect.centerY() + scaleHeight,
                    0f,
                    rect.centerY() + scaleHeight + bigTickHeight,
                    scalePaint
                )
            }
        }
        // Small ticks
        val totalSmallTickCount = (smallTickCount + 1) * (bigTickCount - 1) + 1
        val smallTickStep = (180f - 2 * degreeOffset) / (totalSmallTickCount - 1)
        scalePaint.strokeWidth /= smallTickCount
        repeat(totalSmallTickCount) { i ->
            canvas.withRotation(
                90 - degreeOffset - i * smallTickStep,
                rect.centerX(),
                rect.centerY()
            ) {
                canvas.drawLine(
                    0f,
                    rect.centerY() + scaleHeight,
                    0f,
                    rect.centerY() + scaleHeight + smallTickHeight,
                    scalePaint
                )
            }
        }
        scalePaint.strokeWidth *= smallTickCount
    }

    private fun drawNeedle(canvas: Canvas) {
        rect.set(drawRect)
        rect.inset(scaleOffset, scaleOffset)
        val sweepAngle = (180f - 2 * degreeOffset) * normalizeProgress
        val scaleHeight = rect.height() / 2
        canvas.withTranslation(rect.centerX(), rect.centerY()) {
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
            canvas.drawCircle(0f, 0f, nippleRadius, needlePaint)
        }
    }

    private fun drawScoreboard(canvas: Canvas) {
        val text = totalProgress.toString()
        digitPaint.getTextBounds(text, 0, text.length, textRect)
        val oneDigitWidthAdjustment = digitPaint.measureText("0", 0, 1) / 6
        val textWidth = textRect.width()
        val textHeight = textRect.height()
        val scoreboardWidth = textWidth + borderPaint.strokeWidth + textPadding
        val scoreboardHeight = 2 * (textHeight + nippleRadius + borderPaint.strokeWidth + textPadding)
        rect.left = drawRect.centerX() - scoreboardWidth / 2
        rect.right = drawRect.centerX() + scoreboardWidth / 2
        rect.top = drawRect.centerY() + scoreboardHeight / 2
        rect.bottom = drawRect.centerY() - scoreboardHeight / 2
        canvas.drawRoundRect(rect, textPadding, textPadding, backgroundPaint)
        canvas.drawRoundRect(rect, textPadding, textPadding, borderPaint)
        val textX = rect.centerX() - textWidth / 2 - oneDigitWidthAdjustment
        val textY = rect.centerY() - textHeight - borderPaint.strokeWidth / 2 - textPadding / 2
        canvas.withScale(1f, -1f) {
            canvas.drawText(text, textX, -textY, digitPaint)
        }
    }

    private val normalizeProgress: Float
        @FloatRange(from = -1.0, to = 1.0)
        get() = progress.toFloat() / maxProgress

    @ColorInt
    private fun getColorForBucket(number: Int): Int {
        if (totalProgress < 0 && number >= 0) {
            return Color.DKGRAY
        }
        val bucketCount = bigTickCount - 1
        when (bucketCount) {
            2 -> when (number) {
                0 -> return fillColors[0]
                1 -> return fillColors[4]
            }
            3 -> when (number) {
                0 -> return fillColors[0]
                1 -> return fillColors[2]
                2 -> return fillColors[4]
            }
            4 -> when (number) {
                0 -> return fillColors[1]
                1 -> return fillColors[2]
                2 -> return fillColors[3]
                3 -> return fillColors[4]
            }
            5 -> when (number) {
                0 -> return fillColors[0]
                1 -> return fillColors[1]
                2 -> return fillColors[2]
                3 -> return fillColors[3]
                4 -> return fillColors[4]
            }
        }
        return Color.TRANSPARENT
    }

    private fun forwardOvershootAnimation(value: Int) {
        progress = 0
        overshootListener?.onOvershoot(forward = true)
        forwardNeedleRotation {
            addProgress(value, animate = true, force = true)
        }
    }

    private fun backwardOvershootAnimation(value: Int) {
        progress = maxProgress
        overshootListener?.onOvershoot(forward = false)
        backwardNeedleRotation {
            addProgress(value, animate = true, force = true)
        }
    }

    private fun forwardNeedleRotation(onEndListener: () -> Unit) {
        val listener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                overshootAnimator.removeListener(this)
                progress = 0
                onEndListener()
            }
        }
        val outOfScale = 360 * maxProgress / (180f - 2 * degreeOffset)
        overshootAnimator.addListener(listener)
        overshootAnimator.addUpdateListener {
            progress = it.animatedValue as Int
        }
        val passedProgress = (outOfScale - maxProgress) / maxProgress
        val speedForOvershooting = 10 * progressPerSecond
        val overshotDuration = (1000f * passedProgress / speedForOvershooting).toLong()
        val vibration = VibrationEffect.createOneShot(overshotDuration, 200)
        overshootAnimator.duration = overshotDuration
        overshootAnimator.setIntValues(maxProgress, outOfScale.toInt())
        vibrator.defaultVibrator.vibrate(vibration)
        overshootAnimator.start()
    }

    private fun backwardNeedleRotation(onEndListener: () -> Unit) {
        val listener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                overshootAnimator.removeListener(this)
                progress = maxProgress
                onEndListener()
            }
        }
        val outOfScale = 360 * maxProgress / (180f - 2 * degreeOffset)
        overshootAnimator.addListener(listener)
        overshootAnimator.addUpdateListener {
            progress = it.animatedValue as Int
        }
        val passedProgress = (outOfScale - maxProgress) / maxProgress
        val speedForOvershooting = 5 * progressPerSecond
        val overshotDuration = (1000f * passedProgress / speedForOvershooting).toLong()
        overshootAnimator.duration = overshotDuration
        overshootAnimator.setIntValues(0, -outOfScale.toInt() + maxProgress)
        overshootAnimator.start()
    }

    private fun setupVibrationForAfterAnimation() {
        val clickVibration = VibrationEffect.createPredefined(EFFECT_CLICK)
        var vibrateIsDone = false
        progressAnimator.interpolator = BounceInterpolator()
        progressAnimator.addUpdateListener {
            // These conditions only for [BounceInterpolator]!
            if (!vibrateIsDone
                && bounceInterpolatorThreshold <= it.animatedFraction
                && it.animatedFraction <= bounceInterpolatorThreshold + 0.05
            ) {
                vibrateIsDone = true
                vibrator.defaultVibrator.vibrate(clickVibration)
            }
            if (vibrateIsDone && it.animatedFraction > 0.95f) {
                vibrateIsDone = false
            }
        }
    }
}