# Oscilloscope Phosphor Accumulator Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Заменить точечный рендер осциллографа на фосфорный аккумулятор, который растеризует отрезки между соседними сэмплами в float-сетку с весом, обратным длине отрезка.

**Architecture:** Новый заголовочный класс `PhosphorGrid` на чистом C++ хранит сетку `columns × 512 × 2 float` в column-major порядке. Аудиопоток дописывает новые столбцы прямо из существующей JNI-функции `add()`; GL-поток забирает грязные столбцы и заливает их в текстуру `RG16F` через `glTexSubImage2D`. Рендерер рисует один полноэкранный quad, вертикальное распределение яркости целиком приходит из сетки.

**Tech Stack:** C++17 (NDK 26/29, CMake), Kotlin, Android OpenGL ES 3.0, GLSurfaceView, Jetpack Compose.

---

## Global Constraints

- Спецификация: `docs/superpowers/specs/2026-07-20-oscilloscope-phosphor-design.md`.
- Автоматических тестов нет — на машине разработки отсутствует хост-компилятор C++ (проверены `g++`, `clang++`, `cl`; NDK собирает только под Android). Верификация каждой задачи: `.\gradlew.bat :app:assembleDebug` плюс осмотр на устройстве в финальной задаче.
- Чётность индекса сэмпла сохраняет текущий смысл: **чётный индекс — канал 0**, цвет пурпурный, видимость `bools[2]`; **нечётный — канал 1**, цвет жёлтый, видимость `bools[1]`. Это поведение действующего шейдера, менять его нельзя.
- Фигуры Лиссажу не трогаем. Проверено: `Scope.kt:331` создаёт отдельный `MyGLRendererLissagu`, связи с осциллографом нет.
- `PhosphorGrid.h` не должен включать JNI- или GL-заголовки. Вся связь с платформой живёт в `PhosphorJni.cpp`.

## File Structure

- Создать `app/src/main/cpp/route/PhosphorGrid.h` — ядро: сетка, растеризация отрезков, кольцевой буфер, затухание. Без зависимостей от платформы.
- Создать `app/src/main/cpp/route/PhosphorJni.cpp` — глобальный экземпляр `PhosphorGrid`, привязка конфигурации к `AudioHistoryBuffer`, JNI-функции.
- Создать `app/src/main/java/com/example/generator2/features/scope/NativePhosphor.kt` — объявления `external fun`.
- Изменить `app/src/main/cpp/CMakeLists.txt` — добавить новый файл в сборку.
- Изменить `app/src/main/cpp/route/FloatDirectBuffer.cpp` — передавать входящий аудиопакет в аккумулятор.
- Изменить `app/src/main/java/com/example/generator2/features/scope/opengl/render/MyGLRendererOscill.kt` — полностью переписать под quad и текстуру.
- Изменить `app/src/main/java/com/example/generator2/features/scope/Scope.kt:242` — убрать вызов удалённого метода.

---

### Task 1: Ядро аккумулятора

**Files:**
- Create: `app/src/main/cpp/route/PhosphorGrid.h`

