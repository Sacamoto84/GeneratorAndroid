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

// Режим метаморфозы, зеркало MORPH_MODE_* из CarrierMorph.kt
static const int MORPH_MODE_SMOOTH_C = 1;

/** Первый активный слот в маске. Вызывается только при mask != 0. */
static inline uint8_t firstMorphSlot(int mask) {
    for (uint8_t s = 0; s < 3; s++) if (mask & (1 << s)) return s;
    return 0;
}

/** Следующий активный слот по кругу. При единственном активном возвращает его же. */
static inline uint8_t nextMorphSlot(uint8_t slot, int mask) {
    for (int i = 1; i <= 3; i++) {
        auto s = (uint8_t) ((slot + i) % 3);
        if (mask & (1 << s)) return s;
    }
    return slot;
}

/**
 * Метаморфоза несущей: держит пару таблиц (текущая и следующая форма)
 * и продвигает состояние на каждый сэмпл.
 * Когда метаморфоза выключена, обе таблицы — обычный buffer_carrier,
 * коэффициент смешивания нулевой, поведение точно как раньше.
 */
struct MorphRunner {
    StructureCh *s = nullptr;
    bool on = false;
    bool smooth = false;
    int mask = 0;
    uint32_t steps = 1;
    float inv_steps = 0.0f;
    const float *pA = nullptr;
    const float *pB = nullptr;

    void init(StructureCh *ch, bool en, int mode, int st, int msk) {
        s = ch;
        mask = msk;
        on = en && msk != 0;
        smooth = (mode == MORPH_MODE_SMOOTH_C);
        steps = (uint32_t) (st > 0 ? st : 1);
        inv_steps = 1.0f / (float) steps;
        refresh();
    }

    void refresh() {
        if (!on) {
            pA = s->buffer_carrier;
            pB = s->buffer_carrier;
            return;
        }
        pA = s->buffer_morph[s->morph_slot];
        pB = smooth ? s->buffer_morph[nextMorphSlot(s->morph_slot, mask)] : pA;
    }

    /** Значение несущей по фазовому индексу + продвижение состояния на сэмпл. */
    inline float sample(uint32_t idx, bool wrapped) {
        float t = 0.0f;
        if (on && smooth) {
            t = (float) s->morph_counter * inv_steps;
            if (t > 1.0f) t = 1.0f;
        }
        float c = pA[idx] + t * (pB[idx] - pA[idx]);
        if (on) advance(wrapped);
        return c;
    }

