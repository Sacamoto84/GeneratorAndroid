# Раскладка нативного кода по папкам — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Разложить 33 файла из `app/src/main/cpp` по папкам, зеркалящим пакеты Kotlin, не меняя логики.

**Architecture:** Принадлежность файла определяют имена его JNI-функций: `features_scope_*` едет в `scope/`, `features_generator_*` в `generator/`, `Spectrogram_*` в `spectrogram/`. Код, не входящий в сборку, уезжает в `attic/`. Папка `route/` исчезает, её содержимое расходится между `scope/` и `spectrogram/`. Внутри файлов правятся только три строки включений — все три укорачиваются, потому что файлы съезжаются в одну папку.

**Tech Stack:** C++17, CMake 3.22.1, Android NDK 29.0.14206865, Gradle.

---

## Global Constraints

- Спецификация: `docs/superpowers/specs/2026-07-21-cpp-layout-design.md`.
- Перемещать только через `git mv`, иначе история файлов оборвётся и `git log --follow` перестанет работать.
- Логику не трогать. Единственные правки внутри файлов — три строки включений из Task 2 и переименование файла `generator2.cpp`.
- Автотестов в проекте нет и хост-компилятора C++ на машине нет — ограничение принято владельцем проекта раньше. Проверка: сборка плюс сверка экспортируемых символов.
- Работа делается одним коммитом: между перемещением файлов и правкой `CMakeLists.txt` проект не собирается, промежуточное состояние коммитить нельзя.

## File Structure

Итоговое дерево `app/src/main/cpp`:

- `scope/` — осциллограф: `canvas.cpp`, `scope.h`, `scope.cpp`, `nativeLib.cpp`, `FloatDirectBuffer.h`, `FloatDirectBuffer.cpp`, `PhosphorGrid.h`, `PhosphorJni.cpp`, `FloatRingBuffer.h`
- `generator/` — генератор: `renderchannel.h`, `renderchannel.cpp`
- `spectrogram/` — спектрограмма: `plasma.cpp`, `jniFFT.cpp`, `fft.h`, `colormaps.h`, `colormaps.cpp`, `waterfall.h`, `waterfall.cpp`, `scale.h`, `scale.cpp`, `auformat.h`, `auformat.cpp`, `ScaleBuffer.h`, `ScaleBufferBase.h`, `BufferAverage.h`, `BufferIO.h`, `global.h`, `global.cpp`, `FloatRingBufferFFT.h`
- `attic/` — вне сборки: `minidsp.c`, `minidsp.h`, `CrossCorrelation.h`, `CumulativeMovingAverage.h`
- `audio/`, `fftw-3.3.9/` — без изменений
- `CMakeLists.txt` — пути в списке исходников

---

### Task 1: Снять эталон символов и переложить файлы

**Files:**
- Create: `app/src/main/cpp/scope/`, `app/src/main/cpp/generator/`, `app/src/main/cpp/spectrogram/`, `app/src/main/cpp/attic/`
- Move: 33 файла согласно File Structure

- [ ] **Step 1: Снять эталонный список символов.**

  Делается до любых изменений — с ним будем сверяться в Task 4.

  ```powershell
  $so = Get-ChildItem app\build\intermediates\cxx -Recurse -Filter libplasma.so |
        Where-Object { $_.FullName -like "*arm64-v8a*" } |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
  $nm = "$env:LOCALAPPDATA\Android\Sdk\ndk\29.0.14206865\toolchains\llvm\prebuilt\windows-x86_64\bin\llvm-nm.exe"
  & $nm -D --defined-only $so.FullName |
        ForEach-Object { ($_ -split '\s+')[-1] } | Sort-Object |
        Set-Content symbols-before.txt -Encoding utf8
  (Get-Content symbols-before.txt | Measure-Object -Line).Lines
  ```

  Ожидается `1496`. Если библиотека не найдена, сначала выполнить
  `.\gradlew.bat :app:assembleDebug`, затем повторить.

  Файл `symbols-before.txt` создаётся в корне проекта и удаляется в Task 4 —
  коммитить его не нужно.

- [ ] **Step 2: Создать папки.**

  ```powershell
  New-Item -ItemType Directory -Force app\src\main\cpp\scope
  New-Item -ItemType Directory -Force app\src\main\cpp\generator
  New-Item -ItemType Directory -Force app\src\main\cpp\spectrogram
  New-Item -ItemType Directory -Force app\src\main\cpp\attic
  ```

