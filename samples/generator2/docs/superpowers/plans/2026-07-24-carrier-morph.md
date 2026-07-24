# Метаморфоза несущей — план реализации

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить в канал генератора режим метаморфозы — до трёх форм несущей, сменяющих друг друга по кругу ступенчато или через линейный морфинг за заданное время.

**Architecture:** Смешивание считается посэмплово в нативном рендере (`renderchannel.cpp`) через пару указателей на таблицы форм и коэффициент `t`, выведенный из счётчика сэмплов. Kotlin отдаёт в JNI четыре скаляра (вкл, режим, длина шага в сэмплах, битовая маска активных слотов) и грузит формы слотов через существующий `sendBuffer`. UI — новая карточка `CardMorph` по образцу `CardMaster`.

**Tech Stack:** Kotlin, Jetpack Compose, C++ (JNI, NDK), JUnit4, Gradle. Спека: `docs/superpowers/specs/2026-07-24-carrier-morph-design.md`.

**Рабочая директория для всех команд:** `G:/GeneratorAndroid/samples/generator2`

---

## Структура файлов

| Файл | Ответственность |
|---|---|
| `app/src/main/java/.../features/generator/CarrierMorph.kt` | **создать** — константы режимов + три чистые функции (шаг в сэмплах, маска слотов, эффективность) |
| `app/src/test/java/.../features/generator/CarrierMorphTest.kt` | **создать** — юнит-тесты этих функций |
| `app/src/main/java/.../features/generator/Generator.kt` | 18 полей `DataLiveData`, `StructureCh.buffer_morph` |
| `app/src/main/cpp/generator/renderchannel.h` | состояние метаморфозы в `StructureCh` |
| `app/src/main/cpp/generator/renderchannel.cpp` | `MorphRunner`, JNI-параметры, 4 ветки рендера, `sendBuffer case 4-6` |
| `app/src/main/java/.../features/generator/RenderChannel.kt` | сигнатура `external fun`, чтение `liveData`, вызов JNI |
| `app/src/main/java/.../features/generator/Spinner_Send_Buffer.kt` | `GeneratorMOD.MORPH0/1/2`, загрузка форм слотов |
| `app/src/main/java/.../features/generator/observe.kt` | 6 коллекторов на имена форм слотов |
| `app/src/main/java/.../screens/mainscreen4/ui/UIspinner.kt` | режимы `MORPH0/1/2`, параметр `enable` |
| `app/src/main/java/.../screens/mainscreen4/card/cardMorph.kt` | **создать** — карточка метаморфозы |
| `app/src/main/java/.../screens/mainscreen4/card/cardCard.kt` | подключение карточки |
| `app/src/main/java/.../screens/mainscreen4/card/cardCarrier.kt` | гашение спиннера несущей при включённой метаморфозе |
| `app/src/main/java/.../features/presets/presetsSaveFile.kt` | сохранение 18 полей |
| `app/src/main/java/.../features/presets/presetsReadFile.kt` | чтение 18 полей |
| `app/src/main/java/.../features/presets/presetsToLiveData.kt` | перенос 18 полей в живые данные |

Вся логика метаморфозы, доступная для юнит-тестов, живёт в отдельном `CarrierMorph.kt` — не размазывается по `RenderChannel.kt`. Пофреймовое состояние живёт только в нативе, как у мастер-громкости.

---

### Task 1: Чистые хелперы метаморфозы

**Files:**
- Create: `app/src/main/java/com/example/generator2/features/generator/CarrierMorph.kt`
- Test: `app/src/test/java/com/example/generator2/features/generator/CarrierMorphTest.kt`

- [ ] **Step 1: Написать падающий тест**

Создать `app/src/test/java/com/example/generator2/features/generator/CarrierMorphTest.kt`:

```kotlin
package com.example.generator2.features.generator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarrierMorphTest {

    @Test
    fun `время шага в сэмплы`() {
        assertEquals(48000, morphSteps(1f, 48000))
        assertEquals(24000, morphSteps(0.5f, 48000))
        assertEquals(4800, morphSteps(0.1f, 48000))
        assertEquals(4800000, morphSteps(100f, 48000))
    }

    @Test
    fun `время за границами зажимается`() {
        assertEquals(morphSteps(0.1f, 48000), morphSteps(0.001f, 48000))
        assertEquals(morphSteps(100f, 48000), morphSteps(500f, 48000))
    }

    @Test
    fun `шаг никогда не меньше одного сэмпла`() {
        assertTrue(morphSteps(0.1f, 1) >= 1)
    }

    @Test
    fun `маска слотов собирается по битам`() {
        assertEquals(0, morphMask(false, false, false))
        assertEquals(1, morphMask(true, false, false))
        assertEquals(2, morphMask(false, true, false))
        assertEquals(4, morphMask(false, false, true))
        assertEquals(5, morphMask(true, false, true))
        assertEquals(7, morphMask(true, true, true))
    }

    @Test
    fun `метаморфоза не работает без активных слотов`() {
        assertFalse(morphEffective(true, 0))
    }

    @Test
    fun `метаморфоза не работает выключенной`() {
        assertFalse(morphEffective(false, 7))
    }

    @Test
    fun `метаморфоза работает при включении и хотя бы одном слоте`() {
        assertTrue(morphEffective(true, 1))
        assertTrue(morphEffective(true, 4))
    }
}
```

- [ ] **Step 2: Запустить тест — убедиться, что падает**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.generator2.features.generator.CarrierMorphTest"
```

Ожидаемо: `BUILD FAILED`, ошибки компиляции `Unresolved reference: morphSteps`, `morphMask`, `morphEffective`.

- [ ] **Step 3: Написать минимальную реализацию**

Создать `app/src/main/java/com/example/generator2/features/generator/CarrierMorph.kt`:

```kotlin
package com.example.generator2.features.generator

import kotlin.math.roundToInt

// Режимы метаморфозы несущей (ch*_Morph_Mode)
const val MORPH_MODE_STEP = 0    // Ступень: резкая смена формы по обороту фазы
const val MORPH_MODE_SMOOTH = 1  // Плавно: линейный морфинг текущей формы в следующую

/** Длительность одного шага в секундах (зажим 0.1..100) -> число сэмплов, не меньше 1. */
fun morphSteps(time: Float, sampleRate: Int): Int =
    (time.coerceIn(0.1f, 100f) * sampleRate).roundToInt().coerceAtLeast(1)

