package com.example.generator2

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLES10.GL_LIGHTING
import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.generator2.features.audio.BufSplitFloat
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyGLRenderer : GLSurfaceView.Renderer {

    private var program: Int = 0

    private var vertexShader: Int = 0
    private var fragmentShader: Int = 0

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: FloatBuffer

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
        glClearColor(0.5f, 0.5f, 0.5f, 1.0f)

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

            glViewport(0, 0, width, height)

            //Эта строка очищает буфер цвета, заполняя его цветом, установленным в glClearColor
            glClear(GL_COLOR_BUFFER_BIT)

            val positionHandle = glGetAttribLocation(program, "signalLevel")
            // Включаем массив вершинных атрибутов
            glEnableVertexAttribArray(positionHandle)

            glVertexAttribPointer(
                positionHandle, //index Указывает индекс универсального атрибута вершины, который должен быть изменен.
                1,
                GL_FLOAT, //Определяет тип данных каждого компонента в массиве.
                false,
                4 * 0,
                vertexBuffer
            )

            val stepXHandle = glGetUniformLocation(program, "len")
            val len = vertexBuffer.limit() / 1 - 1
            glUniform1f(stepXHandle, len.toFloat())

            // Рендерим объект
            glDrawArrays(GL_POINTS, 0, vertexBuffer.limit() / 1)

            // Отключаем массивы вершинных атрибутов по завершении
            glDisableVertexAttribArray(positionHandle)

        }

    }

    var width: Int = 1
    var height: Int = 1

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height

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
            glDeleteProgram(program)
            //GLES20.glDeleteShader(vertexShader)
            //GLES20.glDeleteShader(fragmentShader)
            program = 0
        }
    }

    private val shouldPlay = AtomicBoolean(false)

    fun onResume() {
        shouldPlay.compareAndSet(false,true)
    }

    fun onPause() {
        shouldPlay.compareAndSet(true, false)
    }

}

@SuppressLint("ViewConstructor")
class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private var renderer : Renderer? = null //= MyGLRenderer()

    private var hasSetShader = false


    init {
        id = generateViewId()
    }


    init {
        setEGLContextClientVersion(3)
        //setRenderer(renderer)
         //renderMode = RENDERMODE_WHEN_DIRTY
        preserveEGLContextOnPause = true
    }




    fun setShaderRenderer(
        renderer: Renderer
    ) {

        if (hasSetShader.not()) {
            setRenderer(
                renderer
            )
           this.renderer = renderer

           renderMode = RENDERMODE_WHEN_DIRTY
        }
        hasSetShader = true
    }


//    fun requestRender() {
//        //renderer.updateVertices(vertices)
//        super.requestRender()
//    }

//    fun deleteProgram() {
//        renderer.deleteProgram()
//    }

    fun onDestroy() {
        super.onDetachedFromWindow()
    }

    override fun onResume() {
        super.onResume()
        Timber.d("!!! MyGLSurfaceView onResume")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("!!! MyGLSurfaceView onPause")
    }

}


@Composable
fun GLShader(
    renderer: MyGLRenderer,
    modifier: Modifier = Modifier,
    update : (MyGLSurfaceView?) ->  Unit
) {

    var view: MyGLSurfaceView? = remember {
        null
    }

//    LaunchedEffect(key1 = update) {
//        view?.requestRender()
//    }



    val lifeCycleState = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(key1 = lifeCycleState) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    view?.onResume()
                    renderer.onResume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    view?.onPause()
                    renderer.onPause()
                }
                else -> {
                }
            }
        }
        lifeCycleState.addObserver(observer)

        onDispose {
            Timber.d("View Disposed ${view.hashCode()}")
            lifeCycleState.removeObserver(observer)
            view?.onPause()
            view = null
        }
    }

    AndroidView(modifier = modifier,
        factory = {
            MyGLSurfaceView(it)
        }) { glSurfaceView ->
        view = glSurfaceView

        glSurfaceView.debugFlags = GLSurfaceView.DEBUG_CHECK_GL_ERROR or GLSurfaceView.DEBUG_LOG_GL_CALLS

        glSurfaceView.setShaderRenderer(
            renderer
        )

        update.invoke(view)
    }



}