- [ ] **Step 3: Переложить файлы осциллографа.**

  ```powershell
  cd app\src\main\cpp
  git mv canvas.cpp scope\canvas.cpp
  git mv scope.h scope\scope.h
  git mv scope.cpp scope\scope.cpp
  git mv generator2.cpp scope\nativeLib.cpp
  git mv route\FloatDirectBuffer.h scope\FloatDirectBuffer.h
  git mv route\FloatDirectBuffer.cpp scope\FloatDirectBuffer.cpp
  git mv route\PhosphorGrid.h scope\PhosphorGrid.h
  git mv route\PhosphorJni.cpp scope\PhosphorJni.cpp
  git mv route\FloatRingBuffer.h scope\FloatRingBuffer.h
  cd ..\..\..\..
  ```

  `generator2.cpp` переименовывается в `nativeLib.cpp`: внутри него функции
  `Java_com_example_generator2_features_scope_NativeLib_*`, то есть это буфер
  осциллографа, а прежнее имя наводило на генератор.

- [ ] **Step 4: Переложить файлы генератора.**

  ```powershell
  cd app\src\main\cpp
  git mv renderchannel.h generator\renderchannel.h
  git mv renderchannel.cpp generator\renderchannel.cpp
  cd ..\..\..\..
  ```

- [ ] **Step 5: Переложить файлы спектрограммы.**

  ```powershell
  cd app\src\main\cpp
  git mv plasma.cpp spectrogram\plasma.cpp
  git mv fft.h spectrogram\fft.h
  git mv colormaps.h spectrogram\colormaps.h
  git mv colormaps.cpp spectrogram\colormaps.cpp
  git mv waterfall.h spectrogram\waterfall.h
  git mv waterfall.cpp spectrogram\waterfall.cpp
  git mv scale.h spectrogram\scale.h
  git mv scale.cpp spectrogram\scale.cpp
  git mv auformat.h spectrogram\auformat.h
  git mv auformat.cpp spectrogram\auformat.cpp
  git mv ScaleBuffer.h spectrogram\ScaleBuffer.h
  git mv ScaleBufferBase.h spectrogram\ScaleBufferBase.h
  git mv BufferAverage.h spectrogram\BufferAverage.h
  git mv BufferIO.h spectrogram\BufferIO.h
  git mv route\jniFFT.cpp spectrogram\jniFFT.cpp
  git mv route\global.h spectrogram\global.h
  git mv route\global.cpp spectrogram\global.cpp
  git mv route\FloatRingBufferFFT.h spectrogram\FloatRingBufferFFT.h
  cd ..\..\..\..
  ```

- [ ] **Step 6: Переложить код вне сборки.**

  ```powershell
  cd app\src\main\cpp
  git mv minidsp.c attic\minidsp.c
  git mv minidsp.h attic\minidsp.h
  git mv CrossCorrelation.h attic\CrossCorrelation.h
  git mv CumulativeMovingAverage.h attic\CumulativeMovingAverage.h
  cd ..\..\..\..
  ```

- [ ] **Step 7: Убедиться, что папка `route` опустела и исчезла.**

  ```powershell
  Test-Path app\src\main\cpp\route
  Get-ChildItem app\src\main\cpp -File -Name
  ```

  Ожидается `False` и единственный оставшийся файл в корне — `CMakeLists.txt`.
  Если `route` ещё существует, значит какой-то файл не переехал: посмотреть
  `Get-ChildItem app\src\main\cpp\route` и доложить, не удаляя вручную.

---

### Task 2: Поправить включения, пересекавшие границу `route`

**Files:**
- Modify: `app/src/main/cpp/scope/nativeLib.cpp`
- Modify: `app/src/main/cpp/spectrogram/plasma.cpp`
- Modify: `app/src/main/cpp/spectrogram/jniFFT.cpp`

- [ ] **Step 1: Поправить `nativeLib.cpp`.**

  Заменить строку

  ```cpp
  #include "route/FloatRingBuffer.h"
  ```

  на

  ```cpp
  #include "FloatRingBuffer.h"
  ```

  Оба файла теперь лежат в `scope/`, путь стал лишним.

- [ ] **Step 2: Поправить `plasma.cpp`.**

  Заменить строку

  ```cpp
  #include "route/global.h"
  ```

  на

  ```cpp
  #include "global.h"
  ```

