//
// Created by user on 28.05.2024.
//

#include "scope.h"


void Scope::calculatePixel() {

    float maxL;
    float maxR = h - 1.0f;
    float minR;
    if (is_one_two) {
        maxL = h - 1.0f;
        minR = 0.0f;
    } else {
        maxL = h / 2.0f;
        minR = h / 2.0f;
    }

    jsize temp1 = env->GetArrayLength(buf_rn) - 1;
    int temp2 = w - 1;
    int temp3 = 0;

    jfloat *bigPointnL = env->GetFloatArrayElements(big_pointn_l, nullptr);
    jfloat *bigPointnR = env->GetFloatArrayElements(big_pointn_r, nullptr);

    jfloat *bufRN = env->GetFloatArrayElements(buf_rn, nullptr);
    jfloat *bufLN = env->GetFloatArrayElements(buf_ln, nullptr);

    for (int x = start; x < end; x++) {
        int mapX = (x * temp1 / temp2);
        if (mapX < 0) mapX = 0;
        if (mapX > temp1) mapX = temp1;

        ////////
        for (int i = 0; i < max_pixel_buffer; i++) {
            int offset = mapX + i;
            if (offset > temp1) offset = temp1;
            temp3 = i * 2 + x * max_pixel_buffer * 2;
            *(bigPointnR + temp3) = x;
            *(bigPointnL + temp3) = x;
            *(bigPointnR + temp3 + 1) = (*(bufRN + offset) + 1.0f) * (maxR - minR) / 2.0f + minR;
            *(bigPointnL + temp3 + 1) = (*(bufLN + offset) + 1.0f) * maxL / 2.0f;
        }


    }


    env->ReleaseFloatArrayElements(buf_rn, bufRN, 0);
    env->ReleaseFloatArrayElements(buf_ln, bufLN, 0);







}
