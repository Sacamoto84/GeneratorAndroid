//
// Created by user on 12.06.2024.
//

#ifndef GENERATOR2_RENDERCHANNEL_H
#define GENERATOR2_RENDERCHANNEL_H


#include <atomic>
#include <cstdint>
#include <cstring>
#include <vector>

constexpr int WAVE_TABLE_SIZE = 1024;

/**
 * Таблица формы волны с двойной буферизацией.
 *
 * Пишет UI-поток (sendBuffer), читает аудиопоток. Раньше запись шла прямо в тот
 * массив, который в этот момент читал рендер, и блок мог отрендериться на
 * полуобновлённой таблице — щелчок при смене формы. Теперь новая форма
 * готовится в свободном слоте, а публикуется одним atomic-store.
 *
 * Рендер берёт указатель один раз на блок: подмена между блоками, внутри блока
 * таблица неизменна.
 */
struct WaveTable {
    float slot[2][WAVE_TABLE_SIZE] = {{0.0f}};
    std::atomic<uint8_t> active{0};

    /** Указатель на опубликованную форму. Действителен до следующего блока. */
    [[nodiscard]] const float *read() const noexcept {
        return slot[active.load(std::memory_order_acquire)];
    }

    /** Опубликовать новую форму целиком. */
    void write(const float *src) noexcept {
        const uint8_t next = active.load(std::memory_order_relaxed) ^ 1u;
        std::memcpy(slot[next], src, sizeof(float) * WAVE_TABLE_SIZE);
        active.store(next, std::memory_order_release);
    }

    /** Заполнить оба слота константой, до первой загрузки формы. */
    void fill(float value) noexcept {
        for (auto &s : slot)
            for (float &v : s) v = value;
    }
};

struct StructureCh {

    int ch = 0;

    WaveTable buffer_carrier;
    WaveTable buffer_am;
    WaveTable buffer_fm;

    // Мастер-громкость
    WaveTable buffer_master;                // форма Плавного, 0..1 (инициализируется 1.0)
    uint32_t phase_accumulator_master = 0;  // DDS Плавного
    float master_current_gain = 1.0f;       // текущий гейн для фейда
    uint32_t master_onoff_counter = 0;      // счётчик сэмплов Вкл/Выкл
    bool master_onoff_on = true;            // текущая фаза Вкл/Выкл (старт ON)

    uint32_t phase_accumulator_carrier = 0;
    uint32_t phase_accumulator_am = 0;
    uint32_t phase_accumulator_fm = 0;

    // Метаморфоза несущей
    WaveTable buffer_morph[3];                  // 3 слота форм несущей, -1..1
    uint8_t  morph_slot = 0;                    // форма, из которой перетекаем (0..2)
    uint8_t  morph_slot_next = 0;               // форма, в которую перетекаем (0..2)
    uint32_t morph_phase = 0;                   // DDS-фаза шага, полный оборот = один шаг
    uint32_t morph_fade_phase = 0;              // окно кроссфейда Ступени, зафиксировано на шаг

    StructureCh() {
        buffer_master.fill(1.0f); // до загрузки формы — пропуск сигнала
    }
};


#endif //GENERATOR2_RENDERCHANNEL_H
