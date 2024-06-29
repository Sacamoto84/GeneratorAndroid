//
// Created by user on 26.06.2024.
//

#ifndef GENERATOR2_GLOBAL_H
#define GENERATOR2_GLOBAL_H

struct PerfCounters {
    // perf counters
    uint32_t recordedChunks = 0;
    uint32_t processedChunks = 0;
    uint32_t droppedBuffers = 0;
    uint32_t iterationsPerChunk = 0;
};

struct Context {
    pthread_attr_t attr{};
    pthread_t worker{};
    sem_t headwriteprotect{};

    AndroidBitmapInfo info{};
    void *pixels = nullptr;

    pthread_mutex_t scaleLock{};

    bool exit = false;

    float volume = 30;

    PerfCounters perfCounters;

    double millisecondsWaitingInLoopSemaphore = 0;
    double millisecondsProcessingChunk = 0;

    AudioQueue *pFreeQueue = nullptr;       // user
    AudioQueue *pRecQueue = nullptr;        // user

    float fractionOverlap = .5f; // 0 to 1
    float decay = .005f;

    int barsHeight = 150;
    int waterFallRaw = barsHeight;


    bool redoScale = false;
};

extern double now_ms();

#endif //GENERATOR2_GLOBAL_H