- [ ] **Step 3: Поправить `jniFFT.cpp`.**

  Заменить восемь строк

  ```cpp
  #include "../fft.h"
  #include "../colormaps.h"
  #include "../waterfall.h"
  #include "../scale.h"
  #include "../auformat.h"
  #include "../ScaleBufferBase.h"
  #include "../ScaleBuffer.h"
  #include "../BufferAverage.h"
  ```

  на

  ```cpp
  #include "fft.h"
  #include "colormaps.h"
  #include "waterfall.h"
  #include "scale.h"
  #include "auformat.h"
  #include "ScaleBufferBase.h"
  #include "ScaleBuffer.h"
  #include "BufferAverage.h"
  ```

  Порядок строк в файле сохранить как есть; менять только сами пути.

- [ ] **Step 4: Убедиться, что путей через границу больше не осталось.**

  ```powershell
  Select-String -Path app\src\main\cpp\scope\*.cpp,app\src\main\cpp\scope\*.h,app\src\main\cpp\spectrogram\*.cpp,app\src\main\cpp\spectrogram\*.h,app\src\main\cpp\generator\*.cpp,app\src\main\cpp\generator\*.h -Pattern '#include "(\.\.|route/)'
  ```

  Ожидается пустой вывод.

---

### Task 3: Обновить CMakeLists

**Files:**
- Modify: `app/src/main/cpp/CMakeLists.txt:15-38`

- [ ] **Step 1: Заменить список исходников и убрать мёртвые строки про `route`.**

  Заменить блок со строки `#add_subdirectory(route)` по закрывающую скобку
  `add_library` включительно, то есть строки 15-38, на:

  ```cmake
  add_library(${CMAKE_PROJECT_NAME} SHARED
          # List C/C++ source files with relative paths to this CMakeLists.txt.
          scope/canvas.cpp
          scope/scope.cpp
          scope/nativeLib.cpp
          scope/FloatDirectBuffer.cpp
          scope/PhosphorJni.cpp

          generator/renderchannel.cpp

          spectrogram/plasma.cpp
          spectrogram/jniFFT.cpp
          spectrogram/global.cpp
          spectrogram/colormaps.cpp
          spectrogram/scale.cpp
          spectrogram/waterfall.cpp
          spectrogram/auformat.cpp
  )
  ```

  Закомментированные строки `#add_subdirectory(route)` и
  `#INCLUDE_DIRECTORIES(route)` удаляются: папки больше нет.

  Строки `add_subdirectory(audio)`, `INCLUDE_DIRECTORIES(audio)`,
  `add_subdirectory(fftw-3.3.9)`, `INCLUDE_DIRECTORIES(fftw-3.3.9/api)`,
  `target_link_libraries` и блок с флагами компиляции не трогать.

  Папка `attic/` в сборку не добавляется — в этом её смысл.

- [ ] **Step 2: Проверить состав файла.**

  ```powershell
  Get-Content app\src\main\cpp\CMakeLists.txt
  ```

  Убедиться, что в списке исходников ровно 13 файлов, все с префиксом папки, и
  что упоминаний `route` в файле не осталось.

---

### Task 4: Собрать, сверить символы, закоммитить

**Files:** нет новых изменений, только проверка

- [ ] **Step 1: Собрать.**

  ```powershell
  .\gradlew.bat :app:assembleDebug
  ```

  Ожидается `BUILD SUCCESSFUL`.

  Если CMake жалуется на отсутствующий файл по старому пути — это кеш в
  `app/.cxx`. Удалить каталог и собрать заново:

  ```powershell
  Remove-Item -Recurse -Force app\.cxx
  .\gradlew.bat :app:assembleDebug
  ```

- [ ] **Step 2: Снять список символов после перекладки.**

  ```powershell
  $so = Get-ChildItem app\build\intermediates\cxx -Recurse -Filter libplasma.so |
        Where-Object { $_.FullName -like "*arm64-v8a*" } |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
  $nm = "$env:LOCALAPPDATA\Android\Sdk\ndk\29.0.14206865\toolchains\llvm\prebuilt\windows-x86_64\bin\llvm-nm.exe"
  & $nm -D --defined-only $so.FullName |
        ForEach-Object { ($_ -split '\s+')[-1] } | Sort-Object |
        Set-Content symbols-after.txt -Encoding utf8
  ```

- [ ] **Step 3: Сверить списки.**

  Это главная проверка работы: она доказывает, что перекладка не порвала связь
  с Kotlin.

  ```powershell
  Compare-Object (Get-Content symbols-before.txt) (Get-Content symbols-after.txt)
  ```

  Ожидается пустой вывод — списки совпадают полностью.

  Если вывод не пуст, перекладка что-то сломала. Строки со стрелкой `<=`
  показывают пропавшие символы, `=>` — появившиеся. Не коммитить, доложить
  расхождение целиком.

