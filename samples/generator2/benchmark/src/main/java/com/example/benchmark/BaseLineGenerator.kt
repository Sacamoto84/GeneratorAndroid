package com.example.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun generateBaseProfile()= baselineRule.collectBaselineProfile(
        packageName = "com.example.generator2"
    )
    {
        pressHome()
        startActivityAndWait() //Запустить активити и подождать отрисовки первого кадра

//          val buttonGoToScript = device.findObject(By.res("buttonM4GoToScript"))// Modififier.testTag("tag")
//          buttonGoToScript.click() //Переход на скрипты
//
//        val buttonM4ScriptGoBack = device.findObject(By.res("buttonM4ScriptGoBack"))
//         buttonM4ScriptGoBack.click()
//
//        repeat(10)
//        {
//            // Waits for content to be visible, which represents time to fully drawn.
//            device.wait(Until.hasObject(By.res("buttonM4GoToScript")), 5_000)
//            device.findObject(By.res("buttonM4GoToScript"))?.click() //Переход на скрипты
//            //device.waitForIdle()
//            device.wait(Until.hasObject(By.res("buttonM4ScriptGoBack")), 5_000)
//            device.findObject(By.res("buttonM4ScriptGoBack"))?.click() //Переход на скрипты
//            //device.waitForIdle()
//        }


    }

}