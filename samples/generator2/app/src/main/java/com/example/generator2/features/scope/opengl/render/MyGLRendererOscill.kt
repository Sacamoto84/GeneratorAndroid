package com.example.generator2.features.scope.opengl.render

import android.opengl.GLES30.GL_CLAMP_TO_EDGE
import android.opengl.GLES30.GL_COLOR_BUFFER_BIT
import android.opengl.GLES30.GL_COMPILE_STATUS
import android.opengl.GLES30.GL_FRAGMENT_SHADER
import android.opengl.GLES30.GL_HALF_FLOAT
import android.opengl.GLES30.GL_LINEAR
import android.opengl.GLES30.GL_MAX_TEXTURE_SIZE
import android.opengl.GLES30.GL_RG
import android.opengl.GLES30.GL_RG16F
import android.opengl.GLES30.GL_REPEAT
import android.opengl.GLES30.GL_TEXTURE0
import android.opengl.GLES30.GL_TEXTURE_2D
import android.opengl.GLES30.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES30.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES30.GL_TEXTURE_WRAP_S
import android.opengl.GLES30.GL_TEXTURE_WRAP_T
import android.opengl.GLES30.GL_TRIANGLE_STRIP
import android.opengl.GLES30.GL_VERTEX_SHADER
import android.opengl.GLES30.glActiveTexture
import android.opengl.GLES30.glAttachShader
import android.opengl.GLES30.glBindTexture
import android.opengl.GLES30.glClear
import android.opengl.GLES30.glClearColor
import android.opengl.GLES30.glCompileShader
import android.opengl.GLES30.glCreateProgram
import android.opengl.GLES30.glCreateShader
import android.opengl.GLES30.glDeleteProgram
import android.opengl.GLES30.glDeleteShader
import android.opengl.GLES30.glDeleteTextures
import android.opengl.GLES30.glDrawArrays
import android.opengl.GLES30.glGenTextures
import android.opengl.GLES30.glGetIntegerv
import android.opengl.GLES30.glGetShaderInfoLog
import android.opengl.GLES30.glGetShaderiv
import android.opengl.GLES30.glGetUniformLocation
import android.opengl.GLES30.glLinkProgram
import android.opengl.GLES30.glShaderSource
import android.opengl.GLES30.glTexParameteri
import android.opengl.GLES30.glTexStorage2D
import android.opengl.GLES30.glTexSubImage2D
import android.opengl.GLES30.glUniform1f
import android.opengl.GLES30.glUniform1i
import android.opengl.GLES30.glUniform2f
import android.opengl.GLES30.glUseProgram
import android.opengl.GLES30.glViewport
import android.opengl.GLSurfaceView
import com.example.generator2.features.scope.NativePhosphor
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.math.floor

/** Развёртки от этого значения и выше рисуются как бегущая лента. */
private const val ROLL_THRESHOLD = 32f

/**
 * Яркость. Задаётся в единицах "след, размазанный ровно по всей высоте
 * экрана": при 1.0 такой след выходит на 63% яркости, при 2.0 — на 86%.
 * Плотные участки, вроде вершин синуса, уходят в насыщение раньше, редкие
 * остаются тусклым ореолом — это и есть фосфор.
 */
private const val TONEMAP_GAIN = 2.0f

/** Верхняя граница ширины сетки, совпадает с PhosphorGrid::kMaxColumns. */
private const val MAX_COLUMNS = 4096

/**
 * Временная диагностика микрофризов: раз в 60 кадров пишет в logcat, сколько
 * времени съели update() и заливка текстуры. Снять, когда причина найдена.
 */
private const val DIAG = true

/**
 * Коэффициенты следящего фильтра смещения кольца, критическое затухание при
 * собственной частоте 15 рад/с: расхождение закрывается примерно за четверть
 * секунды без перерегулирования. POSITION_GAIN = 2*w, RATE_GAIN = w*w.
 */