- [ ] **Step 4: Убрать временные файлы.**

  ```powershell
  Remove-Item symbols-before.txt, symbols-after.txt
  ```

- [ ] **Step 5: Проверить, что в индекс попали именно перемещения.**

  ```powershell
  git status --short app\src\main\cpp
  ```

  Ожидается, что файлы показаны как `R` (renamed), а не парами `D` и `??`.
  Если видны `D` и `??`, значит какой-то файл переехал не через `git mv` —
  историю удастся сохранить, добавив его через `git add -A`, но лучше доложить.

- [ ] **Step 6: Закоммитить.**

  ```powershell
  git add -A -- app/src/main/cpp
  git commit -F- <<'EOF'
  refactor: разложить нативный код по папкам

  В app/src/main/cpp лежали вперемешку 33 файла четырёх разных подсистем,
  а папка route собрала под одним именем части двух из них.

  Раскладываем по папкам, зеркалящим пакеты Kotlin. Принадлежность файла
  определяют имена его JNI-функций, а не вкус: features_scope_* уходит в
  scope, features_generator_* в generator, Spectrogram_* в spectrogram.
  Знаешь, где лежит Kotlin — знаешь, где лежит C++.

  Код, не входящий в сборку, уезжает в attic: minidsp на 20 КБ, а также
  CrossCorrelation и CumulativeMovingAverage, которые никто не включает.
  Папка route исчезает, её содержимое расходится по scope и spectrogram.

  generator2.cpp переименован в nativeLib.cpp: внутри него функции
  NativeLib осциллографа, а прежнее имя наводило на генератор.

  Логика не тронута. Правки внутри файлов — только три строки включений,
  и все три стали короче: файлы съехались в одну папку. Набор
  экспортируемых символов libplasma.so сверен до и после, совпадает
  построчно, все 1496.

  Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
  EOF
  ```

  Если оболочка не принимает heredoc, положить текст сообщения во временный
  файл и передать его через `git commit -F <путь>`.

---

### Task 5: Проверка на устройстве

**Files:** нет изменений

- [ ] **Step 1: Установить.**

  ```powershell
  .\gradlew.bat :app:installDebug
  ```

  Ожидается `BUILD SUCCESSFUL`.

- [ ] **Step 2: Проверить, что подсистемы живы.**

  Перекладка не должна была ничего изменить в поведении. Проверяется, что
  каждая нативная подсистема по-прежнему отвечает:

  1. Осциллограф рисует сигнал, развёртка переключается.
  2. Спектрограмма строится и обновляется.
  3. Фигуры Лиссажу рисуются.
  4. Генератор выдаёт сигнал.

  Любое падение с `UnsatisfiedLinkError` означало бы, что символ потерялся, —
  но сверка в Task 4 это уже исключила, так что осмотр здесь подтверждающий.

---

## Plan Self-Review

**Покрытие спецификации.** Целевая структура из спеки разложена по Task 1,
шаги 2-6. Таблица правок включений — Task 2. Изменения `CMakeLists.txt`,
включая удаление строк про `route` и невключение `attic` в сборку, — Task 3.
Переименование `generator2.cpp` — Task 1, шаг 3, с обоснованием. Требование
`git mv` — во всех шагах перемещения плюс проверка в Task 4, шаг 5. Проверка
сборки и сверка символов — Task 4, шаги 1-3. Осмотр на устройстве — Task 5.
Риск с кешем `app/.cxx` — Task 4, шаг 1.

**Плейсхолдеры.** Каждый шаг содержит готовую команду или полный текст правки.
Отложенных решений нет.

**Согласованность имён.** Пути `scope/`, `generator/`, `spectrogram/`, `attic/`
и имя `nativeLib.cpp` совпадают между File Structure, командами перемещения,
списком в `CMakeLists.txt` и проверками. Список исходников в Task 3 содержит
ровно те `.cpp`, что перемещены в Task 1 и компилировались до перекладки:
тринадцать файлов, столько же было и раньше.

**Отклонение от спецификации.** Спека сначала утверждала, что внутри файлов не
меняется ни строки. Проверка включений это опровергла — три файла ссылались
через границу `route/`. Спека исправлена до написания плана, здесь расхождения
нет.
