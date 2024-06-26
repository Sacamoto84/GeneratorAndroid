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
#include "../Goertzel.h"
#include "../colormaps.h"
#include "../waterfall.h"
#include "../scale.h"
#include "../auformat.h"
#include "../ScaleBufferBase.h"
#include "../ScaleBuffer.h"
#include "../ChunkerProcessor.h"
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

/**
 * Кольцевой буфер для LR данных получемых из dataCompressor
 */
FloatRingBufferFFT ringBufferFft = FloatRingBufferFFT(262144);

ScaleBufferBase *pScaleL = nullptr;
ScaleBufferBase *pScaleR = nullptr;

static float sampleRate = 48000.0f;


auto *pFFTL = new myFFT();
auto *pFFTR = new myFFT();

Processor *pProcessorL = pFFTL;
Processor *pProcessorR = pFFTR;

Context context1;

void *loop1(void *init);

void ProcessChunk1();

#define LENPOINT 8192

bool isInitialized = false;

void initFTTLoop() {

    if (isInitialized)
        return;
    pProcessorL->init(LENPOINT, sampleRate);
    pProcessorR->init(LENPOINT, sampleRate);

    context1.exit = false;
    sem_init(&context1.headwriteprotect, 0, 0);
    pthread_attr_init(&context1.attr);
    pthread_create(&context1.worker, &context1.attr, loop1, nullptr);
    isInitialized = true;

}

void *loop1(void *init) {
    LOGE("loop()");

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
    }

    return nullptr;

};

void ProcessChunk1() {
    int iterationsPerChunk = 0;

    //В буфере нет нужного количества данных
    if (ringBufferFft.size() < LENPOINT * 2)
        return;

    float buf[LENPOINT * 2] = {};
    ringBufferFft.read(buf, LENPOINT * 2);

    float bufL[LENPOINT];
    float bufR[LENPOINT];

    for (int i = 0; i < LENPOINT; i++) {
        bufL[i] = buf[2 * i];
        bufR[i] = buf[2 * i + 1];
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

    context1.perfCounters.iterationsPerChunk = iterationsPerChunk;

}



////////////////////////////////////////////////////////////
/**
 * Запуск потока для работы с FFT, запускается один раз
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_startFFTLoop(JNIEnv *env, jobject) {
    // TODO: implement StartFFTLoop()
    initFTTLoop();
}