- [ ] **Step 1: Создать заголовок с состоянием и конфигурацией.**

  Создать `app/src/main/cpp/route/PhosphorGrid.h`:

  ```cpp
  //
  // Фосфорный аккумулятор осциллографа.
  //

  #ifndef GENERATOR2_PHOSPHORGRID_H
  #define GENERATOR2_PHOSPHORGRID_H

  #include <algorithm>
  #include <cmath>
  #include <cstddef>
  #include <mutex>
  #include <vector>

  class PhosphorGrid {
  public:
      static constexpr int kBins = 512;
      static constexpr int kMaxColumns = 4096;
      static constexpr int kMaxSteps = 1024;
      static constexpr float kDecay = 0.85f;

      // Столбец занимает kBins текселей по два float: канал 0 и канал 1.
      static constexpr std::size_t kColumnStride =
              static_cast<std::size_t>(kBins) * 2;

      /**
       * Задаёт геометрию и режим. Любое изменение параметров очищает сетку.
       * @param columns ширина в пикселях, 1..kMaxColumns.
       * @param layout 0 — каналы совмещены, 1 — каналы в своих половинах.
       * @param framesInWindow число стереокадров в окне истории.
       * @param rollMode true при развёртке 32 и выше.
       */
      void configure(int columns, int layout, std::size_t framesInWindow,
                     bool rollMode) {
          if (columns < 1) {
              columns = 1;
          }
          if (columns > kMaxColumns) {
              columns = kMaxColumns;
          }
          if (framesInWindow == 0) {
              return;
          }

          std::lock_guard<std::mutex> lock(mutex_);
          if (columns == columns_ && layout == layout_ &&
              framesInWindow == framesInWindow_ && rollMode == rollMode_) {
              return;
          }

          columns_ = columns;
          layout_ = layout;
          framesInWindow_ = framesInWindow;
          rollMode_ = rollMode;
          // В режиме roll columnsPerFrame_ всегда много меньше единицы:
          // при развёртке 32 окно составляет 36864 стереокадра против
          // максимум 4096 столбцов. Поэтому appendFrames() рисует строго
          // вертикальные отрезки внутри одного столбца, а advanceTo()
          // только очищает пропущенные.
          columnsPerFrame_ = static_cast<float>(columns_) /
                             static_cast<float>(framesInWindow_);
          cells_.assign(static_cast<std::size_t>(columns_) * kColumnStride, 0.0f);
          columnPosition_ = 0.0f;
          lastColumn_ = -1;
          dirtyStart_ = 0;
          dirtyCount_ = columns_;
          hasPrevious_ = false;
          ready_ = true;
      }

      bool isReady() const { return ready_; }
      int columns() const { return columns_; }
      bool isRollMode() const { return rollMode_; }
      const float *data() const { return cells_.data(); }

  private:
      // Разделение по потокам:
      //   аудиопоток        — appendFrames()
      //   поток GL          — configure(), rebuild(), takeDirtyRange(),
      //                       ringOffset(), isReady(), columns(),
      //                       isRollMode(), data()
      //
      // isReady(), columns() и isRollMode() читают поля, которые пишет только
      // configure(), то есть тот же поток GL — блокировка им не нужна.
      //
      // data() отдаёт указатель на cells_ для заливки в текстуру без
      // блокировки: аудиопоток в это время может дописывать ячейки. Гонка
      // допущена сознательно, худшее последствие — один кадр с неполностью
      // накопленным столбцом. Держать мьютекс на время glTexSubImage2D нельзя,
      // это застопорило бы аудиопоток. Указатель нельзя сохранять между
      // вызовами: configure() перевыделяет cells_.
      mutable std::mutex mutex_;
      std::vector<float> cells_;
      int columns_ = 0;
      int layout_ = 0;
      bool rollMode_ = true;
      bool ready_ = false;
      std::size_t framesInWindow_ = 0;
      float columnsPerFrame_ = 0.0f;
      float columnPosition_ = 0.0f;
      int lastColumn_ = -1;
      int dirtyStart_ = 0;
      int dirtyCount_ = 0;
      bool hasPrevious_ = false;
      float previous_[2] = {0.0f, 0.0f};
  };

  #endif // GENERATOR2_PHOSPHORGRID_H
  ```

- [ ] **Step 2: Добавить отображение сэмпла в бин и точечный вклад.**

  Вставить в секцию `private:` файла `PhosphorGrid.h`, сразу после объявления
  полей, перед закрывающей `};`:

  ```cpp
      /** Переводит уровень сигнала канала в координату бина. */
      float binOf(float level, int channel) const {
          float y = level;
          if (layout_ == 1) {
              y = (channel == 0) ? level * 0.5f - 0.5f : level * 0.5f + 0.5f;
          }
          if (y < -1.0f) {
              y = -1.0f;
          }
          if (y > 1.0f) {
              y = 1.0f;
          }
          return (y + 1.0f) * 0.5f * static_cast<float>(kBins - 1);
      }

      /** Раскладывает вес по двум соседним бинам одного столбца. */
      void splat(int column, float bin, int channel, float weight) {
          if (column < 0 || column >= columns_) {
              return;
          }
          if (bin < 0.0f) {
              bin = 0.0f;
          }
          const float maxBin = static_cast<float>(kBins - 1);
          if (bin > maxBin) {
              bin = maxBin;
          }

          const int low = static_cast<int>(bin);
          const int high = (low + 1 < kBins) ? low + 1 : low;
          const float fraction = bin - static_cast<float>(low);

          float *base = cells_.data() +
                        static_cast<std::size_t>(column) * kColumnStride;
          base[low * 2 + channel] += weight * (1.0f - fraction);
          base[high * 2 + channel] += weight * fraction;
      }

      void clearColumn(int column) {
          float *base = cells_.data() +
                        static_cast<std::size_t>(column) * kColumnStride;
          std::fill(base, base + kColumnStride, 0.0f);
      }
  ```

- [ ] **Step 3: Добавить растеризацию отрезка.**

  Вставить в секцию `private:` файла `PhosphorGrid.h`, после `clearColumn`:

  ```cpp
      /**
       * Растеризует путь луча между двумя сэмплами. Полная энергия отрезка
       * равна weight независимо от его длины — это интеграл времени
       * задержки луча.
       */
      void drawSegment(float columnA, float binA, float columnB, float binB,
                       int channel, float weight) {
          const float deltaColumn = columnB - columnA;
          const float deltaBin = binB - binA;
          const float span = std::max(std::fabs(deltaColumn), std::fabs(deltaBin));

          int steps = static_cast<int>(std::ceil(span));
          if (steps < 1) {
              steps = 1;
          }
          if (steps > kMaxSteps) {
              steps = kMaxSteps;
          }

          const float share = weight / static_cast<float>(steps);
          const float stepColumn = deltaColumn / static_cast<float>(steps);
          const float stepBin = deltaBin / static_cast<float>(steps);

          float column = columnA;
          float bin = binA;
          for (int i = 0; i < steps; ++i) {
              splat(static_cast<int>(column), bin, channel, share);
              column += stepColumn;
              bin += stepBin;
          }
      }
  ```

