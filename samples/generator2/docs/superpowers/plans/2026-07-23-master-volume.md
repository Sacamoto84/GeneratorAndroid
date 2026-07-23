# Мастер-громкость — план реализации

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить на каждый канал генератора ступень «мастер-громкость» — медленную огибающую 0..1 с тремя режимами (Плавный формой 0.1..100с, Вкл/Выкл по двум временам, общая momentary-Кнопка), применяемую после volume/AM/FM.

**Architecture:** Per-sample DSP огибающей — в нативе (`renderchannel.cpp`), как и весь остальной DSP; состояние (фаза/гейн/счётчик/буфер формы) хранится в `StructureCh`. Чистые Kotlin-хелперы (период→r, сек→сэмплы, активность Кнопки) вынесены и покрыты юнит-тестами. UI — новая карточка на канал + общая кнопка на главном экране. Параметры сохраняются в пресетах.

**Tech Stack:** Kotlin, Jetbrains Compose, JNI/C++ (CMake), JUnit4, MutableStateFlow, Satchel (KV пресеты).

**Спека:** `docs/superpowers/specs/2026-07-23-master-volume-design.md`

---

## Структура файлов

**Создать:**
- `app/src/main/java/com/example/generator2/features/generator/MasterVolume.kt` — чистые хелперы + константы режимов
- `app/src/test/java/com/example/generator2/features/generator/MasterVolumeTest.kt` — юнит-тесты хелперов
- `app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardMaster.kt` — UI карточки на канал
- `app/src/main/java/com/example/generator2/screens/mainscreen4/card/MasterButton.kt` — общая кнопка

**Изменить:**
- `app/src/main/cpp/generator/renderchannel.h` — поля `StructureCh`
- `app/src/main/cpp/generator/renderchannel.cpp` — `sendBuffer case 3`, JNI-сигнатура, пост-проход огибающей
- `app/src/main/java/.../features/generator/RenderChannel.kt` — `external fun` + чтение liveData + вызов
- `app/src/main/java/.../features/generator/Generator.kt` — поля `DataLiveData`, `StructureCh.buffer_master`
- `app/src/main/java/.../features/generator/Spinner_Send_Buffer.kt` — `GeneratorMOD.MASTER`
- `app/src/main/java/.../features/generator/observe.kt` — коллекторы формы мастера
- `app/src/main/java/.../screens/mainscreen4/ui/UIspinner.kt` — ветка `"MASTER"`
- `app/src/main/java/.../screens/mainscreen4/card/cardCard.kt` — подключить `CardMaster`
- `app/src/main/java/.../screens/mainscreen4/mainscreen4.kt` — подключить `MasterButton`
- `app/src/main/java/.../features/presets/presetsReadFile.kt` — чтение полей
- `app/src/main/java/.../features/presets/presetsSaveFile.kt` — запись полей
- `app/src/main/java/.../features/presets/presetsToLiveData.kt` — перенос полей

---

## Task 1: Чистые Kotlin-хелперы + юнит-тесты

**Files:**
- Create: `app/src/main/java/com/example/generator2/features/generator/MasterVolume.kt`
- Test: `app/src/test/java/com/example/generator2/features/generator/MasterVolumeTest.kt`

- [ ] **Step 1: Написать падающий тест**

Создать `app/src/test/java/com/example/generator2/features/generator/MasterVolumeTest.kt`:

```kotlin
package com.example.generator2.features.generator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MasterVolumeTest {

    @Test
    fun `период в r положительный на всём диапазоне`() {
        assertTrue(masterPeriodToR(0.1f, 48000) > 0)
        assertTrue(masterPeriodToR(2f, 48000) > 0)
        assertTrue(masterPeriodToR(100f, 48000) > 0)
    }

    @Test
    fun `меньший период даёт больший r`() {
        assertTrue(masterPeriodToR(0.1f, 48000) > masterPeriodToR(100f, 48000))
    }

    @Test
    fun `период за границами зажимается`() {
        assertEquals(masterPeriodToR(0.1f, 48000), masterPeriodToR(0.01f, 48000))
        assertEquals(masterPeriodToR(100f, 48000), masterPeriodToR(500f, 48000))
    }

    @Test
    fun `секунды в сэмплы`() {
        assertEquals(48000, secToSamples(1f, 48000))
        assertEquals(24000, secToSamples(0.5f, 48000))
        assertEquals(4800, secToSamples(0.1f, 48000))
    }

    @Test
    fun `кнопка активна если канал включён и режим кнопка`() {
        assertTrue(masterButtonActive(true, MASTER_MODE_BUTTON, false, MASTER_MODE_SLOW))
        assertTrue(masterButtonActive(false, MASTER_MODE_SLOW, true, MASTER_MODE_BUTTON))
    }

    @Test
    fun `кнопка не активна без включённого режима кнопка`() {
        assertFalse(masterButtonActive(true, MASTER_MODE_SLOW, true, MASTER_MODE_ONOFF))
        assertFalse(masterButtonActive(false, MASTER_MODE_BUTTON, false, MASTER_MODE_BUTTON))
    }
}
```

