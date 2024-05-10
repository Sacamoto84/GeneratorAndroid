//
// Created by user on 10.05.2024.
//

#ifndef GENERATOR2_TFT_H
#define GENERATOR2_TFT_H

#include <stdint.h>
#include <math.h>
#include <stdio.h>
#include <cstdint>
#include <android/bitmap.h>
#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

class TFT {
public:

    uint16_t WIDTH = 0;
    uint16_t HEIGHT = 0;

    uint16_t *buffer16 = NULL;

    uint16_t initialized = 0;

    void createBuffer(uint16_t w, uint16_t h){
        WIDTH = w;
        HEIGHT = h;
        buffer16 = new uint16_t[(w * h) + 1024];
        initialized = 1;
    }

    void setPixel(int16_t x, int16_t y, uint16_t color) {
        if ((x < 0) || (y < 0) || (x >= WIDTH) || (y >= HEIGHT))
            return;

        uint32_t l = x + (y * WIDTH);
        buffer16[l] = color;
    }

    uint16_t getPixel16(int32_t x, int32_t y) {
        if ((x < 0) || (y < 0) || (x >= WIDTH) || (y >= HEIGHT))
            return 0;
        return buffer16[x + y * WIDTH];
    }

    void fill(uint16_t color) {
        uint32_t Color = color * 65536 + color;
        uint32_t *p;
        p = (uint32_t*) &buffer16[0];

        uint32_t max = HEIGHT * WIDTH / 2 / 4 / 2 / 2;

        while (max--) {
            *p++ = Color;
            *p++ = Color;
            *p++ = Color;
            *p++ = Color;
            *p++ = Color;
            *p++ = Color;
            *p++ = Color;
            *p++ = Color;

            *p++ = Color;
            *p++ = Color;
            *p++ = Color;
            *p++ = Color;
            *p++ = Color;
            *p++ = Color;
            *p++ = Color;
            *p++ = Color;
        }
        return;
    }


    uint16_t alphaBlend(uint8_t alpha, uint16_t fgc, uint16_t bgc) {
        // For speed use fixed point maths and rounding to permit a power of 2 division
        uint16_t fgR = ((fgc >> 10) & 0x3E) + 1;
        uint16_t fgG = ((fgc >> 4) & 0x7E) + 1;
        uint16_t fgB = ((fgc << 1) & 0x3E) + 1;

        uint16_t bgR = ((bgc >> 10) & 0x3E) + 1;
        uint16_t bgG = ((bgc >> 4) & 0x7E) + 1;
        uint16_t bgB = ((bgc << 1) & 0x3E) + 1;

        // Shift right 1 to drop rounding bit and shift right 8 to divide by 256
        uint16_t r = (((fgR * alpha) + (bgR * (255 - alpha))) >> 9);
        uint16_t g = (((fgG * alpha) + (bgG * (255 - alpha))) >> 9);
        uint16_t b = (((fgB * alpha) + (bgB * (255 - alpha))) >> 9);

        // Combine RGB565 colours into 16 bits
        return (r << 11) | (g << 5) | (b << 0);
    }

};


#ifdef __cplusplus
}
#endif

#endif //GENERATOR2_TFT_H