- [ ] **Step 4: Добавить дописывание новых сэмплов в режиме roll.**

  Вставить в секцию `public:` файла `PhosphorGrid.h`, после `configure`:

  ```cpp
      /**
       * Дописывает новый аудиопакет. Вызывается из аудиопотока.
       * @param interleaved чередующиеся сэмплы двух каналов.
       * @param frames число стереокадров в пакете.
       */
      void appendFrames(const float *interleaved, std::size_t frames) {
          if (interleaved == nullptr || frames == 0) {
              return;
          }

          std::lock_guard<std::mutex> lock(mutex_);
          if (!ready_ || !rollMode_) {
              return;
          }

          // Энергия одного кадра подобрана так, чтобы суммарная энергия
          // столбца не зависела от развёртки.
          const float weight = columnsPerFrame_;

          for (std::size_t i = 0; i < frames; ++i) {
              const int column = static_cast<int>(columnPosition_);
              if (column != lastColumn_) {
                  advanceTo(column);
              }

              const float current[2] = {interleaved[i * 2], interleaved[i * 2 + 1]};
              if (hasPrevious_) {
                  for (int channel = 0; channel < 2; ++channel) {
                      const float from = binOf(previous_[channel], channel);
                      const float to = binOf(current[channel], channel);
                      drawSegment(static_cast<float>(column), from,
                                  static_cast<float>(column), to,
                                  channel, weight);
                  }
              }
              previous_[0] = current[0];
              previous_[1] = current[1];
              hasPrevious_ = true;

              columnPosition_ += columnsPerFrame_;
              while (columnPosition_ >= static_cast<float>(columns_)) {
                  columnPosition_ -= static_cast<float>(columns_);
              }
          }

          // Текущий столбец продолжит накапливать энергию в следующих пакетах,
          // поэтому его нужно перезаливать в текстуру, пока он не завершён.
          if (lastColumn_ >= 0) {
              markDirty(lastColumn_);
          }
      }
  ```

  Затем вставить в секцию `private:`, после `drawSegment`:

  ```cpp
      /** Очищает все столбцы от предыдущего до нового и помечает их грязными. */
      void advanceTo(int column) {
          if (lastColumn_ < 0) {
              clearColumn(column);
              markDirty(column);
              lastColumn_ = column;
              return;
          }

          int cursor = lastColumn_;
          while (cursor != column) {
              cursor = (cursor + 1) % columns_;
              clearColumn(cursor);
              markDirty(cursor);
          }
          lastColumn_ = column;
      }

      void markDirty(int column) {
          if (dirtyCount_ == 0) {
              dirtyStart_ = column;
              dirtyCount_ = 1;
              return;
          }
          if (dirtyCount_ >= columns_) {
              dirtyStart_ = 0;
              dirtyCount_ = columns_;
              return;
          }

          const int offset = (column - dirtyStart_ + columns_) % columns_;
          if (offset >= dirtyCount_) {
              dirtyCount_ = offset + 1;
          }
          if (dirtyCount_ > columns_) {
              dirtyStart_ = 0;
              dirtyCount_ = columns_;
          }
      }
  ```

- [ ] **Step 5: Добавить полный пересчёт для режима развёртки.**

  Вставить в секцию `public:` файла `PhosphorGrid.h`, после `appendFrames`:

  ```cpp
      /**
       * Пересчитывает всю сетку из окна истории. Вызывается из GL-потока
       * при развёртке меньше 32.
       */
      void rebuild(const float *interleaved, std::size_t frames) {
          if (interleaved == nullptr || frames < 2) {
              return;
          }

          std::lock_guard<std::mutex> lock(mutex_);
          if (!ready_ || rollMode_) {
              return;
          }

          for (float &cell : cells_) {
              cell *= kDecay;
          }

          const float step = static_cast<float>(columns_) /
                             static_cast<float>(frames);
          // Множитель (1 - kDecay) выравнивает установившуюся яркость
          // с режимом roll, где затухания нет.
          const float weight = step * (1.0f - kDecay);

          float column = 0.0f;
          for (std::size_t i = 1; i < frames; ++i) {
              for (int channel = 0; channel < 2; ++channel) {
                  const float from = binOf(interleaved[(i - 1) * 2 + channel], channel);
                  const float to = binOf(interleaved[i * 2 + channel], channel);
                  drawSegment(column, from, column + step, to, channel, weight);
              }
              column += step;
          }

          dirtyStart_ = 0;
          dirtyCount_ = columns_;
      }
  ```

- [ ] **Step 6: Добавить выдачу грязного диапазона и смещения кольца.**

  Вставить в секцию `public:` файла `PhosphorGrid.h`, после `rebuild`:

  ```cpp
      /**
       * Забирает диапазон столбцов, изменившихся с прошлого вызова, и
       * сбрасывает его. Диапазон кольцевой: может пересекать край сетки.
       */
      void takeDirtyRange(int *start, int *count) {
          std::lock_guard<std::mutex> lock(mutex_);
          *start = dirtyStart_;
          *count = dirtyCount_;
          dirtyStart_ = 0;
          dirtyCount_ = 0;
      }

      /** Смещение чтения текстуры, чтобы новейший столбец был у правого края. */
      float ringOffset() const {
          // lastColumn_ пишет аудиопоток, поэтому читать его без мьютекса
          // нельзя. Вызов происходит раз в кадр, стоимость блокировки нулевая.
          std::lock_guard<std::mutex> lock(mutex_);
          if (!rollMode_ || columns_ <= 0 || lastColumn_ < 0) {
              return 0.0f;
          }
          return static_cast<float>(lastColumn_ + 1) /
                 static_cast<float>(columns_);
      }
  ```