/** Битовая маска активных слотов: bit0 | bit1 | bit2. */
fun morphMask(slot0: Boolean, slot1: Boolean, slot2: Boolean): Int =
    (if (slot0) 1 else 0) or (if (slot1) 2 else 0) or (if (slot2) 4 else 0)

/** Метаморфоза реально работает, только если включена и есть хотя бы один активный слот. */
fun morphEffective(en: Boolean, mask: Int): Boolean = en && mask != 0
```

- [ ] **Step 4: Запустить тест — убедиться, что проходит**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.generator2.features.generator.CarrierMorphTest"
```

Ожидаемо: `BUILD SUCCESSFUL`, 8 тестов пройдено.

- [ ] **Step 5: Коммит**

```bash
git add app/src/main/java/com/example/generator2/features/generator/CarrierMorph.kt app/src/test/java/com/example/generator2/features/generator/CarrierMorphTest.kt && git commit -m "feat(метаморфоза): хелперы шага, маски слотов и активности"
```

---

### Task 2: Поля данных и буферы слотов в Kotlin

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/generator/Generator.kt`

- [ ] **Step 1: Добавить 18 полей в `DataLiveData`**

В `Generator.kt` найти строку `var ch2_Master_TOff: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR PS PC` и сразу после неё вставить:

```kotlin

    // Метаморфоза несущей CH1
    var ch1_Morph_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),               //PR PS PC
    var ch1_Morph_Mode: MutableStateFlow<Int> = MutableStateFlow(1),                     //PR PS PC 0=Ступень 1=Плавно
    var ch1_Morph_Time: MutableStateFlow<Float> = MutableStateFlow(2f),                  //PR PS PC сек 0.1..100, длительность шага
    var ch1_Morph_Slot0_EN: MutableStateFlow<Boolean> = MutableStateFlow(true),          //PR PS PC
    var ch1_Morph_Slot1_EN: MutableStateFlow<Boolean> = MutableStateFlow(true),          //PR PS PC
    var ch1_Morph_Slot2_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),         //PR PS PC
    var ch1_Morph_Slot0_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"),   //PR PS PC
    var ch1_Morph_Slot1_Filename: MutableStateFlow<String> = MutableStateFlow("Square"), //PR PS PC
    var ch1_Morph_Slot2_Filename: MutableStateFlow<String> = MutableStateFlow("Ramp"),   //PR PS PC

    // Метаморфоза несущей CH2
    var ch2_Morph_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),               //PR PS PC
    var ch2_Morph_Mode: MutableStateFlow<Int> = MutableStateFlow(1),                     //PR PS PC 0=Ступень 1=Плавно
    var ch2_Morph_Time: MutableStateFlow<Float> = MutableStateFlow(2f),                  //PR PS PC сек 0.1..100, длительность шага
    var ch2_Morph_Slot0_EN: MutableStateFlow<Boolean> = MutableStateFlow(true),          //PR PS PC
    var ch2_Morph_Slot1_EN: MutableStateFlow<Boolean> = MutableStateFlow(true),          //PR PS PC
    var ch2_Morph_Slot2_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),         //PR PS PC
    var ch2_Morph_Slot0_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"),   //PR PS PC
    var ch2_Morph_Slot1_Filename: MutableStateFlow<String> = MutableStateFlow("Square"), //PR PS PC
    var ch2_Morph_Slot2_Filename: MutableStateFlow<String> = MutableStateFlow("Ramp"),   //PR PS PC
```

- [ ] **Step 2: Добавить буферы слотов в Kotlin-`StructureCh`**

В том же файле, в `data class StructureCh`, найти строку `var buffer_fm: FloatArray = FloatArray(1024),      //-1..1` и сразу после неё вставить:

```kotlin

    //Слоты форм несущей для метаморфозы, -1..1
    var buffer_morph: Array<FloatArray> = Array(3) { FloatArray(1024) },
```

- [ ] **Step 3: Проверить компиляцию**

```bash
./gradlew :app:compileDebugKotlin
```

Ожидаемо: `BUILD SUCCESSFUL`. Предупреждения об `equals`/`hashCode` для массивов в data-классе — существующие, не новые.

- [ ] **Step 4: Коммит**

```bash
git add app/src/main/java/com/example/generator2/features/generator/Generator.kt && git commit -m "feat(метаморфоза): поля DataLiveData и буферы слотов в StructureCh"
```

---

### Task 3: Нативное состояние и приём буферов слотов

**Files:**
- Modify: `app/src/main/cpp/generator/renderchannel.h`
- Modify: `app/src/main/cpp/generator/renderchannel.cpp`

- [ ] **Step 1: Добавить состояние метаморфозы в `StructureCh`**

В `renderchannel.h` найти строку `uint32_t phase_accumulator_fm = 0;` и сразу после неё вставить:

```cpp

    // Метаморфоза несущей
    float    buffer_morph[3][1024] = {{0.0f}};  // 3 слота форм несущей, -1..1
    uint8_t  morph_slot = 0;                    // текущий активный слот (0..2)
    uint32_t morph_counter = 0;                 // сэмплов пройдено в текущем шаге
```

- [ ] **Step 2: Принимать буферы слотов в `sendBuffer`**

В `renderchannel.cpp`, в функции `Java_com_example_generator2_features_generator_RenderChannel_sendBuffer`, найти блок:

```cpp
        case 3 : {
            destination = pStructureCh->buffer_master;
            break;
        }
```

и сразу после него вставить:

```cpp
        case 4 :
        case 5 :
        case 6 : {
            destination = pStructureCh->buffer_morph[modulation - 4];
            break;
        }
```

- [ ] **Step 3: Проверить сборку натива**

```bash
./gradlew :app:assembleDebug
```

Ожидаемо: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Коммит**

```bash
git add app/src/main/cpp/generator/renderchannel.h app/src/main/cpp/generator/renderchannel.cpp && git commit -m "feat(метаморфоза): нативное состояние слотов и приём их форм"
```

---

