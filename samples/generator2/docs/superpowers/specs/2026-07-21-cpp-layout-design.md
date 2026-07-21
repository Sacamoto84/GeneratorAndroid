# Раскладка нативного кода по папкам — Design

**Дата:** 2026-07-21
**Статус:** утверждён, готов к написанию плана

## Проблема

Каталог `app/src/main/cpp` содержит 25 файлов вперемешку: спектрограмма,
осциллограф, генератор и служебные буферы лежат рядом без разделения. Папка
`route/` собрала под одним именем части двух разных подсистем. Найти нужный
файл можно только перебором.

Часть содержимого мертва. `minidsp.c` и `minidsp.h` занимают 20 КБ, не входят в
сборку и никем не включаются. `CrossCorrelation.h` и
`CumulativeMovingAverage.h` тоже не включаются никем.

Имя `generator2.cpp` обманывает: файл содержит функции
`Java_com_example_generator2_features_scope_NativeLib_*`, то есть относится к
осциллографу, а не к генератору.

## Решение

Разложить файлы по папкам, зеркалящим пакеты Kotlin. Принадлежность каждого
файла определяется не вкусом, а именами его JNI-функций:

| JNI-имя | Пакет Kotlin | Папка |
|---|---|---|
| `features_scope_NativeCanvas_*` | `features.scope` | `scope/` |
| `features_scope_NativeLib_*` | `features.scope` | `scope/` |
| `features_scope_NativePhosphor_*` | `features.scope` | `scope/` |
| `features_scope_NativeFloatDirectBuffer_*` | `features.scope` | `scope/` |
| `features_generator_RenderChannel_*` | `features.generator` | `generator/` |
| `Spectrogram_*` | `Spectrogram` | `spectrogram/` |

Знаешь, где лежит Kotlin — знаешь, где лежит C++.

Внутри файлов не меняется ни одной строки. JNI-имена от расположения файла не
зависят, поэтому набор экспортируемых символов обязан остаться прежним.

### Целевая структура

```
app/src/main/cpp/
  CMakeLists.txt
  scope/
    canvas.cpp
    scope.h  scope.cpp
    nativeLib.cpp            (был generator2.cpp)
    FloatDirectBuffer.h  FloatDirectBuffer.cpp
    PhosphorGrid.h
    PhosphorJni.cpp
    FloatRingBuffer.h
  generator/
    renderchannel.h  renderchannel.cpp
  spectrogram/
    plasma.cpp
    jniFFT.cpp
    fft.h
    colormaps.h  colormaps.cpp
    waterfall.h  waterfall.cpp
    scale.h  scale.cpp
    auformat.h  auformat.cpp
    ScaleBuffer.h  ScaleBufferBase.h
    BufferAverage.h  BufferIO.h
    global.h  global.cpp
    FloatRingBufferFFT.h
  attic/
    minidsp.c  minidsp.h
    CrossCorrelation.h
    CumulativeMovingAverage.h
  audio/           без изменений
  fftw-3.3.9/      без изменений
```

Папка `route/` исчезает: её содержимое расходится по `scope/` и `spectrogram/`.

Папка `attic/` собирает код, который не участвует в сборке. Она остаётся вне
`CMakeLists.txt`, то есть ничего не компилирует — просто держит файлы на виду,
не смешивая их с рабочими.

### Переименование

`generator2.cpp` становится `nativeLib.cpp`. Файл всё равно переезжает, а имя
вводит в заблуждение: по нему кажется, что это генератор, тогда как внутри
буфер осциллографа.

Ничего, кроме `CMakeLists.txt`, на это имя не ссылается: JNI-функции получают
имена от сигнатуры, а не от файла.

## Почему включения не сломаются

`#include "x.h"` ищет файл сначала рядом с включающим, поэтому включения внутри
одной папки продолжат работать без правок. Проверено по всему дереву: границу
папки пересекает единственное включение — спектрограмма тянет `audio_common.h`
из `audio/`. Оно уже покрыто строкой `INCLUDE_DIRECTORIES(audio)` в
`CMakeLists.txt`.

Так вышло потому, что группы совпали с фактическими кластерами зависимостей:
`plasma.cpp` включает `fft.h`, `colormaps.h`, `waterfall.h`, `scale.h`,
`auformat.h`, `ScaleBufferBase.h`, `ScaleBuffer.h`, `BufferAverage.h` и
`global.h` — все они едут в `spectrogram/` вместе с ним.

## Границы работы

Логика не трогается. JNI не отделяется от алгоритмов, файлы не режутся, функции
не переименовываются. Это решение принято сознательно: разделение JNI и чистого
C++ дало бы переносимость в другой проект, но требует переписывания
`plasma.cpp`, где 17 JNI-функций перемешаны с логикой спектрограммы. Такая
работа заслуживает отдельной спеки.

Единственное исключение — переименование `generator2.cpp`, обоснованное выше.

## Проверка

Автотестов в проекте нет и хост-компилятора C++ на машине нет — это ограничение
принято владельцем проекта раньше и здесь не меняется.

1. `.\gradlew.bat :app:assembleDebug` собирается под все четыре ABI.
2. Список экспортируемых символов `libplasma.so` совпадает с тем, что был до
   перекладки. Снимается через `llvm-nm -D` до и после, сравнивается построчно.
   Это главная проверка: она доказывает, что перекладка ничего не сломала в
   привязке к Kotlin.
3. Осмотр на устройстве: осциллограф, спектрограмма, фигуры Лиссажу и генератор
   работают как прежде.

## Файлы

**Перемещаются** — 25 файлов по таблице структуры выше, через `git mv`, чтобы
`git log --follow` продолжал показывать историю каждого.

**Изменяется** — `app/src/main/cpp/CMakeLists.txt`: пути в списке исходников
получают префикс папки, `generator2.cpp` меняется на `scope/nativeLib.cpp`.

## Известные риски

Каталог `app/.cxx` кеширует прежние пути CMake. Если сборка после перекладки
заупрямится, каталог удаляется и пересоздаётся — данных в нём нет, только
промежуточные объектники.