- [ ] **Step 7: Проверить компиляцию.**

  Файл ещё не подключён к сборке, поэтому проверяем его отдельной задачей в Task 2. Здесь достаточно убедиться, что структура файла корректна: секции идут в порядке `public:` (`configure`, `appendFrames`, `rebuild`, `takeDirtyRange`, `ringOffset`, геттеры), затем `private:` (поля, `binOf`, `splat`, `clearColumn`, `drawSegment`, `advanceTo`, `markDirty`).

- [ ] **Step 8: Коммит.**

  ```powershell
  git add -- app/src/main/cpp/route/PhosphorGrid.h
  git commit -m "feat: add phosphor grid accumulator core"
  ```

---

### Task 2: Привязка к JNI и аудиопотоку

**Files:**
- Create: `app/src/main/cpp/route/PhosphorJni.cpp`
- Create: `app/src/main/java/com/example/generator2/features/scope/NativePhosphor.kt`
- Modify: `app/src/main/cpp/CMakeLists.txt`
- Modify: `app/src/main/cpp/route/FloatDirectBuffer.cpp:1-9,41-59`

- [ ] **Step 1: Создать JNI-слой.**

  Создать `app/src/main/cpp/route/PhosphorJni.cpp`:

  ```cpp
  //
  // JNI-привязка фосфорного аккумулятора.
  //

  #include "PhosphorGrid.h"
  #include "FloatDirectBuffer.h"

  #include <jni.h>

  PhosphorGrid phosphorGrid;

  extern AudioHistoryBuffer audioHistoryBuffer;

  namespace {

  int requestedColumns = 0;
  int requestedLayout = 0;
  bool requestedRollMode = true;

  /**
   * Пересобирает конфигурацию, когда меняются запрошенные параметры или
   * размер окна истории. Окно известно только после первого аудиопакета,
   * поэтому проверка выполняется на каждом обращении.
   */
  void ensureConfigured() {
      if (requestedColumns <= 0) {
          return;
      }
      const std::size_t frames = audioHistoryBuffer.window() / 2;
      if (frames == 0) {
          return;
      }
      phosphorGrid.configure(requestedColumns, requestedLayout, frames,
                             requestedRollMode);
  }

  } // namespace

  extern "C"
  JNIEXPORT void JNICALL
  Java_com_example_generator2_features_scope_NativePhosphor_configure(
          JNIEnv * /* env */, jobject /* thiz */, jint columns, jint layout,
          jboolean rollMode) {
      requestedColumns = columns;
      requestedLayout = layout;
      requestedRollMode = (rollMode == JNI_TRUE);
      ensureConfigured();
  }

  extern "C"
  JNIEXPORT jintArray JNICALL
  Java_com_example_generator2_features_scope_NativePhosphor_update(
          JNIEnv *env, jobject /* thiz */) {
      ensureConfigured();

      if (phosphorGrid.isReady() && !phosphorGrid.isRollMode()) {
          const float *window = audioHistoryBuffer.read();
          if (window != nullptr) {
              phosphorGrid.rebuild(window, audioHistoryBuffer.window() / 2);
          }
      }

      jint range[2] = {0, 0};
      phosphorGrid.takeDirtyRange(&range[0], &range[1]);

      jintArray result = env->NewIntArray(2);
      if (result == nullptr) {
          return nullptr;
      }
      env->SetIntArrayRegion(result, 0, 2, range);
      return result;
  }

  extern "C"
  JNIEXPORT jobject JNICALL
  Java_com_example_generator2_features_scope_NativePhosphor_gridBuffer(
          JNIEnv *env, jobject /* thiz */) {
      if (!phosphorGrid.isReady()) {
          return nullptr;
      }
      const jlong bytes = static_cast<jlong>(phosphorGrid.columns()) *
                          static_cast<jlong>(PhosphorGrid::kColumnStride) *
                          static_cast<jlong>(sizeof(float));
      return env->NewDirectByteBuffer(
              const_cast<float *>(phosphorGrid.data()), bytes);
  }

  extern "C"
  JNIEXPORT jfloat JNICALL
  Java_com_example_generator2_features_scope_NativePhosphor_ringOffset(
          JNIEnv * /* env */, jobject /* thiz */) {
      return phosphorGrid.ringOffset();
  }
  ```

