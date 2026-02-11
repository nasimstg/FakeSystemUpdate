package io.softexforge.fakesysupdate

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

class PanicGaugeDrawable(
    private val level: Int,
    private val trackColor: Int,
    private val fillColor: Int,
    private val glowColor: Int
) : Drawable() {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = trackColor
        pathEffect = DashPathEffect(floatArrayOf(6f, 10f), 0f)
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = fillColor
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 28f
        color = glowColor
        maskFilter = BlurMaskFilter(14f, BlurMaskFilter.Blur.NORMAL)
        strokeCap = Paint.Cap.ROUND
    }

    override fun draw(canvas: Canvas) {
        val rect = RectF(bounds)
        rect.inset(20f, 20f)
        val startAngle = 135f
        val totalSweep = 270f

        // Draw track
        canvas.drawArc(rect, startAngle, totalSweep, false, trackPaint)

        // Draw glow behind fill
        val fillSweep = totalSweep * level / 100f
        if (fillSweep > 0f) {
            canvas.drawArc(rect, startAngle, fillSweep, false, glowPaint)
            canvas.drawArc(rect, startAngle, fillSweep, false, fillPaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        fillPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fillPaint.colorFilter = colorFilter
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
