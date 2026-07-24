# Метаморфоза несущей — дизайн

Дата: 2026-07-24

## 1. Цель

Сейчас форма несущей выбирается из библиотеки один раз и держится постоянно. Добавить режим
**метаморфозы**: до трёх форм несущей на канал, которые сменяют друг друга по кругу во время
генерации.

Два способа смены:

1. **Ступень** — по истечении времени `T` форма резко меняется на следующую.
2. **Плавно** — форма непрерывно перетекает в следующую, полный переход занимает `T`.

## 2. Решения (зафиксированы при брейнсторме)

| Вопрос | Решение |
|---|---|
| Цикл форм | По кругу `A→B→C→A`, бесконечно. |
| Смысл `T` | Одно время на весь режим, `T` = длительность **одного шага**. Полный круг = `N·T`. |
| Анти-щелчок в Ступени | Время вышло — ждём ближайшего оборота фазы несущей и только тогда меняем форму. Задержка ≤ 1 период несущей. |
| Место в UI | Новая карточка `CardMorph` между `CardCarrier` и `CardAM`. |
| Связь со старой несущей | 3 независимых слота. При `Morph_EN` спиннер в `CardCarrier` гаснет, старая форма сохраняется и возвращается при выключении. |
| Число форм | Галочка вкл/выкл на каждый из 3 слотов. Активные слоты идут по кругу в порядке номеров. |
| Где считать | Посэмплово в нативе, через пару указателей и коэффициент смешивания. |
| Математика смешивания | Линейный лерп (не equal-power). |

## 3. Архитектура

Всё в нативе (`app/src/main/cpp/generator/`), как мастер-громкость: там уже живут буферы форм
и пофреймовое состояние в `StructureCh`. Каждый канал — свой набор.

### 3.1 Состояние в `StructureCh` (`renderchannel.h`)

```cpp
float    buffer_morph[3][1024] = {0.0f};  // 3 слота форм несущей, -1..1
uint8_t  morph_slot    = 0;               // текущий активный слот (0..2)
uint32_t morph_counter = 0;               // сэмплов пройдено в текущем шаге
```

Kotlin-`StructureCh` — только `var buffer_morph: Array<FloatArray> = Array(3) { FloatArray(1024) }`
(нужно для загрузки форм через `sendBuffer`; слот и счётчик живут только в нативе).

### 3.2 Ядро: пара указателей + `t`

Обе формы читаются по одному и тому же фазовому индексу `phase_accumulator_carrier >> 22`,
поэтому смешивание на выходе тождественно равно смешиванию таблиц. Перед циклом рендера
считаем `pA`, `pB`, `inv_steps`; внутри цикла в каждой из 4 веток вместо
`buffer_carrier[idx]` идёт:

```cpp
float t = t_of(morph_counter);                 // 0 в Ступени, counter*inv_steps в Плавном
float c = pA[idx] + t * (pB[idx] - pA[idx]);
```

`t` пересчитывается каждый сэмпл (§4.1). `pA`/`pB` пересчитываются только в момент смены
слота — то есть раз в `T` секунд, а не каждый сэмпл.

Единый путь вычисления сэмпла, ветвлений в горячей формуле нет. Вырожденные случаи
покрываются данными:

| Ситуация | `pA` | `pB` | `t` |
|---|---|---|---|
| Метаморфоза выключена | `buffer_carrier` | `buffer_carrier` | 0 |
| Активных слотов 0 | `buffer_carrier` | `buffer_carrier` | 0 |
| Активен 1 слот | этот слот | этот же слот | 0 |
| Режим Ступень | текущий слот | текущий слот | 0 |
| Режим Плавно, ≥2 слота | текущий | следующий активный | 0..1 |

Цена при выключенной метаморфозе: +1 загрузка и +2 флопа на сэмпл.

## 4. Математика

### 4.1 Коэффициент смешивания

```
steps     = max(1, round(T * sampleRate))
inv_steps = 1.0f / steps
t         = min(1.0f, morph_counter * inv_steps)      // Плавно
t         = 0.0f                                       // Ступень
```

`t` **выводится из счётчика**, а не накапливается через `t += dt`. Причина: если пользователь
крутит `T` во время генерации, `steps` меняется и накопленный `t` рассинхронизируется со
счётчиком (уедет за 1). Вывод из счётчика самокорректируется и не копит ошибку округления.
Цена та же — один `mul` на сэмпл.

### 4.2 Почему линейный лерп, а не equal-power

Линейный лерп по общему фазовому индексу — это буквально морфинг геометрии волны: синус
линейно превращается в пилу, промежуточная форма есть среднее двух. Equal-power (sin/cos)
кроссфейд дал бы на середине по 0.707 от каждой формы — для коррелированных форм это перелёт
по амплитуде и искажение геометрии.

Побочный эффект линейного: если две формы противофазны (синус и инвертированный синус), при
`t=0.5` получится тишина. Нормализацию не делаем — формы в библиотеке коррелированы.

### 4.3 Режим Плавно