- [ ] **Step 2: Передавать входящий аудиопакет в аккумулятор.**

  В `app/src/main/cpp/route/FloatDirectBuffer.cpp` после строки 5
  (`#include "FloatDirectBuffer.h"`) добавить:

  ```cpp
  #include "PhosphorGrid.h"

  extern PhosphorGrid phosphorGrid;
  ```

  Затем в функции `Java_com_example_generator2_features_scope_NativeFloatDirectBuffer_add`
  заменить строку

  ```cpp
      audioHistoryBuffer.add(elements, len, item_count);
  ```

  на

  ```cpp
      audioHistoryBuffer.add(elements, len, item_count);
      phosphorGrid.appendFrames(elements, static_cast<std::size_t>(len) / 2);
  ```

- [ ] **Step 3: Подключить новый файл к сборке.**

  В `app/src/main/cpp/CMakeLists.txt` заменить строку

  ```cmake
          route/FloatDirectBuffer.cpp
  ```

  на

  ```cmake
          route/FloatDirectBuffer.cpp
          route/PhosphorJni.cpp
  ```

- [ ] **Step 4: Создать Kotlin-объявления.**

  Создать `app/src/main/java/com/example/generator2/features/scope/NativePhosphor.kt`:

  ```kotlin
  package com.example.generator2.features.scope

  import java.nio.ByteBuffer

  object NativePhosphor {

      /** Число бинов по вертикали, должно совпадать с PhosphorGrid::kBins. */
      const val BINS = 512

      init {
          System.loadLibrary("plasma")
      }

      /**
       * Задаёт геометрию сетки и режим отрисовки.
       * @param columns ширина области вывода в пикселях.
       * @param layout 0 — каналы совмещены, 1 — каналы в своих половинах.
       * @param rollMode true при развёртке 32 и выше.
       */
      external fun configure(columns: Int, layout: Int, rollMode: Boolean)

      /** Возвращает [начальный столбец, количество] изменившихся столбцов. */
      external fun update(): IntArray

      /** Direct-буфер всей сетки: columns * BINS * 2 float. */
      external fun gridBuffer(): ByteBuffer?

      /** Смещение чтения текстуры в диапазоне [0, 1). */
      external fun ringOffset(): Float
  }
  ```

- [ ] **Step 5: Проверить сборку.**

  Запустить:

  ```powershell
  .\gradlew.bat :app:assembleDebug
  ```

  Ожидается `BUILD SUCCESSFUL`. Если CMake ругается на `PhosphorGrid.h not
  found`, проверить, что оба файла лежат в `app/src/main/cpp/route/`.

- [ ] **Step 6: Коммит.**

  ```powershell
  git add -- app/src/main/cpp/route/PhosphorJni.cpp app/src/main/cpp/route/FloatDirectBuffer.cpp app/src/main/cpp/CMakeLists.txt app/src/main/java/com/example/generator2/features/scope/NativePhosphor.kt
  git commit -m "feat: wire phosphor grid to audio packets over JNI"
  ```

---

