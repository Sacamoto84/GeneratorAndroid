package com.example.generator2

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLES10.GL_LIGHT0
import android.opengl.GLES10.GL_LIGHT2
import android.opengl.GLES10.GL_LIGHTING
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import com.example.generator2.features.audio.BufSplitFloat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL10.GL_LIGHT1


class MyGLRenderer : GLSurfaceView.Renderer {

    private var program: Int = 0

    var vertexShader: Int = 0
    var fragmentShader: Int = 0

    private lateinit var vertexBuffer: FloatBuffer

    private val vertexShaderCode =
        """
    #version 300 es
    
    in float signalLevel;

    uniform float len;
    
    out vec4 ourColor; // Передаем цвет во фрагментный шейдер
    
    void main() {

//       if (gl_VertexID % 2 == 0) 
//       {
//            ourColor = vec4(1.0, 0.0, 1.0, 0.00001);
//       }
//       else
//       {
//            ourColor = vec4(1.0, 1.0, 0.0, 0.00001);
//       }
       
       ourColor = vec4(1.0, 1.0, 0.0, 0.00001);
       
             
    
        // Игнорируем вершины с индексами, которые не равны 0, 1, 16, 17, 32, 33 и т.д.
//        if (gl_VertexID % 4 > 1) {
//            // Помещаем игнорируемые вершины за пределы видимой области, сохраняя их положение
//            gl_Position = vec4(2.0, 2.0, 0.0, 1.0);
//            ourColor = vec4(0.0, 0.0, 0.0, 0.0);   // Устанавливаем цвет в прозрачный
//        } else {
            float x = float(gl_VertexID) * 2.0 / len - 1.0;     
            gl_Position = vec4(x, signalLevel, 0.0, 1.0);
//        }
        
        gl_PointSize = 1.0;
    }
        
""".trimIndent()


    //(x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
    // inmin = 0
    // inmax = len
    // outmin = -1
    // outmax = 1f

    // (x - 0) * (1f + 1f) / (len - 1 - 0) -1
    // x * 2f / (len - 1) - 1


    // float x = offsetX + float(gl_VertexID) * stepX;
//    #version 300 es
//    in float signalLevel;
//    uniform float stepX;
//    uniform float offsetX;
//    uniform float scaleY;
//    void main() {
//        float x = offsetX + gl_VertexID * stepX;
//        float y = signalLevel * scaleY * 2.0 - 1.0;
//        gl_Position = vec4(x, y, 0.0, 1.0);
//        gl_PointSize = 4.0;
//    }

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
        //vertexBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder()).asFloatBuffer()

        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        println("!!! init onSurfaceCreated")
        //Устанавливаем цвет, который будет очищен
        GLES32.glClearColor(0.5f, 0.0f, 0.0f, 1.0f)

        vertexShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        fragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES32.glCreateProgram().also {
            //Подключить шейдеры к программе
            GLES32.glAttachShader(it, vertexShader)
            GLES32.glAttachShader(it, fragmentShader)
            //Скомпоновать программу
            GLES32.glLinkProgram(it)
        }






        GLES32.glDisable(GL_LIGHT0)
        GLES32.glDisable(GL_LIGHT1)
        GLES32.glDisable(GL_LIGHT2)

        GLES32.glDisable(GL_LIGHTING)

        GLES32.glUseProgram(program)

    }

    override fun onDrawFrame(gl: GL10?) {
        //println("!!! init onDrawFrame")
        // val nanos = measureNanoTime {

        //Эта строка очищает буфер цвета, заполняя его цветом, установленным в glClearColor
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)

        val positionHandle = GLES32.glGetAttribLocation(program, "signalLevel")
        // Включаем массив вершинных атрибутов
        GLES32.glEnableVertexAttribArray(positionHandle)
//
        GLES32.glVertexAttribPointer(
            positionHandle, //index Указывает индекс универсального атрибута вершины, который должен быть изменен.
            1,
            GLES32.GL_FLOAT, //Определяет тип данных каждого компонента в массиве.
            false,
            4 * 0,
            vertexBuffer
        )
//
        val stepXHandle = GLES32.glGetUniformLocation(program, "len")
        val len = vertexBuffer.limit()/1 - 1
        GLES32.glUniform1f(stepXHandle, len.toFloat())

        // Рендерим объект
        GLES32.glDrawArrays(GLES32.GL_POINTS, 0, vertexBuffer.limit()/1)
//
//        //GLES30.glDrawElements(GLES30.GL_POINTS, vertexBuffer.limit(), GLES30.GL_FLOAT, vertexBuffer)
//

        // Отключаем массивы вершинных атрибутов по завершении
        GLES32.glDisableVertexAttribArray(positionHandle)


        // println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! t1 ${t1 / 1000} us")
        // }
        // println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ${nanos / 1000} us")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //Установить размер отображаемого окна
        GLES32.glViewport(0, 0, width, height)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES32.glCreateShader(type).also { shader ->

//            if (shader == 0) throw RuntimeException(
//                "Could not create shader $type: ${
//                    GLES30.glGetShaderInfoLog(
//                        shader
//                    )
//                }"
//            )

            //Загрузка кода шейдера
            GLES32.glShaderSource(shader, shaderCode)
            //Компиляция шейдера
            GLES32.glCompileShader(shader)

            val compiled = IntArray(1)
            //Проверка результата компиляции
            GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                //Если не получилось, то удаляем шейдер
                GLES32.glDeleteShader(shader)
                throw RuntimeException(
                    "Could not compile shader $type: ${
                        GLES32.glGetShaderInfoLog(
                            shader
                        )
                    }"
                )
            }
        }
    }


    private val bufSplit = BufSplitFloat()

    private lateinit var pairFlatArray: Pair<FloatArray, FloatArray>

    fun updateVertices(newVertices: FloatArray) {

        pairFlatArray = bufSplit.split(newVertices)

        vertexBuffer = ByteBuffer.allocateDirect(pairFlatArray.second.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(pairFlatArray.second)
        vertexBuffer.position(0)
    }

    fun deleteProgram() {

        println("!!! init deleteProgram")

        if (program != 0) {
            GLES32.glDeleteProgram(program)
            //GLES20.glDeleteShader(vertexShader)
            //GLES20.glDeleteShader(fragmentShader)
            program = 0
        }
    }

}

@SuppressLint("ViewConstructor")
class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer = MyGLRenderer()

    init {
        setEGLContextClientVersion(3)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateVertices(vertices: FloatArray) {
        renderer.updateVertices(vertices)
        requestRender()
    }

    fun deleteProgram() {
        renderer.deleteProgram()
    }

}

