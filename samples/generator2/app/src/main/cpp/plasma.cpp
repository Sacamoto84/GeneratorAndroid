

#define _USE_MATH_DEFINES

#include <cmath>
#include <cstdio>
#include <cstdlib>
#include "fft.h"
#include "Goertzel.h"
#include "colormaps.h"
#include "waterfall.h"
#include "scale.h"
#include "auformat.h"
#include "ScaleBufferBase.h"
#include "ScaleBuffer.h"
#include "ChunkerProcessor.h"
#include "BufferAverage.h"
#include "audio_common.h"
#include <jni.h>
#include <ctime>
#include <algorithm>
#include <android/log.h>
#include <android/bitmap.h>
#include <pthread.h>
#include <semaphore.h>
#include "route/global.h"

Processor *pProcessorDeferred = nullptr;
Processor *pProcessor = nullptr;

ScaleBufferBase *pScale = nullptr;

BufferIODouble *m_pHoldedData = nullptr;

ChunkerProcessor chunker;

BufferAverage bufferAverage;

float sampleRate = 48000.0f;

void GetBufferQueues(float *pSampleRate, AudioQueue **pFreeQ, AudioQueue **pRecQ);

//static double now_ms() {
//    struct timeval tv{};
//    gettimeofday(&tv, nullptr);
//    return tv.tv_sec * 1000. + tv.tv_usec / 1000.;
//}

Context context;

void ProcessChunk() {
    int iterationsPerChunk = 0;

    //Из context.pRecQueue читаем данные которые будем помещять в  ChunkerProcessor.recQueue отдельными кусками

    // pass available buffers to processor
    {
        sample_buf *buf = nullptr;
        while (context.pRecQueue->front(&buf)) {
            context.pRecQueue->pop();
            //Помещаем в буфер данных        ChunkerProcessor::recQueue
            if (!chunker.pushAudioChunk(buf)) {
                //если нет места в буфере то увеличим счетчик пропущенных кусков
                context.perfCounters.droppedBuffers++;
            }
        }
    }

    //если у нас достаточно данных в очереди, обработайте FFT
    while (chunker.Process(pProcessor, context.decay, context.fractionOverlap)) {
        //Готовые данные после FTT
        BufferIODouble *bufferIO = pProcessor->getBufferIO();

        bufferIO = bufferAverage.Do(bufferIO);

        if (bufferIO != nullptr) {
            context.perfCounters.processedChunks++;

            if (pScale) {
                pthread_mutex_lock(&context.scaleLock);
                //LOGE("Begin DrawLine");

                pScale->Build(bufferIO, context.volume);

                // advance waterfall
                context.waterFallRaw -= 1;
                if (context.waterFallRaw < context.barsHeight) {
                    context.waterFallRaw = static_cast<int>(context.info.height - 1);
                }

                // draw line
                if (context.pixels != nullptr) {
                    drawWaterFallLine(&context.info, context.waterFallRaw, context.pixels,
                                      pScale->GetBuffer());
                }

                //LOGE("End   DrawLine");
                pthread_mutex_unlock(&context.scaleLock);
            }
        }

        iterationsPerChunk++;
    }

    context.perfCounters.iterationsPerChunk = iterationsPerChunk;

    // return processed buffers
    {
        sample_buf *buf = nullptr;
        while (chunker.getFreeBufferFrontAndPop(&buf)) {
            context.pFreeQueue->push(buf);
        }
    }

    // если процессор изменился, замените его сейчас
    if (pProcessorDeferred != nullptr) {
        pthread_mutex_lock(&context.scaleLock);
        delete pProcessor;
        pProcessor = pProcessorDeferred;
        pProcessorDeferred = nullptr;

        chunker.Reset();

        pScale->PreBuild(pProcessor);
        pthread_mutex_unlock(&context.scaleLock);
    }
}