### Task 3: Переписать рендерер осциллографа

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/scope/opengl/render/MyGLRendererOscill.kt` — заменить целиком

- [ ] **Step 1: Заменить содержимое рендерера.**

  Полностью заменить `app/src/main/java/com/example/generator2/features/scope/opengl/render/MyGLRendererOscill.kt` на:

  ```kotlin
  package com.example.generator2.features.scope.opengl.render

  import android.opengl.GLES30.GL_CLAMP_TO_EDGE
  import android.opengl.GLES30.GL_COLOR_BUFFER_BIT
  import android.opengl.GLES30.GL_COMPILE_STATUS
  import android.opengl.GLES30.GL_FLOAT
  import android.opengl.GLES30.GL_FRAGMENT_SHADER
  import android.opengl.GLES30.GL_LINEAR
  import android.opengl.GLES30.GL_RG
  import android.opengl.GLES30.GL_RG16F
  import android.opengl.GLES30.GL_REPEAT
  import android.opengl.GLES30.GL_TEXTURE0
  import android.opengl.GLES30.GL_TEXTURE_2D
  import android.opengl.GLES30.GL_TEXTURE_MAG_FILTER
  import android.opengl.GLES30.GL_TEXTURE_MIN_FILTER
  import android.opengl.GLES30.GL_TEXTURE_WRAP_S
  import android.opengl.GLES30.GL_TEXTURE_WRAP_T
  import android.opengl.GLES30.GL_TRIANGLE_STRIP
  import android.opengl.GLES30.GL_VERTEX_SHADER
  import android.opengl.GLES30.glActiveTexture
  import android.opengl.GLES30.glAttachShader
  import android.opengl.GLES30.glBindTexture
  import android.opengl.GLES30.glClear
  import android.opengl.GLES30.glClearColor
  import android.opengl.GLES30.glCompileShader
  import android.opengl.GLES30.glCreateProgram
  import android.opengl.GLES30.glCreateShader
  import android.opengl.GLES30.glDeleteProgram
  import android.opengl.GLES30.glDeleteShader
  import android.opengl.GLES30.glDeleteTextures
  import android.opengl.GLES30.glDrawArrays
  import android.opengl.GLES30.glGenTextures
  import android.opengl.GLES30.glGetShaderInfoLog
  import android.opengl.GLES30.glGetShaderiv
  import android.opengl.GLES30.glGetUniformLocation
  import android.opengl.GLES30.glLinkProgram
  import android.opengl.GLES30.glShaderSource
  import android.opengl.GLES30.glTexParameteri
  import android.opengl.GLES30.glTexStorage2D
  import android.opengl.GLES30.glTexSubImage2D
  import android.opengl.GLES30.glUniform1f
  import android.opengl.GLES30.glUniform1i
  import android.opengl.GLES30.glUniform2f
  import android.opengl.GLES30.glUseProgram
  import android.opengl.GLES30.glViewport
  import android.opengl.GLSurfaceView
  import com.example.generator2.features.scope.NativePhosphor
  import java.nio.ByteBuffer
  import java.nio.ByteOrder
  import java.util.concurrent.atomic.AtomicBoolean
  import javax.microedition.khronos.egl.EGLConfig
  import javax.microedition.khronos.opengles.GL10

  /** Развёртки от этого значения и выше рисуются как бегущая лента. */
  private const val ROLL_THRESHOLD = 32f

  /** Подобранное усиление тонмаппинга: ядро луча выходит на полную яркость. */
  private const val TONEMAP_GAIN = 6.0f

  class MyGLRendererOscill : GLSurfaceView.Renderer {

      private var program: Int = 0
      private var vertexShader: Int = 0
      private var fragmentShader: Int = 0

      private var texture: Int = 0
      private var textureColumns: Int = 0

      private var gridBuffer: ByteBuffer? = null
      private var configuredColumns: Int = 0
      private var configuredLayout: Int = -1
      private var configuredRoll: Boolean? = null

      var compressorCount: Float = 0f

      val bools = intArrayOf(0, 1, 1) //oneTwo 0-one 1-two, L 1-true, R

      private val vertexShaderCode =
          """
  #version 300 es

  out vec2 uv;

  void main() {
      // Полноэкранный quad из четырёх вершин без буфера вершин.
      vec2 corner = vec2(float(gl_VertexID & 1), float((gl_VertexID >> 1) & 1));
      uv = corner;
      gl_Position = vec4(corner * 2.0 - 1.0, 0.0, 1.0);
  }
  """.trimIndent()

      private val fragmentShaderCode =
          """
  #version 300 es
  precision mediump float;

  uniform sampler2D grid;
  uniform float ringOffset;
  uniform float gain;
  uniform vec2 visibility;

  in vec2 uv;
  out vec4 fragColor;

  void main() {
      vec2 energy = texture(grid, vec2(fract(uv.x + ringOffset), uv.y)).rg;

      // Мягкое насыщение: ядро луча яркое, ореол остаётся плавным.
      float first = 1.0 - exp(-energy.r * gain);
      float second = 1.0 - exp(-energy.g * gain);

      vec3 color = vec3(1.0, 0.0, 1.0) * first * visibility.x
                 + vec3(1.0, 1.0, 0.0) * second * visibility.y;

      fragColor = vec4(color + vec3(0.0, 0.15, 0.0), 1.0);
  }
  """.trimIndent()

      override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
          glClearColor(0.0f, 0.15f, 0.0f, 1f)

          vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
          fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)

          program = glCreateProgram().also {
              glAttachShader(it, vertexShader)
              glAttachShader(it, fragmentShader)
              glLinkProgram(it)
          }

          glUseProgram(program)

          // Текстура пересоздаётся при первом кадре с известной шириной.
          textureColumns = 0
          configuredColumns = 0
          configuredLayout = -1
          configuredRoll = null
      }

      override fun onDrawFrame(gl: GL10?) {
          if (!shouldPlay.get() || program == 0 || width <= 0) {
              return
          }

          val rollMode = compressorCount >= ROLL_THRESHOLD
          val layout = bools[0]

          if (configuredColumns != width ||
              configuredLayout != layout ||
              configuredRoll != rollMode
          ) {
              NativePhosphor.configure(width, layout, rollMode)
              configuredColumns = width
              configuredLayout = layout
              configuredRoll = rollMode
              gridBuffer = null
              ensureTexture(width)
          }

          val range = NativePhosphor.update()
          uploadColumns(range[0], range[1])

          glClear(GL_COLOR_BUFFER_BIT)
          glUseProgram(program)

          glActiveTexture(GL_TEXTURE0)
          glBindTexture(GL_TEXTURE_2D, texture)
          glUniform1i(glGetUniformLocation(program, "grid"), 0)
          glUniform1f(glGetUniformLocation(program, "ringOffset"), NativePhosphor.ringOffset())
          glUniform1f(glGetUniformLocation(program, "gain"), TONEMAP_GAIN)
          glUniform2f(
              glGetUniformLocation(program, "visibility"),
              if (bools[2] == 1) 1.0f else 0.0f,
              if (bools[1] == 1) 1.0f else 0.0f
          )

          glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
      }

      private fun ensureTexture(columns: Int) {
          if (textureColumns == columns && texture != 0) {
              return
          }
          if (texture != 0) {
              glDeleteTextures(1, intArrayOf(texture), 0)
              texture = 0
          }

          val handles = IntArray(1)
          glGenTextures(1, handles, 0)
          texture = handles[0]

          glBindTexture(GL_TEXTURE_2D, texture)
          glTexStorage2D(GL_TEXTURE_2D, 1, GL_RG16F, columns, NativePhosphor.BINS)
          glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
          glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
          // По горизонтали кольцо, поэтому фильтрация должна заворачиваться.
          glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
          glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

          textureColumns = columns
      }

      /** Заливает грязный кольцевой диапазон, разбивая его на куски по краю сетки. */
      private fun uploadColumns(start: Int, count: Int) {
          if (count <= 0 || textureColumns <= 0) {
              return
          }

          val buffer = gridBuffer ?: NativePhosphor.gridBuffer()?.also {
              it.order(ByteOrder.nativeOrder())
              gridBuffer = it
          } ?: return

          glBindTexture(GL_TEXTURE_2D, texture)

          val columnBytes = NativePhosphor.BINS * 2 * 4
          var offset = 0
          while (offset < count) {
              val column = (start + offset) % textureColumns
              val chunk = minOf(count - offset, textureColumns - column)

              buffer.position(column * columnBytes)
              glTexSubImage2D(
                  GL_TEXTURE_2D, 0,
                  column, 0,
                  chunk, NativePhosphor.BINS,
                  GL_RG, GL_FLOAT,
                  buffer.slice().order(ByteOrder.nativeOrder())
              )
              offset += chunk
          }
          buffer.position(0)
      }

      var width: Int = 0
      var height: Int = 0

      override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
          this.width = width
          this.height = height
          glViewport(0, 0, width, height)
      }

      private fun loadShader(type: Int, shaderCode: String): Int {
          return glCreateShader(type).also { shader ->
              glShaderSource(shader, shaderCode)
              glCompileShader(shader)

              val compiled = IntArray(1)
              glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0)
              if (compiled[0] == 0) {
                  glDeleteShader(shader)
                  throw RuntimeException(
                      "Could not compile shader $type: ${glGetShaderInfoLog(shader)}"
                  )
              }
          }
      }

      fun deleteProgram() {
          if (texture != 0) {
              glDeleteTextures(1, intArrayOf(texture), 0)
              texture = 0
              textureColumns = 0
          }
          if (program != 0) {
              glDeleteProgram(program)
              program = 0
          }
          gridBuffer = null
      }

      private val shouldPlay = AtomicBoolean(false)

      fun onResume() {
          shouldPlay.compareAndSet(false, true)
      }

      fun onPause() {
          shouldPlay.compareAndSet(true, false)
      }
  }
  ```

  Обрати внимание: `visibility.x` соответствует каналу чётных индексов
  (пурпурный, `bools[2]`), `visibility.y` — нечётным (жёлтый, `bools[1]`).
  Это тот же порядок, что был в старом вершинном шейдере.

- [ ] **Step 2: Проверить сборку.**

  Запустить:

  ```powershell
  .\gradlew.bat :app:assembleDebug
  ```

  Ожидается ошибка компиляции Kotlin в `Scope.kt`: `Unresolved reference:
  updateVerticesDirect`. Это ожидаемо — вызов убирается в Task 4.

- [ ] **Step 3: Коммит.**

  Коммит выполняется вместе с Task 4, потому что проект не собирается между
  этими задачами. Перейти к Task 4 без коммита.

---

### Task 4: Подключить рендерер и проверить на устройстве

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/scope/Scope.kt:242`

