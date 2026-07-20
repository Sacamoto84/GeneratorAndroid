//
// Фосфорный аккумулятор осциллографа.
//

#ifndef GENERATOR2_PHOSPHORGRID_H
#define GENERATOR2_PHOSPHORGRID_H

#include <algorithm>
#include <atomic>
#include <cmath>
#include <cstdint>
#include <cstring>
#include <cstddef>
#include <mutex>
#include <vector>

class PhosphorGrid {
public:
    static constexpr int kBins = 512;
    static constexpr int kMaxColumns = 4096;
    static constexpr int kMaxSteps = 1024;

    // На сколько частей дробится участок между отсчётами при ведении по
    // кривой. Четырёх хватает: кубика через четыре отсчёта при десяти
    // отсчётах на период отличается от синуса на доли бина.
    static constexpr int kCurveSteps = 4;

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
        half_.assign(cells_.size(), 0u);
        columnPosition_ = 0.0f;
        lastColumn_ = -1;
        dirtyRange_.store(packRange(0, columns_), std::memory_order_relaxed);
        historyCount_ = 0;
        ready_ = true;
    }

    /**
     * Дописывает новый аудиопакет. Вызывается из аудиопотока.
     * @param interleaved чередующиеся сэмплы двух каналов.
     * @param frames число стереокадров в пакете.
     */
    void appendFrames(const float *interleaved, std::size_t frames) {
        if (interleaved == nullptr || frames == 0) {
            return;
        }

        // Считаем пакет до всех проверок: счётчик нужен и в режиме развёртки,
        // где этот метод сразу выходит.
        packetSerial_.fetch_add(1, std::memory_order_relaxed);

        // Режим проверяем до мьютекса: в режиме развёртки его держит rebuild()
        // из потока GL всю пересборку, и аудиопоток вставал бы там на
        // миллисекунды впустую — работы для него всё равно нет.
        if (!rollMode_.load(std::memory_order_relaxed)) {
            return;
        }

        std::unique_lock<std::mutex> lock(mutex_);
        if (!ready_ || !rollMode_) {
            return;
        }

        // Энергия одного кадра подобрана так, чтобы суммарная энергия
        // столбца не зависела от развёртки.
        const float weight = columnsPerFrame_;

        for (std::size_t i = 0; i < frames; ++i) {
            const int column = static_cast<int>(columnPosition_);
            if (column != lastColumn_) {
                // Растеризация пакета занимает заметное время, и держать
                // мьютекс всю дорогу значит тормозить каждый кадр отрисовки.
                // Граница столбца — место, где поток GL можно пустить вперёд.
                lock.unlock();
                lock.lock();
                // Пока мьютекс был отпущен, configure() мог сменить геометрию.
                // Тогда локальная позиция уже не относится к этой сетке —
                // бросаем остаток пакета, следующий начнётся с чистого листа.
                if (!ready_ || !rollMode_ || column >= columns_) {
                    return;
                }
                advanceTo(column);
            }

            // Окно из четырёх отсчётов: рисуем участок между вторым и третьим,
            // крайние задают наклон кривой на его концах.
            for (int channel = 0; channel < 2; ++channel) {
                history_[0][channel] = history_[1][channel];
                history_[1][channel] = history_[2][channel];
                history_[2][channel] = history_[3][channel];
                history_[3][channel] = interleaved[i * 2 + channel];
            }
            if (historyCount_ < 4) {
                ++historyCount_;
            }

            if (historyCount_ == 4) {
                for (int channel = 0; channel < 2; ++channel) {
                    drawCurve(static_cast<float>(column),
                              static_cast<float>(column),
                              history_[0][channel], history_[1][channel],
                              history_[2][channel], history_[3][channel],
                              channel, weight);
                }
            }

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

        // Послесвечения в режиме развёртки нет: кадр собирается с нуля.
        std::fill(cells_.begin(), cells_.end(), 0.0f);

        const float step = static_cast<float>(columns_) /
                           static_cast<float>(frames);
        const float weight = step;

        float column = 0.0f;
        for (std::size_t i = 1; i < frames; ++i) {
            // Соседи участка. На краях окна дублируем крайний отсчёт —
            // наклон там всё равно неоткуда взять.
            const std::size_t before = (i >= 2) ? i - 2 : 0;
            const std::size_t after = (i + 1 < frames) ? i + 1 : frames - 1;

            for (int channel = 0; channel < 2; ++channel) {
                drawCurve(column, column + step,
                          interleaved[before * 2 + channel],
                          interleaved[(i - 1) * 2 + channel],
                          interleaved[i * 2 + channel],
                          interleaved[after * 2 + channel],
                          channel, weight);
            }
            column += step;
        }

        dirtyRange_.store(packRange(0, columns_), std::memory_order_relaxed);
    }

    /**
     * Забирает диапазон столбцов, изменившихся с прошлого вызова, и
     * сбрасывает его. Диапазон кольцевой: может пересекать край сетки.
     */
    void takeDirtyRange(int *start, int *count) {
        // Без мьютекса: обмен атомарен, а ждать здесь аудиопоток нельзя.
        const std::uint64_t range =
                dirtyRange_.exchange(0, std::memory_order_relaxed);
        *start = rangeStart(range);
        *count = rangeCount(range);
    }

    /**
     * Помечает всю сетку грязной. Нужно, когда GL-сторона пересоздала
     * текстуру: её содержимое потеряно, а конфигурация могла не измениться,
     * и configure() тогда ничего не пометит.
     */
    void markAllDirty() {
        std::lock_guard<std::mutex> lock(mutex_);
        if (!ready_) {
            return;
        }
        dirtyRange_.store(packRange(0, columns_), std::memory_order_relaxed);
    }

    /** Смещение чтения текстуры, чтобы новейший столбец был у правого края. */
    float ringOffset() const {
        // lastColumn_ атомарный, мьютекс здесь брать нельзя: аудиопоток
        // держит его во время растеризации, и поток GL вставал бы в очередь
        // на каждом кадре — именно это давало микрофризы.
        const int last = lastColumn_.load(std::memory_order_relaxed);
        if (!rollMode_ || columns_ <= 0 || last < 0) {
            return 0.0f;
        }
        return static_cast<float>(last + 1) /
               static_cast<float>(columns_);
    }

    /**
     * Переводит кольцевой диапазон столбцов в half-float для заливки в
     * текстуру RG16F. Отдавать драйверу float32 нельзя: конверсию он делает
     * сам и медленно, около 340 МБ/с, что при полной сетке съедает
     * почти десять миллисекунд.
     *
     * Вызывается из потока GL. Мьютекс не берётся сознательно, ровно как в
     * data(): аудиопоток в это время может дописывать ячейки, и худшее
     * последствие — один столбец, собранный из двух соседних состояний.
     */
    void packHalf(int start, int count) {
        if (count <= 0 || columns_ <= 0) {
            return;
        }

        for (int i = 0; i < count; ++i) {
            const int column = (start + i) % columns_;
            const std::size_t base =
                    static_cast<std::size_t>(column) * kColumnStride;
            for (std::size_t k = 0; k < kColumnStride; ++k) {
                half_[base + k] = floatToHalf(cells_[base + k]);
            }
        }
    }

    const std::uint16_t *halfData() const { return half_.data(); }

    /**
     * Номер последнего принятого пакета. Растёт в обоих режимах и позволяет
     * GL-стороне понять, появились ли новые сэмплы с прошлого кадра.
     */
    unsigned packetSerial() const {
        return packetSerial_.load(std::memory_order_relaxed);
    }

    bool isReady() const { return ready_; }
    int columns() const { return columns_; }
    bool isRollMode() const { return rollMode_; }
    const float *data() const { return cells_.data(); }

private:
    // Разделение по потокам:
    //   аудиопоток        — appendFrames()
    //   поток GL          — configure(), rebuild(), takeDirtyRange(),
    //                       packHalf(), ringOffset(), isReady(), columns(),
    //                       isRollMode(), packetSerial(), halfData()
    //
    // isReady(), columns() и isRollMode() читают поля, которые пишет только
    // configure(), то есть тот же поток GL — блокировка им не нужна.
    //
    // packHalf() читает cells_ без блокировки, halfData() отдаёт результат
    // для заливки в текстуру. Аудиопоток в это время может дописывать
    // ячейки. Гонка допущена сознательно, худшее последствие — один кадр с
    // неполностью накопленным столбцом. Держать мьютекс на время конверсии
    // и glTexSubImage2D нельзя, это застопорило бы аудиопоток. Указатель
    // нельзя сохранять между вызовами: configure() перевыделяет half_.
    mutable std::mutex mutex_;
    std::vector<float> cells_;
    // Зеркало сетки в half-float, только для выдачи в OpenGL.
    std::vector<std::uint16_t> half_;
    int columns_ = 0;
    int layout_ = 0;
    // Атомарный: appendFrames() проверяет режим до взятия мьютекса, иначе в
    // режиме развёртки аудиопоток вставал бы на всё время rebuild().
    std::atomic<bool> rollMode_{true};
    bool ready_ = false;
    std::size_t framesInWindow_ = 0;
    float columnsPerFrame_ = 0.0f;
    float columnPosition_ = 0.0f;
    // Атомарные: их читает поток GL без мьютекса.
    std::atomic<int> lastColumn_{-1};
    std::atomic<unsigned> packetSerial_{0};
    // Грязный диапазон живёт в одном атомарном слове, а не под мьютексом:
    // поток GL забирает его на каждом кадре, и ждать там аудиопоток нельзя.
    // Тот может быть вытеснен планировщиком прямо внутри критической секции,
    // и тогда кадр стоит миллисекундами — наблюдали 7.4 мс.
    // Старшие 32 бита — начало диапазона, младшие — количество столбцов.
    std::atomic<std::uint64_t> dirtyRange_{0};

    static std::uint64_t packRange(int start, int count) {
        return (static_cast<std::uint64_t>(static_cast<std::uint32_t>(start)) << 32) |
               static_cast<std::uint32_t>(count);
    }

    static int rangeStart(std::uint64_t range) {
        return static_cast<int>(static_cast<std::uint32_t>(range >> 32));
    }

    static int rangeCount(std::uint64_t range) {
        return static_cast<int>(static_cast<std::uint32_t>(range));
    }
    // Скользящее окно из четырёх последних отсчётов по каналам. Рисуемый
    // участок лежит между вторым и третьим, крайние задают наклон.
    int historyCount_ = 0;
    float history_[4][2] = {{0.0f, 0.0f}, {0.0f, 0.0f},
                            {0.0f, 0.0f}, {0.0f, 0.0f}};

    /**
     * float32 в half-float. Значения сетки неотрицательные и невелики, но
     * конвертер общий: денормали схлопываются в ноль, переполнение
     * насыщается максимумом.
     */
    static std::uint16_t floatToHalf(float value) {
        std::uint32_t bits;
        std::memcpy(&bits, &value, sizeof(bits));

        const std::uint32_t sign = (bits >> 16) & 0x8000u;
        const int exponent = static_cast<int>((bits >> 23) & 0xFFu) - 127 + 15;
        const std::uint32_t mantissa = bits & 0x7FFFFFu;

        if (exponent <= 0) {
            return static_cast<std::uint16_t>(sign);
        }
        if (exponent >= 31) {
            return static_cast<std::uint16_t>(sign | 0x7BFFu);
        }
        return static_cast<std::uint16_t>(
                sign | (static_cast<std::uint32_t>(exponent) << 10) |
                (mantissa >> 13));
    }

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

    /**
     * Сплайн Катмулла-Рома: значение сигнала между p1 и p2 при t от 0 до 1.
     * p0 и p3 задают наклон на концах участка.
     */
    static float catmullRom(float p0, float p1, float p2, float p3, float t) {
        const float t2 = t * t;
        const float t3 = t2 * t;
        return 0.5f * (2.0f * p1 +
                       (-p0 + p2) * t +
                       (2.0f * p0 - 5.0f * p1 + 4.0f * p2 - p3) * t2 +
                       (-p0 + 3.0f * p1 - 3.0f * p2 + p3) * t3);
    }

    /**
     * Растеризует путь луча между отсчётами p1 и p2, ведя его по кривой,
     * а не по прямой.
     *
     * Прямая между отсчётами срезает вершины. При 10 отсчётах на период
     * выборка попадает в фазы 0°, 36°, 72° и так далее, максимум приходится
     * на sin 72° = 0.951, а хорда между соседними отсчётами опускает пик ещё
     * ниже. Пока частота сигнала кратна частоте дискретизации, срез
     * постоянен и не виден, но стоит ей уйти на пару герц, как набор фаз
     * начинает медленно ползти и глубина среза гуляет — по краям экрана идёт
     * волна. Кривая через четыре отсчёта восстанавливает вершину и волну
     * убирает.
     *
     * Полная энергия участка равна weight независимо от числа дроблений.
     */
    void drawCurve(float columnA, float columnB, float p0, float p1, float p2,
                   float p3, int channel, float weight) {
        // Отклонение кривой от хорды задаётся второй разностью. Там, где
        // сигнал почти прямой, дробить нечего: хорда и так точнее половины
        // бина, а дробление стоило бы вчетверо дороже.
        const float bend = std::max(std::fabs(p0 - 2.0f * p1 + p2),
                                    std::fabs(p1 - 2.0f * p2 + p3));
        const float deviationBins =
                bend * 0.125f * static_cast<float>(kBins - 1) * 0.5f;

        const float startBin = binOf(p1, channel);
        if (deviationBins < 0.5f) {
            drawSegment(columnA, startBin, columnB, binOf(p2, channel),
                        channel, weight);
            return;
        }

        const float share = weight / static_cast<float>(kCurveSteps);
        const float columnStep =
                (columnB - columnA) / static_cast<float>(kCurveSteps);

        float fromColumn = columnA;
        float fromBin = startBin;

        for (int step = 1; step <= kCurveSteps; ++step) {
            const float t =
                    static_cast<float>(step) / static_cast<float>(kCurveSteps);
            const float toColumn =
                    columnA + columnStep * static_cast<float>(step);
            const float toBin = binOf(catmullRom(p0, p1, p2, p3, t), channel);

            drawSegment(fromColumn, fromBin, toColumn, toBin, channel, share);

            fromColumn = toColumn;
            fromBin = toBin;
        }
    }

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
        std::uint64_t expected = dirtyRange_.load(std::memory_order_relaxed);
        std::uint64_t desired;

        // Цикл CAS: поток GL может забрать диапазон между чтением и записью.
        do {
            int start = rangeStart(expected);
            int count = rangeCount(expected);

            if (count == 0) {
                start = column;
                count = 1;
            } else if (count >= columns_) {
                start = 0;
                count = columns_;
            } else {
                const int offset = (column - start + columns_) % columns_;
                if (offset >= count) {
                    count = offset + 1;
                }
                if (count > columns_) {
                    start = 0;
                    count = columns_;
                }
            }

            desired = packRange(start, count);
        } while (!dirtyRange_.compare_exchange_weak(expected, desired,
                                                    std::memory_order_relaxed));
    }
};

#endif // GENERATOR2_PHOSPHORGRID_H