    inline void advance(bool wrapped) {
        // Слот мог погаснуть, пока играли — уходим с него, но только по обороту фазы
        bool slot_invalid = !(mask & (1 << s->morph_slot));

        if (smooth && !slot_invalid) {
            if (++s->morph_counter >= steps) {
                s->morph_counter = 0;
                s->morph_slot = nextMorphSlot(s->morph_slot, mask);
                refresh();
            }
            return;
        }

        if (!slot_invalid && s->morph_counter < steps) {
            s->morph_counter++;
            return;
        }

        if (wrapped) {
            s->morph_counter = 0;
            s->morph_slot = slot_invalid ? firstMorphSlot(mask)
                                         : nextMorphSlot(s->morph_slot, mask);
            refresh();
        }
    }
};


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

                                                                              jboolean master_en,
                                                                              jint master_mode,
                                                                              jint r_master,
                                                                              jint on_samples,
                                                                              jint off_samples,
                                                                              jboolean button_active,
                                                                              jboolean button_pressed,

                                                                              jboolean morph_en,
                                                                              jint morph_mode,
                                                                              jint morph_steps,
                                                                              jint morph_mask,

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

    MorphRunner morph;
    morph.init(pStructureCh, morph_en, morph_mode, morph_steps, morph_mask);

    if (!en_fm && !en_am) {
        for (int i = 0; i < num_frames; i++) {
            uint32_t prev = pStructureCh->phase_accumulator_carrier;
            pStructureCh->phase_accumulator_carrier += r_c32;
            bool wrapped = pStructureCh->phase_accumulator_carrier < prev;
            uint32_t idx = pStructureCh->phase_accumulator_carrier >> 22;
            tempArrayElements[i] = volume * morph.sample(idx, wrapped);
        }
    }

    if (!en_fm && en_am) {
        for (int i = 0; i < num_frames; i++) {
            uint32_t prev = pStructureCh->phase_accumulator_carrier;
            pStructureCh->phase_accumulator_carrier += r_c32;
            bool wrapped = pStructureCh->phase_accumulator_carrier < prev;
            uint32_t idx = pStructureCh->phase_accumulator_carrier >> 22;

            pStructureCh->phase_accumulator_am += r_am32;
            tempArrayElements[i] = volume * morph.sample(idx, wrapped)
                                   * (pStructureCh->buffer_am[pStructureCh->phase_accumulator_am >> 22] * am_depth + 1.0f - am_depth);
        }
    }

    if (en_fm && !en_am) {

        for (int i = 0; i < num_frames; i++) {
            pStructureCh->phase_accumulator_fm += r_fm32;

            uint32_t prev = pStructureCh->phase_accumulator_carrier;

            pStructureCh->phase_accumulator_carrier +=
                    static_cast<unsigned int>(convertHzToR(
                            pStructureCh->buffer_fm[pStructureCh->phase_accumulator_fm >> 22], sampleRate));

            bool wrapped = pStructureCh->phase_accumulator_carrier < prev;
            uint32_t idx = pStructureCh->phase_accumulator_carrier >> 22;

            tempArrayElements[i] = volume * morph.sample(idx, wrapped);
        }
    }

    if (en_fm && en_am) {

        for (int i = 0; i < num_frames; i++) {

            pStructureCh->phase_accumulator_fm += r_fm32;

            delta = MAX32/sampleRate;

            uint32_t prev = pStructureCh->phase_accumulator_carrier;
            pStructureCh->phase_accumulator_carrier += static_cast<uint32_t>(static_cast<float>(delta) * pStructureCh->buffer_fm[pStructureCh->phase_accumulator_fm >> 22]);
            bool wrapped = pStructureCh->phase_accumulator_carrier < prev;
            uint32_t idx = pStructureCh->phase_accumulator_carrier >> 22;

            pStructureCh->phase_accumulator_am += r_am32;

            tempArrayElements[i] = volume * morph.sample(idx, wrapped) *
                           (pStructureCh->buffer_am[pStructureCh->phase_accumulator_am >> 22] * am_depth + 1.0f - am_depth);
        }

    }

    // Мастер-громкость: огибающая 0..1 поверх канала, с фейдом ~5 мс от щелчков
    float master_step = 1.0f / (0.005f * (float) sample_rate);
    for (int i = 0; i < num_frames; i++) {
        float target;
        if (button_active) {
            target = button_pressed ? 1.0f : 0.0f;          // общий оверрайд обоих каналов
        } else if (!master_en) {
            target = 1.0f;
        } else if (master_mode == 1) {                       // Плавный
            target = pStructureCh->buffer_master[pStructureCh->phase_accumulator_master >> 22];
            pStructureCh->phase_accumulator_master += (uint32_t) r_master;
        } else if (master_mode == 2) {                       // Вкл/Выкл
            target = pStructureCh->master_onoff_on ? 1.0f : 0.0f;
            uint32_t limit = pStructureCh->master_onoff_on ? (uint32_t) on_samples
                                                           : (uint32_t) off_samples;
            if (++pStructureCh->master_onoff_counter >= limit) {
                pStructureCh->master_onoff_on = !pStructureCh->master_onoff_on;
                pStructureCh->master_onoff_counter = 0;
            }
        } else {                                             // Кнопка (mode==3): недостижимо при button_active==false
            target = button_pressed ? 1.0f : 0.0f;
        }

        float g = pStructureCh->master_current_gain;
        float d = target - g;
        if (d > master_step) d = master_step;
        else if (d < -master_step) d = -master_step;
        g += d;
        pStructureCh->master_current_gain = g;

        tempArrayElements[i] *= g;
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
        case 3 : {
            destination = pStructureCh->buffer_master;
            break;
        }
        case 4 :
        case 5 :
        case 6 : {
            destination = pStructureCh->buffer_morph[modulation - 4];
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