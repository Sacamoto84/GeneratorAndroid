# Режим «3-фаза» — план реализации

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Отдельный режим генератора «3-фаза»: один синус, положение точки в круге превращается в
L/R по математике restim, точку водят 16 паттернов restim или палец, есть калибровка A/B/C и
Center reduction, настройки сохраняются в пресеты.

**Architecture:** Чистый Kotlin-пакет `features/threephase` (математика калибровки, паттерны,
движок контрольных точек) покрыт JVM-тестами против эталонов, сгенерированных кодом restim.
Посэмпловый рендер — новый нативный `threephase.cpp`, его вызывает `Generator.renderAudio`, когда
режим включён. UI — карточка с кругом вместо карточек каналов и кнопка «3Φ» в верхней панели.

**Tech Stack:** Kotlin, Jetpack Compose, C++ (JNI, NDK), JUnit4, Gradle; эталоны — Python из venv
restim.

**Spec:** `docs/superpowers/specs/2026-10-29-three-phase-mode-design.md`

**Рабочая директория для всех команд:** `H:/GeneratorAndroid/samples/generator2`

**Gradle:** только через скилл `gradle-run` (`python` + `gradlew.bat -p <root>`, см. память
`gradle-cli-windows`). Ниже указаны только задачи Gradle.

## Global Constraints

- Пакет `com.example.generator2.features.threephase`, тесты — тот же пакет в `app/src/test`.
- В аудиопотоке (`fillPoints`, `update` паттернов, JNI-рендер) нет аллокаций.
- Время паттернов и фаза — `Double` внутри, позиции наружу — `Float`. Остаток от деления как в
  Python: `mod`, не `%`.
- Ключи пресетов `tp_*`, отсутствующий ключ — значение по умолчанию; старые ключи не трогать.
- Имена паттернов дословно как в restim; ручной паттерн — `"Палец"`.
- Дефолты: `tp_EN=false`, `tp_Pattern="Circle"`, `tp_Velocity=1.0`, `tp_Amplitude=1.0`,
  `tp_Carrier_Fr=700.0`, `tp_Volume=0.1`, `tp_Calib_A/B/C=0.0`, `tp_Center=13.0`.
- Диапазоны UI: скорость 0.1…10, амплитуда 0…1, частота 50…10000, громкость 0…1, A/B/C −40…+10
  dB, Center 0…20 %.
- Амплитуда каналов на выходе не больше `maxVolume0` (L) и `maxVolume1` (R) из
  `gen.liveData`, и никогда не больше 1.0.
- Константы: `STEP = 32` сэмпла на контрольную точку, переход паттернов 0.3 с, переход режимов
  0.02 с, сглаживание пальца τ = 0.02 с.

## Review Focus

1. Имя паттерна с символами `ᴒ` / `∧` и скобками переживает запись и чтение пресета → тест в Task 5.
2. Пресет с неизвестным или старым именем паттерна открывается с `Circle` → тест в Task 5.
3. Блок, длина которого не кратна `STEP` или меньше `STEP`, и смена размера блока: позиция на
   стыке блоков непрерывна → тест в Task 4.
4. Повторная смена паттерна, пока идёт переход: старт нового перехода — текущая смешанная точка,
   без скачка → тест в Task 4.
5. Калибровка с невозможным треугольником (`0, −40, −40`) или одинаковыми значениями
   (`−10, −10, −10`): матрица конечна, `|L|, |R| ≤ 1` → тест в Task 1. «Палец» с двумя точками
   в одно и то же время (длительность 0) не делит на ноль → тест в Task 3.

---

## Структура файлов

