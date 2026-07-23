//
// Created by user on 12.06.2024.
//

#ifndef GENERATOR2_RENDERCHANNEL_H
#define GENERATOR2_RENDERCHANNEL_H


#include <vector>

struct StructureCh {

    int ch = 0;

    float buffer_carrier[1024] = {0.0f};
    float buffer_am[1024] = {0.0f};
    float buffer_fm[1024] = {0.0f};

    float buffer_normalised_Fm[1024] = {0.0f};

    // Мастер-громкость
    float buffer_master[1024];              // форма Плавного, 0..1 (инициализируется 1.0)
    uint32_t phase_accumulator_master = 0;  // DDS Плавного
    float master_current_gain = 1.0f;       // текущий гейн для фейда
    uint32_t master_onoff_counter = 0;      // счётчик сэмплов Вкл/Выкл
    bool master_onoff_on = true;            // текущая фаза Вкл/Выкл (старт ON)

    uint32_t phase_accumulator_carrier = 0;
    uint32_t phase_accumulator_am = 0;
    uint32_t phase_accumulator_fm = 0;

    StructureCh() {
        for (float &v : buffer_master) v = 1.0f; // до загрузки формы — пропуск сигнала
    }
};


#endif //GENERATOR2_RENDERCHANNEL_H
