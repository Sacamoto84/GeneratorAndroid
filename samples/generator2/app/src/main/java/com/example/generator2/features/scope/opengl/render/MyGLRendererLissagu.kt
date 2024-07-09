package com.example.generator2.features.scope.opengl.render

import android.opengl.GLSurfaceView
import android.opengl.GLES10.GL_LIGHTING
import android.opengl.GLES20.glUniform3iv
import android.opengl.GLES30.GL_COLOR_BUFFER_BIT
import android.opengl.GLES30.GL_COMPILE_STATUS
import android.opengl.GLES30.GL_FLOAT
import android.opengl.GLES30.GL_FRAGMENT_SHADER
import android.opengl.GLES30.GL_POINTS
import android.opengl.GLES30.GL_VERTEX_SHADER
import android.opengl.GLES30.glAttachShader
import android.opengl.GLES30.glClear
import android.opengl.GLES30.glClearColor
import android.opengl.GLES30.glCompileShader
import android.opengl.GLES30.glCreateProgram
import android.opengl.GLES30.glCreateShader
import android.opengl.GLES30.glDeleteProgram
import android.opengl.GLES30.glDeleteShader
import android.opengl.GLES30.glDisable
import android.opengl.GLES30.glDisableVertexAttribArray
import android.opengl.GLES30.glDrawArrays
import android.opengl.GLES30.glEnableVertexAttribArray
import android.opengl.GLES30.glGetAttribLocation
import android.opengl.GLES30.glGetShaderInfoLog
import android.opengl.GLES30.glGetShaderiv
import android.opengl.GLES30.glGetUniformLocation
import android.opengl.GLES30.glLinkProgram
import android.opengl.GLES30.glShaderSource
import android.opengl.GLES30.glUniform1f
import android.opengl.GLES30.glUseProgram
import android.opengl.GLES30.glVertexAttribPointer
import android.opengl.GLES30.glViewport

import com.example.generator2.features.audio.BufSplitFloat
import com.example.generator2.features.scope.NativeFloatDirectBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRendererLissagu: GLSurfaceView.Renderer {

    private var program: Int = 0

    private var vertexShader: Int = 0
    private var fragmentShader: Int = 0

    private var vertexBuffer: FloatBuffer

    val bools = intArrayOf(0, 1, 1) //oneTwo 0-one 1-two, L 1-true, R

    private val vertexShaderCode =
        """
    #version 300 es
    
    layout(location = 0) in vec2 aPosition;
   
    out vec4 ourColor; // Передаем цвет во фрагментный шейдер
    
    void main() {
        ourColor = vec4(1.0, 1.0, 1.0, 0.90001);
        gl_Position = vec4(aPosition, 0.0, 1.0);
        gl_PointSize = 5.0;
    }
        
""".trimIndent()

    private val fragmentShaderCode =
        """
#version 300 es
precision mediump float;

in vec4 ourColor; // Получаем цвет из вертексного шейдера

out vec4 fragColor;
void main() {
    fragColor = ourColor;
}
""".trimIndent()

    private var vertices = floatArrayOf(
        -0.5f, 0.5f, 0.0f,
    )

    init {

        println("!!! init MyGLRenderer")

        // Инициализация для OpenGL ES 3.0
        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        //vertexBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder()).asFloatBuffer()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        println("!!! init onSurfaceCreated")

        //Устанавливаем цвет, который будет очищен
        glClearColor(0.0f, 0.15f, 0.0f, 1f)

        vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
        fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = glCreateProgram().also {
            //Подключить шейдеры к программе
            glAttachShader(it, vertexShader)
            glAttachShader(it, fragmentShader)
            //Скомпоновать программу
            glLinkProgram(it)
        }

        glDisable(GL_LIGHTING)
        glUseProgram(program)

    }

    override fun onDrawFrame(gl: GL10?) {

        if (shouldPlay.get()) {
            //println("!!! init onDrawFrame")

            //glViewport(0, 0, width, height)

            //Эта строка очищает буфер цвета, заполняя его цветом, установленным в glClearColor
            glClear(GL_COLOR_BUFFER_BIT)

            val positionHandle = 0//glGetAttribLocation(program, "aPosition")
            // Включаем массив вершинных атрибутов
            glEnableVertexAttribArray(positionHandle)

            glVertexAttribPointer(
                positionHandle, //index Указывает индекс универсального атрибута вершины, который должен быть изменен.
                2,
                GL_FLOAT, //Определяет тип данных каждого компонента в массиве.
                false,
                 0,
                vertexBuffer
            )
//
//            //==========================================================================
//
//            //==========================================================================
//            // Рендерим объект
            glDrawArrays(GL_POINTS, 0, vertexBuffer.limit() / 2)
//            // Отключаем массивы вершинных атрибутов по завершении
            glDisableVertexAttribArray(positionHandle)

        }

    }



    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //Установить размер отображаемого окна
        glViewport(0, 0, width, height)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return glCreateShader(type).also { shader ->
            //Загрузка кода шейдера
            glShaderSource(shader, shaderCode)
            //Компиляция шейдера
            glCompileShader(shader)

            val compiled = IntArray(1)
            //Проверка результата компиляции
            glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                //Если не получилось, то удаляем шейдер
                glDeleteShader(shader)
                throw RuntimeException(
                    "Could not compile shader $type: ${
                        glGetShaderInfoLog(
                            shader
                        )
                    }"
                )
            }
        }
    }

    private val bufSplit = BufSplitFloat()

    private lateinit var pairFlatArray: Pair<FloatArray, FloatArray>

    fun updateVerticesDirect() {
        vertexBuffer = NativeFloatDirectBuffer.getByteBufferSmallLissagu(4096).asFloatBuffer()
        vertexBuffer.position(0)
    }

    fun deleteProgram() {
        println("!!! init deleteProgram")
        if (program != 0) {
            glDeleteProgram(program)
            //GLES20.glDeleteShader(vertexShader)
            //GLES20.glDeleteShader(fragmentShader)
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