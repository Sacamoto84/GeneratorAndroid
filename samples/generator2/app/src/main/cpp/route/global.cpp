//
// Created by user on 26.06.2024.
//

#include <jni.h>
#include <cstring>
#include <memory>

#include "FloatRingBufferFFT.h"

double now_ms() {
    struct timeval tv{};
    gettimeofday(&tv, nullptr);
    return tv.tv_sec * 1000. + tv.tv_usec / 1000.;
}
