# Каналы CHL/CHR Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Канал, подписанный «левый», реально играет в левом ухе; всё приложение говорит «CHL/CHR» вместо «1/2»; идентичность канала везде обозначена цветами осциллографа (CHL = жёлтый #FFFF00, CHR = магента #FF00FF).

**Architecture:** Точечный фикс интерлива в `AudioMixerPump` (чётный сэмпл LRLR = левое ухо) + адаптация шейдера осциллографа; затем механический ренейм `ch1/ch2 → chL/chR` слоями (liveData → bare-объекты → enum/карточки → скрипты → ноды), с легаси-именами на границах сериализации (ключи пресетов, JSON графов, алиасы скрипт-команд). Цвета — две константы темы, единственный источник в Kotlin.

**Tech Stack:** Kotlin + Compose, C++ (NDK: PhosphorGrid), GLSL ES 3.0, JUnit4, Gradle (Windows: `.\gradlew.bat`).

**Спека:** `docs/superpowers/specs/2026-07-27-channel-lr-design.md`

---

## Карта файлов

| Файл | Роль |
|---|---|
| `app/.../features/audio/AudioMixerPump.kt` | точка правды интерлива, дефолты роутов |
| `app/.../features/audio/bufMerge.kt`, `split.kt` | утилиты (не меняются, покрываются тестом) |
| `app/.../features/scope/opengl/render/MyGLRendererOscill.kt` | цвета шейдера, wiring visibility |
| `app/src/main/cpp/scope/PhosphorGrid.h` | верх/низ разложения каналов |
| `app/.../features/generator/Generator.kt` | DataLiveData (44 поля ch1_*/ch2_*), gen.ch1/ch2 |
| `app/.../features/generator/Spinner_Send_Buffer.kt` | enum GeneratorCH |
| `app/.../features/presets/presets*.kt` | граница сериализации — ключи НЕ переименовывать |
| `app/.../features/script/ScriptCommand.kt` | парсер: алиасы CH1/CH2 |
| `app/.../screens/scripting/ui/ScriptKeyboard.kt`, `atom/ScriptItem.kt` | клавиатура и раскраска команд |
| `app/.../features/nodes/model/NodeGraph.kt`, `GraphDto.kt` | домен StepParams; DTO-имена JSON остаются |
| `app/.../screens/nodes/dialog/ReadGenDialog.kt` | пикер CHL/CHR |
| `app/.../theme/Color.kt` | colorChL / colorChR |
| `app/.../screens/mainscreen4/**` | карточки, UIspinner, mainscreen4 |
| `app/.../features/mp3/compose/Mp3Route.kt` | бейдж L/R |
| `app/.../features/scope/Scope.kt` | кнопки L/R, sync-селектор |
| `app/src/test/java/.../features/audio/BufMergeTest.kt` | новый тест |
| `app/src/test/java/.../features/script/ScriptCommandTest.kt` | тесты алиасов |

Конвенция после фикса (зафиксирована комментариями в коде):

```
интерлив v: [L, R, L, R, ...]  — чётный индекс = левое ухо (AudioTrack LRLR)
phosphor grid: канал 0 = чётный = L = жёлтый = верхняя половина
               канал 1 = нечётный = R = магента = нижняя половина
```

---

### Task 1: Тест конвенции интерлива

**Files:**
- Create: `app/src/test/java/com/example/generator2/features/audio/BufMergeTest.kt`

- [ ] **Step 1: Написать тест**

```kotlin
package com.example.generator2.features.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class BufMergeTest {

    @Test
    fun `первый аргумент bufMerge попадает в чётные индексы - левое ухо LRLR`() {
        val left = floatArrayOf(1f, 2f, 3f)
        val right = floatArrayOf(-1f, -2f, -3f)

        val v = bufMerge(left, right)

        assertArrayEquals(floatArrayOf(1f, -1f, 2f, -2f, 3f, -3f), v, 0f)
    }

    @Test
    fun `split возвращает чётные сэмплы первым элементом пары - левый канал`() {
        val interleaved = floatArrayOf(1f, -1f, 2f, -2f)

        val (l, r) = split(interleaved)

        assertArrayEquals(floatArrayOf(1f, 2f), l, 0f)
        assertArrayEquals(floatArrayOf(-1f, -2f), r, 0f)
    }
}
```