- [ ] **Step 2: Запустить тест — убедиться, что падает (не компилируется)**

Run:
```bash
./gradlew :app:testDebugUnitTest --tests "com.example.generator2.features.generator.MasterVolumeTest"
```
Expected: FAIL — `unresolved reference: masterPeriodToR` (и остальные символы).

- [ ] **Step 3: Реализовать хелперы**

Создать `app/src/main/java/com/example/generator2/features/generator/MasterVolume.kt`:

```kotlin
package com.example.generator2.features.generator

import kotlin.math.roundToInt

// Режимы мастер-громкости (ch*_Master_Mode)
const val MASTER_MODE_SLOW = 1    // Плавный: модуляция формой
const val MASTER_MODE_ONOFF = 2   // Вкл/Выкл: гейт по двум временам
const val MASTER_MODE_BUTTON = 3  // Кнопка: общий momentary-оверрайд

/**
 * DDS-инкремент фазы для Плавного режима.
 * Период в секундах (зажим 0.1..100) -> частота 0.01..10 Гц -> прирост фазы на сэмпл.
 * Формула как в RenderChannel.convertHzToR.
 */
fun masterPeriodToR(period: Float, sampleRate: Int): Int {
    val p = period.coerceIn(0.1f, 100f)
    val freq = 1f / p
    return ((4294967296L / sampleRate) * freq).toInt()
}

/** Секунды (зажим 0.1..100) -> число сэмплов, не меньше 1. */
fun secToSamples(sec: Float, sampleRate: Int): Int =
    (sec.coerceIn(0.1f, 100f) * sampleRate).roundToInt().coerceAtLeast(1)

/**
 * Активен ли глобальный оверрайд Кнопки:
 * хотя бы один включённый канал стоит в режиме Кнопка.
 */
fun masterButtonActive(
    ch1En: Boolean, ch1Mode: Int,
    ch2En: Boolean, ch2Mode: Int
): Boolean =
    (ch1En && ch1Mode == MASTER_MODE_BUTTON) ||
    (ch2En && ch2Mode == MASTER_MODE_BUTTON)
```

- [ ] **Step 4: Запустить тест — убедиться, что проходит**

Run:
```bash
./gradlew :app:testDebugUnitTest --tests "com.example.generator2.features.generator.MasterVolumeTest"
```
Expected: PASS (6 тестов).

- [ ] **Step 5: Коммит**

```bash
git add app/src/main/java/com/example/generator2/features/generator/MasterVolume.kt app/src/test/java/com/example/generator2/features/generator/MasterVolumeTest.kt
git commit -m "feat(мастер-громкость): хелперы период-в-r, сек-в-сэмплы, активность кнопки"
```

---

## Task 2: Поля данных `DataLiveData` + `StructureCh.buffer_master`

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/generator/Generator.kt`

- [ ] **Step 1: Добавить поля мастера в `DataLiveData`**

В `Generator.kt` после строк `ch2AmDepth` (около строки 218) добавить в конструктор `data class DataLiveData(`:

```kotlin
    // Мастер-громкость CH1
    var ch1_Master_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),        //PR PS PC
    var ch1_Master_Mode: MutableStateFlow<Int> = MutableStateFlow(1),              //PR PS PC 1=Плавный 2=Вкл/Выкл 3=Кнопка
    var ch1_Master_Period: MutableStateFlow<Float> = MutableStateFlow(2f),         //PR PS PC сек 0.1..100
    var ch1_Master_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"),//PR PS PC форма Плавного
    var ch1_Master_TOn: MutableStateFlow<Float> = MutableStateFlow(1f),            //PR PS PC сек 0.1..100
    var ch1_Master_TOff: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR PS PC сек 0.1..100

    // Мастер-громкость CH2
    var ch2_Master_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),        //PR PS PC
    var ch2_Master_Mode: MutableStateFlow<Int> = MutableStateFlow(1),              //PR PS PC
    var ch2_Master_Period: MutableStateFlow<Float> = MutableStateFlow(2f),         //PR PS PC
    var ch2_Master_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"),//PR PS PC
    var ch2_Master_TOn: MutableStateFlow<Float> = MutableStateFlow(1f),            //PR PS PC
    var ch2_Master_TOff: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR PS PC

    // Общая кнопка мастер-громкости (runtime, НЕ в пресетах)
    var masterButton: MutableStateFlow<Boolean> = MutableStateFlow(false),
