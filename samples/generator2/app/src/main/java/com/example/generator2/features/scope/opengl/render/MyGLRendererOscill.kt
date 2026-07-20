package com.example.generator2.features.scope.opengl.render

import android.opengl.GLES30.GL_CLAMP_TO_EDGE
import android.opengl.GLES30.GL_COLOR_BUFFER_BIT
import android.opengl.GLES30.GL_COMPILE_STATUS
import android.opengl.GLES30.GL_FLOAT
import android.opengl.GLES30.GL_FRAGMENT_SHADER
import android.opengl.GLES30.GL_LINEAR
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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** Развёртки от этого значения и выше рисуются как бегущая лента. */
private const val ROLL_THRESHOLD = 32f

/** Подобранное усиление тонмаппинга: ядро луча выходит на полную яркость. */
private const val TONEMAP_GAIN = 6.0f

class MyGLRendererOscill : GLSurfaceView.Renderer {

    private var program: Int = 0
    private var vertexShader: Int = 0
    private var fragmentShader: Int = 0

    private var texture: Int = 0
    private var textureColumns: Int = 0

    private var gridBuffer: ByteBuffer? = null
    private var configuredColumns: Int = 0
    private var configuredLayout: Int = -1
    private var configuredRoll: Boolean? = null

    var compressorCount: Float = 0f

    val bools = intArrayOf(0, 1, 1) //oneTwo 0-one 1-two, L 1-true, R

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
    vec2 energy = texture(grid, vec2(fract(uv.x + ringOffset), uv.y)).rg;

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

        // Текстура пересоздаётся при первом кадре с известной шириной.
        textureColumns = 0
        configuredColumns = 0
        configuredLayout = -1
        configuredRoll = null
    }

    override fun onDrawFrame(gl: GL10?) {
        if (!shouldPlay.get() || program == 0 || width <= 0) {
            return
        }

        val rollMode = compressorCount >= ROLL_THRESHOLD
        val layout = bools[0]

        if (configuredColumns != width ||
            configuredLayout != layout ||
            configuredRoll != rollMode
        ) {
            NativePhosphor.configure(width, layout, rollMode)
            configuredColumns = width
            configuredLayout = layout
            configuredRoll = rollMode
            gridBuffer = null
            ensureTexture(width)
        }

        val range = NativePhosphor.update()
        uploadColumns(range[0], range[1])

        glClear(GL_COLOR_BUFFER_BIT)
        glUseProgram(program)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture)
        glUniform1i(glGetUniformLocation(program, "grid"), 0)
        glUniform1f(glGetUniformLocation(program, "ringOffset"), NativePhosphor.ringOffset())
        glUniform1f(glGetUniformLocation(program, "gain"), TONEMAP_GAIN)
        glUniform2f(
            glGetUniformLocation(program, "visibility"),
            if (bools[2] == 1) 1.0f else 0.0f,
            if (bools[1] == 1) 1.0f else 0.0f
        )

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
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
        glTexStorage2D(GL_TEXTURE_2D, 1, GL_RG16F, columns, NativePhosphor.BINS)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        // По горизонтали кольцо, поэтому фильтрация должна заворачиваться.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        textureColumns = columns
    }

    /** Заливает грязный кольцевой диапазон, разбивая его на куски по краю сетки. */
    private fun uploadColumns(start: Int, count: Int) {
        if (count <= 0 || textureColumns <= 0) {
            return
        }

        val buffer = gridBuffer ?: NativePhosphor.gridBuffer()?.also {
            it.order(ByteOrder.nativeOrder())
            gridBuffer = it
        } ?: return

        glBindTexture(GL_TEXTURE_2D, texture)

        val columnBytes = NativePhosphor.BINS * 2 * 4
        var offset = 0
        while (offset < count) {
            val column = (start + offset) % textureColumns
            val chunk = minOf(count - offset, textureColumns - column)

            buffer.position(column * columnBytes)
            glTexSubImage2D(
                GL_TEXTURE_2D, 0,
                column, 0,
                chunk, NativePhosphor.BINS,
                GL_RG, GL_FLOAT,
                buffer.slice().order(ByteOrder.nativeOrder())
            )
            offset += chunk
        }
        buffer.position(0)
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
        }
        if (program != 0) {
            glDeleteProgram(program)
            program = 0
        }
        gridBuffer = null
    }

    private val shouldPlay = AtomicBoolean(false)

    fun onResume() {
        shouldPlay.compareAndSet(false, true)
    }

    fun onPause() {
        shouldPlay.compareAndSet(true, false)
    }
}