- [ ] **Step 2: Запустить тест**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.generator2.features.audio.BufMergeTest"`
Expected: PASS (bufMerge/split уже ведут себя так; тест фиксирует конвенцию). Если `split` вернул пару в другом порядке — остановиться и перепроверить `split.kt` (return должен быть `Pair(leftChannel, rightChannel)`).

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/generator2/features/audio/BufMergeTest.kt
git commit -m "test: конвенция интерлива bufMerge/split — чётный сэмпл = левый"
```

---

### Task 2: Физический фикс L/R + адаптация осциллографа + константы цветов

Ушам и глазам меняем соответствие одним коммитом: после него канал «L» играет слева и рисуется жёлтым сверху (картинка осциллографа внешне не меняется).

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/audio/AudioMixerPump.kt:44-45,283-292`
- Modify: `app/src/main/java/com/example/generator2/features/scope/opengl/render/MyGLRendererOscill.kt:171,216-221,416-420`
- Modify: `app/src/main/cpp/scope/PhosphorGrid.h:375-380`
- Modify: `app/src/main/java/com/example/generator2/theme/Color.kt`

- [ ] **Step 1: Дефолты роутов в AudioMixerPump.kt**

Было (строки 44-45):
```kotlin
    val routeR = MutableStateFlow(ROUTESTREAM.GEN) //Выбор источника для вывода сигнала
    val routeL = MutableStateFlow(ROUTESTREAM.OFF)
```
Стало:
```kotlin
    val routeR = MutableStateFlow(ROUTESTREAM.GEN) //Выбор источника для вывода сигнала
    val routeL = MutableStateFlow(ROUTESTREAM.GEN)
```

- [ ] **Step 2: Порядок bufMerge в AudioMixerPump.kt**

Было (строки 283-292):
```kotlin
            //───────────────────────────────────────────────┐
            // Переворот канала                              │
            //───────────────────────────────────────────────┤
            val v = if (shuffle.value) {                  // │
                bufMerge(outL, outR)                      // │
            } else {                                      // │
                //Нормальный режим                        // │
                bufMerge(outR, outL)                      // │
            }                                             // │
            //───────────────────────────────────────────────┘
```
Стало:
```kotlin
            //───────────────────────────────────────────────┐
            // Переворот канала                              │
            //───────────────────────────────────────────────┤
            // Интерлив AudioTrack: чётный сэмпл кадра —     │
            // ЛЕВОЕ ухо (LRLR), поэтому в нормальном        │
            // режиме outL идёт первым аргументом.           │
            val v = if (shuffle.value) {                  // │
                bufMerge(outR, outL)                      // │
            } else {                                      // │
                //Нормальный режим                        // │
                bufMerge(outL, outR)                      // │
            }                                             // │
            //───────────────────────────────────────────────┘
```

- [ ] **Step 3: Константы цветов в theme/Color.kt**

После строки `val colorOrange = Color(0xFFD8BD12)` добавить:
```kotlin
//Цвета идентичности каналов. Синхронизированы с фрагментным шейдером
//осциллографа (MyGLRendererOscill): жёлтый vec3(1,1,0), магента vec3(1,0,1).
val colorChL = Color(0xFFFFFF00) //левый канал, жёлтый
val colorChR = Color(0xFFFF00FF) //правый канал, магента
```

- [ ] **Step 4: Цвета в шейдере MyGLRendererOscill.kt**

Было (строки 216-221, внутри fragmentShaderCode):
```glsl
    float first = 1.0 - exp(-energy.r * gain);
    float second = 1.0 - exp(-energy.g * gain);

    vec3 color = vec3(1.0, 0.0, 1.0) * first * visibility.x
               + vec3(1.0, 1.0, 0.0) * second * visibility.y;
```
Стало:
```glsl
    float first = 1.0 - exp(-energy.r * gain);
    float second = 1.0 - exp(-energy.g * gain);

    // Канал 0 (чётный сэмпл интерлива) = левый = жёлтый,
    // канал 1 = правый = магента. Цвета = colorChL/colorChR из theme/Color.kt.
    vec3 color = vec3(1.0, 1.0, 0.0) * first * visibility.x
               + vec3(1.0, 0.0, 1.0) * second * visibility.y;
```

- [ ] **Step 5: Wiring visibility в MyGLRendererOscill.kt**

Было (строки 416-420):
```kotlin
        glUniform2f(
            visibilityHandle,
            if (bools[2] == 1) 1.0f else 0.0f,
            if (bools[1] == 1) 1.0f else 0.0f
        )
```
Стало:
```kotlin
        glUniform2f(
            visibilityHandle,
            if (bools[1] == 1) 1.0f else 0.0f,
            if (bools[2] == 1) 1.0f else 0.0f
        )
```

Комментарий у объявления (строка 171), было:
```kotlin
    val bools = intArrayOf(0, 1, 1) //oneTwo 0-one 1-two, L 1-true, R
```
Стало:
```kotlin
    //[0] oneTwo 0-one 1-two; [1] L (жёлтый, visibility.x); [2] R (магента, visibility.y)
    val bools = intArrayOf(0, 1, 1)
```

- [ ] **Step 6: Верх/низ в PhosphorGrid.h**