```

> Вставлять внутри списка параметров `data class`, каждое поле оканчивается запятой. Убедиться, что предыдущее поле (`ch2AmDepth = ...`) тоже оканчивается запятой.

- [ ] **Step 2: Добавить `buffer_master` в Kotlin-`StructureCh`**

В `Generator.kt` в `data class StructureCh(` (около строки 239), после `var buffer_am: FloatArray = FloatArray(1024),` добавить:

```kotlin
    var buffer_master: FloatArray = FloatArray(1024) { 1f }, //0..1, дефолт 1.0 = пропуск
```

- [ ] **Step 3: Проверить компиляцию**

Run:
```bash
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Коммит**

```bash
git add app/src/main/java/com/example/generator2/features/generator/Generator.kt
git commit -m "feat(мастер-громкость): поля DataLiveData и буфер формы в StructureCh"
```

---

## Task 3: Загрузка формы мастера (enum, Spinner_Send_Buffer, observe, UIspinner)

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/generator/Spinner_Send_Buffer.kt`
- Modify: `app/src/main/java/com/example/generator2/features/generator/observe.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/mainscreen4/ui/UIspinner.kt`

- [ ] **Step 1: Добавить `MASTER` в enum `GeneratorMOD`**

В `Spinner_Send_Buffer.kt` строка 10:

```kotlin
enum class GeneratorMOD { CR, AM, FM, MASTER }
```

- [ ] **Step 2: Обработать `MASTER` в `Spinner_Send_Buffer`**

В `Spinner_Send_Buffer.kt`, в блоке `if (CH == GeneratorCH.CH0) { when (Mod) { ... } }` добавить ветку рядом с `GeneratorMOD.AM`:

```kotlin
            GeneratorMOD.MASTER -> {
                gen.ch1.buffer_master = byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, 0f, 1f)
                RenderChannel().sendBuffer(0, 3, gen.ch1.buffer_master)
            }
```

И в блоке `else { when (Mod) { ... } }` (CH1):

```kotlin
            GeneratorMOD.MASTER -> {
                gen.ch2.buffer_master = byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, 0f, 1f)
                RenderChannel().sendBuffer(1, 3, gen.ch2.buffer_master)
            }
```

> `index`/`buf` уже берутся из `itemlistAM` для любого `Mod != CR` — MASTER использует ту же библиотеку форм, менять их не нужно.

- [ ] **Step 3: Подписать коллекторы формы мастера**

В `observe.kt` после строк с `ch2_AM_Filename` (около строки 16) добавить:

```kotlin
    GlobalScope.launch(dispatchers) { gen.liveData.ch1_Master_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH0, GeneratorMOD.MASTER, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2_Master_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH1, GeneratorMOD.MASTER, it, gen ) } }
```

> `StateFlow.collect` сразу отдаёт текущее значение, поэтому дефолтная форма `"09_Ramp"` загрузится в нативный `buffer_master` на старте.

- [ ] **Step 4: Добавить ветку `"MASTER"` в `UIspinner`**

В `UIspinner.kt`:

В `when (Mod) { ... }` выбора списка (около строки 49) добавить:
```kotlin
            "MASTER" -> itemlist = gen.itemlistAM
```

В блоке `if (CH == "CH0") { when (Mod) { ... } }` (около строки 59) добавить:
```kotlin
                "MASTER" -> currentValue = gen.liveData.ch1_Master_Filename.value
```
и в `else { when (Mod) { ... } }` (около строки 65):
```kotlin
                "MASTER" -> currentValue = gen.liveData.ch2_Master_Filename.value
```

В `onClick` DropdownMenuItem, в `if (CH == "CH0") { when (Mod) { ... } }` (около строки 145) добавить:
```kotlin
                                        "MASTER" -> gen.liveData.ch1_Master_Filename.value = currentValue
```
и в ветке `else` (около строки 153):
```kotlin
                                        "MASTER" -> gen.liveData.ch2_Master_Filename.value = currentValue
```

- [ ] **Step 5: Проверить компиляцию**

Run:
```bash
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Коммит**

```bash
git add app/src/main/java/com/example/generator2/features/generator/Spinner_Send_Buffer.kt app/src/main/java/com/example/generator2/features/generator/observe.kt app/src/main/java/com/example/generator2/screens/mainscreen4/ui/UIspinner.kt
git commit -m "feat(мастер-громкость): загрузка формы Плавного режима из библиотеки AM"
```

---

## Task 4: Нативные поля `StructureCh` + `sendBuffer case 3`

**Files:**
- Modify: `app/src/main/cpp/generator/renderchannel.h`
- Modify: `app/src/main/cpp/generator/renderchannel.cpp:164-180`

- [ ] **Step 1: Добавить поля и конструктор в нативный `StructureCh`**

В `renderchannel.h` заменить тело `struct StructureCh { ... };` на:

```cpp
struct StructureCh {

    int ch = 0;

    float buffer_carrier[1024] = {0.0f};
    float buffer_am[1024] = {0.0f};
    float buffer_fm[1024] = {0.0f};

    float buffer_normalised_Fm[1024] = {0.0f};

    // Мастер-громкость
    float buffer_master[1024];              // форма Плавного, 0..1 (инициализируется 1.0)
    uint32_t phase_accumulator_master = 0;  // DDS Плавного
    float master_current_gain = 1.0f;       // текущий гейн для фейда
    uint32_t master_onoff_counter = 0;      // счётчик сэмплов Вкл/Выкл
    bool master_onoff_on = true;            // текущая фаза Вкл/Выкл (старт ON)

    uint32_t phase_accumulator_carrier = 0;
    uint32_t phase_accumulator_am = 0;
    uint32_t phase_accumulator_fm = 0;

    StructureCh() {
        for (float &v : buffer_master) v = 1.0f; // до загрузки формы — пропуск сигнала
    }
};
```

- [ ] **Step 2: Добавить `case 3` в `sendBuffer`**

В `renderchannel.cpp` в функции `...RenderChannel_sendBuffer`, в `switch (modulation)` после `case 2` добавить:

```cpp
        case 3 : {
            destination = pStructureCh->buffer_master;
            break;
        }
```

- [ ] **Step 3: Собрать нативную часть**

Run:
```bash
./gradlew :app:assembleDebug
```
Expected: BUILD SUCCESSFUL (CMake собирает `plasma` без ошибок).

- [ ] **Step 4: Коммит**

```bash
git add app/src/main/cpp/generator/renderchannel.h app/src/main/cpp/generator/renderchannel.cpp
git commit -m "feat(мастер-громкость): нативные поля StructureCh и приём буфера формы (case 3)"
```

---

## Task 5: JNI-сигнатура + пост-проход огибающей + Kotlin-обвязка

**Files:**
- Modify: `app/src/main/cpp/generator/renderchannel.cpp:32-149`
- Modify: `app/src/main/java/com/example/generator2/features/generator/RenderChannel.kt`

- [ ] **Step 1: Расширить JNI-сигнатуру нативного рендера**

В `renderchannel.cpp` в объявлении `Java_..._jniRenderChannel(...)` добавить параметры **после** `jfloat am_depth,` и **до** `jint channel,`:

```cpp
                                                                              jfloat am_depth,

                                                                              jboolean master_en,
                                                                              jint master_mode,
                                                                              jint r_master,
                                                                              jint on_samples,
                                                                              jint off_samples,
                                                                              jboolean button_active,
                                                                              jboolean button_pressed,

                                                                              jint channel,
                                                                              jfloatArray m_buffer
```

- [ ] **Step 2: Добавить пост-проход огибающей**

В `renderchannel.cpp`, в теле `jniRenderChannel`, **после** блока `if (en_fm && en_am) { ... }` и **перед** строкой `env->SetFloatArrayRegion(m_buffer, 0, num_frames, tempArrayElements.get());` вставить:

```cpp
    // Мастер-громкость: огибающая 0..1 поверх канала, с фейдом ~5 мс от щелчков
    float master_step = 1.0f / (0.005f * (float) sample_rate);
    for (int i = 0; i < num_frames; i++) {
        float target;
        if (button_active) {
            target = button_pressed ? 1.0f : 0.0f;          // общий оверрайд обоих каналов
        } else if (!master_en) {
            target = 1.0f;
        } else if (master_mode == 1) {                       // Плавный
            target = pStructureCh->buffer_master[pStructureCh->phase_accumulator_master >> 22];
            pStructureCh->phase_accumulator_master += (uint32_t) r_master;
        } else if (master_mode == 2) {                       // Вкл/Выкл
            target = pStructureCh->master_onoff_on ? 1.0f : 0.0f;
            uint32_t limit = pStructureCh->master_onoff_on ? (uint32_t) on_samples
                                                           : (uint32_t) off_samples;
            if (++pStructureCh->master_onoff_counter >= limit) {
                pStructureCh->master_onoff_on = !pStructureCh->master_onoff_on;
                pStructureCh->master_onoff_counter = 0;
            }
        } else {                                             // Кнопка (mode==3): недостижимо при button_active==false
            target = button_pressed ? 1.0f : 0.0f;
        }

        float g = pStructureCh->master_current_gain;
        float d = target - g;
        if (d > master_step) d = master_step;
        else if (d < -master_step) d = -master_step;
        g += d;
        pStructureCh->master_current_gain = g;

        tempArrayElements[i] *= g;
    }
```

- [ ] **Step 3: Обновить `external fun jniRenderChannel` в Kotlin**

В `RenderChannel.kt` в объявлении `external fun jniRenderChannel(` добавить параметры после `amDepth: Float,` и до `channel: Int,`:

```kotlin
        volume: Float,
        amDepth: Float,

        masterEN: Boolean,
        masterMode: Int,
        rMaster: Int,
        onSamples: Int,
        offSamples: Int,
        buttonActive: Boolean,
        buttonPressed: Boolean,

        channel: Int,// 0 1 номер канала

        mBuffer: FloatArray
```