| Файл | Ответственность |
|---|---|
| `app/src/test/resources/threephase/gen_reference.py` | **создать** — генератор эталонов из restim |
| `app/src/test/resources/threephase/{calib,math,patterns}.csv` | **создать** (генерируются) |
| `app/src/test/java/.../features/threephase/ReferenceCsv.kt` | **создать** — чтение CSV в тестах |
| `.../features/threephase/ThreePhaseMath.kt` | **создать** — калибровка → `K`, центр, эталонный сэмпл |
| `.../features/threephase/ThreePhaseGeometry.kt` | **создать** — экран ↔ (alpha, beta) |
| `.../features/threephase/patterns/ThreePhasePattern.kt` | **создать** — интерфейс, имена, набор |
| `.../features/threephase/patterns/{Mathematical,Basic,Complex,Experimental}Patterns.kt` | **создать** — 16 паттернов по категориям restim |
| `.../features/threephase/patterns/FingerPattern.kt` | **создать** — «Палец» |
| `.../features/threephase/ThreePhaseEngine.kt` | **создать** — контрольные точки блока |
| `.../features/threephase/ModeCrossfade.kt` | **создать** — кроссфейд при смене режима |
| `.../features/threephase/RenderThreePhase.kt` | **создать** — JNI-обёртка |
| `app/src/main/cpp/generator/threephase.{h,cpp}` | **создать** — посэмпловый рендер |
| `app/src/main/cpp/CMakeLists.txt` | добавить `generator/threephase.cpp` |
| `.../features/generator/Generator.kt` | поля `tp_*`, `threePhase`, ветка в `renderAudio` |
| `.../features/generator/GeneratorObserver.kt` | пересчёт `K` по A/B/C |
| `.../features/audio/AudioMixerPump.kt` | пропуск invertL/invertR в 3-фазе |
| `.../features/presets/presets{ReadFile,SaveFile,ToLiveData}.kt`, `readme.md` | ключи `tp_*` |
| `.../screens/mainscreen4/card/ThreePhaseCircle.kt` | **создать** — круг, точка, касания |
| `.../screens/mainscreen4/card/cardThreePhase.kt` | **создать** — карточка режима |
| `.../screens/mainscreen4/top/TopBar.kt` | кнопка «3Φ» |
| `.../screens/mainscreen4/mainscreen4.kt` | карточка 3-фазы вместо карточек каналов |

`...` = `app/src/main/java/com/example/generator2`.

---

### Task 0: Ветка и спека

- [ ] **Step 1:** `git checkout -b feature/three-phase`
- [ ] **Step 2: Commit**

```bash
git add docs/superpowers/specs/2026-10-29-three-phase-mode-design.md docs/superpowers/plans/2026-10-29-three-phase-mode.md
git commit -m "docs(3-фаза): спека и план режима по мотивам restim"
```

---

### Task 1: Эталоны и математика калибровки

**Files:**
- Create: `app/src/test/resources/threephase/gen_reference.py`, `calib.csv`, `math.csv`, `patterns.csv`
- Create: `app/src/test/java/com/example/generator2/features/threephase/ReferenceCsv.kt`
- Create: `app/src/main/java/com/example/generator2/features/threephase/ThreePhaseMath.kt`
- Test: `app/src/test/java/com/example/generator2/features/threephase/ThreePhaseMathTest.kt`

**Interfaces:**
- Produces:
  - `object ThreePhaseMath`
    - `fun calibrationToUdLr(aDb: Double, bDb: Double, cDb: Double): DoubleArray` — `[neutralDb, rightDb]`
    - `fun channelMatrix(aDb: Float, bDb: Float, cDb: Float): FloatArray` — `[K00, K01, K10, K11]`
    - `fun centerGain(r: Float, reductionPct: Float): Float` — `1 − c·(1 − r)`, `c = reductionPct / 100`
    - `fun sample(alpha: Float, beta: Float, theta: Double, k: FloatArray, out: FloatArray)` —
      `out[0] = L`, `out[1] = R` по формуле спеки «На каждый сэмпл», включая нормализацию в
      единичный круг, без громкости и центра. Это эталон для нативного кода.
  - Тестовый `object ReferenceCsv { fun read(name: String): List<Map<String, String>> }` — читает
    `/threephase/<name>` из ресурсов тестов (UTF-8, первая строка — заголовок, разделитель `,`).

- [ ] **Step 1: Написать `gen_reference.py`**

Запуск: `<venv python> gen_reference.py <restim_root> <out_dir>`. Добавляет `restim_root` в
`sys.path`, пишет файлы в UTF-8 (`open(..., encoding="utf-8", newline="")`), числа — `repr(float)`.

| Файл | Колонки | Наборы |
|---|---|---|
| `calib.csv` | `aDb,bDb,cDb,neutralDb,rightDb,k00,k01,k10,k11` | калибровки: `(0,0,0)`, `(0,-16.73,-25.62)`, `(0,-2.83,-3.18)`, `(-6,0,-3)`, `(0,-40,-40)`, `(-10,-10,-10)` |
| `math.csv` | `aDb,bDb,cDb,alpha,beta,theta,L,R` | те же калибровки × точки `(1,0)`, `(-1,0)`, `(-0.5,0.8660254)`, `(-0.5,-0.8660254)`, `(0,0)`, `(0.3,0.4)`, `(1.2,0.5)` × `theta` `0, 0.7, 2.1, 4.0` |
| `patterns.csv` | `name,step,alpha,beta` | все 16 паттернов из `get_registered_patterns()` кроме `Mouse`, конструктор без аргументов, 600 вызовов `update(1/60)`, `step` с 1 |