private const val POSITION_GAIN = 30.0f
private const val RATE_GAIN = 225.0f

/** Пауза кадров дольше этого — не догоняем, а прыгаем. */
private const val MAX_SMOOTH_STEP_SEC = 0.1f

/** Скачок смещения больше четверти оборота — это переконфигурация, не бег. */
private const val MAX_SMOOTH_DELTA = 0.25f

/**
 * Как часто забираем новые данные и заливаем текстуру.
 *
 * Величина должна делить герцовку экрана нацело, иначе порции данных лягут
 * между вертикальными синхронизациями неравномерно — именно это и давало
 * дёрганье, когда обновление шло по приходу аудиопакетов, то есть 38 раз в
 * секунду. 60 делится и на 60, и на 120. 30 тоже делится и оставляет вчетверо
 * больший запас по времени, но шаг движения выходит вдвое крупнее.
 *
 * Само движение ленты этим не ограничено: смещение кольца сглаживается на
 * каждом кадре, на полной герцовке экрана.
 */
private const val UPDATE_HZ = 60f

/**
 * Порог срабатывания с запасом: ровно UPDATE_HZ брать нельзя, два интервала
 * по 8.33 мс дают 16.66 мс против 16.67 мс порога, и обновление проскочило бы
 * на третий рефреш.
 */
private const val UPDATE_INTERVAL_NS = (0.9e9f / UPDATE_HZ).toLong()

class MyGLRendererOscill : GLSurfaceView.Renderer {

    private var program: Int = 0
    private var vertexShader: Int = 0
    private var fragmentShader: Int = 0

    private var gridHandle: Int = -1
    private var ringOffsetHandle: Int = -1
    private var gainHandle: Int = -1
    private var visibilityHandle: Int = -1

    private var texture: Int = 0
    private var textureColumns: Int = 0
    private var textureHasData: Boolean = false
    private var maxTextureSize: Int = 0

    private var configuredColumns: Int = 0
    private var configuredLayout: Int = -1
    private var configuredRoll: Boolean? = null

    var compressorCount: Float = 0f

    /**
     * Пауза. Аккумулятор продолжает наполняться из аудиопотока, поэтому на
     * паузе нельзя ни забирать грязный диапазон, ни двигать кольцо — иначе
     * застывший кадр поедет вперёд по несвежей текстуре.
     */
    @Volatile
    var isPaused: Boolean = false

    private var frozenRingOffset: Float = 0f

    private var smoothingActive = false
    private var smoothOffset = 0f
    private var smoothRate = 0f
    private var smoothLastNs = 0L

    private var lastUpdateNs = 0L

    val bools = intArrayOf(0, 1, 1) //oneTwo 0-one 1-two, L 1-true, R

    private var diagFrames = 0
    private var diagUpdateNs = 0L
    private var diagUploadNs = 0L
    private var diagColumns = 0L
    private var diagWorstNs = 0L
    private var diagWorstUpdateNs = 0L
    private var diagWorstUploadNs = 0L
    private var diagPrevEndNs = 0L
    private var diagWorstGapNs = 0L

    private val vertexShaderCode =
        """
#version 300 es

out vec2 uv;

void main() {
    // Полноэкранный quad из четырёх вершин без буфера вершин.
    vec2 corner = vec2(float(gl_VertexID & 1), float((gl_VertexID >> 1) & 1));
    uv = corner;
    gl_Position = vec4(corner * 2.0 - 1.0, 0.0, 1.0);
}
""".trimIndent()

