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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import timber.log.Timber
import java.time.format.DateTimeFormatter


@Composable
fun WaterfallComposeView() {

    val context = LocalContext.current

    val waterfallView = remember {
        WaterfallView(context, null).apply {

        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(color = androidx.compose.ui.graphics.Color.Black),
        factory = {
            waterfallView
        },
    )

}

class WaterfallView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private val white: Paint
    private val gray: Paint
    private val black: Paint

    private val drawPaint: Paint

    private var mBitmap: Bitmap? = null

    private val mBarsHeight = 150

    private var mLogX = false
    private var mLogY = true

    var showDebugInfo: Boolean = true

    private var mFormatter: DateTimeFormatter? = null
    private var mScaleTimeAtTop: Long = 0
    private var mSecondsPerScreen = 0f

    var scaleType: Int = 0

    private var bars: Rect? = null

    private var delay: Long = 0

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

    fun clearWaterfall() {
        Spectrogram.ResetScanline()

        val waterfallLines = height - mBarsHeight
        mSecondsPerScreen = waterfallLines / linesPerSecond()
        mScaleTimeAtTop = System.currentTimeMillis()

        if (mBitmap != null) mBitmap!!.eraseColor(Color.BLACK)
    }


    private fun updateScaler() {
        Timber.i("!!! WaterfallView updateScaler() start")
        if (mBitmap == null) return
        Spectrogram.SetScaler(mBitmap!!.width, 100.0, (20000 / 2).toDouble(), mLogX, mLogY)
        Timber.i("!!! WaterfallView updateScaler() end")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Timber.i("!!! WaterfallView onSizeChanged() start")
        if (w > 0 && h > 0) {
            Timber.i("!!! WaterfallView onSizeChanged() w:$w px  h:$h px")
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
            bars = Rect(0, 0, mBitmap!!.width, mBarsHeight)
            updateScaler()
            Spectrogram.Init(mBitmap)
        } else {
            mBitmap = null
        }
        Timber.i("!!! WaterfallView onSizeChanged() end")
    }


    init {

        mFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
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

    fun SetMeasuring(measuring: Boolean) {
        mMeasuring = measuring
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mMeasuring) {
            xxx = event.getX(0)
            yyy = event.getY(0)

            if (yyy <= mBarsHeight) {
                Spectrogram.HoldData()
            }
        } else {
            viewport.onTouchEvent(event)
        }
        return true
    }

    private fun drawWaterfall(canvas: Canvas, currentRow: Int, barsHeight: Int) {

        //Timber.i("!!! currentRow $currentRow")

        // draw waterfall
        val topHalf = (barsHeight + 1) + mBitmap!!.height - currentRow

        canvas.drawBitmap(
            mBitmap!!,
            Rect(0, currentRow , mBitmap!!.width, mBitmap!!.height),
            Rect(0, barsHeight + 1, mBitmap!!.width, topHalf),
            null
        )

        canvas.drawBitmap(
            mBitmap!!,
            Rect(0, barsHeight + 1, mBitmap!!.width, currentRow),
            Rect(0, topHalf, mBitmap!!.width, mBitmap!!.height),
            null
        )

        canvas.drawRect(Rect(0, mBitmap!!.height - 4, mBitmap!!.width, mBitmap!!.height), black)

    }

    private fun lerpf(t: Float, a: Float, b: Float): Float {
        return a + ((b - a) * t)
    }

    private fun lerpu(t: Float, a: Long, b: Long): Long {
        return a + (((b - a).toFloat()) * t).toLong()
    }

    private fun unlerpu(a: Long, b: Long, t: Long): Float {
        return (((t - a).toDouble()) / ((b - a).toDouble())).toFloat()
    }

    override fun onDraw(canvas: Canvas) {

        //Timber.i("!!! WaterfallView onDraw() start")

        canvas.save()
        canvas.translate(viewport.GetPos().x, 0f)
        canvas.scale(viewport.GetScale().x, 1f)

        if (mBitmap != null) {

            val currentRow = Spectrogram.Lock(mBitmap)
            //Timber.i("!!! currentRow: $currentRow")


//
            if (currentRow >= 0) {
                // draw bars

                canvas.drawBitmap(mBitmap!!, bars, bars!!, drawPaint)

                drawWaterfall(canvas, currentRow, mBarsHeight)

            }

            Spectrogram.Unlock(mBitmap)

        }
        canvas.restore()

//        run {
//            val dp = Spectrogram.GetDroppedFrames()
//            if (dp > 0) delay = System.currentTimeMillis()
//            if (delay > 0) {
//                canvas.drawText("Overload!! Dropping audio frames", 10f, 250f, white)
//                if (System.currentTimeMillis() - delay > 500) delay = 0
//            }
//        }

//        //Отображение дебаг иформации
//        if (showDebugInfo) {
//            val x = 10
//            var y = (250 + white.descent() - white.ascent()).toInt()
//            val text = Spectrogram.GetDebugInfo()
//            if (text != null) {
//                for (line in text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
//                    .toTypedArray()) {
//                    canvas.drawText(line, x.toFloat(), y.toFloat(), white)
//                    y = (y + (white.descent() - white.ascent())).toInt()
//                }
//            }
//        }

//        if (processor == ProcessorMode.FFT) {
//            if (mLogX) {
//                //DrawLogarithmicX(canvas)
//            } else {
//                //DrawLinearX(canvas)
//            }
//        } else if (processor == ProcessorMode.MUSIC) {
//            if (mOverlay == Overlay.PIANO) {
//                DrawPianoOverlay(canvas)
//            } else if (mOverlay == Overlay.GUITAR) {
//                DrawGuitarOverlay(canvas)
//            }
//        }

//        // Draw scale
//        //
//        if (scaleType == 2) {
//            //int seconds = mScaleTimeAtBottom.minus(mScaleTimeAtTop).getSecond();
//            run {
//                val ldt = Instant.ofEpochMilli(mScaleTimeAtTop)
//                    .atZone(ZoneId.systemDefault()).toLocalDateTime()
//                val s = mFormatter!!.format(ldt)
//                canvas.drawText(s, (width - 200).toFloat(), mBarsHeight.toFloat(), white)
//            }
//
//            val mScaleTimeAtBottom = mScaleTimeAtTop + (mSecondsPerScreen * 1000).toLong()
//
//            val rd = (2 * 60 * 1000).toLong()
//
//            for (i in 1..20) {
//                val t = i.toFloat() / 20.0f
//
//                val rt = lerpu(
//                    t,
//                    RoundEpoch(mScaleTimeAtTop + rd, rd),
//                    RoundEpoch(mScaleTimeAtBottom + rd, rd)
//                )
//
//                val rtt = unlerpu(mScaleTimeAtTop, mScaleTimeAtBottom, rt)
//                val y = lerpf(rtt, mBarsHeight.toFloat(), height.toFloat())
//
//                val s = mFormatter!!.format(
//                    Instant.ofEpochMilli(rt).atZone(ZoneId.systemDefault()).toLocalDateTime()
//                )
//                canvas.drawText(s, (width - 200).toFloat(), y, white)
//                canvas.drawLine((width - 50).toFloat(), y, width.toFloat(), y, white)
//            }
//        } else {
//            val fm = white.fontMetrics
//            val fontHeight = fm.descent - fm.ascent
//
//            val lines_per_page = height - mBarsHeight
//            val lines_per_second = linesPerSecond()
//
//            if (lines_per_second != m_old_lines_per_second) {
//                m_old_lines_per_second = lines_per_second
//
//                val seconds_per_page = lines_per_page / lines_per_second
//
//                val lines_per_tick = (fontHeight * 2)
//                val seconds_per_tick = lines_per_tick / lines_per_second
//                val ticks_per_page = lines_per_page / lines_per_tick
//                val desired_ticks_per_page = lines_per_page / lines_per_tick
//
//                // round seconds per tick
//                val anchors = floatArrayOf(
//                    0.001f,
//                    0.005f,
//                    0.01f,
//                    0.05f,
//                    0.1f,
//                    0.5f,
//                    1.0f,
//                    5.0f,
//                    10.0f,
//                    15.0f,
//                    30.0f,
//                    60.0f,
//                    5 * 60.0f,
//                    10 * 60.0f,
//                    20 * 60.0f,
//                    30 * 60.0f,
//                    60 * 60.0f
//                )
//
//                var min_err = 10000
//                for (i in anchors.indices) {
//                    var t = 0f
//                    val a = anchors[i]
//                    t = if (a < 1) Math.round(seconds_per_tick * (a * 100)) / (a * 100)
//                    else Math.round(seconds_per_tick * a) / a
//                    if (t > 0) {
//                        val ticks =
//                            ceil((seconds_per_page / a).toDouble()).toInt()
//                        val err =
//                            abs((desired_ticks_per_page - ticks).toDouble()).toInt()
//                        if (err < min_err) {
//                            min_err = err
//                            m_seconds_per_tick = a
//                        }
//                    }
//                }
//            }
//
//            val lines_per_tick = m_seconds_per_tick * lines_per_second
//
//            var t = 0f
//            var i = mBarsHeight + lines_per_tick.toInt()
//            while (i <= height) {
//                val y = i.toFloat()
//                canvas.drawLine((width - 50).toFloat(), y, width.toFloat(), y, white)
//                t += m_seconds_per_tick
//                if (t < 1) canvas.drawText(
//                    String.format("%.2fs", t),
//                    (width - 200).toFloat(),
//                    y,
//                    white
//                )
//                else canvas.drawText(FormatTime(t.toInt()), (width - 200).toFloat(), y, white)
//                i = (i + lines_per_tick).toInt()
//            }
//        }

        // Draw measurements
        //
//        if (mMeasuring) {
//            val xx = viewport.fromScreenSpace(xxx)
//            var str = "none"
//
//            val freq = Spectrogram.XToFreq(xx.toDouble()).toInt()
//            str = String.format("%d Hz", freq)
//
//            canvas.drawText(str, xxx, yyy - mBarsHeight, white)
//            canvas.drawLine(xxx, 0f, xxx, height.toFloat(), white)
//
//            // draw time graphs
//            var delta = linesPerSecond()
//            if (delta * 60 < 100) {
//                delta *= 60f
//            }
//
//            for (i in 1..59) {
//                val yy = yyy - i * delta
//                canvas.drawLine(xxx - 20, yy, xxx + 20, yy, white)
//            }
//        }

        viewport.EnforceMinimumSize()

        invalidate()


    }


    fun RoundEpoch(t: Long, millis: Long): Long {
        return t - (t % millis)
    }

    private fun linesPerSecond(): Float {
        val delta = (48000.0f / (Spectrogram.getFftLength() * (1.0f - Spectrogram.GetOverlap())))
        //delta /= Spectrogram.GetAverageCount().toFloat()
        return delta
    }

    fun FormatTime(t: Int): String {
        val h = (t / (60 * 60))
        val mm = (t % (60 * 60))
        val m = (mm / 60)
        val s = (mm % 60)

        if (h > 0) return if (s > 0) String.format("%dh%02dm%02ds", h, m, s)
        else String.format("%dh%02dm", h, m)
        else if (m > 0) return if (s > 0) String.format("%02dm%02ds", m, s)
        else String.format("%02dm", m)
        else if (s > 0) return String.format("%02ds", s)

        return String.format("err %d", t)
    }

    private fun DrawLogarithmicX(canvas: Canvas) {
        var d = 1f
        for (j in 0..4) {
            for (i in 0..9) {
                var x = Spectrogram.FreqToX((i * d).toDouble())
                x = viewport.toScreenSpace(x)
                canvas.drawLine(x, 0f, x, canvas.height.toFloat(), gray)
            }
            d *= 10f
        }
    }

    private fun DrawLinearX(canvas: Canvas) {
        var i = 0
        while (i < 48000 / 2) {
            var x = Spectrogram.FreqToX(i.toDouble())
            x = viewport.toScreenSpace(x)
            canvas.drawLine(x, 0f, x, canvas.height.toFloat(), gray)
            i += 1000
        }
    }

    private fun DrawPianoOverlay(canvas: Canvas) {
        val bounds = Rect()
        var n = 4
        while (n < 88) {
            var x = Spectrogram.FreqToX(n.toDouble())
            var x1 = Spectrogram.FreqToX((n + 1).toDouble())

            x = viewport.toScreenSpace(x)
            x1 = viewport.toScreenSpace(x1)

            canvas.drawLine(x, 0f, x, canvas.height.toFloat(), gray)

            val text = getNoteName(n)
            white.getTextBounds(text, 0, text.length, bounds)
            if ((x1 - x) > bounds.width()) canvas.drawText(
                text,
                x,
                (10 + bounds.height()).toFloat(),
                white
            )
            n += 12
        }
    }

    private fun DrawGuitarOverlay(canvas: Canvas) {
        val bounds = Rect()
        val notes = intArrayOf(20, 25, 30, 35, 39, 44)
        for (i in notes.indices) {
            var x = Spectrogram.FreqToX(notes[i].toDouble())
            x = viewport.toScreenSpace(x)
            canvas.drawLine(x, 0f, x, canvas.height.toFloat(), gray)

            var x1 = Spectrogram.FreqToX((notes[i] + 1).toDouble())
            x1 = viewport.toScreenSpace(x1)
            canvas.drawLine(
                x1, 0f, x1, canvas.height.toFloat(),
                gray
            )


            val text = getNoteName(notes[i])
            white.getTextBounds(text, 0, text.length, bounds)
            canvas.drawText(text, x, (10 + bounds.height()).toFloat(), white)
        }
    }

    companion object {
        fun getNoteName(n: Int): String {
            val notes = arrayOf("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")
            val octave = (n + 8) / 12
            val noteIdx = (n - 1) % 12
            return notes[noteIdx] + octave.toString()
        }
    }
}