Вычисления в скрипте:
- `neutralDb, rightDb = intensity_ratio_to_ud_lr(10**(a/10), 10**(b/10), 10**(c/10))`;
- `K`: `hw = ThreePhaseHardwareCalibration(n, r)`, `tr = hw.generate_transform_in_ab()`,
  `C = (P @ ab @ tr @ ab_inv @ P_inv) * hw.scaling_contant(tr)`,
  `T = (P @ ab)[:2, :2] / sqrt(3)`, `K = C[:2, :2] @ T`, где `P = potential_to_channel_matrix`,
  `ab = ab_transform` из `stim_math.transforms`;
- `L, R`: `ThreePhaseSignalGenerator.generate(np.array([theta]), np.array([alpha]), np.array([beta]))`,
  затем `hw.apply_transform(L, R)`.

- [ ] **Step 2: Сгенерировать эталоны**

Run:
```bash
PYTHONIOENCODING=utf-8 H:/restim-master/restim-master/venv/Scripts/python.exe app/src/test/resources/threephase/gen_reference.py H:/restim-master/restim-master app/src/test/resources/threephase
```
Expected: три CSV; `calib.csv` — 6 строк данных, `math.csv` — 168, `patterns.csv` — 9600.

- [ ] **Step 3: Написать падающие тесты `ThreePhaseMathTest`**

```kotlin
@Test fun `calibrationToUdLr совпадает с restim`()      // calib.csv, допуск 1e-4 дБ
@Test fun `channelMatrix совпадает с restim`()          // calib.csv, допуск 1e-4
@Test fun `sample совпадает с restim`()                 // math.csv: channelMatrix(a,b,c) → sample(...), допуск 1e-4
@Test fun `центр приглушается на заданный процент`() {
    assertEquals(0.87f, ThreePhaseMath.centerGain(0f, 13f), 1e-6f)
    assertEquals(0.935f, ThreePhaseMath.centerGain(0.5f, 13f), 1e-6f)
    assertEquals(1f, ThreePhaseMath.centerGain(1f, 13f), 1e-6f)
    assertEquals(1f, ThreePhaseMath.centerGain(0f, 0f), 1e-6f)
}
@Test fun `выход не превышает 1 при любой калибровке`() // калибровки из calib.csv × сетка alpha,beta ∈ [-1.5..1.5] шаг 0.1 × theta шаг 0.05: |L|,|R| ≤ 1.0001, всё конечно
```

- [ ] **Step 4: Run** `:app:testDebugUnitTest --tests "com.example.generator2.features.threephase.ThreePhaseMathTest"` — Expected: FAIL (нет `ThreePhaseMath`).

- [ ] **Step 5: Реализовать `ThreePhaseMath`**

Порт `calibration_matrix_from_electrode_intensities` (с `split_point` на комплексных числах как
паре Double), `calibration_matrix_to_ud_lr`, `inverse_scale_in_arbitrary_direction`,
`ThreePhaseHardwareCalibration.generate_transform_in_ab` / `scaling_contant`. `np.linalg.lstsq`
для системы 3×2 → нормальные уравнения `(AᵀA)⁻¹AᵀB`. `largest_eigenvalue` матрицы 2×2 —
по формуле через след и определитель, вещественная часть.