- [ ] **Step 1: Убрать вызов удалённого метода.**

  В `app/src/main/java/com/example/generator2/features/scope/Scope.kt` внутри
  `LaunchedEffect` композабла `Oscilloscope` удалить строку

  ```kotlin
                  shaderRenderer.updateVerticesDirect()
  ```

  Остальные строки блока не трогать — они по-прежнему передают
  `compressorCount` и `bools` в рендерер:

  ```kotlin
                  deferredOscill.receive()
                  shaderRenderer.compressorCount = compressorCount.floatValue
                  shaderRenderer.bools[0] = if (isOneTwo.value) 1 else 0
                  shaderRenderer.bools[1] = if (isVisibleL.value) 1 else 0
                  shaderRenderer.bools[2] = if (isVisibleR.value) 1 else 0
                  view?.requestRender()
  ```

- [ ] **Step 2: Проверить сборку.**

  Запустить:

  ```powershell
  .\gradlew.bat :app:assembleDebug
  ```

  Ожидается `BUILD SUCCESSFUL`.

- [ ] **Step 3: Установить на устройство.**

  Запустить:

  ```powershell
  .\gradlew.bat :app:installDebug
  ```

  Ожидается `BUILD SUCCESSFUL` и появление приложения на устройстве.

- [ ] **Step 4: Проверить основной дефект.**

  Подать синус 4410 Hz при частоте дискретизации 44100 Hz, развёртка 256.

  Ожидается: столбец плотно заполнен по вертикали, разрежённого узора из
  отдельных точек нет. Расстройка сигнала на несколько герц не меняет картину
  качественно.

  Если столбцы пустые — проверить в logcat, что `AudioHistoryBuffer` не пишет
  `add(): invalid JNI arguments`, и что `NativePhosphor.configure` вызывается с
  `columns > 0`.

