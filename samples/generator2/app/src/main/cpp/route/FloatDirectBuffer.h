//
// Created by user on 07.07.2024.
//

#ifndef GENERATOR2_FLOATDIRECTBUFFER_H
#define GENERATOR2_FLOATDIRECTBUFFER_H

#include <jni.h>
#include <iostream>
#include <cstring>
#include <android/log.h>

#define MODULE_NAME "FLOAT_DIRECT_BUFFER"
#define LOGD(...) \
  __android_log_print(ANDROID_LOG_DEBUG, MODULE_NAME, __VA_ARGS__)
#define LOGE(...) \
  __android_log_print(ANDROID_LOG_ERROR, MODULE_NAME, __VA_ARGS__)


#define BUFFER_SIZE 3000000
#define THRESHOLD   2000000

class FloatDirectBuffer {
public:
    float bigBuffer[BUFFER_SIZE];

    /**
     * Сброс поинтеров
     * @param _itemSize  размер одного пакета 1152*2
     * @param _itemCount текущий делитель 1..256
     */
    void clear(int _itemSize, int _itemCount) {
        LOGD("!!! clear()");
        // Заполняем массив нулями
        memset(bigBuffer, 0, BUFFER_SIZE * sizeof(float));

        wP = window();

        itemSize = _itemSize;
        itemCount = _itemCount;
    }


    void add(jfloat *data, jint len, jint _itemCount) {

        //LOGD("!!! add len:%i count:%i wP:%i", len, _itemCount, wP);

        //Изменился размер 1..256
        if ((itemCount != _itemCount) || (itemSize != len)) {
            //LOGD("!!! add mod:%i", wP % itemSize);
            itemCount = _itemCount;
            itemSize = len;
            clear(len, _itemCount);
            memcpy(bigBuffer + wP, data, len * sizeof(float));
            wP += len;
        } else {
            //LOGD("!!! add mod:%i", wP % itemSize);
            memcpy(bigBuffer + wP, data, len * sizeof(float));
            wP += len;
            //Делаем зеркало
            if (wP >= THRESHOLD) {
                memcpy(bigBuffer + wPMirror, data, len * sizeof(float));
                wPMirror += len;

                if (wP >= THRESHOLD + window()) {
                    wP = window();
                    wPMirror = 0;
                    LOGD("!!! reset");
                }
            }
        }
    }

    float *read() {
        //LOGD("!!! read");
        int rP = wP - window();
        //auto mod = rP % itemSize;
        //LOGD("!!! read rP:%i, mod: %i", rP, mod);
        if (rP < 0) {
            rP = 0;
            LOGD("!!! read rP<0");
        }
        float *p = &bigBuffer[0] + rP;
        return p;
    }

    int window() {
        return itemSize * itemCount;
    }

private:

    int wP = 524288;
    int wPMirror = 0;

    int itemSize = 0;  //Размер одного итема 1152*2, 2048, получаем в блоке add
    int itemCount = 0; //Количество итемов, 1..256

};

#endif //GENERATOR2_FLOATDIRECTBUFFER_H