- [ ] **Step 6: Run** тот же тест — Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add app/src/test/resources/threephase app/src/test/java/com/example/generator2/features/threephase app/src/main/java/com/example/generator2/features/threephase/ThreePhaseMath.kt
git commit -m "feat(3-фаза): математика калибровки restim с эталонными тестами"
```

---

### Task 2: Паттерны restim

**Files:**
- Create: `.../features/threephase/patterns/ThreePhasePattern.kt`
- Create: `.../features/threephase/patterns/MathematicalPatterns.kt` (Circle, Butterfly Curve, Rose Curve, Spirograph)
- Create: `.../features/threephase/patterns/BasicPatterns.kt` (Figure 8, Micro Circles, Tri-Phase panning (ᴒ), Tri-Phase panning (∧), Vertical Oscillation)
- Create: `.../features/threephase/patterns/ComplexPatterns.kt` (Jerky Stroke, Orbiting Circles, Tremor Circle, W Shape)
- Create: `.../features/threephase/patterns/ExperimentalPatterns.kt` (Deep Throb, Lightning Strike, Random Walk)
- Test: `app/src/test/java/com/example/generator2/features/threephase/ThreePhasePatternsTest.kt`

**Interfaces:**
- Consumes: `ReferenceCsv.read("patterns.csv")` (Task 1).
- Produces:
  ```kotlin
  interface ThreePhasePattern {
      val name: String
      val alpha: Float          // результат последнего update, без амплитуды
      val beta: Float
      fun reset()               // состояние как у нового экземпляра
      fun update(dt: Double)    // dt — время паттерна (реальное · скорость пользователя)
  }
  object ThreePhasePatterns {
      const val FINGER = "Палец"
      const val DEFAULT = "Circle"
      val NAMES: List<String>   // FINGER, затем 16 имён по алфавиту (String.compareTo)
  }
  class ThreePhasePatternSet {
      val finger: FingerPattern               // Task 3
      val all: List<ThreePhasePattern>        // порядок == ThreePhasePatterns.NAMES
      fun indexOf(name: String): Int          // неизвестное имя → индекс DEFAULT
  }
  ```
  В этой задаче `FingerPattern` — заглушка-класс в `FingerPattern.kt` с `name = FINGER` и
  нулевой позицией; поведение — в Task 3.

- [ ] **Step 1: Написать падающие тесты**

```kotlin
@Test fun `каждый паттерн совпадает с restim 600 шагов`()   // patterns.csv; набор = ThreePhasePatternSet(), update(1.0/60), допуск 1e-4
@Test fun `reset возвращает паттерн к началу`()              // для каждого: 100 шагов, reset, 1 шаг == значение шага 1 из patterns.csv
@Test fun `порядок набора совпадает с NAMES`()               // all.map { it.name } == NAMES; NAMES.size == 17; NAMES[0] == "Палец"
@Test fun `неизвестное имя даёт Circle`()                    // indexOf("Mouse") == NAMES.indexOf("Circle")
```

- [ ] **Step 2: Run** `:app:testDebugUnitTest --tests "com.example.generator2.features.threephase.ThreePhasePatternsTest"` — Expected: FAIL.

- [ ] **Step 3: Реализовать паттерны**

Буквальный порт формул из `H:/restim-master/restim-master/qt_ui/patterns/threephase/*.py`
вместе с внутренними `amplitude = 1.0` и `velocity` (у panning 0.5) из конструкторов restim.
Подводные камни из спеки, раздел «Паттерны»: Random Walk — притяжение
`pos *= (1 − 0.003)^(dt · 60)`; `np.clip` → `coerceIn`; Python `%` → `mod`.

- [ ] **Step 4: Run** тот же тест — Expected: PASS.

- [ ] **Step 5: Commit** `feat(3-фаза): 16 паттернов restim`

---

### Task 3: «Палец»

**Files:**
- Modify: `.../features/threephase/patterns/FingerPattern.kt`
- Test: `app/src/test/java/com/example/generator2/features/threephase/FingerPatternTest.kt`

**Interfaces:**
- Produces:
  ```kotlin
  class Trajectory(val time: DoubleArray, val alpha: FloatArray, val beta: FloatArray) // time[0] == 0
  class FingerPattern : ThreePhasePattern {
      val isTouching: Boolean
      fun touchDown(alpha: Float, beta: Float, timeSec: Double)  // UI-поток
      fun touchMove(alpha: Float, beta: Float, timeSec: Double)  // UI-поток
      fun touchUp()                                               // UI-поток
      fun clear()                                                 // UI-поток: стереть траекторию, точка остаётся
      fun trajectory(): Trajectory?                               // для рисования
  }
  ```
  Потоки — как в спеке, раздел «Палец»: живая позиция в `AtomicLong` (два `Float.toRawBits`),
  запись в `ArrayList` на UI-потоке, публикация `@Volatile Trajectory` в `touchUp`. `touchDown`
  сразу снимает старую траекторию. Входные координаты уже лежат в круге (Task 7).

- [ ] **Step 1: Написать падающие тесты**

```kotlin
@Test fun `пока палец на круге точка сглаженно идёт за ним`()
    // touchDown(0.5f, 0f, 0.0); update(0.02) → alpha ≈ 0.5·(1 − e⁻¹) ±1e-4; после 50 × update(0.02) alpha ≈ 0.5 ±1e-3
@Test fun `отпущенная траектория повторяется по кругу`()
    // down (0,0,t=0), move (1,0,t=1), move (0,1,t=2), up
    // update(0.5) → (0.5, 0); update(1.0) → (0.5, 0.5); update(1.0) → (0.5, 0) — зацикливание по длительности 2 с
@Test fun `меньше двух точек — точка стоит`()               // down(0.3,0.2,0), up, update(1.0) → около (0.3, 0.2)
@Test fun `нулевая длительность не делит на ноль`()        // down(0.3,0,0), move(0.6,0,0), up, update(1.0) → конечные числа, стоит
@Test fun `clear стирает траекторию`()                      // после повтора clear() → trajectory() == null, update не двигает точку
```

- [ ] **Step 2: Run** `--tests "...threephase.FingerPatternTest"` — Expected: FAIL.
- [ ] **Step 3: Реализовать** — сглаживание `k = 1 − exp(−dt / 0.02)` к живой позиции пальца; повтор
  `progress = (progress + dt).mod(duration)`, линейная интерполяция, после `touchUp` начинается с
  `progress = 0`. Если `duration ≤ 0` или точек меньше двух, точка тем же сглаживанием доходит до
  последней позиции пальца и стоит там.
- [ ] **Step 4: Run** — Expected: PASS.
- [ ] **Step 5: Commit** `feat(3-фаза): ручной паттерн «Палец»`

---

### Task 4: Движок контрольных точек и кроссфейд режимов

**Files:**
- Create: `.../features/threephase/ThreePhaseEngine.kt`, `.../features/threephase/ModeCrossfade.kt`
- Test: `app/src/test/java/com/example/generator2/features/threephase/ThreePhaseEngineTest.kt`, `ModeCrossfadeTest.kt`

**Interfaces:**
- Consumes: `ThreePhasePatternSet`, `ThreePhaseMath.channelMatrix` (Tasks 1–3).
- Produces:
  ```kotlin
  class ThreePhaseEngine(val patterns: ThreePhasePatternSet = ThreePhasePatternSet()) {
      companion object { const val STEP = 32; const val PATTERN_CROSSFADE_SEC = 0.3 }
      @Volatile var k: FloatArray                    // старт: channelMatrix(0f, 0f, 0f)
      @Volatile var displayAlpha: Float; @Volatile var displayBeta: Float
      val points: FloatArray                         // [a0, b0, a1, b1, …], переиспользуется, растёт
      /** Заполнить points, вернуть число точек n = ceil(frames / STEP) + 1. */
      fun fillPoints(frames: Int, sampleRate: Int, patternName: String, velocity: Float, amplitude: Float): Int
  }
  const val MODE_CROSSFADE_SEC = 0.02f
  /** Первые n сэмплов dst = old·(1 − t) + dst·t, t = (i + 1) / n. */
  fun crossfadeFrom(oldL: FloatArray, oldR: FloatArray, dstL: FloatArray, dstR: FloatArray, n: Int)
  ```
  Семантика `fillPoints`:
  - точка `j` соответствует сэмплу `min(j · STEP, frames)`;
  - точка 0 — последняя точка прошлого блока; в первом блоке — текущая позиция паттерна ×
    амплитуда (до первого `update` у паттерна `(0, 0)`); первый вызов принимает паттерн без
    перехода;
  - шаг: `dtReal = Δсэмплов / sampleRate`, паттерну даётся `dtReal · velocity`, а «Пальцу» при
    `isTouching` — `dtReal`;
  - позиция = паттерн × амплитуда, без нормализации (нормализует рендер);
  - смена имени → `reset()` нового паттерна, линейный переход за 0.3 с реального времени от
    последней выданной точки; смена во время перехода стартует от текущей смешанной точки;
  - поиск индекса только когда строка имени изменилась (сравнение ссылок, затем `equals`);
  - `displayAlpha/Beta` = последняя точка.

- [ ] **Step 1: Написать падающие тесты**

```kotlin
// ThreePhaseEngineTest
@Test fun `число точек и последняя на конце блока`()          // frames 4096 → 129; frames 100 → 5; frames 10 → 2
@Test fun `Circle проходит четверть оборота`()                 // amplitude 1, velocity 1, 48000 Гц, кадров на π/2 с суммарно: последняя точка ≈ (0, 1) ±1e-3
@Test fun `стык блоков непрерывен при любых размерах`()        // блоки 4096, 100, 10, 33: points[0..1] нового == последняя пара прошлого
@Test fun `амплитуда масштабирует позицию`()                   // amplitude 0.5 → |pos| ≈ 0.5 для Circle
@Test fun `смена паттерна без скачка`()                        // Circle → Figure 8: points[0..1] == прошлая точка; через 0.3 с позиция == Figure 8 × амплитуда ±1e-4
@Test fun `повторная смена во время перехода стартует с текущей точки`() // Circle → Figure 8 на 0.1 с → Spirograph: первая точка == последняя смешанная
@Test fun `палец под пальцем игнорирует скорость`()           // velocity 10, touchDown(0.5f,0f): через 0.02 с alpha ≈ 0.5·(1 − e⁻¹) ±1e-3
// ModeCrossfadeTest
@Test fun `кроссфейд линейный и заканчивается новым сигналом`() // old = 1, dst = 0, n = 4 → dst = [0.75, 0.5, 0.25, 0]; сэмплы после n не тронуты
```

- [ ] **Step 2: Run** `--tests "...threephase.ThreePhaseEngineTest" --tests "...threephase.ModeCrossfadeTest"` — Expected: FAIL.
- [ ] **Step 3: Реализовать** `ThreePhaseEngine` и `crossfadeFrom`.
- [ ] **Step 4: Run** — Expected: PASS.
- [ ] **Step 5: Commit** `feat(3-фаза): движок контрольных точек и переходы`

---

### Task 5: Поля режима и пресеты

**Files:**
- Modify: `.../features/generator/Generator.kt` (`DataLiveData`)
- Modify: `.../features/presets/presetsReadFile.kt`, `presetsSaveFile.kt` (`presetsSnapshot`), `presetsToLiveData.kt`, `readme.md`
- Test: `app/src/test/java/com/example/generator2/features/presets/PresetsRoundTripTest.kt`, `PresetsReadFileTest.kt`

**Interfaces:**
- Produces: в `DataLiveData` — `tp_EN: MutableStateFlow<Boolean>`, `tp_Pattern: MutableStateFlow<String>`,
  `tp_Velocity`, `tp_Amplitude`, `tp_Carrier_Fr`, `tp_Volume`, `tp_Calib_A`, `tp_Calib_B`,
  `tp_Calib_C`, `tp_Center: MutableStateFlow<Float>` с дефолтами из Global Constraints; ключи
  пресетов совпадают с именами полей. Комментарий `//PR PS PC` как у соседей.

