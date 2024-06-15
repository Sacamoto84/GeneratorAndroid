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

    uint32_t phase_accumulator_carrier = 0;
    uint32_t phase_accumulator_am = 0;
    uint32_t phase_accumulator_fm = 0;

};


#endif //GENERATOR2_RENDERCHANNEL_H