### Task 4: Загрузка форм слотов из библиотеки

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/generator/Spinner_Send_Buffer.kt`
- Modify: `app/src/main/java/com/example/generator2/features/generator/observe.kt`

- [ ] **Step 1: Расширить `GeneratorMOD` и убрать дублирование выбора списка**

В `Spinner_Send_Buffer.kt` заменить строку:

```kotlin
enum class GeneratorMOD { CR, AM, FM, MASTER }
```

на:

```kotlin
enum class GeneratorMOD { CR, AM, FM, MASTER, MORPH0, MORPH1, MORPH2 }
```

Затем заменить блок выбора списка и буфера — этот кусок:

```kotlin
    val index = if (Mod == GeneratorMOD.CR)
        gen.itemlistCarrier.indexOfFirst { it.name == name }
    else
        gen.itemlistAM.indexOfFirst { it.name == name }

    if (index == -1) {
        return
    }

    val buf = if (Mod == GeneratorMOD.CR)
        gen.itemlistCarrier[index].buf
    else
        gen.itemlistAM[index].buf
```

на:

```kotlin
    //Несущая и все слоты метаморфозы берутся из библиотеки несущих
    val list = when (Mod) {
        GeneratorMOD.CR, GeneratorMOD.MORPH0, GeneratorMOD.MORPH1, GeneratorMOD.MORPH2 ->
            gen.itemlistCarrier

        else -> gen.itemlistAM
    }

    val index = list.indexOfFirst { it.name == name }

    if (index == -1) {
        return
    }

    val buf = list[index].buf
```

- [ ] **Step 2: Добавить ветки слотов в оба канала**

В том же файле, в блоке `if (CH == GeneratorCH.CH0) { when (Mod) { ... } }`, перед строкой `else -> {` вставить:

```kotlin
            GeneratorMOD.MORPH0, GeneratorMOD.MORPH1, GeneratorMOD.MORPH2 -> {
                val slot = Mod.ordinal - GeneratorMOD.MORPH0.ordinal
                gen.ch1.buffer_morph[slot] =
                    byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, -1f, 1f)
                RenderChannel().sendBuffer(0, 4 + slot, gen.ch1.buffer_morph[slot])
            }
```

В блоке `else { when (Mod) { ... } }` (канал CH1), перед строкой `else -> {gen.ch2.buffer_carrier =` вставить:

```kotlin
            GeneratorMOD.MORPH0, GeneratorMOD.MORPH1, GeneratorMOD.MORPH2 -> {
                val slot = Mod.ordinal - GeneratorMOD.MORPH0.ordinal
                gen.ch2.buffer_morph[slot] =
                    byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, -1f, 1f)
                RenderChannel().sendBuffer(1, 4 + slot, gen.ch2.buffer_morph[slot])
            }
```

- [ ] **Step 3: Подписаться на имена форм слотов**

В `observe.kt` после строки с `ch2_FM_Filename` вставить:

```kotlin

    GlobalScope.launch(dispatchers) { gen.liveData.ch1_Morph_Slot0_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH0, GeneratorMOD.MORPH0, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch1_Morph_Slot1_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH0, GeneratorMOD.MORPH1, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch1_Morph_Slot2_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH0, GeneratorMOD.MORPH2, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2_Morph_Slot0_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH1, GeneratorMOD.MORPH0, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2_Morph_Slot1_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH1, GeneratorMOD.MORPH1, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2_Morph_Slot2_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH1, GeneratorMOD.MORPH2, it, gen ) } }
```

- [ ] **Step 4: Проверить компиляцию**

```bash
./gradlew :app:compileDebugKotlin
```

Ожидаемо: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Коммит**

```bash
git add app/src/main/java/com/example/generator2/features/generator/Spinner_Send_Buffer.kt app/src/main/java/com/example/generator2/features/generator/observe.kt && git commit -m "feat(метаморфоза): загрузка форм слотов из библиотеки несущих"
```

---

### Task 5: Морфинг в нативном рендере

Натив и Kotlin меняются вместе: имя JNI-функции не содержит сигнатуры, поэтому рассогласование числа параметров компилируется, но падает в рантайме. Обе стороны — в одном коммите.

**Files:**
- Modify: `app/src/main/cpp/generator/renderchannel.cpp`
- Modify: `app/src/main/java/com/example/generator2/features/generator/RenderChannel.kt`

- [ ] **Step 1: Добавить хелперы слотов и `MorphRunner`**

В `renderchannel.cpp` после функции `map(...)` (перед `extern "C"` с `jniRenderChannel`) вставить:

```cpp
// Режим метаморфозы, зеркало MORPH_MODE_* из CarrierMorph.kt
static const int MORPH_MODE_SMOOTH_C = 1;

/** Первый активный слот в маске. Вызывается только при mask != 0. */
static inline uint8_t firstMorphSlot(int mask) {
    for (uint8_t s = 0; s < 3; s++) if (mask & (1 << s)) return s;
    return 0;
}

/** Следующий активный слот по кругу. При единственном активном возвращает его же. */
static inline uint8_t nextMorphSlot(uint8_t slot, int mask) {
    for (int i = 1; i <= 3; i++) {
        auto s = (uint8_t) ((slot + i) % 3);
        if (mask & (1 << s)) return s;
    }
    return slot;
}

/**
 * Метаморфоза несущей: держит пару таблиц (текущая и следующая форма)
 * и продвигает состояние на каждый сэмпл.
 * Когда метаморфоза выключена, обе таблицы — обычный buffer_carrier,
 * коэффициент смешивания нулевой, поведение точно как раньше.
 */
struct MorphRunner {
    StructureCh *s = nullptr;
    bool on = false;
    bool smooth = false;
    int mask = 0;
    uint32_t steps = 1;
    float inv_steps = 0.0f;
    const float *pA = nullptr;
    const float *pB = nullptr;

    void init(StructureCh *ch, bool en, int mode, int st, int msk) {
        s = ch;
        mask = msk;
        on = en && msk != 0;
        smooth = (mode == MORPH_MODE_SMOOTH_C);
        steps = (uint32_t) (st > 0 ? st : 1);
        inv_steps = 1.0f / (float) steps;
        refresh();
    }

    void refresh() {
        if (!on) {
            pA = s->buffer_carrier;
            pB = s->buffer_carrier;
            return;
        }
        pA = s->buffer_morph[s->morph_slot];
        pB = smooth ? s->buffer_morph[nextMorphSlot(s->morph_slot, mask)] : pA;
    }