Было (строки 376-380):
```cpp
    /** Переводит уровень сигнала канала в координату бина. */
    float binOf(float level, int channel) const {
        float y = level;
        if (layout_ == 1) {
            y = (channel == 0) ? level * 0.5f - 0.5f : level * 0.5f + 0.5f;
        }
```
Стало:
```cpp
    /** Переводит уровень сигнала канала в координату бина. */
    float binOf(float level, int channel) const {
        float y = level;
        if (layout_ == 1) {
            // Канал 0 (левый, жёлтый) — верхняя половина, канал 1 — нижняя.
            y = (channel == 0) ? level * 0.5f + 0.5f : level * 0.5f - 0.5f;
        }
```

- [ ] **Step 7: Собрать (включая NDK)**

Run: `.\gradlew.bat :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/generator2/features/audio/AudioMixerPump.kt app/src/main/java/com/example/generator2/features/scope/opengl/render/MyGLRendererOscill.kt app/src/main/cpp/scope/PhosphorGrid.h app/src/main/java/com/example/generator2/theme/Color.kt
git commit -m "fix(audio): левый канал реально играет слева, осциллограф следует конвенции LRLR"
```

---

### Task 3: Ренейм префиксных полей liveData (ключи пресетов сохраняются)

Массовая замена идентификаторов `ch1_* → chL_*`, `ch2_* → chR_*` (плюс `ch1AmDepth`, `ch1FmMin/Max` и парные) во всех .kt main+test, затем возврат строковых ключей в файлах пресетов.

**Files:**
- Modify: все `*.kt` в `app/src/main/java` и `app/src/test/java` (механически)
- Modify: `app/src/main/java/com/example/generator2/features/presets/presetsSaveFile.kt`, `presetsReadFile.kt`, `presetsToLiveData.kt` (возврат ключей + комментарий)

- [ ] **Step 1: Проверить, что лишних строковых литералов нет**

Run: `rg '"ch[12]' app/src/main/java --files-with-matches`
Expected: только три файла `features/presets/presets*.kt` (readme.md не в счёт — это не .kt). Если появились другие файлы — остановиться и добавить их в Step 3 по той же схеме.

- [ ] **Step 2: Массовая замена (PowerShell)**

```powershell
$files = Get-ChildItem app/src/main/java,app/src/test/java -Recurse -Filter *.kt
foreach ($f in $files) {
  $t = [IO.File]::ReadAllText($f.FullName)
  $n = $t.Replace('ch1_','chL_').Replace('ch2_','chR_').
         Replace('ch1AmDepth','chLAmDepth').Replace('ch2AmDepth','chRAmDepth').
         Replace('ch1FmMin','chLFmMin').Replace('ch1FmMax','chLFmMax').
         Replace('ch2FmMin','chRFmMin').Replace('ch2FmMax','chRFmMax')
  if ($n -ne $t) { [IO.File]::WriteAllText($f.FullName, $n) }
}
```

- [ ] **Step 3: Вернуть легаси-ключи в файлах пресетов**

```powershell
Get-ChildItem app/src/main/java/com/example/generator2/features/presets -Filter *.kt | ForEach-Object {
  $t = [IO.File]::ReadAllText($_.FullName)
  [IO.File]::WriteAllText($_.FullName, $t.Replace('"chL','"ch1').Replace('"chR','"ch2'))
}
```

После этого строки выглядят так (пример из presetsSaveFile.kt — ключ старый, поле новое):
```kotlin
    satchel["ch1_EN"] = gen.liveData.chL_EN.value
```

- [ ] **Step 4: Комментарий на границе сериализации**

В начало каждого из трёх файлов пресетов (после package/imports) добавить:
```kotlin
//ВНИМАНИЕ: строковые ключи "ch1_*"/"ch2_*" — легаси-формат файлов пресетов.
//НЕ переименовывать в chL_/chR_: сломаются пресеты пользователей.
```

- [ ] **Step 5: Проверить чистоту замены**

Run: `rg '"ch[LR]' app/src/main/java`
Expected: пусто (все строковые ключи вернулись к ch1_/ch2_).

Run: `rg 'ch[12]_' app/src/main/java app/src/test/java --glob '*.kt'`
Expected: только строковые литералы в `features/presets/*.kt` (внутри кавычек). Идентификаторов `ch1_`/`ch2_` не осталось.

- [ ] **Step 6: Собрать и прогнать тесты**

Run: `.\gradlew.bat :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, все тесты PASS. Если компиляция упала — смотреть места, где идентификатор не подпал под шаблоны (исправить руками по тому же словарю замен).

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor(gen): liveData ch1_/ch2_ → chL_/chR_, ключи пресетов сохранены"
```

---

### Task 4: Ренейм объектов gen.ch1/gen.ch2

