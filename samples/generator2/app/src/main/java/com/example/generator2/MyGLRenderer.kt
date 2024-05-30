package com.example.generator2

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.system.measureNanoTime

class MyGLRenderer : GLSurfaceView.Renderer {

    private var program: Int = 0
    private lateinit var vertexBuffer: FloatBuffer

    private val vertexShaderCode =
        """
    #version 300 es
    in float signalLevel;
    
    uniform float len;
    
    out vec4 ourColor; // Передаем цвет во фрагментный шейдер
    
    void main() {
  
 
        float x = float(gl_VertexID) * 2.0 / (len) - 1.0 ;
             
        if (gl_VertexID % 2 == 0) 
        {
            ourColor = vec4(1.0, 0.0, 1.0, 0.00001);
        }
       else
       {
            ourColor = vec4(1.0, 1.0, 0.0, 0.00001);
       }
       
        float y = signalLevel;
        gl_Position = vec4(x, y, 0.0, 1.0);
        gl_PointSize = 0.01;
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

        //vertexBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder()).asFloatBuffer()

        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        GLES30.glClearColor(0.1f, 0.0f, 0.0f, 1.0f)

        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        // val nanos = measureNanoTime {

        //Эта строка очищает буфер цвета, заполняя его цветом, установленным в glClearColor
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUseProgram(program)

        val positionHandle = GLES30.glGetAttribLocation(program, "signalLevel")
        GLES30.glEnableVertexAttribArray(positionHandle)

        GLES30.glVertexAttribPointer(
            positionHandle,
            1,
            GLES30.GL_FLOAT,
            false,
            0,
            vertexBuffer
        )

        val stepXHandle = GLES30.glGetUniformLocation(program, "len")
        val len = vertexBuffer.limit() - 1
        GLES30.glUniform1f(stepXHandle, len.toFloat())


        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, vertexBuffer.limit())

        //GLES30.glDrawElements(GLES30.GL_POINTS, vertexBuffer.limit(), GLES30.GL_FLOAT, vertexBuffer)

        GLES30.glDisableVertexAttribArray(positionHandle)

        // println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! t1 ${t1 / 1000} us")
        // }
        // println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ${nanos / 1000} us")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //Установить размер отображаемого окна
        GLES30.glViewport(0, 0, width, height)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES30.glCreateShader(type).also { shader ->
            GLES30.glShaderSource(shader, shaderCode)
            GLES30.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                GLES30.glDeleteShader(shader)
                throw RuntimeException(
                    "Could not compile shader $type: ${
                        GLES30.glGetShaderInfoLog(
                            shader
                        )
                    }"
                )
            }
        }
    }

    fun updateVertices(newVertices: FloatArray) {
        vertexBuffer = ByteBuffer.allocateDirect(newVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(newVertices)
        vertexBuffer.position(0)
    }


}

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: MyGLRenderer = MyGLRenderer()

    init {
        setEGLContextClientVersion(3)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateVertices(vertices: FloatArray) {
        renderer.updateVertices(vertices)
        requestRender()
    }
}

@Composable
fun MyGLSurfaceViewContainer(signalLevels: FloatArray, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            MyGLSurfaceView(context).apply {
                updateVertices(signalLevels)
            }
        },
        modifier = modifier
    )
}

@Composable
fun SignalGraph(signalLevels: FloatArray, modifier: Modifier = Modifier) {

    Column(modifier = Modifier
        .fillMaxSize()
        .then(modifier)) {
        val width = LocalConfiguration.current.screenWidthDp
        val height = LocalConfiguration.current.screenHeightDp

        val vertices = remember(signalLevels) {
            prepareVertices(signalLevels)
        }

        MyGLSurfaceViewContainer(
            vertices,
            Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}

fun prepareVertices(signalLevels: FloatArray): FloatArray {
    return signalLevels
}