    /** Значение несущей по фазовому индексу + продвижение состояния на сэмпл. */
    inline float sample(uint32_t idx, bool wrapped) {
        float t = 0.0f;
        if (on && smooth) {
            t = (float) s->morph_counter * inv_steps;
            if (t > 1.0f) t = 1.0f;
        }
        float c = pA[idx] + t * (pB[idx] - pA[idx]);
        if (on) advance(wrapped);
        return c;
    }

    inline void advance(bool wrapped) {
        // Слот мог погаснуть, пока играли — уходим с него, но только по обороту фазы
        bool slot_invalid = !(mask & (1 << s->morph_slot));

        if (smooth && !slot_invalid) {
            if (++s->morph_counter >= steps) {
                s->morph_counter = 0;
                s->morph_slot = nextMorphSlot(s->morph_slot, mask);
                refresh();
            }
            return;
        }

        if (!slot_invalid && s->morph_counter < steps) {
            s->morph_counter++;
            return;
        }

        if (wrapped) {
            s->morph_counter = 0;
            s->morph_slot = slot_invalid ? firstMorphSlot(mask)
                                         : nextMorphSlot(s->morph_slot, mask);
            refresh();
        }
    }
};
```

- [ ] **Step 2: Добавить параметры в JNI-сигнатуру**

В `renderchannel.cpp`, в объявлении `Java_com_example_generator2_features_generator_RenderChannel_jniRenderChannel`, найти строку `jboolean button_pressed,` и сразу после неё вставить:

```cpp

                                                                              jboolean morph_en,
                                                                              jint morph_mode,
                                                                              jint morph_steps,
                                                                              jint morph_mask,
```

- [ ] **Step 3: Завести `MorphRunner` перед ветками рендера**

В той же функции найти строку `uint64_t delta = 0;` и сразу после неё вставить:

```cpp

    MorphRunner morph;
    morph.init(pStructureCh, morph_en, morph_mode, morph_steps, morph_mask);
```

- [ ] **Step 4: Переписать ветку CR (без FM, без AM)**

Заменить блок:

```cpp
    if (!en_fm && !en_am) {
        for (int i = 0; i < num_frames; i++) {
            pStructureCh->phase_accumulator_carrier += r_c32;
            tempArrayElements[i] = volume * pStructureCh->buffer_carrier[pStructureCh->phase_accumulator_carrier >> 22];
        }
    }
```

на:

```cpp
    if (!en_fm && !en_am) {
        for (int i = 0; i < num_frames; i++) {
            uint32_t prev = pStructureCh->phase_accumulator_carrier;
            pStructureCh->phase_accumulator_carrier += r_c32;
            bool wrapped = pStructureCh->phase_accumulator_carrier < prev;
            uint32_t idx = pStructureCh->phase_accumulator_carrier >> 22;
            tempArrayElements[i] = volume * morph.sample(idx, wrapped);
        }
    }
```

- [ ] **Step 5: Переписать ветку CR+AM**

Заменить блок:

```cpp
    if (!en_fm && en_am) {
        for (int i = 0; i < num_frames; i++) {
            pStructureCh->phase_accumulator_carrier += r_c32;

            pStructureCh->phase_accumulator_am += r_am32;
            tempArrayElements[i] = volume * pStructureCh->buffer_carrier[pStructureCh->phase_accumulator_carrier >> 22]
                                   * (pStructureCh->buffer_am[pStructureCh->phase_accumulator_am >> 22] * am_depth + 1.0f - am_depth);
        }
    }
```

на:

```cpp
    if (!en_fm && en_am) {
        for (int i = 0; i < num_frames; i++) {
            uint32_t prev = pStructureCh->phase_accumulator_carrier;
            pStructureCh->phase_accumulator_carrier += r_c32;
            bool wrapped = pStructureCh->phase_accumulator_carrier < prev;
            uint32_t idx = pStructureCh->phase_accumulator_carrier >> 22;

            pStructureCh->phase_accumulator_am += r_am32;
            tempArrayElements[i] = volume * morph.sample(idx, wrapped)
                                   * (pStructureCh->buffer_am[pStructureCh->phase_accumulator_am >> 22] * am_depth + 1.0f - am_depth);
        }
    }
```

- [ ] **Step 6: Переписать ветку CR+FM**

Заменить блок:

```cpp
    if (en_fm && !en_am) {

        for (int i = 0; i < num_frames; i++) {
            pStructureCh->phase_accumulator_fm += r_fm32;

            pStructureCh->phase_accumulator_carrier +=

                    static_cast<unsigned int>(convertHzToR(
                    pStructureCh->buffer_fm[pStructureCh->phase_accumulator_fm >> 22], sampleRate)


                            );

            tempArrayElements[i] = volume * pStructureCh->buffer_carrier[pStructureCh->phase_accumulator_carrier >> 22];
        }
    }
```

на:

```cpp
    if (en_fm && !en_am) {

        for (int i = 0; i < num_frames; i++) {
            pStructureCh->phase_accumulator_fm += r_fm32;

            uint32_t prev = pStructureCh->phase_accumulator_carrier;

            pStructureCh->phase_accumulator_carrier +=
                    static_cast<unsigned int>(convertHzToR(
                            pStructureCh->buffer_fm[pStructureCh->phase_accumulator_fm >> 22], sampleRate));

            bool wrapped = pStructureCh->phase_accumulator_carrier < prev;
            uint32_t idx = pStructureCh->phase_accumulator_carrier >> 22;

            tempArrayElements[i] = volume * morph.sample(idx, wrapped);
        }
    }
```

- [ ] **Step 7: Переписать ветку CR+FM+AM**

Заменить блок:

```cpp
    if (en_fm && en_am) {

        for (int i = 0; i < num_frames; i++) {

            pStructureCh->phase_accumulator_fm += r_fm32;

            delta = MAX32/sampleRate;

            pStructureCh->phase_accumulator_carrier += static_cast<uint32_t>(static_cast<float>(delta) * pStructureCh->buffer_fm[pStructureCh->phase_accumulator_fm >> 22]);   //(unsigned int)(convertHzToR(pStructureCh->buffer_fm[pStructureCh->phase_accumulator_fm >> 22], sampleRate));
            pStructureCh->phase_accumulator_am += r_am32;

            tempArrayElements[i] = volume * pStructureCh->buffer_carrier[pStructureCh->phase_accumulator_carrier >> 22] *
                           (pStructureCh->buffer_am[pStructureCh->phase_accumulator_am >> 22] * am_depth + 1.0f - am_depth);
        }

    }