Оставшиеся «голые» `ch1`/`ch2` в features/generator: поля `Generator.ch1/ch2` и их использования.

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/generator/Generator.kt`
- Modify: `app/src/main/java/com/example/generator2/features/generator/Spinner_Send_Buffer.kt`
- Modify: прочие файлы каталога по результату grep

- [ ] **Step 1: Замена по границе слова в features/generator**

```powershell
Get-ChildItem app/src/main/java/com/example/generator2/features/generator -Filter *.kt | ForEach-Object {
  $t = [IO.File]::ReadAllText($_.FullName)
  $n = [regex]::Replace($t, '\bch1\b', 'chL')
  $n = [regex]::Replace($n, '\bch2\b', 'chR')
  if ($n -ne $t) { [IO.File]::WriteAllText($_.FullName, $n) }
}
```

Ключевые места, которые должны получиться в Generator.kt:
```kotlin
    val chL: StructureCh = StructureCh(ch = 0)
    val chR: StructureCh = StructureCh(ch = 1)
```
и в renderAudio:
```kotlin
            l = if (liveData.chL_EN.value)
                RenderChannel().renderChanel(liveData, chL, numFrames / 2, sampleRate)
```
Поле `StructureCh.ch: Int` (0/1 — нативный индекс) НЕ трогаем.

- [ ] **Step 2: Найти использования вне features/generator**

Run: `rg '\.ch1\b|\.ch2\b' app/src/main/java app/src/test/java --glob '*.kt'`
Expected: остаются только `params.ch1/params.ch2` в features/nodes и `ch1/ch2` в GraphDto.kt (их черёд — Task 7). Если нашлись обращения `gen.ch1`/`gen.ch2` в других местах (например screens/) — заменить на `gen.chL`/`gen.chR` руками.

- [ ] **Step 3: Собрать и прогнать тесты**

Run: `.\gradlew.bat :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, PASS

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor(gen): объекты gen.ch1/ch2 → chL/chR"
```

---

### Task 5: Enum GeneratorCH.CHL/CHR вместо строк "CH0"/"CH1"

Убирает третий слой имён (0-индексные строки в карточках).

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/generator/Spinner_Send_Buffer.kt:8,42`
- Modify: `app/src/main/java/com/example/generator2/features/generator/observe.kt` (15 вызовов)
- Modify: `app/src/main/java/com/example/generator2/screens/mainscreen4/mainscreen4.kt:169,199`
- Modify: `app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardCarrier.kt`, `cardCard.kt`, `cardAM.kt`, `cardFM.kt`, `cardMaster.kt`, `cardMorph.kt`, `atom/buttonChEn.kt`, `ui/UIspinner.kt`

- [ ] **Step 1: Переименовать значения enum**

Spinner_Send_Buffer.kt строка 8, было:
```kotlin
enum class GeneratorCH { CH0, CH1 }
```
Стало:
```kotlin
enum class GeneratorCH { CHL, CHR }
```

- [ ] **Step 2: Глобальная замена ссылок на enum**

```powershell
Get-ChildItem app/src/main/java,app/src/test/java -Recurse -Filter *.kt | ForEach-Object {
  $t = [IO.File]::ReadAllText($_.FullName)
  $n = $t.Replace('GeneratorCH.CH0','GeneratorCH.CHL').Replace('GeneratorCH.CH1','GeneratorCH.CHR')
  if ($n -ne $t) { [IO.File]::WriteAllText($_.FullName, $n) }
}
```

- [ ] **Step 3: Карточки — параметр-строку на enum**

Образец (cardCarrier.kt). Было:
```kotlin
fun CardCarrier(str: String = "CH0", gen: Generator) {

    val chEN: State<Boolean> =
        if (str == "CH0") gen.liveData.chL_EN.collectAsState() else gen.liveData.chR_EN.collectAsState()
```
Стало:
```kotlin
fun CardCarrier(ch: GeneratorCH = GeneratorCH.CHL, gen: Generator) {

    val chEN: State<Boolean> =
        if (ch == GeneratorCH.CHL) gen.liveData.chL_EN.collectAsState() else gen.liveData.chR_EN.collectAsState()
```

Тот же шаблон применить во всех файлах списка: сигнатура `str: String = "CH0"` (или `CH: String`) → `ch: GeneratorCH = GeneratorCH.CHL`; каждое сравнение `str == "CH0"` / `CH == "CH0"` → `ch == GeneratorCH.CHL`; добавить импорт `com.example.generator2.features.generator.GeneratorCH`.

Список для обхода — вывод команды:
Run: `rg '"CH0"|"CH1"' app/src/main/java/com/example/generator2/screens/mainscreen4 -n`