```
t = min(1.0f, morph_counter * inv_steps);
c = pA[idx] + t*(pB[idx] - pA[idx]);
if (++morph_counter >= steps) {
    morph_counter = 0;
    morph_slot    = next(morph_slot, morph_mask);
    pA            = buffer_morph[morph_slot];                       // пересчёт указателей
    pB            = buffer_morph[next(morph_slot, morph_mask)];     // раз в T секунд
}
```

Разрыва на границе шага нет по построению: в конце шага k форма равна B при `t→1`, в начале
шага k+1 `pA` становится тем же B при `t=0`.

### 4.4 Режим Ступень

Ждём оборота фазы, чтобы не щёлкало:

```
prev    = phase_accumulator_carrier;
phase_accumulator_carrier += r;                        // или FM-инкремент
wrapped = (phase_accumulator_carrier < prev);          // прошли конец таблицы

if (morph_counter < steps)  morph_counter++;
else if (wrapped) {
    morph_counter = 0;
    morph_slot    = next(morph_slot, morph_mask);
    pA = pB       = buffer_morph[morph_slot];   // в Ступени pA == pB, t == 0
}
```

Задержка ≤ 1 период несущей: при 20 Гц — 50 мс, при 400 Гц — 2.5 мс, при 4 кГц — 0.25 мс.

### 4.5 `next()`

Следующий установленный бит маски по кругу от текущего слота. Если активен один слот —
возвращает его же. Если маска пустая — метаморфоза не активна и `next()` не вызывается.

## 5. Модель данных

### 5.1 `DataLiveData` (`Generator.kt`) — 9 полей на канал, `//PR PS PC`

| Поле | Тип | Дефолт | Назначение |
|---|---|---|---|
| `ch1_Morph_EN` | `MutableStateFlow<Boolean>` | `false` | вкл метаморфозы канала |
| `ch1_Morph_Mode` | `MutableStateFlow<Int>` | `1` | 0=Ступень, 1=Плавно |
| `ch1_Morph_Time` | `MutableStateFlow<Float>` | `2f` | сек, 0.1..100 — длительность шага |
| `ch1_Morph_Slot0_EN` | `MutableStateFlow<Boolean>` | `true` | слот 0 участвует в цикле |
| `ch1_Morph_Slot1_EN` | `MutableStateFlow<Boolean>` | `true` | слот 1 участвует |
| `ch1_Morph_Slot2_EN` | `MutableStateFlow<Boolean>` | `false` | слот 2 участвует |
| `ch1_Morph_Slot0_Filename` | `MutableStateFlow<String>` | `"Sine"` | форма слота 0 |
| `ch1_Morph_Slot1_Filename` | `MutableStateFlow<String>` | `"Square"` | форма слота 1 |
| `ch1_Morph_Slot2_Filename` | `MutableStateFlow<String>` | `"Ramp"` | форма слота 2 |

Зеркально `ch2_*`. Плоский стиль — как у существующих `master_*`.

Библиотека форм несущей (`app/src/main/assets/Carrier/`): `Sine`, `Square`, `Ramp`, `Dnramp`,
`HWave`, `HWave2`.

### 5.2 Kotlin-хелперы (`RenderChannel.kt`), чистые и тестируемые

```kotlin
fun morphSteps(time: Float, sampleRate: Int): Int      // max(1, round(time*sampleRate))
fun morphMask(s0: Boolean, s1: Boolean, s2: Boolean): Int  // биты 0/1/2
fun morphEffective(en: Boolean, mask: Int): Boolean    // en && mask != 0
```

## 6. JNI

`jniRenderChannel` / `external fun jniRenderChannel` — 4 новых параметра:

```
jboolean morph_en      // morphEffective(EN, mask)
jint     morph_mode    // 0=Ступень, 1=Плавно
jint     morph_steps   // morphSteps(T, sampleRate)
jint     morph_mask    // битовая маска активных слотов
```

Флага сброса нет: натив сам проверяет, что `morph_slot` установлен в `morph_mask`; если слот
погас — прыгает на первый активный. Самодостаточно.

`sendBuffer(ch, modulation, data)` — `case 4/5/6: destination = buffer_morph[modulation - 4]`.

## 7. Загрузка форм

`Spinner_Send_Buffer.kt`:

- `enum GeneratorMOD { CR, AM, FM, MASTER, MORPH0, MORPH1, MORPH2 }`
- ветки `MORPH0/1/2` берут список `gen.itemlistCarrier` и маппинг `0..4095 → -1..1` — как `CR`,
  затем `RenderChannel().sendBuffer(ch, 4 + slot, gen.chX.buffer_morph[slot])`

`observe.kt` — 6 новых коллекторов (2 канала × 3 слота) по образцу строк 13-20.

`UIspinner.kt` — `"MORPH0"/"MORPH1"/"MORPH2"` в оба `when`, список `gen.itemlistCarrier`.

## 8. UI

### 8.1 `CardMorph(str: String, gen: Generator)`

