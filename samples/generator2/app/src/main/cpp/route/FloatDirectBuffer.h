//
// Created by user on 07.07.2024.
//

#ifndef GENERATOR2_FLOATDIRECTBUFFER_H
#define GENERATOR2_FLOATDIRECTBUFFER_H

#include <jni.h>
#include <iostream>
#include <cstring>

#define BUFFER_SIZE 3000000

class FloatDirectBuffer {
public:
    float bigBuffer[BUFFER_SIZE];

    /**
     * Сброс поинтеров
     * @param _itemSize  размер одного пакета 1152*2
     * @param _itemCount текущий делитель 1..256
     */
    void clear(int _itemSize, int _itemCount) {
        // Заполняем массив нулями
        memset(bigBuffer, 0, BUFFER_SIZE * sizeof(float));
        rP = 0;
        wP = _itemCount * _itemSize;
        itemSize = _itemSize;
        itemCount = _itemCount;
    }


    void add(jfloat *data, jint len) {
        memcpy(bigBuffer + wP, data, len * sizeof(float));
        wP += len;
        if (wP > static_cast<int>(0.8f * BUFFER_SIZE)) {
            wP = itemCount * itemSize;
        }
    }

    float * read() {
        float * p = &bigBuffer[0] + wP;
        wP += itemSize;
        return p;
    }


private:

    int wP = 524288;
    int rP = 0;

    int itemSize = 0;  //Размер одного итема 1152*2, 2048, получаем в блоке add
    int itemCount = 0; //Количество итемов, 1..256

};

#endif //GENERATOR2_FLOATDIRECTBUFFER_H
