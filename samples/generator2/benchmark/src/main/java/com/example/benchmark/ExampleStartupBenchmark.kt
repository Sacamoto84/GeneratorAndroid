package com.example.benchmark

import androidx.benchmark.macro.*
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This is an example startup benchmark.
 *
 * It navigates to the device's home screen, and launches the default activity.
 *
 * Before running this benchmark:
 * 1) switch your app's active build variant in the Studio (affects Studio runs only)
 * 2) add `<profileable android:shell="true" />` to your app's manifest, within the `<application>` tag
 *
 * Run this benchmark from Studio to see startup measurements, and captured system traces
 * for investigating your app's performance.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ExampleStartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

//   @Test
//   fun startUpCompilationModeNone()    = startup(CompilationMode.None())
//   @Test
//   fun startUpCompilationModePartial() = startup(CompilationMode.Partial())
////

    @Test
    fun scriptModeNone() = script(CompilationMode.None())

    @Test
    fun scriptModePartial() = script(CompilationMode.Partial())

    private fun startup(mode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = "com.example.generator2",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD,
        compilationMode = mode,
        setupBlock = {
            pressHome()
        }
    ) {

        startActivityAndWait()
    }


    private fun script(mode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = "com.example.generator2",
        metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
        iterations = 1,
        startupMode = StartupMode.COLD,
        compilationMode = mode,
        setupBlock = {

        }
    )
    {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.res("buttonM4GoToScript")), 5_000)
        device.findObject(By.res("buttonM4GoToScript"))?.click() //Переход на скрипты
        device.wait(Until.hasObject(By.res("buttonM4ScriptGoBack")), 5_000)
        device.findObject(By.res("buttonM4ScriptGoBack"))?.click() //Переход на скрипты

    }

}

fun MacrobenchmarkScope.addElementAndScrollDown() {

//    val button = device.findObject(By.text("Click"))
//    //val buttonEdit = device.findObject(By.res("edit"))
//
//    button.click()
//
//    device.waitForIdle()

    // pressHome()
    // startActivityAndWait() //Запустить активити и подождать отрисовки первого кадра


    val buttonGoToScript =
        device.findObject(By.res("buttonM4GoToScript"))// Modififier.testTag("tag")
    val buttonM4ScriptGoBack = device.findObject(By.res("buttonM4ScriptGoBack"))

    repeat(10)
    {
        buttonGoToScript.click() //Переход на скрипты
        buttonM4ScriptGoBack.click() //Переход на скрипты
    }

    device.waitForIdle()

}