- [ ] **Step 5: Проверить остальные режимы.**

  1. Развёртки 0.125–16: форма сигнала читается, точки сливаются в непрерывный
     след, при непериодическом сигнале видно послесвечение.
  2. Развёртки 32–256: лента бежит без разрывов и без видимого шва на месте
     кольцевого стыка.
  3. Переключение видимости левого и правого канала гасит нужный цвет.
  4. Переключение совмещённого и раздельного режима каналов перестраивает
     картину и не оставляет мусора от предыдущего режима.
  5. Пауза замораживает изображение.
  6. Частота кадров на развёртке 256 не ниже, чем до изменений.

- [ ] **Step 6: Подстроить яркость, если требуется.**

  Если след слишком тусклый или пересвечен, изменить `TONEMAP_GAIN` в
  `MyGLRendererOscill.kt` и повторить шаги 2–4. Диапазон разумных значений
  примерно от `2.0f` до `20.0f`.

- [ ] **Step 7: Коммит.**

  ```powershell
  git add -- app/src/main/java/com/example/generator2/features/scope/opengl/render/MyGLRendererOscill.kt app/src/main/java/com/example/generator2/features/scope/Scope.kt
  git commit -m "feat: render oscilloscope from phosphor grid texture"
  ```

---

## Plan Self-Review

**Покрытие спецификации.** Ядро `PhosphorGrid.h` с сеткой `columns × 512 × 2`
в column-major порядке и растеризацией отрезков — Task 1. Два режима работы,
roll и sweep с затуханием 0.85 — Task 1, шаги 4 и 5. JNI-интерфейс из четырёх
функций и повторное использование глобального `audioHistoryBuffer` — Task 2.
Полноэкранный quad, текстура `RG16F`, дозапись грязных столбцов, экспоненциальный
тонмаппинг, видимость каналов в шейдере и режим совмещения в C++ — Task 3.
Верификация из семи пунктов — Task 4, шаги 4 и 5.

**Плейсхолдеры.** Каждый шаг, меняющий код, содержит полный текст этого кода.
Отложенных решений и формулировок вида «добавить обработку ошибок» нет.

**Согласованность имён.** `configure`, `appendFrames`, `rebuild`,
`takeDirtyRange`, `ringOffset`, `isReady`, `columns`, `isRollMode`, `data`,
`kBins`, `kColumnStride` используются одинаково в `PhosphorGrid.h`,
`PhosphorJni.cpp` и `NativePhosphor.kt`. Порядок каналов — чётный индекс в `.r`
и `visibility.x`, нечётный в `.g` и `visibility.y` — одинаков в `binOf`,
`appendFrames`, `rebuild` и фрагментном шейдере.

**Отклонение от спецификации.** В спецификации риск связи с Лиссажу помечен как
требующий проверки. Проверка выполнена до написания плана: `Scope.kt:331`
создаёт отдельный `MyGLRendererLissagu`, общего состояния с осциллографом нет.
Риск снят, отдельная задача не нужна.

## Правки после ревью (коммит `8de1f4a`)

Код рендерера в Task 3 выше — **исходная редакция**. Два ревью нашли в ней
дефекты, актуальный код смотри в коммите `8de1f4a`, а не в этом документе.

**Текстура была развёрнута неверно.** Создавалась как `columns × BINS` и
заливалась построчно, но сетка хранит 512 бинов одного столбца подряд.
Раскладки совпадали только при заливке ровно одного столбца, а любая заливка
нескольких столбцов скремблила картинку — то есть каждый кадр в режиме
развёртки, каждая переконфигурация и каждый первый кадр. Текстура развёрнута в
`BINS × columns`, оси UV в шейдере и режимы обмотки поменяны местами.

**Пересозданная текстура не перерисовывалась.** `configure()` уходит в быстрый
выход при неизменных параметрах и не помечает сетку грязной, а вызывается он
каждый кадр — этот guard обязан остаться. Но при пересоздании композабла
(уход с экрана и возврат) появлялась новая пустая текстура, тогда как нативный
`phosphorGrid` — глобал и считал, что ничего не изменилось. До полного оборота
кольца на экране был мусор, при развёртке 256 это 6.68 секунды. Добавлен
`PhosphorGrid::markAllDirty()` и JNI-обёртка `invalidate()`, рендерер зовёт её
после создания новой текстуры.

Прочее: кэш `gridBuffer` на стороне Kotlin убран — нативная сторона умеет
переконфигурироваться сама при смене окна истории, и Kotlin об этом не узнавал;
локации юниформов кэшируются; ширина сетки ограничена `GL_MAX_TEXTURE_SIZE`;
quad не рисуется до первой успешной заливки; длина окна в `update()` читается
один раз.

## Замечания вне объёма работы

Развёртки меньше 1 сломаны и до этих изменений. Жест на `Scope.kt:305`
позволяет опустить `compressorCount` до `0.125`, но `Scope.kt:171` передаёт
значение как `compressorCount.floatValue.toInt()`, что даёт `0`, и
`AudioHistoryBuffer::isValidConfiguration` отвергает пакет. Кнопка на
`Scope.kt:134` при этом ограничивает то же значение снизу единицей. План этого
не чинит.
