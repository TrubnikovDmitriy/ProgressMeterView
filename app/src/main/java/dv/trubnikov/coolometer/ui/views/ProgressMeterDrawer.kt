package dv.trubnikov.coolometer.ui.views

import android.content.Context
import android.graphics.*
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.tools.setTextSizeForSize
import dv.trubnikov.coolometer.tools.withMathCoordinates

class ProgressMeterDrawer(
    private val context: Context,
    var progress: Int = 0,
    var maxProgress: Int = 1000,
    var totalProgress: Int = progress,
    var smallTickCount: Int = 2,
    var bigTickCount: Int = 5,
    var degreeOffset: Float = 25f,
    var gravity: Gravity = Gravity.CENTER,
    val widthHeightRatio: Float = 3f / 2f,
) {

    enum class Gravity { CENTER, TOP, BOTTOM }

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
        typeface = context.resources.getFont(R.font.digital_numbers)
        textSize = context.resources.getDimension(R.dimen.meter_digital_number_size)
        isFakeBoldText = true
    }
    private val drawRect = RectF()
    private val textRect = Rect()
    private val rect = RectF()
    private val path = Path()

    private val dp = context.resources.getDimension(R.dimen.one_dp)
    private val textPadding = 16 * dp
    private val scaleOffset = 200f
    private var nippleRadius = 12 * dp

    private val normalizeProgress: Float
        @FloatRange(from = -1.0, to = 1.0)
        get() = progress.toFloat() / maxProgress

    fun setSize(width: Int, height: Int) {
        // It defines sizes that depend on the size of this view
        val desiredHeight = minOf(width / widthHeightRatio, height * widthHeightRatio)
        val heightForScoreboard = desiredHeight - width / 2 - borderPaint.strokeWidth * 2 - textPadding
        digitPaint.setTextSizeForSize(width / 25f, heightForScoreboard, "0")
        nippleRadius = width / 30f
    }

    fun draw(canvas: Canvas, width: Int, height: Int) {
        val halfWidth = width / 2f
        val desiredHeight = minOf(width / widthHeightRatio, height * widthHeightRatio)
        val dy = (desiredHeight - width) / 2f
        drawRect.set(-halfWidth, -halfWidth, +halfWidth, +halfWidth)
        drawRect.offset(0f, dy)

        val translateByY = when (gravity) {
            Gravity.CENTER -> 0
            Gravity.TOP -> -(height - halfWidth + dy).toInt()
            Gravity.BOTTOM -> +(height - halfWidth + dy).toInt()
        }

        canvas.withMathCoordinates(width, height + translateByY) {
            drawScoreboard(canvas)
            drawBackground(canvas)
            drawFilling(canvas)
            drawScale(canvas)
            drawNeedle(canvas)
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
        rect.inset(
            scaleOffset - fillPaint.strokeWidth / 2,
            scaleOffset - fillPaint.strokeWidth / 2
        )
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
        digitPaint.getTextBounds("0", 0, 1, textRect)
        val oneDigitSizeAdjustment = textRect.width() / 15
        val textWidth = text.length * textRect.width()
        val textHeight = textRect.height()
        val scoreboardWidth = textWidth + borderPaint.strokeWidth + textPadding
        val scoreboardHeight = 2 * (textHeight + nippleRadius + borderPaint.strokeWidth) + textPadding
        rect.left = drawRect.centerX() - scoreboardWidth / 2
        rect.right = drawRect.centerX() + scoreboardWidth / 2
        rect.top = drawRect.centerY() + scoreboardHeight / 2
        rect.bottom = drawRect.centerY() - scoreboardHeight / 2
        canvas.drawRoundRect(rect, textPadding, textPadding, backgroundPaint)
        canvas.drawRoundRect(rect, textPadding, textPadding, borderPaint)
        val textX = rect.centerX() - textWidth / 2 - oneDigitSizeAdjustment
        val textY = rect.centerY() - textHeight - borderPaint.strokeWidth / 2 - textPadding / 1.25f
        canvas.withScale(1f, -1f) {
            canvas.drawText(text, textX, -textY, digitPaint)
        }
    }

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
}