    private val fragmentShaderCode =
        """
#version 300 es
precision mediump float;

uniform sampler2D grid;
uniform float ringOffset;
uniform float gain;
uniform vec2 visibility;

in vec2 uv;
out vec4 fragColor;

void main() {
    // Текстура развёрнута под column-major память сетки: по X идут бины,
    // по Y — столбцы. Поэтому оси меняются местами.
    vec2 energy = texture(grid, vec2(uv.y, fract(uv.x + ringOffset))).rg;

    // Мягкое насыщение: ядро луча яркое, ореол остаётся плавным.
    float first = 1.0 - exp(-energy.r * gain);
    float second = 1.0 - exp(-energy.g * gain);

    vec3 color = vec3(1.0, 0.0, 1.0) * first * visibility.x
               + vec3(1.0, 1.0, 0.0) * second * visibility.y;

    fragColor = vec4(color + vec3(0.0, 0.15, 0.0), 1.0);
}
""".trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.15f, 0.0f, 1f)

        vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
        fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = glCreateProgram().also {
            glAttachShader(it, vertexShader)
            glAttachShader(it, fragmentShader)
            glLinkProgram(it)
        }

        glUseProgram(program)

        gridHandle = glGetUniformLocation(program, "grid")
        ringOffsetHandle = glGetUniformLocation(program, "ringOffset")
        gainHandle = glGetUniformLocation(program, "gain")
        visibilityHandle = glGetUniformLocation(program, "visibility")

        val limit = IntArray(1)
        glGetIntegerv(GL_MAX_TEXTURE_SIZE, limit, 0)
        maxTextureSize = limit[0]

        // Контекст новый: прежние объекты умерли вместе со старым.
        texture = 0
        textureColumns = 0
        textureHasData = false
        configuredColumns = 0
        configuredLayout = -1
        configuredRoll = null
    }

    /**
     * Ширина сетки: столбец на пиксель экрана. Не может превышать ни лимит
     * GL, ни лимит аккумулятора.
     */
    private fun columnsFor(surfaceWidth: Int): Int {
        val limit = if (maxTextureSize > 0) maxTextureSize else MAX_COLUMNS
        return maxOf(1, minOf(surfaceWidth, limit, MAX_COLUMNS))
    }

    override fun onDrawFrame(gl: GL10?) {
        if (!shouldPlay.get() || program == 0 || width <= 0) {
            return
        }

        if (isPaused) {
            // Фильтр останавливаем: иначе после снятия паузы он будет
            // догонять всё, что кольцо накрутило, пока кадр стоял.
            smoothingActive = false
            drawGrid(frozenRingOffset)
            return
        }

        val rollMode = compressorCount >= ROLL_THRESHOLD
        val layout = bools[0]
        val columns = columnsFor(width)

        if (configuredColumns != columns ||
            configuredLayout != layout ||
            configuredRoll != rollMode
        ) {
            NativePhosphor.configure(columns, layout, rollMode)
            configuredColumns = columns
            configuredLayout = layout
            configuredRoll = rollMode
            ensureTexture(columns)
            // Смещение теперь считается от другой сетки — начинаем заново.
            smoothingActive = false
        }

        val startNs = System.nanoTime()

        // Данные забираем на своей частоте, кратной герцовке экрана. Движение
        // ленты при этом идёт каждый кадр — сглаживание ниже.
        if (startNs - lastUpdateNs >= UPDATE_INTERVAL_NS) {
            lastUpdateNs = startNs

            val range = NativePhosphor.update()
            if (range == null) {
                // Рисуем всё равно: режим непрерывный, и молчаливый выход
                // подменил бы буфер неотрисованным содержимым.
                drawGrid(frozenRingOffset)
                return
            }

            val updatedNs = if (DIAG) System.nanoTime() else 0L

            uploadColumns(range[0], range[1])

            if (DIAG) {
                recordDiagnostics(startNs, updatedNs, range[1])
            }
        }

        frozenRingOffset = smoothRingOffset(NativePhosphor.ringOffset())
        drawGrid(frozenRingOffset)
    }

    /**
     * Ведёт ленту между рывками смещения.
     *
     * Кольцо двигает аудиопоток, а он приходит порциями по 26 мс: смещение
     * прыгает сразу на несколько столбцов и потом стоит, пока экран успевает
     * обновиться три раза. Прыжки видно как дёрганье.
     *
     * Забегать вперёд нельзя — за головой записи лежат нестёртые столбцы с
     * прошлого оборота. Поэтому идём следом: интегратор держит скорость
     * ленты, пропорциональная часть не даёт расхождению накопиться.
     * Отставание выходит порядка четверти секунды при окне в шесть секунд.
     */
    private fun smoothRingOffset(target: Float): Float {
        val nowNs = System.nanoTime()

        if (!smoothingActive) {
            smoothingActive = true
            smoothLastNs = nowNs
            smoothOffset = target
            smoothRate = 0f
            return target
        }

        val dt = (nowNs - smoothLastNs) / 1_000_000_000f
        smoothLastNs = nowNs

        if (dt <= 0f || dt > MAX_SMOOTH_STEP_SEC) {
            smoothOffset = target
            smoothRate = 0f
            return target
        }

        // Кратчайший путь по кольцу: цель могла перевалить через край.
        var delta = target - smoothOffset
        if (delta > 0.5f) delta -= 1.0f
        if (delta < -0.5f) delta += 1.0f

        if (abs(delta) > MAX_SMOOTH_DELTA) {
            smoothOffset = target
            smoothRate = 0f
            return target
        }

        smoothRate += delta * RATE_GAIN * dt
        smoothOffset += (smoothRate + delta * POSITION_GAIN) * dt
        smoothOffset -= floor(smoothOffset)

        return smoothOffset
    }

    /** Выводит сетку на экран с заданным смещением кольца. */
    private fun drawGrid(ringOffset: Float) {
        glClear(GL_COLOR_BUFFER_BIT)

        // До первой заливки содержимое текстуры не определено — показываем
        // только фон, иначе на экран попадёт мусор из видеопамяти.
        if (!textureHasData) {
            return
        }

        glUseProgram(program)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture)
        glUniform1i(gridHandle, 0)
        glUniform1f(ringOffsetHandle, ringOffset)
        // Энергия столбца нормирована к единице, значит на один бин
        // приходится порядка 1/BINS. Приводим усиление к этому масштабу,
        // иначе константа теряет смысл при смене числа бинов.
        glUniform1f(gainHandle, TONEMAP_GAIN * NativePhosphor.BINS)
        glUniform2f(
            visibilityHandle,
            if (bools[2] == 1) 1.0f else 0.0f,
            if (bools[1] == 1) 1.0f else 0.0f
        )

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
    }

    /**
     * Копит статистику кадра и раз в 60 кадров сбрасывает её в logcat.
     *
     * update — сколько ждали нативную сторону, включая мьютекс аудиопотока.
     * upload — сколько заняла заливка текстуры.
     * cols — средний размер грязного диапазона.
     * gapMax — самый большой промежуток между обновлениями данных. Ждём здесь
     * ровный интервал 1/UPDATE_HZ: разброс означает, что кадр не уложился в
     * рефреш и уехал на следующий.
     */
    private fun recordDiagnostics(startNs: Long, updatedNs: Long, columns: Int) {
        val endNs = System.nanoTime()

        diagUpdateNs += updatedNs - startNs
        diagUploadNs += endNs - updatedNs
        diagColumns += columns.toLong()
        diagWorstNs = maxOf(diagWorstNs, endNs - startNs)
        // Выброс редкий, поэтому важно знать не сумму, а кто именно выбросил:
        // ожидание мьютекса в update() или синхронизация с GPU в заливке.
        diagWorstUpdateNs = maxOf(diagWorstUpdateNs, updatedNs - startNs)
        diagWorstUploadNs = maxOf(diagWorstUploadNs, endNs - updatedNs)
        if (diagPrevEndNs != 0L) {
            diagWorstGapNs = maxOf(diagWorstGapNs, startNs - diagPrevEndNs)
        }
        diagPrevEndNs = endNs

        if (++diagFrames < 60) {
            return
        }

        android.util.Log.d(
            "PHOSPHOR",
            "update=${diagUpdateNs / diagFrames / 1000}us" +
                " upload=${diagUploadNs / diagFrames / 1000}us" +
                " cols=${diagColumns / diagFrames}" +
                " worstUpd=${diagWorstUpdateNs / 1000}us" +
                " worstUpl=${diagWorstUploadNs / 1000}us" +
                " gapMax=${diagWorstGapNs / 1000}us"
        )

        diagFrames = 0
        diagUpdateNs = 0
        diagUploadNs = 0
        diagColumns = 0
        diagWorstNs = 0
        diagWorstUpdateNs = 0
        diagWorstUploadNs = 0
        diagWorstGapNs = 0
    }

    private fun ensureTexture(columns: Int) {
        if (textureColumns == columns && texture != 0) {
            return
        }
        if (texture != 0) {
            glDeleteTextures(1, intArrayOf(texture), 0)
            texture = 0
        }

        val handles = IntArray(1)
        glGenTextures(1, handles, 0)
        texture = handles[0]

        glBindTexture(GL_TEXTURE_2D, texture)
        // Развёрнуто под column-major память: X — бины, Y — столбцы.
        // Тогда один столбец сетки это одна непрерывная строка текстуры.
        glTexStorage2D(GL_TEXTURE_2D, 1, GL_RG16F, NativePhosphor.BINS, columns)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        // Кольцо идёт по Y, фильтрация должна заворачиваться именно там.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

        textureColumns = columns
        textureHasData = false

        // Текстура новая и пустая, но configure() мог уйти в быстрый выход и
        // ничего не пометить грязным. Просим сетку отдать себя целиком.
        NativePhosphor.invalidate()
    }

    /** Заливает грязный кольцевой диапазон, разбивая его на куски по краю сетки. */
    private fun uploadColumns(start: Int, count: Int) {
        if (count <= 0 || textureColumns <= 0) {
            return
        }

        // Буфер запрашивается заново каждый кадр: configure() перевыделяет
        // нативную память, и сохранённая ссылка может протухнуть.
        val buffer = NativePhosphor.gridBuffer() ?: return
        buffer.order(ByteOrder.nativeOrder())

        glBindTexture(GL_TEXTURE_2D, texture)

        // Два канала по два байта: сетка приходит уже в half-float.
        val columnBytes = NativePhosphor.BINS * 2 * 2
        var offset = 0
        while (offset < count) {
            val column = (start + offset) % textureColumns
            val chunk = minOf(count - offset, textureColumns - column)

            buffer.position(column * columnBytes)
            glTexSubImage2D(
                GL_TEXTURE_2D, 0,
                0, column,
                NativePhosphor.BINS, chunk,
                GL_RG, GL_HALF_FLOAT,
                buffer.slice().order(ByteOrder.nativeOrder())
            )
            offset += chunk
        }

        textureHasData = true
    }

    var width: Int = 0
    var height: Int = 0

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height
        glViewport(0, 0, width, height)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return glCreateShader(type).also { shader ->
            glShaderSource(shader, shaderCode)
            glCompileShader(shader)

            val compiled = IntArray(1)
            glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                glDeleteShader(shader)
                throw RuntimeException(
                    "Could not compile shader $type: ${glGetShaderInfoLog(shader)}"
                )
            }
        }
    }

    fun deleteProgram() {
        if (texture != 0) {
            glDeleteTextures(1, intArrayOf(texture), 0)
            texture = 0
            textureColumns = 0
            textureHasData = false
        }
        if (program != 0) {
            glDeleteProgram(program)
            program = 0
        }
    }

    private val shouldPlay = AtomicBoolean(false)

    fun onResume() {
        shouldPlay.compareAndSet(false, true)
    }

    fun onPause() {
        shouldPlay.compareAndSet(true, false)
    }
}
