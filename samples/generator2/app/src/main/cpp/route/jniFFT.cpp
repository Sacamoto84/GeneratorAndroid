//
// Created by user on 26.06.2024.
//

#include <jni.h>
#include <cstring>
#include <memory>
#include <cmath>
#include <cstdio>
#include <cstdlib>
#include "../fft.h"
#include "../colormaps.h"
#include "../waterfall.h"
#include "../scale.h"
#include "../auformat.h"
#include "../ScaleBufferBase.h"
#include "../ScaleBuffer.h"
#include "../BufferAverage.h"
#include "audio_common.h"
#include <jni.h>
#include <ctime>
#include <algorithm>
#include <android/log.h>
#include <android/bitmap.h>
#include <pthread.h>
#include <semaphore.h>

#include "global.h"

#include "FloatRingBufferFFT.h"

#define LENPOINT 8192

/**
 * Кольцевой буфер для LR данных получемых из dataCompressor
 */
FloatRingBufferFFT ringBufferFft = FloatRingBufferFFT(262144);

ScaleBufferBase *pScaleL = nullptr;
ScaleBufferBase *pScaleR = nullptr;


//Частота дискретизации для все процов
static float sampleRate = 48000.0f;

myFFT *pProcessorL = new myFFT();
myFFT *pProcessorR = new myFFT();

float buf[LENPOINT * 2] = {};
float bufL[LENPOINT];
float bufR[LENPOINT];

Context context1;

void *loop1(void *init);

void ProcessChunk1();

ScaleBufferBase *GetScale(bool logX, bool logY);


bool isInitialized = false;

void initFTTLoop() {

    LOGE("!!! initFTTLoop");

    if (isInitialized)
        return;

    pProcessorL->init(LENPOINT, sampleRate);
    pProcessorR->init(LENPOINT, sampleRate);

    //Создать новый pScale
    pScaleL = new ScaleBufferLogLog();
    pScaleR = new ScaleBufferLogLog();

    //Настроить на ширину картинки и маминимальную и максимальную частоту
    pScaleL->setOutputWidth(1024, static_cast<float>(100), static_cast<float>(10000));
    pScaleR->setOutputWidth(1024, static_cast<float>(100), static_cast<float>(10000));

    pScaleL->PreBuild(pProcessorL);
    pScaleR->PreBuild(pProcessorR);

    context1.exit = false;
    sem_init(&context1.headwriteprotect, 0, 0);
    pthread_attr_init(&context1.attr);
    pthread_create(&context1.worker, &context1.attr, loop1, nullptr);
    isInitialized = true;
    LOGE("!!! initFTTLoop isInitialized = true");
}

void *loop1(void *init) {
    LOGE("!!! loop()");

    for (;;) {
        // wait for buffer
        double sem_start = now_ms();
        sem_wait(&context1.headwriteprotect);
        double sem_stop = now_ms();

        //Выход если Disconect
        if (context1.exit)
            break;

        double chunk_start = now_ms();
        ProcessChunk1();
        double chunk_stop = now_ms();

        context1.millisecondsWaitingInLoopSemaphore = sem_stop - sem_start;
        context1.millisecondsProcessingChunk = chunk_stop - chunk_start;

        //LOGD("!!! time %f ms", context1.millisecondsProcessingChunk);

    }

    return nullptr;

};

void ProcessChunk1() {

    //LOGD("!!! ProcessChunk1()");

    int iterationsPerChunk = 0;

    //В буфере нет нужного количества данных
    if (ringBufferFft.size() < (LENPOINT * 2)) {
        //LOGE("!!! ProcessChunk1() ringBufferFft.size() < LENPOINT * 2");
        return;
    }

    while (ringBufferFft.size() >= (LENPOINT * 2)) {

        ringBufferFft.peek(buf, LENPOINT * 2);

        if (!ringBufferFft.gotoNext(LENPOINT / 16)) {
            break;
        }

        for (int i = 0; i < LENPOINT; i++) {
            bufL[i] = buf[2 * i] * 20;
            bufR[i] = buf[2 * i + 1] * 20;
        }

        pProcessorL->convertFloatToFFT(&bufL[0], LENPOINT);
        pProcessorR->convertFloatToFFT(&bufR[0], LENPOINT);

        pProcessorL->computePower(static_cast<float>(context1.decay));
        pProcessorR->computePower(static_cast<float>(context1.decay));

        //Готовые данные после FTT
        BufferIODouble *bufferIO_L = pProcessorL->getBufferIO();
        BufferIODouble *bufferIO_R = pProcessorR->getBufferIO();

        if (bufferIO_L != nullptr) {
            context1.perfCounters.processedChunks++;

            if (pScaleL) {
                pthread_mutex_lock(&context1.scaleLock);

                pScaleL->Build(bufferIO_L, context1.volume);

                context1.waterFallRaw -= 1;

                if (context1.waterFallRaw < context1.barsHeight) {
                    context1.waterFallRaw = static_cast<int>(context1.info.height - 1);
                }

                // draw line
                if (context1.pixels != nullptr) {
                    drawWaterFallLine(&context1.info, context1.waterFallRaw, context1.pixels,
                                      pScaleL->GetBuffer());
                }

                pthread_mutex_unlock(&context1.scaleLock);
            }
        }

        iterationsPerChunk++;

    }

    context1.perfCounters.iterationsPerChunk = iterationsPerChunk;

}