- [ ] **Step 1: Дописать падающие тесты**

```kotlin
// PresetsRoundTripTest: новый тест
@Test fun `настройки 3-фазы переживают запись и чтение`()
    // tp_EN=true, tp_Pattern="Tri-Phase panning (ᴒ)", velocity 2.5, amplitude 0.75, carrier 900, volume 0.3, A -1.5, B -16.73, C -25.62, center 7.5 → всё равно после roundTrip
    // второй прогон с "Tri-Phase panning (∧)"
// PresetsReadFileTest: новые тесты
@Test fun `старый пресет без tp ключей открывается с дефолтами`()   // файл только с ch1_EN → tp_EN false, tp_Pattern "Circle", tp_Volume 0.1, tp_Center 13
@Test fun `неизвестный паттерн в пресете становится Circle`()       // tp_Pattern=Mouse → "Circle"
```

- [ ] **Step 2: Run** `--tests "com.example.generator2.features.presets.*"` — Expected: FAIL.
- [ ] **Step 3: Реализовать** — поля, запись, чтение (имя вне `ThreePhasePatterns.NAMES` → `DEFAULT`), перенос в `presetsToLiveData`, 10 строк в таблицу `readme.md`.
- [ ] **Step 4: Run** — Expected: PASS. (`KvFile` пишет через Java-сериализацию, Unicode в именах сохраняется; тест это закрепляет.)
- [ ] **Step 5: Commit** `feat(3-фаза): поля режима и пресеты`