Вызовы в mainscreen4.kt, было:
```kotlin
                    CardCard("CH0", vm.audioMixerPump.gen)
...
                        CardCard("CH1", vm.audioMixerPump.gen)
```
Стало:
```kotlin
                    CardCard(GeneratorCH.CHL, vm.audioMixerPump.gen)
...
                        CardCard(GeneratorCH.CHR, vm.audioMixerPump.gen)
```

- [ ] **Step 4: Проверить, что строк не осталось**

Run: `rg '"CH0"|"CH1"|"CH2"' app/src/main/java/com/example/generator2/screens/mainscreen4 app/src/main/java/com/example/generator2/features/generator`
Expected: пусто.

- [ ] **Step 5: Собрать и прогнать тесты**

Run: `.\gradlew.bat :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, PASS. Компилятор укажет пропущенные call-site — исправить по образцу Step 3.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor(ui): enum GeneratorCH.CHL/CHR вместо строк CH0/CH1"
```

---

### Task 6: Скрипт-язык — CHL/CHR + легаси-алиасы (TDD)

Парсер понимает новые и старые имена; клавиатура выдаёт только новые.

**Files:**
- Test: `app/src/test/java/com/example/generator2/features/script/ScriptCommandTest.kt`
- Modify: `app/src/main/java/com/example/generator2/features/script/ScriptCommand.kt:181-186,244-252`
- Modify: `app/src/main/java/com/example/generator2/screens/scripting/ui/ScriptKeyboard.kt:267-318,873`
- Modify: `app/src/main/java/com/example/generator2/screens/scripting/atom/ScriptItem.kt:60-77`

- [ ] **Step 1: Написать падающие тесты алиасов**

Добавить в ScriptCommandTest.kt после теста `команды генератора`:
```kotlin
    @Test
    fun `новые имена каналов CHL CHR`() {
        assertEquals(Cmd.GenSwitch(1, GenBlock.CR, true), parseCommand("CHL CR ON"))
        assertEquals(Cmd.GenSwitch(2, GenBlock.FM, false), parseCommand("CHR FM OFF"))
        assertEquals(
            Cmd.GenValue(1, GenBlock.CR, GenParam.FR, Operand.Const(440f)),
            parseCommand("CRL FR 440")
        )
        assertEquals(
            Cmd.GenValue(2, GenBlock.FM, GenParam.DEV, Operand.Const(100f)),
            parseCommand("FMR DEV 100")
        )
        assertEquals(Cmd.GenMod(2, GenBlock.AM, "Sine"), parseCommand("AMR MOD Sine"))
        assertEquals(
            Cmd.ReadGen(0, 1, GenBlock.CR, GenParam.FR), parseCommand("READ F0 CRL FR")
        )
    }

    @Test
    fun `легаси-алиасы CH1 CH2 продолжают работать`() {
        assertEquals(parseCommand("CHL CR ON"), parseCommand("CH1 CR ON"))
        assertEquals(parseCommand("CHR CR ON"), parseCommand("CH2 CR ON"))
        assertEquals(parseCommand("CRL FR 440"), parseCommand("CR1 FR 440"))
        assertEquals(parseCommand("FMR DEV 100"), parseCommand("FM2 DEV 100"))
    }
```
Сигнатура подтверждена: `Cmd.ReadGen(val dst: Int, val ch: Int, val block: GenBlock, val param: GenParam)` (ScriptCommand.kt:117).

- [ ] **Step 2: Запустить тесты — убедиться, что падают**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.generator2.features.script.ScriptCommandTest"`
Expected: FAIL — `новые имена каналов CHL CHR` падает с «не разобран номер канала» / «неизвестная команда CHL».

- [ ] **Step 3: Реализовать алиасы в парсере**

ScriptCommand.kt, функция channel (строки 181-186), было:
```kotlin
    //CH1 CR1 AM1 FM1 -> номер канала
    fun channel(token: String): Int = when (token.last()) {
        '1' -> 1
        '2' -> 2
        else -> fail("не разобран номер канала: $token")
    }
```
Стало:
```kotlin
    //CHL CRL AML FML -> канал 1 (левый), CHR CRR AMR FMR -> канал 2 (правый).
    //Цифры 1/2 — легаси-алиасы старых скриптов.
    fun channel(token: String): Int = when (token.last()) {
        'L', '1' -> 1
        'R', '2' -> 2
        else -> fail("не разобран номер канала: $token")
    }
```