////////////////////////////////////////////////////////////
/**
 * Запуск потока для работы с FFT, запускается один раз
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_startFFTLoop(JNIEnv *env, jobject) {
    initFTTLoop();
}

/**
 * Отправить порцию данных в буфер FloatRingBufferFFT
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_sentToFloatRingBufferFFT(JNIEnv *env, jobject thiz,
                                                                 jfloatArray buf, jint len) {

    ///LOGD("!!! sentToFloatRingBufferFFT..start");
    jfloat *point = env->GetFloatArrayElements(buf, nullptr);
    ringBufferFft.add(point, len);
    env->ReleaseFloatArrayElements(buf, point, 0);
    sem_post(&context1.headwriteprotect);
    //LOGE("!!! sentToFloatRingBufferFFT..end");
}


extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_Init(JNIEnv *env, jobject, jobject bitmap) {
    pthread_mutex_lock(&context1.scaleLock);

    int ret;
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &context1.info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &context1.pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    pthread_mutex_unlock(&context1.scaleLock);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_SetScaler(JNIEnv *env, jobject,
                                                  jint screenWidth,
                                                  jdouble minFreq,
                                                  jdouble maxFreq,
                                                  jboolean bLogX,
                                                  jboolean bLogY
) {
    pthread_mutex_lock(&context1.scaleLock);
    LOGE("Begin SetScaler");

    delete (pScaleL);
    delete (pScaleR);

    //Создать новый pScale
    pScaleL = GetScale(bLogX, bLogY);
    pScaleR = GetScale(bLogX, bLogY);

    //Настроить на ширину картинки и маминимальную и максимальную частоту
    pScaleL->setOutputWidth(screenWidth, static_cast<float>(minFreq), static_cast<float>(maxFreq));
    pScaleR->setOutputWidth(screenWidth, static_cast<float>(minFreq), static_cast<float>(maxFreq));

    pScaleL->PreBuild(pProcessorL);
    pScaleR->PreBuild(pProcessorR);

    LOGE("End   SetScaler");
    pthread_mutex_unlock(&context1.scaleLock);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_generator2_Spectrogram_Lock(JNIEnv *env, jobject, jobject bitmap) {
    pthread_mutex_lock(&context1.scaleLock);
//    LOGE("Begin Lock");

    if (pScaleL != nullptr) {
        drawSpectrumBars(&context1.info, context1.pixels, context1.barsHeight, pScaleL->GetBuffer());
    }

    AndroidBitmap_unlockPixels(env, bitmap);
//    LOGE("End   Lock");

    return context1.waterFallRaw;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_Unlock(JNIEnv *env, jobject, jobject bitmap) {
//    LOGE("Begin Unlock");
    int ret;
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &context1.info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &context1.pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

//    LOGE("End   Unlock");
    pthread_mutex_unlock(&context1.scaleLock);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_generator2_Spectrogram_GetDebugInfo(JNIEnv *env, jobject) {
    char sout[1024];
    char *pOut = sout;
    static double last_time = now_ms();

    PerfCounters *pC = &context1.perfCounters;
    pOut += sprintf(pOut, "Buffers:\n");
    pOut += sprintf(pOut, " - Recorded %i\n", pC->recordedChunks);
    pOut += sprintf(pOut, " - Dropped %i\n", pC->droppedBuffers);
    pOut += sprintf(pOut, "FFTs %i\n", pC->processedChunks);
    pOut += sprintf(pOut, "iterations Per Chunk %i\n", pC->iterationsPerChunk);
    pOut += sprintf(pOut, "Queue sizes:\n");
//    pOut += sprintf(pOut, "- Recorded %i\n",
//                    (context1.pRecQueue != nullptr) ? context1.pRecQueue->size() : 0);
//    pOut += sprintf(pOut, "- Free %i\n",
//                    (context1.pFreeQueue != nullptr) ? context1.pFreeQueue->size() : 0);
    pOut += sprintf(pOut, "Timings:\n");
    pOut += sprintf(pOut, " - Waiting for audio %.1f ms\n",
                    context1.millisecondsWaitingInLoopSemaphore);
    pOut += sprintf(pOut, " - Processing chunk %.2fms\n", context1.millisecondsProcessingChunk);
    //pOut += sprintf(pOut, "Progress: %i %%\n", bufferAverage.getProgress());

    return env->NewStringUTF(sout);
}


/**
 * Задать высоту в пикселях бара падающей волны, по умолчанию 500
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_setBarsHeight(JNIEnv *env, jobject,
                                                      jint barsHeight_) {
    context1.barsHeight = barsHeight_;
    context1.waterFallRaw = barsHeight_;
}

/**
 * Задать частоту дискретизации для процессора
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_setSampleRate(JNIEnv *env, jobject, jint samplerate) {
    if (pProcessorL != nullptr){
        pProcessorL->m_sampleRate = static_cast<float >(samplerate);
    }

    if (pProcessorR != nullptr){
        pProcessorR->m_sampleRate = static_cast<float >(samplerate);
    }
}

ScaleBufferBase *GetScale(bool logX, bool logY) {
    if (logX && logY)
        return new ScaleBufferLogLog();
    if (!logX && !logY)
        return new ScaleBufferLinLin();
    if (logX)
        return new ScaleBufferLogLin();
    if (logY)
        return new ScaleBufferLinLog();

    return nullptr;
}