```

на:

```cpp
    if (en_fm && en_am) {

        for (int i = 0; i < num_frames; i++) {

            pStructureCh->phase_accumulator_fm += r_fm32;

            delta = MAX32/sampleRate;

            uint32_t prev = pStructureCh->phase_accumulator_carrier;
            pStructureCh->phase_accumulator_carrier += static_cast<uint32_t>(static_cast<float>(delta) * pStructureCh->buffer_fm[pStructureCh->phase_accumulator_fm >> 22]);
            bool wrapped = pStructureCh->phase_accumulator_carrier < prev;
            uint32_t idx = pStructureCh->phase_accumulator_carrier >> 22;

            pStructureCh->phase_accumulator_am += r_am32;

            tempArrayElements[i] = volume * morph.sample(idx, wrapped) *
                           (pStructureCh->buffer_am[pStructureCh->phase_accumulator_am >> 22] * am_depth + 1.0f - am_depth);
        }

    }
```

- [ ] **Step 8: Добавить параметры в `external fun` на стороне Kotlin**

В `RenderChannel.kt`, в объявлении `external fun jniRenderChannel(...)`, найти строку `buttonPressed: Boolean,` и сразу после неё вставить:

```kotlin

        morphEN: Boolean,
        morphMode: Int,
        morphSteps: Int,
        morphMask: Int,
```

- [ ] **Step 9: Читать параметры метаморфозы из `liveData`**

В `RenderChannel.kt`, в функции `renderChanel`, найти блок объявлений:

```kotlin
        val masterEN: Boolean
        val masterMode: Int
        val masterPeriod: Float
        val masterTOn: Float
        val masterTOff: Float
```

и сразу после него вставить:

```kotlin

        val morphEN: Boolean
        val morphMode: Int
        val morphTime: Float
        val morphSlotMask: Int
```

Затем в ветке `if (ch.ch == 0) {` после строки `masterTOff = liveData.ch1_Master_TOff.value` вставить:

```kotlin
            morphEN = liveData.ch1_Morph_EN.value
            morphMode = liveData.ch1_Morph_Mode.value
            morphTime = liveData.ch1_Morph_Time.value
            morphSlotMask = morphMask(
                liveData.ch1_Morph_Slot0_EN.value,
                liveData.ch1_Morph_Slot1_EN.value,
                liveData.ch1_Morph_Slot2_EN.value
            )
```

и в ветке `} else {` после строки `masterTOff = liveData.ch2_Master_TOff.value` вставить:

```kotlin
            morphEN = liveData.ch2_Morph_EN.value
            morphMode = liveData.ch2_Morph_Mode.value
            morphTime = liveData.ch2_Morph_Time.value
            morphSlotMask = morphMask(
                liveData.ch2_Morph_Slot0_EN.value,
                liveData.ch2_Morph_Slot1_EN.value,
                liveData.ch2_Morph_Slot2_EN.value
            )
```

- [ ] **Step 10: Посчитать производные и передать в JNI**

В `RenderChannel.kt` найти строку `val buttonPressed = liveData.masterButton.value` и сразу после неё вставить:

```kotlin

        val morphStepSamples = morphSteps(morphTime, sampleRate)
        val morphOn = morphEffective(morphEN, morphSlotMask)
```

Затем в вызове `jniRenderChannel(...)` найти строку `buttonPressed,` и сразу после неё вставить:

```kotlin

            morphOn,
            morphMode,
            morphStepSamples,
            morphSlotMask,
```

- [ ] **Step 11: Собрать проект целиком**

```bash
./gradlew :app:assembleDebug
```

Ожидаемо: `BUILD SUCCESSFUL`.

- [ ] **Step 12: Проверить, что старые тесты не сломались**

```bash
./gradlew :app:testDebugUnitTest
```

Ожидаемо: `BUILD SUCCESSFUL`.

- [ ] **Step 13: Коммит**

```bash
git add app/src/main/cpp/generator/renderchannel.cpp app/src/main/java/com/example/generator2/features/generator/RenderChannel.kt && git commit -m "feat(метаморфоза): смешивание форм несущей в нативном рендере"
```

---

### Task 6: Спиннер умеет слоты метаморфозы и умеет гаснуть

**Files:**
- Modify: `app/src/main/java/com/example/generator2/screens/mainscreen4/ui/UIspinner.kt`

- [ ] **Step 1: Добавить параметр `enable`**

В `UIspinner.kt` заменить сигнатуру:

```kotlin
    fun Spinner(
        CH: String,
        Mod: String,
        transparent: Boolean = false,
        modifier: Modifier = Modifier,
        filename: State<String>,
        gen: Generator
    ) {
```

на:

```kotlin
    fun Spinner(
        CH: String,
        Mod: String,
        transparent: Boolean = false,
        modifier: Modifier = Modifier,
        filename: State<String>,
        gen: Generator,
        enable: Boolean = true
    ) {
```

В блоке импортов, сразу после `import androidx.compose.ui.Modifier`, добавить:

```kotlin
import androidx.compose.ui.draw.alpha
```

- [ ] **Step 2: Гасить и блокировать спиннер при `enable == false`**

Заменить:

```kotlin
            Row(modifier = Modifier
                .clickable {
                    expanded.value = !expanded.value
                }
```

на:

```kotlin
            Row(modifier = Modifier
                .alpha(if (enable) 1f else 0.35f)
                .clickable(enabled = enable) {
                    expanded.value = !expanded.value
                }
```

- [ ] **Step 3: Добавить слоты метаморфозы в выбор списка**

Заменить:

```kotlin
            "MASTER" -> itemlist = gen.itemlistAM
        }
```

на:

```kotlin
            "MASTER" -> itemlist = gen.itemlistAM
            "MORPH0", "MORPH1", "MORPH2" -> itemlist = gen.itemlistCarrier
        }
```

- [ ] **Step 4: Читать текущее имя формы слота**

Заменить:

```kotlin
                "MASTER" -> currentValue = gen.liveData.ch1_Master_Filename.value
            }
        } else {
```

на:

```kotlin
                "MASTER" -> currentValue = gen.liveData.ch1_Master_Filename.value
                "MORPH0" -> currentValue = gen.liveData.ch1_Morph_Slot0_Filename.value
                "MORPH1" -> currentValue = gen.liveData.ch1_Morph_Slot1_Filename.value
                "MORPH2" -> currentValue = gen.liveData.ch1_Morph_Slot2_Filename.value
            }
        } else {
```

Заменить:

```kotlin
                "MASTER" -> currentValue = gen.liveData.ch2_Master_Filename.value
            }
        }
