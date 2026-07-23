# Мастер-громкость — дизайн

Дата: 2026-07-23

## 1. Цель

Добавить в генератор ступень «мастер-громкость» — медленную амплитудную огибающую на
каждый канал. Функционально повторяет AM-модуляцию, но с большими временами (секунды, не
герцы) и тремя режимами работы.

Три режима на канал:

1. **Плавный** — модуляция формой с периодом 0.1..100 с (выбор формы из библиотеки).
2. **Вкл/Выкл** — гейт: заданное время звук есть, заданное время звука нет.
3. **Кнопка** — momentary: держишь кнопку — звук идёт, отпускаешь — тишины. Кнопка
   **общая** на оба канала (глобальный оверрайд), см. §5.

## 2. Решения (зафиксированы при брейнсторме)

| Вопрос | Решение |
|---|---|
| Область / место в цепи | Поканально, множитель на выходе рендера **после** volume/AM/FM. Кнопка — общая на оба канала. |
| Отношение к `volume0/volume1` | Отдельный каскад. Существующую громкость не трогаем. |
| Анти-щелчок | Да, линейный фейд ~5 мс на каждом переходе 0↔звук. |
| Плавный режим | Период 0.1..100 с, полный размах 0..1 (без ручки глубины). Форма из библиотеки. |
| Режим Кнопка | Глобальный оверрайд: пока хотя бы один включённый канал в режиме Кнопка — физическая кнопка гейтит **оба** канала, перекрывая их Плавный/Вкл-Выкл. |
| Вкл/Выкл | Два независимых времени `TOn` и `TOff`, каждое 0.1..100 с, старт с ON. |
| UI | Новая карточка на канал (как `CardAM`/`CardFM`) + общая кнопка на главном экране. |

## 3. Архитектура

Ступень мастер-громкости реализуется в **нативе** (`app/src/main/cpp/generator/`),
потому что там уже живут буферы форм (`buffer_*`) и пофреймовое состояние
(`phase_accumulator_*`) в `StructureCh`. Каждый канал имеет свой набор.

Сигнальная цепь на канал (в `jniRenderChannel`):

```
существующие 4 ветки (CR / CR+AM / CR+FM / CR+AM+FM) заполняют tempArrayElements[i]
        │
        ▼
доп. пост-проход:  tempArrayElements[i] *= masterGain(i)
```

`masterGain(i)` считается пофреймово с сохранением состояния между вызовами (буферы по
1024 сэмпла, рендер идёт чанками `numFrames/2`).

## 4. Модель данных

### 4.1 `DataLiveData` (`Generator.kt`) — новые поля, `//PR PS PC` (в пресетах)

На каждый канал (`ch1_*` и `ch2_*`):

| Поле | Тип | Дефолт | Назначение |
|---|---|---|---|
| `ch1_Master_EN` | `MutableStateFlow<Boolean>` | `false` | вкл мастер-громкости канала |
| `ch1_Master_Mode` | `MutableStateFlow<Int>` | `1` | 1=Плавный, 2=Вкл/Выкл, 3=Кнопка |
| `ch1_Master_Period` | `MutableStateFlow<Float>` | `2f` | сек 0.1..100 (Плавный) |
| `ch1_Master_Filename` | `MutableStateFlow<String>` | `"09_Ramp"` | форма (Плавный) |
| `ch1_Master_TOn` | `MutableStateFlow<Float>` | `1f` | сек 0.1..100 (Вкл/Выкл) |
| `ch1_Master_TOff` | `MutableStateFlow<Float>` | `1f` | сек 0.1..100 (Вкл/Выкл) |

Аналогично `ch2_*`.

### 4.2 Runtime-поле (НЕ в пресетах)

| Поле | Тип | Дефолт | Назначение |
|---|---|---|---|
| `masterButton` | `MutableStateFlow<Boolean>` | `false` | состояние общей физ. кнопки (momentary) |

