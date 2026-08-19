package com.example.generator2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.compose.ui.unit.sp
import timber.log.Timber
import kotlin.math.abs

/**
 * Водопад спектра: сверху полоски текущего кадра, ниже уезжающая история.
 *
 * Расчёт отметок частотной шкалы вынесен в FrequencyScale.kt, здесь остаётся
 * только отрисовка и разбор касаний.
 */
class WaterfallView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private val white: Paint
    private val gray: Paint
    private val black: Paint

    private val drawPaint: Paint

    private var mBitmap: Bitmap? = null

    private val mBarsHeight = 150
    private val minFrequencyHz = 0
    private val maxFrequencyHz = 5_000

    /** Логарифмическая шкала не определена в нуле, поэтому для неё нижняя граница поднимается. */
    private val logMinFrequencyHz = 20

    private val scalerMinFrequencyHz: Int
        get() = if (mLogX) maxOf(minFrequencyHz, logMinFrequencyHz) else minFrequencyHz

    private var mLogX = false
    private var mLogY = true

    var showDebugInfo: Boolean = true

    //Область полосок текущего кадра, пересчитывается при смене размера вью
    private val bars = Rect()

    private val viewport = Viewport()

    var xxx: Float = 0f
    var yyy: Float = 0f

    var mMeasuring: Boolean = false

    var logX: Boolean
        get() = mLogX
        set(b) {
            mLogX = b
            updateScaler()
        }
    var logY: Boolean
        get() = mLogY
        set(b) {
            mLogY = b
            updateScaler()
        }

    init {

        white = Paint()
        white.color = Color.WHITE
        val scaledSize = 20.sp.value
        white.textSize = scaledSize.toFloat()

        gray = Paint()
        gray.color = Color.GRAY
        gray.alpha = 128 + 64

        black = Paint()
        black.color = Color.BLACK
        black.alpha = 255

        drawPaint = Paint()
        drawPaint.isAntiAlias = false
        drawPaint.isFilterBitmap = false

        viewport.Init(this)
    }

    fun clearWaterfall() {
        Spectrogram.ResetScanline()
        mBitmap?.eraseColor(Color.BLACK)
    }

    private fun updateScaler() {
        Timber.i("!!! WaterfallView updateScaler() start")
        val bitmap = mBitmap ?: return
        Spectrogram.SetScaler(
            bitmap.width,
            scalerMinFrequencyHz.toDouble(),
            maxFrequencyHz.toDouble(),
            mLogX,
            mLogY,
        )
        Timber.i("!!! WaterfallView updateScaler() end")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Timber.i("!!! WaterfallView onSizeChanged() start")
        if (w > 0 && h > 0) {
            Timber.i("!!! WaterfallView onSizeChanged() w:$w px  h:$h px")
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
            mBitmap = bitmap
            bars.set(0, 0, bitmap.width, mBarsHeight)
            updateScaler()
            Spectrogram.Init(bitmap)
        } else {
            mBitmap = null
        }
        Timber.i("!!! WaterfallView onSizeChanged() end")
    }

    fun SetMeasuring(measuring: Boolean) {
        mMeasuring = measuring
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mMeasuring) {
            xxx = event.getX(0)
            yyy = event.getY(0)
        } else {
            viewport.onTouchEvent(event)
        }
        return true
    }

    /**
     * История как кольцевой буфер: сверху рисуем хвост от currentRow до конца
     * картинки, следом начало — так шов уезжает вместе с новыми строками.
     */
    private fun drawWaterfall(canvas: Canvas, bitmap: Bitmap, currentRow: Int, barsHeight: Int) {

        val topHalf = (barsHeight + 1) + bitmap.height - currentRow

        canvas.drawBitmap(
            bitmap,
            Rect(0, currentRow, bitmap.width, bitmap.height),
            Rect(0, barsHeight + 1, bitmap.width, topHalf),
            null
        )

        canvas.drawBitmap(
            bitmap,
            Rect(0, barsHeight + 1, bitmap.width, currentRow),
            Rect(0, topHalf, bitmap.width, bitmap.height),
            null
        )

        canvas.drawRect(Rect(0, bitmap.height - 4, bitmap.width, bitmap.height), black)

    }

    override fun onDraw(canvas: Canvas) {

        val bitmap = mBitmap

        canvas.save()
        canvas.translate(viewport.GetPos().x, 0f)
        canvas.scale(viewport.GetScale().x, 1f)

        if (bitmap != null) {

            val currentRow = Spectrogram.Lock(bitmap)

            if (currentRow >= 0) {
                // draw bars
                canvas.drawBitmap(bitmap, bars, bars, drawPaint)
                drawWaterfall(canvas, bitmap, currentRow, mBarsHeight)
            }
            Spectrogram.Unlock(bitmap)
            drawFrequencyGrid(canvas)

        }
        canvas.restore()

        if (bitmap != null) {
            drawFrequencyScale(canvas)
        }

        //Отображение дебаг иформации
        if (showDebugInfo) {
            drawDebugInfo(canvas)
        }

        if (mMeasuring) {
            drawMeasurement(canvas)
        }

        viewport.EnforceMinimumSize()

        invalidate()

    }

    private fun drawDebugInfo(canvas: Canvas) {
        val x = 10
        var y = (250 + white.descent() - white.ascent()).toInt()
        val text = Spectrogram.GetDebugInfo() ?: return
        for (line in text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            canvas.drawText(line, x.toFloat(), y.toFloat(), white)
            y = (y + (white.descent() - white.ascent())).toInt()
        }
    }

    /** Курсор измерения: частота под пальцем, ближайший пик и сетка по времени. */
    private fun drawMeasurement(canvas: Canvas) {
        val xx = viewport.fromScreenSpace(xxx)

        val cursorFrequency = Spectrogram.XToFreq(xx.toDouble())
        canvas.drawText(
            String.format("%d Hz", cursorFrequency.toInt()),
            xxx,
            yyy - mBarsHeight,
            white
        )

        // Окно поиска берём по ширине пикселя, чтобы курсор цеплял тот пик,
        // на который наведён, а не соседний.
        val searchHz = abs(
            Spectrogram.XToFreq(viewport.fromScreenSpace(xxx + PEAK_SEARCH_PX).toDouble()) -
                    cursorFrequency
        ).coerceAtLeast(MIN_PEAK_SEARCH_HZ)

        val peakFrequency =
            Spectrogram.findPeakFreq(cursorFrequency.toDouble(), searchHz.toDouble())
        if (peakFrequency >= 0f) {
            canvas.drawText(
                String.format("^ %.2f Hz", peakFrequency),
                xxx,
                yyy - mBarsHeight + (white.descent() - white.ascent()),
                white
            )
        }

        canvas.drawLine(xxx, 0f, xxx, height.toFloat(), white)

        // draw time graphs
        var delta = linesPerSecond()
        if (delta * 60 < 100) {
            delta *= 60f
        }

        for (i in 1..59) {
            val yy = yyy - i * delta
            canvas.drawLine(xxx - 20, yy, xxx + 20, yy, white)
        }
    }

    private fun linesPerSecond(): Float {
        val delta = 1f
        //val delta = (48000.0f / (Spectrogram.getFftLength() * (1.0f - Spectrogram.GetOverlap())))
        //delta /= Spectrogram.GetAverageCount().toFloat()
        return delta
    }

    /** Вертикальные линии сетки по всей высоте водопада. */
    private fun drawFrequencyGrid(canvas: Canvas) {
        val scaleTop = mBarsHeight - 24f
        for (tick in visibleTicks()) {
            val x = Spectrogram.FreqToX(tick.frequencyHz.toDouble())
            if (x !in 0f..width.toFloat()) continue

            gray.alpha = if (tick.isMajor) 160 else 72
            canvas.drawLine(x, 0f, x, scaleTop, gray)
        }
        gray.alpha = 192
    }

    /** Линейка с подписями поверх полосок. Подписи не наезжают друг на друга. */
    private fun drawFrequencyScale(canvas: Canvas) {
        val scaleTop = mBarsHeight - 24f
        var previousLabelRight = Float.NEGATIVE_INFINITY

        canvas.drawRect(0f, scaleTop, width.toFloat(), mBarsHeight.toFloat(), black)
        for (tick in visibleTicks()) {
            val x = viewport.toScreenSpace(Spectrogram.FreqToX(tick.frequencyHz.toDouble()))
            if (x !in 0f..width.toFloat()) continue

            gray.alpha = if (tick.isMajor) 192 else 96
            val tickHeight = if (tick.isMajor) 7f else 4f
            canvas.drawLine(x, scaleTop, x, scaleTop + tickHeight, gray)
            if (!tick.isMajor) continue

            val label = formatFrequency(tick.frequencyHz)
            val labelWidth = white.measureText(label)
            val labelLeft = (x - labelWidth / 2).coerceIn(0f, width - labelWidth)
            if (labelLeft < previousLabelRight + 8f) continue

            canvas.drawText(label, labelLeft, mBarsHeight - 5f, white)
            previousLabelRight = labelLeft + labelWidth
        }
        gray.alpha = 192
    }

    /** Границы видимого диапазона зависят от текущего зума вьюпорта. */
    private fun visibleTicks(): List<FrequencyTick> {
        val leftFrequency = Spectrogram.XToFreq(viewport.fromScreenSpace(0f).toDouble())
        val rightFrequency =
            Spectrogram.XToFreq(viewport.fromScreenSpace(width.toFloat()).toDouble())
        val range = scalerMinFrequencyHz.toFloat()..maxFrequencyHz.toFloat()

        return frequencyTicks(
            visibleMin = minOf(leftFrequency, rightFrequency).coerceIn(range),
            visibleMax = maxOf(leftFrequency, rightFrequency).coerceIn(range),
            logarithmic = mLogX,
            widthPx = width
        )
    }

    companion object {

        /** Полуширина окна поиска пика в пикселях экрана. */
        private const val PEAK_SEARCH_PX = 20f

        /** Нижняя граница окна поиска: на сильном зуме пиксель уже одного бина. */
        private const val MIN_PEAK_SEARCH_HZ = 5f
    }
}