Ветки when (строки 244-252), было:
```kotlin
        //CH[1 2] [CR AM FM] [ON OFF]
        "CH1", "CH2" -> {
```
Стало:
```kotlin
        //CH[L R] [CR AM FM] [ON OFF]; CH1/CH2 — легаси
        "CHL", "CHR", "CH1", "CH2" -> {
```
Было:
```kotlin
        //CR[1 2] AM[1 2] FM[1 2]
        "CR1", "CR2", "AM1", "AM2", "FM1", "FM2" -> {
```
Стало:
```kotlin
        //CR[L R] AM[L R] FM[L R]; цифры — легаси
        "CRL", "CRR", "AML", "AMR", "FML", "FMR",
        "CR1", "CR2", "AM1", "AM2", "FM1", "FM2" -> {
```
`block(head.dropLast(1))` и `block(src.dropLast(1))` в READ работают без изменений («CRL».dropLast(1) == «CR»).

- [ ] **Step 4: Запустить тесты — зелёные**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.generator2.features.script.ScriptCommandTest"`
Expected: PASS

- [ ] **Step 5: Клавиатура выдаёт новые имена, клавиши окрашены по каналу**

ScriptKeyboard.kt. Сначала дать KeyX параметр цвета (строка 184), было:
```kotlin
    fun KeyX(label: String, onClick: () -> Unit) {

        TemplateButtonBottomBar(str = label, onClick = {
            onClick()
        })
    }
```
Стало (`TemplateButtonBottomBar` уже имеет `contentColor: Color = Color.White`):
```kotlin
    fun KeyX(label: String, color: Color = Color.White, onClick: () -> Unit) {

        TemplateButtonBottomBar(str = label, contentColor = color, onClick = {
            onClick()
        })
    }
```
Импорты: `androidx.compose.ui.graphics.Color`, `com.example.generator2.theme.colorChL`, `com.example.generator2.theme.colorChR`.

Затем в блоках строк 267-318 заменить label и аргумент listCommandAddToIndex попарно:
`"CH1"`→`"CHL"`, `"CR1"`→`"CRL"`, `"AM1"`→`"AML"`, `"FM1"`→`"FML"`, `"CH2"`→`"CHR"`, `"CR2"`→`"CRR"`, `"AM2"`→`"AMR"`, `"FM2"`→`"FMR"`; клавишам L-группы передать `colorChL`, R-группы — `colorChR`.
Образец, было:
```kotlin
            KeyX("CH1", onClick = {
                Haptic.click()
                listCommandAddToIndex(0, "CH1")
```
Стало:
```kotlin
            KeyX("CHL", color = colorChL, onClick = {
                Haptic.click()
                listCommandAddToIndex(0, "CHL")
```

Строка 873, было:
```kotlin
                    if ((listCommand[0] == "CR1") || (listCommand[0] == "CR2"))
```
Стало:
```kotlin
                    if (listCommand[0] in listOf("CRL", "CRR", "CR1", "CR2"))
```

- [ ] **Step 6: Раскраска команд в ScriptItem.kt**

Было (строки 60-77):
```kotlin
            "CH1", "CH2" -> {
                color = Color(0xFFFFDF30)
                background = Color(0xFF012F50)
            }

            "CR1", "CR2" -> {
                color = Color(0xFF00FFFF)
            }

            "AM1", "AM2" -> {
                color = Color.Green
            }

            "FM1", "FM2" -> {
                color = Color(0xFFFF7A21)
            }
```
Стало (цвета канала — из темы; старые токены — та же раскраска):
```kotlin
            "CHL", "CH1" -> {
                color = colorChL
                background = Color(0xFF012F50)
            }

            "CHR", "CH2" -> {
                color = colorChR
                background = Color(0xFF012F50)
            }

            "CRL", "CRR", "CR1", "CR2" -> {
                color = Color(0xFF00FFFF)
            }

            "AML", "AMR", "AM1", "AM2" -> {
                color = Color.Green
            }

            "FML", "FMR", "FM1", "FM2" -> {
                color = Color(0xFFFF7A21)
            }
```
Добавить импорты `com.example.generator2.theme.colorChL`, `com.example.generator2.theme.colorChR`.

- [ ] **Step 7: Собрать и прогнать все тесты**

Run: `.\gradlew.bat :app:testDebugUnitTest`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat(script): команды CHL/CHR с легаси-алиасами CH1/CH2"
```

---

### Task 7: Ноды — домен chL/chR, JSON совместим

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/nodes/model/NodeGraph.kt:48-50`
- Modify: `app/src/main/java/com/example/generator2/features/nodes/model/GraphDto.kt:40-41,106-107,190-191`
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/dialog/ReadGenDialog.kt:62`
- Modify: по grep — `NodeGraphCompiler.kt`, `NodeGraphValidator.kt`, `screens/nodes/vm/VMNodes.kt`, `screens/nodes/dialog/StepDialog.kt`, тесты nodes

- [ ] **Step 1: Домен StepParams**

NodeGraph.kt, было (строки 48-50):
```kotlin
data class StepParams(val ch1: ChannelParams, val ch2: ChannelParams) {
    val checkedCount: Int get() = ch1.checkedCount + ch2.checkedCount
}
```
Стало:
```kotlin
data class StepParams(val chL: ChannelParams, val chR: ChannelParams) {
    val checkedCount: Int get() = chL.checkedCount + chR.checkedCount
}
```

- [ ] **Step 2: DTO — имена JSON остаются ch1/ch2**

GraphDto.kt, свойства DTO (строки 40-41) НЕ переименовывать, добавить комментарий:
```kotlin
    //Легаси-имена полей JSON сохранённых графов — НЕ переименовывать в chL/chR
    val ch1: ChannelDto? = null,
    val ch2: ChannelDto? = null,