### 4.3 `StructureCh` — Kotlin-зеркало (`Generator.kt:239`) и C++ (`renderchannel.h`)

Добавить:

```cpp
float    buffer_master[1024] = {0.0f};   // форма Плавного, 0..1
uint32_t phase_accumulator_master = 0;   // DDS Плавного
float    master_current_gain = 1.0f;     // текущий гейн для фейда
uint32_t master_onoff_counter = 0;       // счётчик сэмплов Вкл/Выкл
bool     master_onoff_on = true;         // текущая фаза Вкл/Выкл (старт ON)
```

Kotlin-`StructureCh` — только `var buffer_master: FloatArray = FloatArray(1024)` (нужно
для загрузки формы через `sendBuffer`; фазы/гейн живут только в нативе).

## 5. Логика `masterGain` (пофреймово, на канал)

Вход на канал (передаётся в JNI из `RenderChannel.renderChanel`, как сейчас
`volume`/`amDepth`):

- `masterEN: Boolean`
- `masterMode: Int` (1/2/3)
- `rMaster: Int` — DDS-инкремент Плавного, `rMaster = convertHzToR(1f / period, sampleRate)`
- `onSamples: Int = TOn * sampleRate`, `offSamples: Int = TOff * sampleRate`
- `buttonActive: Boolean` — глобальный оверрайд активен (см. ниже)
- `buttonPressed: Boolean` — `masterButton`

`buttonActive` считается в Kotlin один раз на рендер и одинаков для обоих каналов:

```
buttonActive = (ch1_Master_EN && ch1_Master_Mode == 3) ||
               (ch2_Master_EN && ch2_Master_Mode == 3)
```

Расчёт `target` на сэмпл:

```
if (buttonActive)                 target = buttonPressed ? 1f : 0f   // оверрайд ОБОИХ каналов
else if (!masterEN)               target = 1f
else switch (masterMode):
    1 (Плавный):  target = buffer_master[phase_accumulator_master >> 22]
                  phase_accumulator_master += rMaster
    2 (Вкл/Выкл): target = master_onoff_on ? 1f : 0f
                  if (++master_onoff_counter >= (master_onoff_on ? onSamples : offSamples)) {
                      master_onoff_on = !master_onoff_on
                      master_onoff_counter = 0
                  }
    3 (Кнопка):   target = buttonPressed ? 1f : 0f   // недостижимо: mode==3 ⇒ buttonActive
```

Фейд (анти-щелчок), единый путь для всех режимов:

```
step = 1f / (0.005f * sampleRate)          // ~5 мс полный ход 0↔1
delta = target - master_current_gain
master_current_gain += clamp(delta, -step, +step)
gain = master_current_gain
```

Плавный не искажается: его `target` меняется медленно (период ≥0.1 с ≫ 5 мс), слю-лимит
не срабатывает.

## 6. JNI

`jniRenderChannel` / `external fun jniRenderChannel` — добавить параметры из §5
(`masterEN, masterMode, rMaster, onSamples, offSamples, buttonActive, buttonPressed`).

`sendBuffer(ch, modulation, data)` — `case 3: destination = buffer_master`.

## 7. Загрузка формы Плавного

`Spinner_Send_Buffer.kt`:

- `enum GeneratorMOD { CR, AM, FM, MASTER }`
- ветка `MASTER`: `gen.chX.buffer_master = byteToFloatArrayLittleEndianMap(buf, 0f,4095f, 0f,1f)`
  (диапазон 0..1, как AM), затем `RenderChannel().sendBuffer(ch, 3, gen.chX.buffer_master)`.
- источник форм — существующий `itemlistAM` (та же библиотека модуляций).

## 8. UI

### 8.1 `CardMaster(str: String, gen: Generator)`

Новый файл `screens/mainscreen4/card/cardMaster.kt`, паттерн `cardAM.kt`. Добавить в
`CardCard` (`cardCard.kt`) после `CardFM`.