```

на:

```kotlin
                "MASTER" -> currentValue = gen.liveData.ch2_Master_Filename.value
                "MORPH0" -> currentValue = gen.liveData.ch2_Morph_Slot0_Filename.value
                "MORPH1" -> currentValue = gen.liveData.ch2_Morph_Slot1_Filename.value
                "MORPH2" -> currentValue = gen.liveData.ch2_Morph_Slot2_Filename.value
            }
        }
```

- [ ] **Step 5: Записывать выбранное имя формы слота**

В обработчике `onClick` заменить:

```kotlin
                                        "MASTER" -> gen.liveData.ch1_Master_Filename.value = currentValue
                                    }
```

на:

```kotlin
                                        "MASTER" -> gen.liveData.ch1_Master_Filename.value = currentValue
                                        "MORPH0" -> gen.liveData.ch1_Morph_Slot0_Filename.value = currentValue
                                        "MORPH1" -> gen.liveData.ch1_Morph_Slot1_Filename.value = currentValue
                                        "MORPH2" -> gen.liveData.ch1_Morph_Slot2_Filename.value = currentValue
                                    }
```

и заменить:

```kotlin
                                        "MASTER" -> gen.liveData.ch2_Master_Filename.value = currentValue

                                    }
```

на:

```kotlin
                                        "MASTER" -> gen.liveData.ch2_Master_Filename.value = currentValue
                                        "MORPH0" -> gen.liveData.ch2_Morph_Slot0_Filename.value = currentValue
                                        "MORPH1" -> gen.liveData.ch2_Morph_Slot1_Filename.value = currentValue
                                        "MORPH2" -> gen.liveData.ch2_Morph_Slot2_Filename.value = currentValue
                                    }
```

- [ ] **Step 6: Проверить компиляцию**

```bash
./gradlew :app:compileDebugKotlin
```

Ожидаемо: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Коммит**

```bash
git add app/src/main/java/com/example/generator2/screens/mainscreen4/ui/UIspinner.kt && git commit -m "feat(метаморфоза): спиннер слотов и режим гашения"
```

---

### Task 7: Карточка метаморфозы

Файлы карточек (`cardCarrier.kt`, `cardMaster.kt`, `cardAM.kt`) лежат в **корневом пакете** — без объявления `package`, и импортируются в `cardCard.kt` по голому имени. `cardMorph.kt` следует тому же шаблону.

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardMorph.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardCard.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardCarrier.kt`

- [ ] **Step 1: Создать карточку**

