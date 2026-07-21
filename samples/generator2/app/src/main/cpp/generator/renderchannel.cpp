//
// Created by user on 12.06.2024.
//
#include <jni.h>
#include "renderchannel.h"
#include <cstdlib> // для alloca
#include <omp.h>
#include <chrono>
#include <android/log.h>

#define LOG_TAG "MyJNIModule"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

StructureCh structureCh[2];

uint64_t MAX32 = 4294967296;

inline float convertHzToR(float hz, uint32_t sampleRate) {
//    return (48000.0f / (float)sampleRate) *
//           (hz * 16384.0f / 3.798f * 2.0f * 1000.0f / 48.8f / 2.0f * 1000.0f / 988.0f);
//    return (48000.0f * 89499.347f * hz / sampleRate);

    return (float)(MAX32/ sampleRate)  * hz ;
}

float map(float x, float in_min, float in_max, float out_min, float out_max) {
    return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_generator_RenderChannel_jniRenderChannel(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jobject ch,
                                                                              jint num_frames,
                                                                              jint sample_rate,
        //,
                                                                              jint r_c,
                                                                              jint r_am,
                                                                              jint r_fm,
                                                                              jboolean en_ch,
                                                                              jboolean en_am,
                                                                              jboolean en_fm,
                                                                              jfloat volume,
                                                                              jfloat am_depth,

                                                                              jint channel,
                                                                              jfloatArray m_buffer

) {

    if (!en_ch) {
        return;
    }

    if (num_frames <= 0)
        throw std::runtime_error("num_frames <= 0");

    // Начало измерения времени
    auto start = std::chrono::high_resolution_clock::now();

    // Создание нового массива jfloatArray
    //jfloatArray floatArray = env->NewFloatArray(num_frames);

    // Конец измерения времени
//    auto end = std::chrono::high_resolution_clock::now();
//    std::chrono::duration<double, std::micro> elapsed = end - start;
//    // Логирование времени выполнения
//    LOGI("Elapsed time: %.1f us", elapsed.count());


//
    StructureCh *pStructureCh = &structureCh[channel];
//
//    //float tempArrayElements[num_frames];
//    //auto* tempArrayElements = new float[num_frames];
//
    std::unique_ptr<float[]> tempArrayElements = std::make_unique<float[]>(num_frames);
//
    auto r_fm32 = (uint32_t) r_fm;
    auto r_am32 = (uint32_t) r_am;
    auto r_c32 = (uint32_t) r_c;
//
    auto sampleRate = static_cast<uint64_t>(sample_rate);

    uint64_t delta = 0;

    if (!en_fm && !en_am) {
        for (int i = 0; i < num_frames; i++) {
            pStructureCh->phase_accumulator_carrier += r_c32;
            tempArrayElements[i] = volume * pStructureCh->buffer_carrier[pStructureCh->phase_accumulator_carrier >> 22];
        }
    }

    if (!en_fm && en_am) {
        for (int i = 0; i < num_frames; i++) {
            pStructureCh->phase_accumulator_carrier += r_c32;

            pStructureCh->phase_accumulator_am += r_am32;
            tempArrayElements[i] = volume * pStructureCh->buffer_carrier[pStructureCh->phase_accumulator_carrier >> 22]
                                   * (pStructureCh->buffer_am[pStructureCh->phase_accumulator_am >> 22] * am_depth + 1.0f - am_depth);
        }
    }

    if (en_fm && !en_am) {

        for (int i = 0; i < num_frames; i++) {
            pStructureCh->phase_accumulator_fm += r_fm32;

            pStructureCh->phase_accumulator_carrier +=

                    static_cast<unsigned int>(convertHzToR(
                    pStructureCh->buffer_fm[pStructureCh->phase_accumulator_fm >> 22], sampleRate)


                            );

            tempArrayElements[i] = volume * pStructureCh->buffer_carrier[pStructureCh->phase_accumulator_carrier >> 22];
        }
    }

    if (en_fm && en_am) {

        for (int i = 0; i < num_frames; i++) {

            pStructureCh->phase_accumulator_fm += r_fm32;

            delta = MAX32/sampleRate;

            pStructureCh->phase_accumulator_carrier += static_cast<uint32_t>(static_cast<float>(delta) * pStructureCh->buffer_fm[pStructureCh->phase_accumulator_fm >> 22]);   //(unsigned int)(convertHzToR(pStructureCh->buffer_fm[pStructureCh->phase_accumulator_fm >> 22], sampleRate));
            pStructureCh->phase_accumulator_am += r_am32;

            tempArrayElements[i] = volume * pStructureCh->buffer_carrier[pStructureCh->phase_accumulator_carrier >> 22] *
                           (pStructureCh->buffer_am[pStructureCh->phase_accumulator_am >> 22] * am_depth + 1.0f - am_depth);
        }

    }

    // Заполнение jfloatArray данными из tempArray
    env->SetFloatArrayRegion(m_buffer, 0, num_frames, tempArrayElements.get());

    // Конец измерения времени
    auto end = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double, std::micro> elapsed = end - start;
//    // Логирование времени выполнения
//    LOGI("Elapsed time: %.1f us", elapsed.count());
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_generator_RenderChannel_sendBuffer(JNIEnv *env, jobject thiz,
                                                                        jint ch,
                                                                        jint modulation,
                                                                        jfloatArray data

) {
    StructureCh *pStructureCh = &structureCh[ch];

    float *destination = nullptr;

    switch (modulation) {
        case 0 : {
            destination = pStructureCh->buffer_carrier;
            break;
        }
        case 1 : {
            destination = pStructureCh->buffer_am;
            break;
        }
        case 2 : {
            destination = pStructureCh->buffer_fm;
            break;
        }

        default:
            break;
    }

    jfloat *elements = env->GetFloatArrayElements(data, nullptr);
    for (int i = 0; i < 1024; i++) {
        destination[i] = elements[i];
    }
    env->ReleaseFloatArrayElements(data, elements, 0);

}