Состав:

- кнопка **EN** (как в `CardAM`), пишет `chX_Master_EN`
- дропдаун **режима** (`MainscreenTextBoxAndDropdownMenu` или существующий комбобокс):
  Плавный / Вкл-Выкл / Кнопка → `chX_Master_Mode`
- параметры по режиму:
  - Плавный: поле **Period** (сек, 0.1..100) + `UIspinner` формы (`mod=MASTER`)
  - Вкл/Выкл: два поля **TOn**, **TOff** (сек, 0.1..100)
  - Кнопка: параметров нет (гейт от общей кнопки)

### 8.2 Общая кнопка

Крупный momentary-виджет на `mainscreen4.kt` (видим всегда):
`onPress → masterButton.value = true`, `onRelease → masterButton.value = false`
(через `pointerInput`/`detectTapGestures` c `tryAwaitRelease`). Haptic по нажатию.

## 9. Персистентность

Новые `master_*` поля (§4.1) добавить в:

1. `Generator.kt` — объявление в `DataLiveData` (`//PR PS PC`)
2. `presetsReadFile.kt` — `data.X.value = satchel.getOrDefault("X", default)`
3. `presetsSaveFile.kt` — `satchel["X"] = gen.liveData.X.value`
4. `presetsToLiveData.kt` — `gen.liveData.X.value = data.X.value`

`masterButton` — runtime, не сохраняется.

## 10. Крайние случаи

- **Период → Hz:** `f = 1/Period` = 0.01..10 Гц. `rMaster = (2^32/sr)*f`, при 0.01 Гц ≈ 894
  на сэмпл — ненулевой, ок.
- **Оверрайд кнопки** замораживает Плавный/Вкл-Выкл обоих каналов, пока активен (их фазы
  можно не двигать — не важно для MVP).
- **Mono** (`liveData.mono`): рендерится только `ch1`, дублируется в L/R. Мастер `ch1`
  применяется; `buttonActive` учитывает `ch1`.
- **Канал выключен** (`chX_EN=false`): рендер не вызывается (zeroBuffer), мастер не важен.

## 11. Тесты

Юнит (Kotlin, `app/src/test/...`):

- `convertHzToR(1/period)` для period ∈ {0.1, 2, 100} → `rMaster > 0`.
- период Вкл/Выкл: `onSamples + offSamples == round((TOn+TOff)*sr)`.
- шаг фейда: `step` соответствует 5 мс (`0.005*sr` сэмплов на полный ход).
- `buttonActive` истина ⟺ хотя бы один включённый канал с `Mode==3`.

Натив (`renderchannel.cpp`) — ручная проверка: отсутствие щелчков на переходах, корректный
период Плавного и Вкл/Выкл на слух/осциллографе.

## 12. Затрагиваемые файлы

- `app/src/main/cpp/generator/renderchannel.h` — поля `StructureCh`
- `app/src/main/cpp/generator/renderchannel.cpp` — JNI-сигнатура, пост-проход, `sendBuffer case 3`
- `app/src/main/java/.../features/generator/RenderChannel.kt` — `external fun` + чтение `liveData` + вызов
- `app/src/main/java/.../features/generator/Generator.kt` — `DataLiveData` поля, `StructureCh.buffer_master`, `buttonActive`
- `app/src/main/java/.../features/generator/Spinner_Send_Buffer.kt` — `GeneratorMOD.MASTER`
- `app/src/main/java/.../screens/mainscreen4/card/cardMaster.kt` — новая карточка
- `app/src/main/java/.../screens/mainscreen4/card/cardCard.kt` — подключить карточку
- `app/src/main/java/.../screens/mainscreen4/mainscreen4.kt` — общая кнопка
- `app/src/main/java/.../features/presets/{presetsReadFile,presetsSaveFile,presetsToLiveData}.kt` — персист