---

### Task 6: Нативный рендер и включение в генератор

**Files:**
- Create: `app/src/main/cpp/generator/threephase.h`, `threephase.cpp`
- Create: `.../features/threephase/RenderThreePhase.kt`
- Modify: `app/src/main/cpp/CMakeLists.txt` (после `generator/renderchannel.cpp`)
- Modify: `.../features/generator/Generator.kt`, `GeneratorObserver.kt`, `.../features/audio/AudioMixerPump.kt:275-281`

**Interfaces:**
- Consumes: `ThreePhaseEngine.fillPoints/points/k` (Task 4), `crossfadeFrom` (Task 4), поля `tp_*` (Task 5), `ThreePhaseMath.channelMatrix` (Task 1).
- Produces:
  ```kotlin
  object RenderThreePhase {
      init { System.loadLibrary("plasma") }
      external fun jniRender(
          numFrames: Int, sampleRate: Int,
          points: FloatArray, pointCount: Int, step: Int,
          k: FloatArray, carrierHz: Float, volume: Float, centerReductionPct: Float,
          maxVolume0: Float, maxVolume1: Float,
          outL: FloatArray, outR: FloatArray,
      )
  }
  // Generator
  val threePhase: ThreePhaseEngine
  ```
  JNI-символ: `Java_com_example_generator2_features_threephase_RenderThreePhase_jniRender`.