Создать `app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardMorph.kt`:

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
import com.example.generator2.features.generator.MORPH_MODE_SMOOTH
import com.example.generator2.features.generator.MORPH_MODE_STEP
import com.example.generator2.screens.common.modifier.noRippleClickable
import com.example.generator2.screens.mainscreen4.ms4SwitchWidth
import com.example.generator2.screens.mainscreen4.textStyleButtonOnOff
import com.example.generator2.screens.mainscreen4.ui.MainscreenTextBoxAndDropdownMenu
import com.example.generator2.screens.mainscreen4.ui.UIspinner
import com.example.generator2.theme.colorDarkBackground
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun CardMorph(str: String = "CH0", gen: Generator) {

    val isCh0 = str == "CH0"

    val en by (if (isCh0) gen.liveData.ch1_Morph_EN else gen.liveData.ch2_Morph_EN).collectAsState()
    val mode by (if (isCh0) gen.liveData.ch1_Morph_Mode else gen.liveData.ch2_Morph_Mode).collectAsState()
    val time by (if (isCh0) gen.liveData.ch1_Morph_Time else gen.liveData.ch2_Morph_Time).collectAsState()

    Column {

        Box(
            modifier = Modifier
                .background(Color.DarkGray)
                .height(1.dp)
                .fillMaxWidth()
        )

        // Строка 1: включение, режим смены, длительность шага
        Row(Modifier.padding(top = 0.dp), verticalAlignment = Alignment.CenterVertically) {

            MorphButton("MOR", en, Modifier.width(ms4SwitchWidth)) {
                if (isCh0) gen.liveData.ch1_Morph_EN.value = !gen.liveData.ch1_Morph_EN.value
                else gen.liveData.ch2_Morph_EN.value = !gen.liveData.ch2_Morph_EN.value
            }

            fun setMode(m: Int) {
                if (isCh0) gen.liveData.ch1_Morph_Mode.value = m
                else gen.liveData.ch2_Morph_Mode.value = m
            }

            MorphButton("Ступень", mode == MORPH_MODE_STEP, Modifier.weight(1f)) {
                setMode(MORPH_MODE_STEP)
            }
            MorphButton("Плавно", mode == MORPH_MODE_SMOOTH, Modifier.weight(1f)) {
                setMode(MORPH_MODE_SMOOTH)
            }

            MainscreenTextBoxAndDropdownMenu(
                str = String.format("%.1f c", time),
                modifier = Modifier.weight(1f),
                items = listOf("0.1", "0.5", "1.0", "2.0", "5.0", "10.0", "60.0"),
                value = time,
                onChange = {
                    if (isCh0) gen.liveData.ch1_Morph_Time.value = it
                    else gen.liveData.ch2_Morph_Time.value = it
                },
                sensing = 0.05f,
                range = 0.1f..100f,
            )
        }

        // Строка 2: три слота форм — галочка участия + выбор формы
        Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            MorphSlot(str, 0, gen, Modifier.weight(1f))
            MorphSlot(str, 1, gen, Modifier.weight(1f))
            MorphSlot(str, 2, gen, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MorphSlot(str: String, slot: Int, gen: Generator, modifier: Modifier = Modifier) {

    val isCh0 = str == "CH0"

    val enFlow: MutableStateFlow<Boolean> = if (isCh0) when (slot) {
        0 -> gen.liveData.ch1_Morph_Slot0_EN
        1 -> gen.liveData.ch1_Morph_Slot1_EN
        else -> gen.liveData.ch1_Morph_Slot2_EN
    } else when (slot) {
        0 -> gen.liveData.ch2_Morph_Slot0_EN
        1 -> gen.liveData.ch2_Morph_Slot1_EN
        else -> gen.liveData.ch2_Morph_Slot2_EN
    }

    val nameFlow: MutableStateFlow<String> = if (isCh0) when (slot) {
        0 -> gen.liveData.ch1_Morph_Slot0_Filename
        1 -> gen.liveData.ch1_Morph_Slot1_Filename
        else -> gen.liveData.ch1_Morph_Slot2_Filename
    } else when (slot) {
        0 -> gen.liveData.ch2_Morph_Slot0_Filename
        1 -> gen.liveData.ch2_Morph_Slot1_Filename
        else -> gen.liveData.ch2_Morph_Slot2_Filename
    }

    val slotEn by enFlow.collectAsState()

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {

        MorphButton(if (slotEn) "V" else "-", slotEn, Modifier.width(40.dp)) {
            enFlow.value = !enFlow.value
        }

        UIspinner.Spinner(
            CH = str,
            Mod = "MORPH$slot",
            modifier = Modifier
                .padding(start = 4.dp, end = 4.dp)
                .wrapContentWidth()
                .clip(shape = RoundedCornerShape(4.dp)),
            filename = nameFlow.collectAsState(),
            gen = gen,
            enable = slotEn
        )
    }
}

@Composable
private fun MorphButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(start = 8.dp)
            .height(32.dp)
            .border(2.dp, if (selected) Color(0xFF1B5E20) else Color.DarkGray, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF01AE0F) else colorDarkBackground)
            .noRippleClickable(onClick = {
                onClick()
                Haptic.confirm()
            }),
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

- [ ] **Step 2: Подключить карточку в стопку канала**

В `cardCard.kt` заменить:

```kotlin
import CardFM
import CardMaster
```

на:

```kotlin
import CardFM
import CardMaster
import CardMorph
```

и заменить:

```kotlin
            CardCarrier(str, gen = gen)
            CardAM(str, gen = gen)
```

на:

```kotlin
            CardCarrier(str, gen = gen)
            CardMorph(str, gen = gen)
            CardAM(str, gen = gen)
```

- [ ] **Step 3: Гасить спиннер несущей при включённой метаморфозе**

В `cardCarrier.kt` найти:

```kotlin
    //Несущая заблокирована только когда FM включена в режиме минимум/максимум
    val carrierEnable = fmSelectMode.value == 0 || !fmEN.value
```

и заменить на:

```kotlin
    //Несущая заблокирована только когда FM включена в режиме минимум/максимум
    val carrierEnable = fmSelectMode.value == 0 || !fmEN.value

    //Форму несущей задаёт метаморфоза, пока она включена
    val morphEN: State<Boolean> =
        if (str == "CH0") gen.liveData.ch1_Morph_EN.collectAsState()
        else gen.liveData.ch2_Morph_EN.collectAsState()
```

Затем в вызове `UIspinner.Spinner(...)` в этом же файле заменить:

```kotlin
                filename = if (str == "CH0") gen.liveData.ch1_Carrier_Filename.collectAsState()
                else gen.liveData.ch2_Carrier_Filename.collectAsState(), gen = gen
            )
```

на:

```kotlin
                filename = if (str == "CH0") gen.liveData.ch1_Carrier_Filename.collectAsState()
                else gen.liveData.ch2_Carrier_Filename.collectAsState(), gen = gen,
                enable = !morphEN.value
            )
```

- [ ] **Step 4: Проверить компиляцию**

```bash
./gradlew :app:compileDebugKotlin
```

Ожидаемо: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Коммит**

```bash
git add app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardMorph.kt app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardCard.kt app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardCarrier.kt && git commit -m "feat(метаморфоза): карточка канала с режимом, временем и тремя слотами"
```

---

### Task 8: Сохранение в пресетах

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/presets/presetsSaveFile.kt`
- Modify: `app/src/main/java/com/example/generator2/features/presets/presetsReadFile.kt`
- Modify: `app/src/main/java/com/example/generator2/features/presets/presetsToLiveData.kt`

- [ ] **Step 1: Сохранение**

В `presetsSaveFile.kt` найти строку `satchel["ch2_Master_TOff"] = gen.liveData.ch2_Master_TOff.value` и сразу после неё вставить:

```kotlin

    satchel["ch1_Morph_EN"] = gen.liveData.ch1_Morph_EN.value
    satchel["ch1_Morph_Mode"] = gen.liveData.ch1_Morph_Mode.value
    satchel["ch1_Morph_Time"] = gen.liveData.ch1_Morph_Time.value
    satchel["ch1_Morph_Slot0_EN"] = gen.liveData.ch1_Morph_Slot0_EN.value
    satchel["ch1_Morph_Slot1_EN"] = gen.liveData.ch1_Morph_Slot1_EN.value
    satchel["ch1_Morph_Slot2_EN"] = gen.liveData.ch1_Morph_Slot2_EN.value
    satchel["ch1_Morph_Slot0_Filename"] = gen.liveData.ch1_Morph_Slot0_Filename.value
    satchel["ch1_Morph_Slot1_Filename"] = gen.liveData.ch1_Morph_Slot1_Filename.value
    satchel["ch1_Morph_Slot2_Filename"] = gen.liveData.ch1_Morph_Slot2_Filename.value

    satchel["ch2_Morph_EN"] = gen.liveData.ch2_Morph_EN.value
    satchel["ch2_Morph_Mode"] = gen.liveData.ch2_Morph_Mode.value
    satchel["ch2_Morph_Time"] = gen.liveData.ch2_Morph_Time.value
    satchel["ch2_Morph_Slot0_EN"] = gen.liveData.ch2_Morph_Slot0_EN.value
    satchel["ch2_Morph_Slot1_EN"] = gen.liveData.ch2_Morph_Slot1_EN.value
    satchel["ch2_Morph_Slot2_EN"] = gen.liveData.ch2_Morph_Slot2_EN.value
    satchel["ch2_Morph_Slot0_Filename"] = gen.liveData.ch2_Morph_Slot0_Filename.value
    satchel["ch2_Morph_Slot1_Filename"] = gen.liveData.ch2_Morph_Slot1_Filename.value
    satchel["ch2_Morph_Slot2_Filename"] = gen.liveData.ch2_Morph_Slot2_Filename.value
```

- [ ] **Step 2: Чтение**

В `presetsReadFile.kt` найти строку `data.ch2_Master_TOff.value = satchel.getOrDefault("ch2_Master_TOff", 1f)` и сразу после неё вставить:

```kotlin

    data.ch1_Morph_EN.value = satchel.getOrDefault("ch1_Morph_EN", false)
    data.ch1_Morph_Mode.value = satchel.getOrDefault("ch1_Morph_Mode", 1)
    data.ch1_Morph_Time.value = satchel.getOrDefault("ch1_Morph_Time", 2f)
    data.ch1_Morph_Slot0_EN.value = satchel.getOrDefault("ch1_Morph_Slot0_EN", true)
    data.ch1_Morph_Slot1_EN.value = satchel.getOrDefault("ch1_Morph_Slot1_EN", true)
    data.ch1_Morph_Slot2_EN.value = satchel.getOrDefault("ch1_Morph_Slot2_EN", false)
    data.ch1_Morph_Slot0_Filename.value = satchel.getOrDefault("ch1_Morph_Slot0_Filename", "Sine")
    data.ch1_Morph_Slot1_Filename.value = satchel.getOrDefault("ch1_Morph_Slot1_Filename", "Square")
    data.ch1_Morph_Slot2_Filename.value = satchel.getOrDefault("ch1_Morph_Slot2_Filename", "Ramp")

    data.ch2_Morph_EN.value = satchel.getOrDefault("ch2_Morph_EN", false)
    data.ch2_Morph_Mode.value = satchel.getOrDefault("ch2_Morph_Mode", 1)
    data.ch2_Morph_Time.value = satchel.getOrDefault("ch2_Morph_Time", 2f)
    data.ch2_Morph_Slot0_EN.value = satchel.getOrDefault("ch2_Morph_Slot0_EN", true)
    data.ch2_Morph_Slot1_EN.value = satchel.getOrDefault("ch2_Morph_Slot1_EN", true)
    data.ch2_Morph_Slot2_EN.value = satchel.getOrDefault("ch2_Morph_Slot2_EN", false)
    data.ch2_Morph_Slot0_Filename.value = satchel.getOrDefault("ch2_Morph_Slot0_Filename", "Sine")
    data.ch2_Morph_Slot1_Filename.value = satchel.getOrDefault("ch2_Morph_Slot1_Filename", "Square")
    data.ch2_Morph_Slot2_Filename.value = satchel.getOrDefault("ch2_Morph_Slot2_Filename", "Ramp")
```

- [ ] **Step 3: Перенос в живые данные**

В `presetsToLiveData.kt` найти строку `gen.liveData.ch2_Master_TOff.value = data.ch2_Master_TOff.value` и сразу после неё вставить:

```kotlin

    gen.liveData.ch1_Morph_EN.value = data.ch1_Morph_EN.value
    gen.liveData.ch1_Morph_Mode.value = data.ch1_Morph_Mode.value
    gen.liveData.ch1_Morph_Time.value = data.ch1_Morph_Time.value
    gen.liveData.ch1_Morph_Slot0_EN.value = data.ch1_Morph_Slot0_EN.value
    gen.liveData.ch1_Morph_Slot1_EN.value = data.ch1_Morph_Slot1_EN.value
    gen.liveData.ch1_Morph_Slot2_EN.value = data.ch1_Morph_Slot2_EN.value
    gen.liveData.ch1_Morph_Slot0_Filename.value = data.ch1_Morph_Slot0_Filename.value
    gen.liveData.ch1_Morph_Slot1_Filename.value = data.ch1_Morph_Slot1_Filename.value
    gen.liveData.ch1_Morph_Slot2_Filename.value = data.ch1_Morph_Slot2_Filename.value

    gen.liveData.ch2_Morph_EN.value = data.ch2_Morph_EN.value
    gen.liveData.ch2_Morph_Mode.value = data.ch2_Morph_Mode.value
    gen.liveData.ch2_Morph_Time.value = data.ch2_Morph_Time.value
    gen.liveData.ch2_Morph_Slot0_EN.value = data.ch2_Morph_Slot0_EN.value
    gen.liveData.ch2_Morph_Slot1_EN.value = data.ch2_Morph_Slot1_EN.value
    gen.liveData.ch2_Morph_Slot2_EN.value = data.ch2_Morph_Slot2_EN.value
    gen.liveData.ch2_Morph_Slot0_Filename.value = data.ch2_Morph_Slot0_Filename.value
    gen.liveData.ch2_Morph_Slot1_Filename.value = data.ch2_Morph_Slot1_Filename.value
    gen.liveData.ch2_Morph_Slot2_Filename.value = data.ch2_Morph_Slot2_Filename.value
```

- [ ] **Step 4: Собрать и прогнать все тесты**

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Ожидаемо: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Коммит**

```bash
git add app/src/main/java/com/example/generator2/features/presets/ && git commit -m "feat(метаморфоза): сохранение параметров в пресетах"
```

---

## Ручная проверка на устройстве

После Task 8 прогнать сценарии на реальном устройстве — нативную часть юнит-тесты не покрывают.

- [ ] Метаморфоза выключена: звук и форма несущей в точности как до изменений
- [ ] Плавно, слоты 0 и 1 активны, `T = 2 c`: тембр непрерывно перетекает синус↔меандр, разрыва на границе шага нет
- [ ] Плавно, все три слота активны: цикл `Sine→Square→Ramp→Sine`, каждый шаг ровно `T`
- [ ] Ступень, `T = 1 c`: форма меняется резко, щелчка нет
- [ ] Снять галочку с текущего слота во время генерации: уход на первый активный без щелчка
- [ ] Снять галочки со всех слотов: играет обычная несущая из `CardCarrier`, тишины нет
- [ ] Оставить один активный слот: играет он постоянно, без переходов
- [ ] Включить AM и FM поверх метаморфозы: обе модуляции работают как раньше
- [ ] Крутить `T` во время генерации: темп меняется, звук не срывается
- [ ] Сохранить пресет, загрузить другой, вернуться: все параметры метаморфозы восстановились
- [ ] Загрузить пресет, сохранённый до этой фичи: метаморфоза выключена, звук прежний
