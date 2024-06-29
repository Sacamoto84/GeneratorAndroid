

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

        //bufferIO = bufferAverage.Do(bufferIO);

        if (bufferIO != nullptr) {
            context.perfCounters.processedChunks++;

            if ((pScale != nullptr) && (context.pixels != nullptr)) {
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

//void *loop(void *init) {
//    LOGE("loop()");
//
//    return nullptr;
//
//    for (;;) {
//        // wait for buffer
//        double sem_start = now_ms();
//        sem_wait(&context.headwriteprotect);
//        double sem_stop = now_ms();
//
//        //Выход если Disconect
//        if (context.exit)
//            break;
//
//        double chunk_start = now_ms();
//        ProcessChunk();
//        double chunk_stop = now_ms();
//
//        context.millisecondsWaitingInLoopSemaphore = sem_stop - sem_start;
//        context.millisecondsProcessingChunk = chunk_stop - chunk_start;
//    }
//
//    return nullptr;
//}



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
    return 8192;
    //pProcessor->getProcessedLength();
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



extern "C" JNIEXPORT jint JNICALL
Java_com_example_generator2_Spectrogram_GetIterationsPerChunk(JNIEnv *env, jobject) {
    return context.perfCounters.iterationsPerChunk;
}

extern "C" JNIEXPORT double JNICALL
Java_com_example_generator2_Spectrogram_GetMillisecondsPerChunk(JNIEnv *env, jobject) {
    return context.millisecondsProcessingChunk;
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
//    LOGE("chunker.begin();");
//
//    chunker.begin();
//
//    SetRecorderCallback([](void *pCTX, uint32_t msg, void *pData) -> bool {
//        assert(msg == ENGINE_SERVICE_MSG_RECORDED_AUDIO_AVAILABLE);
//
//        context.perfCounters.recordedChunks++;
//
//        GetBufferQueues(&sampleRate, &context.pFreeQueue, &context.pRecQueue);
//
//        sem_post(&context.headwriteprotect);
//        return true;
//    });
//
//    context.exit = false;
//    sem_init(&context.headwriteprotect, 0, 0);
//    pthread_attr_init(&context.attr);
//    pthread_create(&context.worker, &context.attr, loop, nullptr);
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