- [ ] **Step 1: Нативный рендер**

Формулы — спека, раздел «На каждый сэмпл». Решения:
- состояние — `static uint32_t phase`, `static float prevVolume = 0`;
- шаг DDS `(uint32_t)(4294967296.0 / sampleRate * carrierHz)`;
- синус — статическая таблица 1024 + 1 точка, индекс `phase >> 22`, доля `(phase & 0x3FFFFF) / 2^22`; `cos` — та же таблица по `phase + 0x40000000`;
- позиция сэмпла `i`: сегмент `j = i / step`, длина `min(step, numFrames − j·step)`, линейная интерполяция точек `j` и `j+1`;
- громкость — линейная рампа `prevVolume → volume` по блоку, затем `prevVolume = volume`;
- на входе `volume`, `maxVolume0/1` зажимаются в `[0, 1]`, `centerReductionPct` — в `[0, 20]`;
- результат в `outL/outR` через переиспользуемый статический буфер и `SetFloatArrayRegion`, без аллокаций на блок (буфер растёт только при увеличении `numFrames`).

- [ ] **Step 2: Генератор**

`renderAudio`: при `tp_EN` — `fillPoints(frames, sampleRate, tp_Pattern, tp_Velocity, tp_Amplitude)`, затем `jniRender(..., k = threePhase.k, ..., maxVolume0/1 = liveData.maxVolume0/1)` в `bufL/bufR`; иначе прежний путь CHL/CHR/моно. Если `tp_EN` отличается от прошлого блока — второй источник рендерится в отдельную пару переиспользуемых буферов и `crossfadeFrom(old, …, n = min(frames, MODE_CROSSFADE_SEC · sampleRate))`.

- [ ] **Step 3: Наблюдатель и микшер**

`GeneratorObserver.start()`: `combine(tp_Calib_A, tp_Calib_B, tp_Calib_C)` → `channelMatrix` → `gen.threePhase.k = …`. `AudioMixerPump`: инверсия только при `!gen.liveData.tp_EN.value`.

- [ ] **Step 4: Run** `:app:assembleDebug` — Expected: BUILD SUCCESSFUL. Затем `:app:testDebugUnitTest` — Expected: все тесты PASS (прежние не сломаны).

- [ ] **Step 5: Commit** `feat(3-фаза): нативный рендер и режим в генераторе`

---

### Task 7: UI — круг, карточка, кнопка

**Files:**
- Create: `.../features/threephase/ThreePhaseGeometry.kt`, `.../screens/mainscreen4/card/ThreePhaseCircle.kt`, `.../screens/mainscreen4/card/cardThreePhase.kt`
- Modify: `.../screens/mainscreen4/top/TopBar.kt`, `.../screens/mainscreen4/mainscreen4.kt:163-210`
- Test: `app/src/test/java/com/example/generator2/features/threephase/ThreePhaseGeometryTest.kt`