- [ ] **Step 4: Считать параметры мастера и передать в JNI**

В `RenderChannel.kt` в `fun renderChanel(...)`:

Объявить локальные переменные рядом с существующими (после `val amDepth: Float`):
```kotlin
        val masterEN: Boolean
        val masterMode: Int
        val masterPeriod: Float
        val masterTOn: Float
        val masterTOff: Float
```

В ветке `if (ch.ch == 0) { ... }` после `amDepth = liveData.ch1AmDepth.value` добавить:
```kotlin
            masterEN = liveData.ch1_Master_EN.value
            masterMode = liveData.ch1_Master_Mode.value
            masterPeriod = liveData.ch1_Master_Period.value
            masterTOn = liveData.ch1_Master_TOn.value
            masterTOff = liveData.ch1_Master_TOff.value
```

В ветке `else { ... }` после `amDepth = liveData.ch2AmDepth.value` добавить:
```kotlin
            masterEN = liveData.ch2_Master_EN.value
            masterMode = liveData.ch2_Master_Mode.value
            masterPeriod = liveData.ch2_Master_Period.value
            masterTOn = liveData.ch2_Master_TOn.value
            masterTOff = liveData.ch2_Master_TOff.value
```

После `val mBuffer = FloatArray(numFrames)` (перед вызовом `jniRenderChannel`) добавить расчёт:
```kotlin
        val rMaster = masterPeriodToR(masterPeriod, sampleRate)
        val onSamples = secToSamples(masterTOn, sampleRate)
        val offSamples = secToSamples(masterTOff, sampleRate)
        val buttonActive = masterButtonActive(
            liveData.ch1_Master_EN.value, liveData.ch1_Master_Mode.value,
            liveData.ch2_Master_EN.value, liveData.ch2_Master_Mode.value
        )
        val buttonPressed = liveData.masterButton.value
```

В вызове `jniRenderChannel(...)` добавить аргументы после `amDepth,` и до `ch.ch,`:
```kotlin
            volume,
            amDepth,

            masterEN,
            masterMode,
            rMaster,
            onSamples,
            offSamples,
            buttonActive,
            buttonPressed,

            ch.ch, //номер канала
            mBuffer
```

- [ ] **Step 5: Собрать (натив + Kotlin)**

Run:
```bash
./gradlew :app:assembleDebug
```
Expected: BUILD SUCCESSFUL. JNI-сигнатуры Kotlin и C++ совпадают по числу/порядку/типам параметров.

- [ ] **Step 6: Коммит**

```bash
git add app/src/main/cpp/generator/renderchannel.cpp app/src/main/java/com/example/generator2/features/generator/RenderChannel.kt
git commit -m "feat(мастер-громкость): огибающая в нативном рендере с фейдом от щелчков"
```

---