```
Маппинг в домен (строки 106-107), было:
```kotlin
                    ch1 = (ch1 ?: ChannelDto()).toDomain(nodeId),
                    ch2 = (ch2 ?: ChannelDto()).toDomain(nodeId),
```
Стало:
```kotlin
                    chL = (ch1 ?: ChannelDto()).toDomain(nodeId),
                    chR = (ch2 ?: ChannelDto()).toDomain(nodeId),
```
Маппинг в DTO (строки 190-191), было:
```kotlin
            ch1 = b.params.ch1.toDto(),
            ch2 = b.params.ch2.toDto(),
```
Стало (левая часть — поле DTO, остаётся ch1/ch2):
```kotlin
            ch1 = b.params.chL.toDto(),
            ch2 = b.params.chR.toDto(),
```

- [ ] **Step 3: Обновить остальные ссылки домена**

Run: `rg '\.ch1\b|\.ch2\b|\bch1\b|\bch2\b' app/src/main/java/com/example/generator2/features/nodes app/src/main/java/com/example/generator2/screens/nodes app/src/test/java/com/example/generator2/features/nodes -n`
Каждое `params.ch1`/`.ch1` доменного типа → `chL` (аналогично ch2 → chR). Свойства DTO `ch1/ch2` в GraphDto.kt и JSON-строки в тестах GraphDtoTest не трогать (это формат файла).

- [ ] **Step 4: Пикер канала в ReadGenDialog**

Было (строка 62):
```kotlin
                Picker("CH$ch", listOf("CH1", "CH2")) { ch = it + 1 }
```
Стало (внутреннее значение остаётся 1/2 — оно уходит в `NodeBody.ReadGen` и JSON):
```kotlin
                Picker(if (ch == 1) "CHL" else "CHR", listOf("CHL", "CHR")) { ch = it + 1 }
```

- [ ] **Step 5: Компилятор нод**

Run: `rg 'CH|CR|AM|FM' app/src/main/java/com/example/generator2/features/nodes/NodeGraphCompiler.kt -n --glob '*.kt'`
Компилятор генерирует текст скрипт-команд. Если он собирает токены с цифрой (`"CR$ch"`, `"CH$ch"`) — оставить как есть: цифровые токены остаются валидными алиасами, старые графы и золотые тесты не ломаются.

- [ ] **Step 6: Собрать и прогнать тесты**

Run: `.\gradlew.bat :app:testDebugUnitTest`
Expected: PASS (включая все NodeGraph*-тесты)

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor(nodes): домен StepParams chL/chR, формат JSON графов сохранён"
```

---

### Task 8: Единые цвета каналов в UI

**Files:**
- Modify: `app/src/main/java/com/example/generator2/screens/mainscreen4/card/cardCarrier.kt:62-69` (и другие карточки с полосой по grep)
- Modify: `app/src/main/java/com/example/generator2/features/mp3/compose/Mp3Route.kt:48`
- Modify: `app/src/main/java/com/example/generator2/features/scope/Scope.kt:435,450,652,666`

- [ ] **Step 1: Найти все места с зелёной/оранжевой идентичностью канала**

Run: `rg 'colorGreen|colorOrange' app/src/main/java -n`
Expected: полоса в cardCarrier (и, возможно, других карточках), бейдж в Mp3Route. Каждое место, где цвет выбирается по каналу, перевести на colorChL/colorChR (места, где colorGreen используется НЕ для идентичности канала, не трогать).

- [ ] **Step 2: Полоса карточки с лейблом**

cardCarrier.kt, было (строки 62-69):
```kotlin
        Box(
            modifier = Modifier
                .background(if (str == "CH0") colorGreen else colorOrange)
                .height(8.dp)
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {}
```
Стало (учитывая, что после Task 5 параметр называется `ch: GeneratorCH`):
```kotlin
        Box(
            modifier = Modifier
                .background(if (ch == GeneratorCH.CHL) colorChL else colorChR)
                .height(16.dp)
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (ch == GeneratorCH.CHL) "CHL" else "CHR",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
```
Добавить импорты: `colorChL`, `colorChR`, `androidx.compose.ui.text.font.FontWeight`, `androidx.compose.ui.unit.sp`. Тот же шаблон — в остальных карточках с полосой из Step 1.