**Interfaces:**
- Consumes: `Generator.threePhase`, `FingerPattern` touch API (Task 3), поля `tp_*` (Task 5).
- Produces:
  ```kotlin
  object ThreePhaseGeometry {
      fun toScreen(alpha: Float, beta: Float, cx: Float, cy: Float, radius: Float): Pair<Float, Float> // x = cx − beta·R, y = cy − alpha·R
      fun fromScreen(x: Float, y: Float, cx: Float, cy: Float, radius: Float): Pair<Float, Float>     // обратное, прижато к единичному кругу
  }
  @Composable fun ThreePhaseCircle(engine: ThreePhaseEngine, fingerActive: Boolean, modifier: Modifier = Modifier)
  @Composable fun CardThreePhase(gen: Generator)
  ```

- [ ] **Step 1: Падающий тест геометрии**

```kotlin
@Test fun `Neutral сверху, L+ слева снизу, R+ справа снизу`()
    // cx=cy=100, R=100: toScreen(1,0) == (100, 0); toScreen(-0.5, 0.8660254) ≈ (13.4, 150); toScreen(-0.5, -0.8660254) ≈ (186.6, 150)
@Test fun `fromScreen обратен и прижимает к кругу`()
    // fromScreen(toScreen(0.3, -0.4)) ≈ (0.3, -0.4); fromScreen(400, 100) ≈ (0, -1)
```

- [ ] **Step 2: Run** `--tests "...threephase.ThreePhaseGeometryTest"` — FAIL; реализовать; Run — PASS.

- [ ] **Step 3: `ThreePhaseCircle`**

`Canvas`, квадрат по ширине: окружность, треугольник Neutral–L+–R+, подписи «Neutral», «L+», «R+»,
точка из `engine.displayAlpha/Beta`, перечитываемая в цикле `withFrameNanos`. Для «Пальца» —
ломаная `finger.trajectory()`. `pointerInput` подключается только при `fingerActive`:
`touchDown/Move/Up` с координатами из `fromScreen` и временем `uptimeMillis / 1000.0`.

- [ ] **Step 4: `CardThreePhase`**

Сверху вниз (спека, раздел «UI»): круг; выпадающий список `ThreePhasePatterns.NAMES` — при выборе
другого имени `finger.clear()`, затем `tp_Pattern`; далее `MainscreenTextBoxAndDropdownMenu` для
полей:
- скорость: items `0.1, 0.5, 1.0, 2.0, 5.0, 10.0`;
- амплитуда: `0.25, 0.5, 0.75, 1.0`;
- частота: `300, 500, 700, 1000, 1500, 2000`;
- громкость: `0.05, 0.1, 0.2, 0.5, 1.0`.

Диапазоны — из Global Constraints. Сворачиваемая «Калибровка»: A, B, C (items `-20, -10, -6, -3,
0`), Center (items `0, 5, 13, 20`) и кнопка «Сброс» (A = B = C = 0, Center = 13). Оформление —
как у `CardMaster`: `colorDarkBackground`, `Haptic.confirm()` на нажатия.

- [ ] **Step 5: Кнопка и главный экран**

`TopBarAudioSource`: четвёртый `Box` «3Φ», переключает `tp_EN`, текст `Color.Green`, когда
включено, иначе `Color.Gray`. `Mainsreen4`: при `tp_EN` вместо `MasterButton`, `CardCard(CHL)` и
`AnimatedContent` с CHR — `CardThreePhase(gen)`.

- [ ] **Step 6: Run** `:app:assembleDebug` и `:app:testDebugUnitTest` — Expected: BUILD SUCCESSFUL, все PASS.

- [ ] **Step 7: Commit** `feat(3-фаза): круг, карточка режима и кнопка 3Φ`

---

### Task 8: Проверка на устройстве

Ручная, выполняет пользователь (или агент с подключённым устройством через скилл `run`).

- [ ] **Step 1:** Установить debug-сборку, включить «3Φ», открыть осциллограф в режиме Лиссажу.
- [ ] **Step 2:** «Палец»: держать точку в Neutral, внизу, в L+, R+ и в центре — картинки:
  диагональ `/`, диагональ `\`, горизонталь, вертикаль, эллипс (таблица в спеке).
- [ ] **Step 3:** Без щелчков: включение и выключение «3Φ», смена паттерна, движение громкости и
  частоты, загрузка пресета.
- [ ] **Step 4:** Перебрать все 16 паттернов при скорости 1 — точка движется как в restim
  (сравнить с restim на ПК).
- [ ] **Step 5:** Сохранить пресет с 3-фазой, перезапустить приложение, загрузить — настройки на месте.