## Task 6: UI — карточка `CardMaster`

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardMaster.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardCard.kt`

- [ ] **Step 1: Создать `cardMaster.kt`**

Создать `app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardMaster.kt`:

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.generator2.common.haptic.Haptic
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.generator.MASTER_MODE_BUTTON
import com.example.generator2.features.generator.MASTER_MODE_ONOFF
import com.example.generator2.features.generator.MASTER_MODE_SLOW
import com.example.generator2.screens.common.modifier.noRippleClickable
import com.example.generator2.screens.mainscreen4.ms4SwitchWidth
import com.example.generator2.screens.mainscreen4.textStyleButtonOnOff
import com.example.generator2.screens.mainscreen4.ui.MainscreenTextBoxAndDropdownMenu
import com.example.generator2.screens.mainscreen4.ui.UIspinner
import com.example.generator2.theme.colorDarkBackground

@Composable
fun CardMaster(str: String = "CH0", gen: Generator) {

    val isCh0 = str == "CH0"

    val en by (if (isCh0) gen.liveData.ch1_Master_EN else gen.liveData.ch2_Master_EN).collectAsState()
    val mode by (if (isCh0) gen.liveData.ch1_Master_Mode else gen.liveData.ch2_Master_Mode).collectAsState()

    Column {

        Box(
            modifier = Modifier
                .background(Color.DarkGray)
                .height(1.dp)
                .fillMaxWidth()
        )

        // Строка 1: включение + выбор режима
        Row(Modifier.padding(top = 0.dp), verticalAlignment = Alignment.CenterVertically) {

            // Кнопка включения MAS
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(32.dp)
                    .width(ms4SwitchWidth)
                    .border(
                        2.dp,
                        color = if (en) Color(0xFF1B5E20) else Color.DarkGray,
                        RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = if (en) Color(0xFF01AE0F) else colorDarkBackground)
                    .noRippleClickable(onClick = {
                        if (isCh0) gen.liveData.ch1_Master_EN.value = !gen.liveData.ch1_Master_EN.value
                        else gen.liveData.ch2_Master_EN.value = !gen.liveData.ch2_Master_EN.value
                        Haptic.confirm()
                    }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MAS",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (en) colorDarkBackground else Color.LightGray,
                    style = textStyleButtonOnOff
                )
            }

            fun setMode(m: Int) {
                if (isCh0) gen.liveData.ch1_Master_Mode.value = m
                else gen.liveData.ch2_Master_Mode.value = m
                Haptic.confirm()
            }

            ModeButton("Плавно", mode == MASTER_MODE_SLOW, Modifier.weight(1f)) { setMode(MASTER_MODE_SLOW) }
            ModeButton("Вкл/Выкл", mode == MASTER_MODE_ONOFF, Modifier.weight(1f)) { setMode(MASTER_MODE_ONOFF) }
            ModeButton("Кнопка", mode == MASTER_MODE_BUTTON, Modifier.weight(1f)) { setMode(MASTER_MODE_BUTTON) }
        }

        // Строка 2: параметры режима
        Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            when (mode) {
                MASTER_MODE_SLOW -> {
                    val period by (if (isCh0) gen.liveData.ch1_Master_Period else gen.liveData.ch2_Master_Period).collectAsState()
                    MainscreenTextBoxAndDropdownMenu(
                        str = String.format("%.1f", period),
                        modifier = Modifier.weight(1f),
                        items = listOf("0.1", "1.0", "2.0", "10.0", "60.0", "100.0"),
                        value = period,
                        onChange = {
                            if (isCh0) gen.liveData.ch1_Master_Period.value = it
                            else gen.liveData.ch2_Master_Period.value = it
                        },
                        sensing = 0.05f,
                        range = 0.1f..100f,
                    )
                    UIspinner.Spinner(
                        str,
                        "MASTER",
                        modifier = Modifier
                            .padding(top = 0.dp, start = 8.dp, end = 8.dp)
                            .wrapContentWidth()
                            .clip(shape = RoundedCornerShape(4.dp)),
                        filename = if (isCh0) gen.liveData.ch1_Master_Filename.collectAsState()
                        else gen.liveData.ch2_Master_Filename.collectAsState(),
                        gen = gen
                    )
                }

                MASTER_MODE_ONOFF -> {
                    val tOn by (if (isCh0) gen.liveData.ch1_Master_TOn else gen.liveData.ch2_Master_TOn).collectAsState()
                    val tOff by (if (isCh0) gen.liveData.ch1_Master_TOff else gen.liveData.ch2_Master_TOff).collectAsState()
                    MainscreenTextBoxAndDropdownMenu(
                        str = "ON " + String.format("%.1f", tOn),
                        modifier = Modifier.weight(1f),
                        items = listOf("0.1", "0.5", "1.0", "2.0", "5.0", "10.0"),
                        value = tOn,
                        onChange = {
                            if (isCh0) gen.liveData.ch1_Master_TOn.value = it
                            else gen.liveData.ch2_Master_TOn.value = it
                        },
                        sensing = 0.05f,
                        range = 0.1f..100f,
                    )
                    MainscreenTextBoxAndDropdownMenu(
                        str = "OFF " + String.format("%.1f", tOff),
                        modifier = Modifier.weight(1f),
                        items = listOf("0.1", "0.5", "1.0", "2.0", "5.0", "10.0"),
                        value = tOff,
                        onChange = {
                            if (isCh0) gen.liveData.ch1_Master_TOff.value = it
                            else gen.liveData.ch2_Master_TOff.value = it
                        },
                        sensing = 0.05f,
                        range = 0.1f..100f,
                    )
                }

                else -> {
                    Text(
                        text = "Держи общую кнопку MASTER внизу экрана",
                        color = Color.LightGray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .padding(start = 8.dp)
            .height(32.dp)
            .border(2.dp, if (selected) Color(0xFF1B5E20) else Color.DarkGray, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF01AE0F) else colorDarkBackground)
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = if (selected) colorDarkBackground else Color.LightGray,
            style = textStyleButtonOnOff
        )
    }
}
```

> `MainscreenTextBoxAndDropdownMenu`, `ms4SwitchWidth`, `textStyleButtonOnOff` — те же символы, что использует `cardAM.kt`; если сигнатура `MainscreenTextBoxAndDropdownMenu` отличается, скопировать вызов один-в-один из `cardAM.kt:103-114` и заменить поля.

- [ ] **Step 2: Подключить карточку в `CardCard`**

В `cardCard.kt`:

Добавить импорт вверху рядом с `import CardFM`:
```kotlin
import CardMaster
```

В `Column { ... }` после `CardFM(str, gen = gen)` добавить:
```kotlin
            CardMaster(str, gen = gen)
```