- [ ] **Step 3: Бейдж Mp3Route**

Было (строка 48):
```kotlin
                .background(if (ch == "L") colorGreen else colorOrange)
```
Стало:
```kotlin
                .background(if (ch == "L") colorChL else colorChR)
```
Импорты colorGreen/colorOrange в файле удалить, если больше не используются.

- [ ] **Step 4: Scope UI на константы темы**

Scope.kt, кнопки видимости трасс, было (строки 435, 450):
```kotlin
                        color = if (stateIsVisibleL) Color.Yellow else colorTextDisabled,
...
                        color = if (stateIsVisibleR) Color.Magenta else colorTextDisabled,
```
Стало:
```kotlin
                        color = if (stateIsVisibleL) colorChL else colorTextDisabled,
...
                        color = if (stateIsVisibleR) colorChR else colorTextDisabled,
```
Селектор синхронизации, было (строки 652, 666):
```kotlin
                    color = if (oscillSync.value == OSCILLSYNC.L) colorTextEnabled else colorTextDisabled
...
                    color = if (oscillSync.value == OSCILLSYNC.R) colorTextEnabled else colorTextDisabled
```
Стало:
```kotlin
                    color = if (oscillSync.value == OSCILLSYNC.L) colorChL else colorTextDisabled
...
                    color = if (oscillSync.value == OSCILLSYNC.R) colorChR else colorTextDisabled
```

- [ ] **Step 5: Цвет пикера каналов в нодах**

RegisterDialog.kt, сигнатура Picker (строка 79), было:
```kotlin
internal fun Picker(current: String, options: List<String>, onPick: (Int) -> Unit) {
```
Стало:
```kotlin
internal fun Picker(
    current: String,
    options: List<String>,
    color: Color = Color.White,
    onPick: (Int) -> Unit,
) {
```
и в Text пикера `color = Color.White` → `color = color`. Существующие вызовы не меняются (параметр с дефолтом, лямбда остаётся trailing).

ReadGenDialog.kt, вызов из Task 7, было:
```kotlin
                Picker(if (ch == 1) "CHL" else "CHR", listOf("CHL", "CHR")) { ch = it + 1 }
```
Стало:
```kotlin
                Picker(
                    if (ch == 1) "CHL" else "CHR",
                    listOf("CHL", "CHR"),
                    color = if (ch == 1) colorChL else colorChR,
                ) { ch = it + 1 }
```

- [ ] **Step 6: Собрать**

Run: `.\gradlew.bat :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat(ui): единые цвета каналов — жёлтый CHL, магента CHR"
```

---

### Task 9: Финальная верификация

- [ ] **Step 1: Все юнит-тесты**

Run: `.\gradlew.bat :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, 0 failed

- [ ] **Step 2: Полная сборка**

Run: `.\gradlew.bat :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Контроль границ сериализации**

Run: `rg '"ch1_|"ch2_|"ch1Am|"ch2Am|"ch1Fm|"ch2Fm' app/src/main/java/com/example/generator2/features/presets -c`
Expected: числа > 0 (легаси-ключи на месте).

Run: `rg 'val ch1|val ch2' app/src/main/java/com/example/generator2/features/nodes/model/GraphDto.kt`
Expected: обе строки DTO на месте.

Лиссажу: канал подачи `channelAudioOutLissagu` закомментирован в AudioMixerPump (мёртвый путь) — изменений не требует, проверить только что он всё ещё закомментирован.

- [ ] **Step 4: Ручная проверка на устройстве (чек-лист)**

1. Включить только CHL, надеть наушники: звук строго в ЛЕВОМ ухе; на осциллографе — жёлтая трасса в верхней половине.
2. Включить только CHR: звук справа, магента-трасса снизу.
3. Кнопка L на осциллографе гасит жёлтую трассу, R — магенту.
4. Shuffle меняет уши местами.
5. MP3-файл с разными каналами (голос «left»/«right»): уши совпадают со словами; переключатели L/R роутов управляют своими ушами.
6. Загрузить старый пресет — параметры обоих каналов восстановились.
7. Открыть старый скрипт с CH1/CR2 — выполняется; новые кнопки клавиатуры вставляют CHL/CRR; команды окрашены (CHL жёлтый, CHR магента).
8. Открыть старый граф нод — читается; пикер READ показывает CHL/CHR.
9. Waterfall/спектрограмма: сигнал CHL отображается как левый.

- [ ] **Step 5: Отметить выполнение плана**

Проставить все чекбоксы, закоммитить обновлённый план:
```bash
git add docs/superpowers/plans/2026-07-27-channel-lr.md
git commit -m "docs: план CHL/CHR выполнен"
```