void *loop(void *init) {
    LOGE("loop()");

    for (;;) {
        // wait for buffer
        double sem_start = now_ms();
        sem_wait(&context.headwriteprotect);
        double sem_stop = now_ms();

        //Выход если Disconect
        if (context.exit)
            break;

        double chunk_start = now_ms();
        ProcessChunk();
        double chunk_stop = now_ms();

        context.millisecondsWaitingInLoopSemaphore = sem_stop - sem_start;
        context.millisecondsProcessingChunk = chunk_stop - chunk_start;
    }

    return nullptr;
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

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_setProcessorFFT(JNIEnv *env, jobject, jint length) {
    auto *pFFT = new myFFT();
    pFFT->init(length, sampleRate);

    if (pProcessor == nullptr)
        pProcessor = pFFT;
    else
        pProcessorDeferred = pFFT;
}

/**
 * Задать частоту дискретизации для процессора
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_setSampleRate(JNIEnv *env, jobject, jint samplerate) {
    if (pProcessor != nullptr){
        pProcessor->m_sampleRate = static_cast<float >(samplerate);
    }
}

//extern "C" JNIEXPORT void JNICALL
//Java_com_example_generator2_Spectrogram_SetProcessorGoertzel(JNIEnv *env, jobject,
//                                                             jint length) {
//    Goertzel *pGoertzel = new Goertzel();
//    pGoertzel->setMaxMinNotes(1, 88);
//    pGoertzel->init(length, sampleRate);
//
//    if (pProcessor == nullptr)
//        pProcessor = pGoertzel;
//    else
//        pProcessorDeferred = pGoertzel;
//}


/**
 *  Длина массива данных (число точек, по которым будет выполняться преобразование Фурье). 4096
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_example_generator2_Spectrogram_getFftLength(JNIEnv *env, jobject) {
    return pProcessor->getProcessedLength();
}

/////////////////////////////////////////////////// get/sets ///////////////////////////////////////////////////////

/**
 * Задать высоту в пикселях бара падающей волны, по умолчанию 500
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_setBarsHeight(JNIEnv *env, jobject,
                                                      jint barsHeight_) {
    context.barsHeight = barsHeight_;
    context.waterFallRaw = barsHeight_;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_SetOverlap(JNIEnv *env, jobject,
                                                   jfloat timeOverlap_) {
    if (timeOverlap_ > 0.98f)
        timeOverlap_ = 0.98f;

    context.fractionOverlap = timeOverlap_;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_generator2_Spectrogram_GetOverlap(JNIEnv *env, jobject) {
    return context.fractionOverlap;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_SetDecay(JNIEnv *env, jobject, jfloat decay_) {
    context.decay = decay_;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_generator2_Spectrogram_GetDecay(JNIEnv *env, jobject) {
    return context.decay;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_SetVolume(JNIEnv *env, jobject obj, jfloat volume_) {
    context.volume = volume_;
}
extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_generator2_Spectrogram_GetVolume(JNIEnv *env, jobject obj) {
    return context.volume;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_SetAverageCount(JNIEnv *env, jobject, jint c) {
    bufferAverage.setAverageCount(c);
}
extern "C" JNIEXPORT jint JNICALL
Java_com_example_generator2_Spectrogram_GetAverageCount(JNIEnv *env, jobject) {
    return bufferAverage.getAverageCount();
}

extern "C" JNIEXPORT float JNICALL
Java_com_example_generator2_Spectrogram_FreqToX(JNIEnv *env, jobject, jdouble freq) {
    return pScale->FreqToX(static_cast<float>(freq));
}

extern "C" JNIEXPORT float JNICALL
Java_com_example_generator2_Spectrogram_XToFreq(JNIEnv *env, jobject, jdouble x) {
    return pScale->XtoFreq(static_cast<float>(x));
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_HoldData(JNIEnv *env, jobject) {
    pthread_mutex_lock(&context.scaleLock);
    if (m_pHoldedData == nullptr)
        m_pHoldedData = new BufferIODouble(pScale->GetBuffer()->GetSize());

    m_pHoldedData->copy(pScale->GetBuffer());
    pthread_mutex_unlock(&context.scaleLock);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_ClearHeldData(JNIEnv *env, jobject) {
    pthread_mutex_lock(&context.scaleLock);
    free(m_pHoldedData);
    m_pHoldedData = nullptr;
    pthread_mutex_unlock(&context.scaleLock);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_ResetScanline(JNIEnv *env, jobject) {
    pthread_mutex_lock(&context.scaleLock);
    context.waterFallRaw = context.info.height;
    pthread_mutex_unlock(&context.scaleLock);
}

/////////////////////////////////////////////////// Perf counter ///////////////////////////////////////////////////////

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_SetScaler(JNIEnv *env, jobject,
                                                  jint screenWidth,
                                                  jdouble minFreq,
                                                  jdouble maxFreq,
                                                  jboolean bLogX,
                                                  jboolean bLogY
) {
    pthread_mutex_lock(&context.scaleLock);
    LOGE("Begin SetScaler");

    delete (pScale);

    //Создать новый pScale
    pScale = GetScale(bLogX, bLogY);

    //Настроить на ширину картинки и маминимальную и максимальную частоту
    pScale->setOutputWidth(screenWidth, static_cast<float>(minFreq), static_cast<float>(maxFreq));

    pScale->PreBuild(pProcessor);

    LOGE("End   SetScaler");
    pthread_mutex_unlock(&context.scaleLock);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_generator2_Spectrogram_GetIterationsPerChunk(JNIEnv *env, jobject) {
    return context.perfCounters.iterationsPerChunk;
}

extern "C" JNIEXPORT double JNICALL
Java_com_example_generator2_Spectrogram_GetMillisecondsPerChunk(JNIEnv *env, jobject) {
    return context.millisecondsProcessingChunk;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_generator2_Spectrogram_GetDebugInfo(JNIEnv *env, jobject) {
    char sout[1024];
    char *pOut = sout;
    static double last_time = now_ms();

    PerfCounters *pC = &context.perfCounters;
    pOut += sprintf(pOut, "Buffers:\n");
    pOut += sprintf(pOut, " - Recorded %i\n", pC->recordedChunks);
    pOut += sprintf(pOut, " - Dropped %i\n", pC->droppedBuffers);
    pOut += sprintf(pOut, "FFTs %i\n", pC->processedChunks);
    pOut += sprintf(pOut, "iterations Per Chunk %i\n", pC->iterationsPerChunk);
    pOut += sprintf(pOut, "Queue sizes:\n");
    pOut += sprintf(pOut, "- Recorded %i\n",
                    (context.pRecQueue != nullptr) ? context.pRecQueue->size() : 0);
    pOut += sprintf(pOut, "- Free %i\n",
                    (context.pFreeQueue != nullptr) ? context.pFreeQueue->size() : 0);
    pOut += sprintf(pOut, "Timings:\n");
    pOut += sprintf(pOut, " - Waiting for audio %.1f ms\n",
                    context.millisecondsWaitingInLoopSemaphore);
    pOut += sprintf(pOut, " - Processing chunk %.2fms\n", context.millisecondsProcessingChunk);
    pOut += sprintf(pOut, "Progress: %i %%\n", bufferAverage.getProgress());

    return env->NewStringUTF(sout);
}


extern "C" JNIEXPORT int JNICALL
Java_com_example_generator2_Spectrogram_GetDroppedFrames(JNIEnv *env, jobject) {
    static int lastDroppedFrames = 0;
    int dp = context.perfCounters.droppedBuffers;
    int res = dp - lastDroppedFrames;
    lastDroppedFrames = dp;
    return res;
}

/////////////////////////////////////////////////// Connect/disconnect ///////////////////////////////////////////////////////

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_ConnectWithAudioMT(JNIEnv *env, jobject) {
    LOGE("chunker.begin();");

    chunker.begin();

    SetRecorderCallback([](void *pCTX, uint32_t msg, void *pData) -> bool {
        assert(msg == ENGINE_SERVICE_MSG_RECORDED_AUDIO_AVAILABLE);

        context.perfCounters.recordedChunks++;

        GetBufferQueues(&sampleRate, &context.pFreeQueue, &context.pRecQueue);

        sem_post(&context.headwriteprotect);
        return true;
    });

    context.exit = false;
    sem_init(&context.headwriteprotect, 0, 0);
    pthread_attr_init(&context.attr);
    pthread_create(&context.worker, &context.attr, loop, nullptr);
}

//Уничтожаем loop
extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_Disconnect(JNIEnv *env, jobject) {

    context.exit = true;
    sem_post(&context.headwriteprotect);
    pthread_join(context.worker, nullptr); // ждем завершения потока thread
    pthread_attr_destroy(&context.attr);
    sem_destroy(&context.headwriteprotect);

    LOGE("chunker.end();");
    chunker.end();

    AudioQueue *pFreeQueue = nullptr;
    AudioQueue *pRecQueue = nullptr;

    GetBufferQueues(&sampleRate, &pFreeQueue, &pRecQueue);

    sample_buf *buf = nullptr;
    while (chunker.getFreeBufferFrontAndPop(&buf)) {
        pFreeQueue->push(buf);
    }

    delete pProcessor;
    pProcessor = nullptr;

    delete pScale;
    pScale = nullptr;

    delete m_pHoldedData;
    m_pHoldedData = nullptr;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_Init(JNIEnv *env, jobject, jobject bitmap) {
    pthread_mutex_lock(&context.scaleLock);

    int ret;
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &context.info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &context.pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    pthread_mutex_unlock(&context.scaleLock);
}


extern "C" JNIEXPORT jint JNICALL
Java_com_example_generator2_Spectrogram_Lock(JNIEnv *env, jobject, jobject bitmap) {
    pthread_mutex_lock(&context.scaleLock);
//    LOGE("Begin Lock");

    if (pScale != nullptr) {
        drawSpectrumBars(&context.info, context.pixels, context.barsHeight, pScale->GetBuffer());

        if (m_pHoldedData != nullptr) {
            drawHeldData(&context.info, context.pixels, context.barsHeight, m_pHoldedData);
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
//    LOGE("End   Lock");

    return context.waterFallRaw;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_Unlock(JNIEnv *env, jobject, jobject bitmap) {
//    LOGE("Begin Unlock");
    int ret;
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &context.info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &context.pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

//    LOGE("End   Unlock");
    pthread_mutex_unlock(&context.scaleLock);
}