Новый файл `screens/mainscreen4/card/cardMorph.kt`, паттерн `cardMaster.kt`. Подключается в
`CardCard` (`cardCard.kt`) между `CardCarrier` и `CardAM`.

```
[EN] [Ступень|Плавно ▾] [T: 2.0 c]        ← строка 1
[✓][~~~Sine▾] [✓][⊓⊔Square▾] [ ][/Ramp▾]  ← строка 2: галочка + спиннер на слот
```

Галочка выключенного слота гасит его спиннер.

### 8.2 `CardCarrier`

При `chX_Morph_EN` спиннер формы несущей гаснет — тем же приёмом, что уже применён к полю
частоты при FM в режиме min/max (`carrierEnable` в `cardCarrier.kt:55`). Поле частоты несущей
метаморфоза не трогает.

## 9. Персистентность

18 новых полей (§5.1) добавить в четыре места по образцу `master_*`:

1. `Generator.kt` — объявление в `DataLiveData` (`//PR PS PC`)
2. `presetsSaveFile.kt` — `satchel["ch1_Morph_EN"] = gen.liveData.ch1_Morph_EN.value`
3. `presetsReadFile.kt` — `data.ch1_Morph_EN.value = satchel.getOrDefault("ch1_Morph_EN", false)`
4. `presetsToLiveData.kt` — `gen.liveData.ch1_Morph_EN.value = data.ch1_Morph_EN.value`

Старые пресеты не ломаются: ключей `Morph_*` в них нет, `getOrDefault` даёт `EN=false` —
поведение прежнее.

## 10. Крайние случаи

| Случай | Поведение |
|---|---|
| Активных слотов 0 | `morph_mask == 0` → `morphEffective` даёт `false`, играет обычный `buffer_carrier`. Тишины нет. |
| Активен 1 слот | `pA = pB` → постоянно эта форма. Спецветка не нужна. |
| Слот погас на ходу | Натив видит, что `morph_slot` не в маске → прыгает на первый активный **по обороту фазы** (то же правило, что в Ступени). Без щелчка. |
| `T` меняется на ходу | `steps` пересчитывается каждый вызов JNI; при `morph_counter > steps` шаг происходит немедленно, `t` ограничен единицей. |
| FM включена | Инкремент фазы переменный, детект оборота `new < prev` работает так же: инкремент — `uint32`, за сэмпл максимум один оборот. |
| Mono (`liveData.mono`) | Рендерится только `ch1` и дублируется в L/R — работает метаморфоза `ch1`. |
| Канал выключен | `!en_ch` → ранний `return`, метаморфоза не считается. |
| Имя формы в слоте не найдено | `Spinner_Send_Buffer` выходит, буфер слота остаётся нулевым → тишина на этом слоте. Ровно как сейчас у обычной несущей. |
| Противофазные формы | На `t=0.5` возможен провал громкости (§4.2). Не лечим. |

## 11. Тесты

Юнит (Kotlin, `app/src/test/...`) — на хелперы §5.2:

- `morphSteps(T, sr)` для `T ∈ {0.1, 2, 100}` → `== round(T*sr)`, всегда `≥ 1`
- `morphMask(s0,s1,s2)` → биты 0/1/2; все выключены → `0`
- `morphEffective(en, mask)` → `false` при `mask == 0` даже при `en == true`

Натив (`renderchannel.cpp`) — ручная проверка осциллографом и на слух:

- Плавно: форма непрерывно перетекает, разрыва на границе шага нет
- Ступень: смена формы без щелчка
- Период шага соответствует заданному `T`

## 12. Затрагиваемые файлы

- `app/src/main/cpp/generator/renderchannel.h` — поля `StructureCh`
- `app/src/main/cpp/generator/renderchannel.cpp` — JNI-сигнатура, `pA`/`pB`/`t` перед циклом, 4 ветки, `next()`, `sendBuffer case 4-6`
- `app/src/main/java/.../features/generator/RenderChannel.kt` — `external fun`, хелперы §5.2, чтение `liveData`, вызов
- `app/src/main/java/.../features/generator/Generator.kt` — 18 полей `DataLiveData`, `StructureCh.buffer_morph`
- `app/src/main/java/.../features/generator/Spinner_Send_Buffer.kt` — `GeneratorMOD.MORPH0/1/2`
- `app/src/main/java/.../features/generator/observe.kt` — 6 коллекторов
- `app/src/main/java/.../screens/mainscreen4/ui/UIspinner.kt` — `"MORPH0"/"MORPH1"/"MORPH2"`
- `app/src/main/java/.../screens/mainscreen4/card/cardMorph.kt` — новая карточка
- `app/src/main/java/.../screens/mainscreen4/card/cardCard.kt` — подключить карточку
- `app/src/main/java/.../screens/mainscreen4/card/cardCarrier.kt` — гасить спиннер несущей при `Morph_EN`
- `app/src/main/java/.../features/presets/{presetsSaveFile,presetsReadFile,presetsToLiveData}.kt` — персист