- [ ] **Step 3: Проверить компиляцию**

Run:
```bash
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Коммит**

```bash
git add app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardMaster.kt app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardCard.kt
git commit -m "feat(мастер-громкость): карточка канала с выбором режима и параметрами"
```

---

## Task 7: UI — общая кнопка `MasterButton`

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/mainscreen4/card/MasterButton.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/mainscreen4/mainscreen4.kt:167`

- [ ] **Step 1: Создать `MasterButton.kt`**

Создать `app/src/main/java/com/example/generator2/screens/mainscreen4/card/MasterButton.kt`:

```kotlin
package com.example.generator2.screens.mainscreen4.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.generator2.common.haptic.Haptic
import com.example.generator2.features.generator.Generator
import com.example.generator2.screens.mainscreen4.textStyleButtonOnOff
import com.example.generator2.theme.colorDarkBackground

@Composable
fun MasterButton(gen: Generator) {
    val pressed by gen.liveData.masterButton.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (pressed) Color(0xFF01AE0F) else colorDarkBackground)
            .border(
                2.dp,
                if (pressed) Color(0xFF1B5E20) else Color.DarkGray,
                RoundedCornerShape(8.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    gen.liveData.masterButton.value = true
                    Haptic.confirm()
                    tryAwaitRelease()
                    gen.liveData.masterButton.value = false
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "MASTER",
            color = if (pressed) colorDarkBackground else Color.LightGray,
            style = textStyleButtonOnOff
        )
    }
}
```

- [ ] **Step 2: Подключить кнопку на главном экране**

В `mainscreen4.kt` добавить импорт рядом с `import ...card.CardCard`:
```kotlin
import com.example.generator2.screens.mainscreen4.card.MasterButton
```

Перед вызовом `CardCard("CH0", vm.audioMixerPump.gen)` (строка 167) добавить:
```kotlin
                    MasterButton(vm.audioMixerPump.gen)
```

> Кнопка ложится в тот же вертикальный `Column`, что и карточки каналов, поэтому видна на экране генератора.

- [ ] **Step 3: Проверить компиляцию**

Run:
```bash
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Коммит**

```bash
git add app/src/main/java/com/example/generator2/screens/mainscreen4/card/MasterButton.kt app/src/main/java/com/example/generator2/screens/mainscreen4/mainscreen4.kt
git commit -m "feat(мастер-громкость): общая momentary-кнопка на главном экране"
```

---

## Task 8: Персистентность в пресетах

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/presets/presetsReadFile.kt:72-73`
- Modify: `app/src/main/java/com/example/generator2/features/presets/presetsSaveFile.kt:74-75`
- Modify: `app/src/main/java/com/example/generator2/features/presets/presetsToLiveData.kt:51-52`

- [ ] **Step 1: Чтение пресета**

В `presetsReadFile.kt` после строк `data.ch2AmDepth.value = ...` добавить:

```kotlin
    data.ch1_Master_EN.value = satchel.getOrDefault("ch1_Master_EN", false)
    data.ch1_Master_Mode.value = satchel.getOrDefault("ch1_Master_Mode", 1)
    data.ch1_Master_Period.value = satchel.getOrDefault("ch1_Master_Period", 2f)
    data.ch1_Master_Filename.value = satchel.getOrDefault("ch1_Master_Filename", "09_Ramp")
    data.ch1_Master_TOn.value = satchel.getOrDefault("ch1_Master_TOn", 1f)
    data.ch1_Master_TOff.value = satchel.getOrDefault("ch1_Master_TOff", 1f)

    data.ch2_Master_EN.value = satchel.getOrDefault("ch2_Master_EN", false)
    data.ch2_Master_Mode.value = satchel.getOrDefault("ch2_Master_Mode", 1)
    data.ch2_Master_Period.value = satchel.getOrDefault("ch2_Master_Period", 2f)
    data.ch2_Master_Filename.value = satchel.getOrDefault("ch2_Master_Filename", "09_Ramp")
    data.ch2_Master_TOn.value = satchel.getOrDefault("ch2_Master_TOn", 1f)
    data.ch2_Master_TOff.value = satchel.getOrDefault("ch2_Master_TOff", 1f)
```

- [ ] **Step 2: Запись пресета**

В `presetsSaveFile.kt` после строк `satchel["ch2AmDepth"] = ...` добавить:

```kotlin
    satchel["ch1_Master_EN"] = gen.liveData.ch1_Master_EN.value
    satchel["ch1_Master_Mode"] = gen.liveData.ch1_Master_Mode.value
    satchel["ch1_Master_Period"] = gen.liveData.ch1_Master_Period.value
    satchel["ch1_Master_Filename"] = gen.liveData.ch1_Master_Filename.value
    satchel["ch1_Master_TOn"] = gen.liveData.ch1_Master_TOn.value
    satchel["ch1_Master_TOff"] = gen.liveData.ch1_Master_TOff.value

    satchel["ch2_Master_EN"] = gen.liveData.ch2_Master_EN.value
    satchel["ch2_Master_Mode"] = gen.liveData.ch2_Master_Mode.value
    satchel["ch2_Master_Period"] = gen.liveData.ch2_Master_Period.value
    satchel["ch2_Master_Filename"] = gen.liveData.ch2_Master_Filename.value
    satchel["ch2_Master_TOn"] = gen.liveData.ch2_Master_TOn.value
    satchel["ch2_Master_TOff"] = gen.liveData.ch2_Master_TOff.value
```

- [ ] **Step 3: Перенос data -> liveData**

В `presetsToLiveData.kt` после строк `gen.liveData.ch2AmDepth.value = data.ch2AmDepth.value` добавить:

```kotlin
    gen.liveData.ch1_Master_EN.value = data.ch1_Master_EN.value
    gen.liveData.ch1_Master_Mode.value = data.ch1_Master_Mode.value
    gen.liveData.ch1_Master_Period.value = data.ch1_Master_Period.value
    gen.liveData.ch1_Master_Filename.value = data.ch1_Master_Filename.value
    gen.liveData.ch1_Master_TOn.value = data.ch1_Master_TOn.value
    gen.liveData.ch1_Master_TOff.value = data.ch1_Master_TOff.value

    gen.liveData.ch2_Master_EN.value = data.ch2_Master_EN.value
    gen.liveData.ch2_Master_Mode.value = data.ch2_Master_Mode.value
    gen.liveData.ch2_Master_Period.value = data.ch2_Master_Period.value
    gen.liveData.ch2_Master_Filename.value = data.ch2_Master_Filename.value
    gen.liveData.ch2_Master_TOn.value = data.ch2_Master_TOn.value
    gen.liveData.ch2_Master_TOff.value = data.ch2_Master_TOff.value
```

- [ ] **Step 4: Проверить компиляцию**

Run:
```bash
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Коммит**

```bash
git add app/src/main/java/com/example/generator2/features/presets/presetsReadFile.kt app/src/main/java/com/example/generator2/features/presets/presetsSaveFile.kt app/src/main/java/com/example/generator2/features/presets/presetsToLiveData.kt
git commit -m "feat(мастер-громкость): сохранение параметров в пресетах"
```

---

## Task 9: Ручная проверка на устройстве

Юнит-тесты покрывают только чистые хелперы; саму огибающую (нативную) проверяем руками.

- [ ] **Step 1: Собрать и установить**

Run:
```bash
./gradlew :app:installDebug
```
Expected: BUILD SUCCESSFUL, APK установлен.

- [ ] **Step 2: Плавный режим**

Включить CH1, несущую слышно. Открыть карточку MASTER, нажать MAS, режим «Плавно», период 2с, форма из спиннера. Ожидание: громкость плавно ходит с периодом ~2с. Период 100с — очень медленное движение. Период 0.1с — быстрое (~10 Гц).

- [ ] **Step 3: Вкл/Выкл**

Режим «Вкл/Выкл», ON=1с, OFF=2с. Ожидание: 1с звук, 2с тишина, повтор. На переходах — без щелчков (фейд ~5мс).

- [ ] **Step 4: Кнопка (общая)**

CH1 → режим «Кнопка». Включить CH2 в любом режиме. Держать общую кнопку MASTER: звук в обоих каналах идёт, отпустить — тишина в обоих. Проверить, что режим CH2 при этом перекрыт кнопкой.

- [ ] **Step 5: Пресеты**

Сохранить пресет с настройками мастера, сменить, загрузить обратно. Ожидание: EN/режим/период/форма/времена восстановились. Кнопка (`masterButton`) — не сохраняется (всегда отпущена на старте).

- [ ] **Step 6: Коммит (если были правки по итогам проверки)**

```bash
git add -A
git commit -m "fix(мастер-громкость): правки по итогам ручной проверки"
```

---

## Заметки

- **JNI-сигнатуры**: число, порядок и типы параметров `external fun jniRenderChannel` (Kotlin) и `Java_..._jniRenderChannel` (C++) обязаны совпадать. Ошибка проявится как `NoSuchMethodError`/`UnsatisfiedLinkError` в рантайме, компиляция пройдёт. При падении — сверить обе сигнатуры.
- **Дефолт формы**: нативный `buffer_master` инициализируется 1.0 (пропуск). Реальная форма грузится через `observe.kt` при первом значении `ch*_Master_Filename` — до этого Плавный режим даёт полную громкость, не тишину.
- **Оверрайд Кнопки** намеренно перекрывает Плавный/Вкл-Выкл обоих каналов (MVP-решение из спеки).
- **Mono**: рендерится только CH1; `buttonActive` учитывает оба канала, поэтому кнопка CH1 работает и в моно.
```