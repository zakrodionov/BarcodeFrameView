package com.zakrodionov.barcodeframeview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Size
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

interface ScannerOverlay {
    val size: Size
    val scanRect: RectF
}

class ScannerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr), ScannerOverlay {

    companion object {
        private const val DELAY_MS = 7L
        private const val Y_STEP_PX = 5
        private const val HORIZONTAL_LINE_PADDING_DP = 20
        private const val FRAME_HEIGHT_PERCENT = .40f
        private const val FRAME_WIDTH_PERCENT = .80f
    }

    private val scrimPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.barcode_reticle_background)
    }

    private val framePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = 4.dpToPxF
    }

    private val linePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 2.dpToPxF
    }

    private val eraserPaint: Paint = Paint().apply {
        strokeWidth = framePaint.strokeWidth
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val horizontalLinePaddingPx = HORIZONTAL_LINE_PADDING_DP.dpToPxF
    private val refreshRunnable = Runnable { refreshView() }

    private var runAnimation = true
    private var showLine = true
    private var isGoingDown = true

    private var posY = 0

    private var frameRect: RectF by Delegates.notNull()

    override val size: Size
        get() = Size(width, height)

    override val scanRect: RectF
        get() = frameRect

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    public override fun onDraw(canvas: Canvas) {
        calculateFrameRect()

        drawBackground(canvas)

        if (showLine) {
            drawLine(canvas)
        }

        drawFrame(canvas)

        if (runAnimation) {
            postDelayed(refreshRunnable, DELAY_MS)
        }
    }

    private fun calculateFrameRect() {
        val frameHeight = height * FRAME_HEIGHT_PERCENT
        val frameWidth = width * FRAME_WIDTH_PERCENT

        val startX = (width - frameWidth) / 2
        val startY = (height - frameHeight) / 2
        val endX = startX + frameWidth
        val endY = startY + frameHeight

        frameRect = RectF(startX, startY, endX, endY)
    }

    private fun drawLine(canvas: Canvas) {
        val startX = frameRect.left + horizontalLinePaddingPx + (framePaint.strokeWidth / 2f)
        val startY = posY.toFloat() + frameRect.top
        val endX = frameRect.right - horizontalLinePaddingPx - (framePaint.strokeWidth / 2f)
        val endY = posY.toFloat() + frameRect.top
        canvas.drawLine(startX, startY, endX, endY, linePaint)
    }

    private fun drawFrame(canvas: Canvas) {
        canvas.drawRect(frameRect, framePaint)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)

        eraserPaint.style = Paint.Style.FILL
        canvas.drawRect(frameRect, eraserPaint)

        eraserPaint.style = Paint.Style.STROKE
        canvas.drawRect(frameRect, eraserPaint)
    }

    fun startAnimation() {
        runAnimation = true
        showLine = true
        invalidate()
    }

    fun stopAnimation() {
        removeCallbacks(refreshRunnable)
        runAnimation = false
        showLine = false
        reset()
        invalidate()
    }

    private fun reset() {
        posY = 0
        isGoingDown = true
    }

    private fun refreshView() {
        if (isGoingDown) {
            if (posY < frameRect.height()) {
                val tempY = min(frameRect.height().toInt(), posY + Y_STEP_PX)
                posY = tempY
                isGoingDown = true
            } else {
                //We invert the direction of the animation
                posY = frameRect.height().toInt()
                isGoingDown = false
            }
        } else {
            if (posY > 0) {
                val tempY = max(0, posY - Y_STEP_PX)
                posY = tempY
                isGoingDown = false
            } else {
                //We invert the direction of the animation
                posY = 0
                isGoingDown = true
            }
        }

        invalidate()
    }
}
