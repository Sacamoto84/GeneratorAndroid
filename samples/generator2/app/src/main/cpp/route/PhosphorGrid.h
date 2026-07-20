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
        if (!rollMode_ || columns_ <= 0 || lastColumn_ < 0) {
            return 0.0f;
        }
        return static_cast<float>(lastColumn_ + 1) /
               static_cast<float>(columns_);
    }

    bool isReady() const { return ready_; }
    int columns() const { return columns_; }
    bool isRollMode() const { return rollMode_; }
    const float *data() const { return cells_.data(); }

private:
    std::mutex mutex_;
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
};

#endif // GENERATOR2_PHOSPHORGRID_H
