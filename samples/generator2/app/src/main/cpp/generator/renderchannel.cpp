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

// Длительность кроссфейда Ступени, секунд. Столько же берёт анти-щелчок мастер-громкости
// ниже по файлу, и так же считается от частоты дискретизации: приложение переключается
// на 192 кГц там, где устройство тянет, и фиксированное число сэмплов дало бы 1.25 мс.
static const float MORPH_FADE_SEC = 0.005f;

/** Следующий активный слот по кругу. При единственном активном возвращает его же. */
static inline uint8_t nextMorphSlot(uint8_t slot, int mask) {
    for (int i = 1; i <= 3; i++) {
        auto s = (uint8_t) ((slot + i) % 3);
        if (mask & (1 << s)) return s;
    }
    return slot;
}

/**
 * Метаморфоза несущей: держит пару таблиц (форма, из которой перетекаем, и форма,
 * в которую) и продвигает фазу шага на каждый сэмпл.
 *
 * Шаг считается своей DDS-фазой: полный оборот morph_phase = один шаг длиной T.
 * Поэтому смена T во время генерации не сдвигает коэффициент смешивания — меняется
 * прирост фазы, а сама фаза остаётся на месте.
 *
 * Плавно:  t идёт линейно 0..1 через весь шаг.
 * Ступень: t держится на нуле почти весь шаг, потом за ~5 мс уходит в единицу.
 *          На слух это мгновенное переключение, но щелчка нет ни для какой пары
 *          форм — таблицы библиотеки начинаются с разных значений (§4.4 спеки).
 *
 * Когда метаморфоза выключена, обе таблицы — обычный buffer_carrier, коэффициент
 * нулевой, поведение точно как раньше.
 */
struct MorphRunner {
    StructureCh *ch = nullptr;
    bool on = false;
    bool smooth = false;
    int mask = 0;

    uint32_t inc = 1;               // прирост фазы шага на сэмпл
    uint32_t fade_phase_wanted = 1; // окно кроссфейда по текущему T, вступит в силу на границе
    uint32_t fade_start = 0;        // фаза, с которой начинается кроссфейд Ступени
    float inv_fade = 0.0f;          // 1 / длина кроссфейда в единицах фазы

    const float *pA = nullptr;
    const float *pB = nullptr;

    void init(StructureCh *c, bool en, int mode, int steps, int msk, int sample_rate) {
        ch = c;
        mask = msk;
        on = en && msk != 0;
        smooth = (mode == MORPH_MODE_SMOOTH_C);

        auto st = (uint32_t) (steps > 0 ? steps : 1);
        inc = (uint32_t) (MAX32 / st);
        if (inc == 0) inc = 1;

        auto fade = (uint32_t) (MORPH_FADE_SEC * (float) sample_rate);
        if (fade == 0) fade = 1;
        if (fade > st) fade = st;
        fade_phase_wanted = (uint32_t) ((uint64_t) fade * inc);
        if (fade_phase_wanted == 0) fade_phase_wanted = 1;

        if (on) {
            // Идти некуда: и текущая форма, и цель вне маски. Снимаемся на активную сразу —
            // доигрывать нечего, а ждать границы значит держать снятую пользователем форму
            // (при T до 100 с это надолго).
            if (!(mask & (1 << ch->morph_slot)) && !(mask & (1 << ch->morph_slot_next))) {
                ch->morph_slot = nextMorphSlot(ch->morph_slot, mask);
                ch->morph_slot_next = ch->morph_slot;
                ch->morph_phase = 0;
            }

            // Цель ещё не задана: либо только что включили метаморфозу, либо активным был
            // один слот, а стало больше. Шаг начинаем с нуля — иначе подстановка новой цели
            // при уже набежавшем t дала бы скачок.
            if (ch->morph_slot_next == ch->morph_slot) {
                uint8_t n = nextMorphSlot(ch->morph_slot, mask);
                if (n != ch->morph_slot) {
                    ch->morph_slot_next = n;
                    ch->morph_phase = 0;
                }
            }
        }

        // Новая длина шага действует сразу, а окно кроссфейда — только со следующего шага.
        // Иначе сдвиг окна под неподвижной фазой скачком меняет t: смена T в момент, когда
        // фаза уже внутри окна, дала бы щелчок. В начале шага t всё равно ноль, там можно.
        if (ch->morph_fade_phase == 0 || ch->morph_phase == 0)
            ch->morph_fade_phase = fade_phase_wanted;
        applyFade();

        refresh();
    }

    /** Пересчитать границы кроссфейда по зафиксированному на шаг окну. */
    void applyFade() {
        if (ch->morph_fade_phase == 0) ch->morph_fade_phase = 1;
        fade_start = (uint32_t) (MAX32 - ch->morph_fade_phase);
        inv_fade = 1.0f / (float) ch->morph_fade_phase;
    }

    /** Подтянуть указатели к текущей паре слотов. */
    void refresh() {
        if (!on) {
            pA = ch->buffer_carrier.read();
            pB = pA;
            return;
        }
        pA = ch->buffer_morph[ch->morph_slot].read();
        pB = ch->buffer_morph[ch->morph_slot_next].read();
    }

    /** Значение несущей по фазовому индексу + продвижение шага на сэмпл. */
    inline float sample(uint32_t idx) {
        float t = 0.0f;
        if (on) {
            if (smooth) {
                t = (float) ch->morph_phase * (1.0f / 4294967296.0f);
            } else if (ch->morph_phase >= fade_start) {
                t = (float) (ch->morph_phase - fade_start) * inv_fade;
                if (t > 1.0f) t = 1.0f;
            }
        }

        float c = pA[idx] + t * (pB[idx] - pA[idx]);

        if (on) advance();
        return c;
    }

    /** Продвинуть фазу шага; на переполнении взять следующую пару форм. */
    inline void advance() {
        uint32_t prev = ch->morph_phase;
        ch->morph_phase += inc;
        if (ch->morph_phase < prev) {
            // Форма, в которую перетекали, становится текущей — поэтому стыка нет:
            // в конце шага играла ровно она при t→1, в начале следующего играет она же при t=0
            ch->morph_slot = ch->morph_slot_next;
            ch->morph_slot_next = nextMorphSlot(ch->morph_slot, mask);
            ch->morph_fade_phase = fade_phase_wanted;   // новое T вступает в силу здесь
            applyFade();
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
    morph.init(pStructureCh, morph_en, morph_mode, morph_steps, morph_mask, sample_rate);

    // Указатели на таблицы берём по одному на блок: пока идёт рендер, sendBuffer
    // может готовить новую форму, но опубликует её только целиком и только к
    // следующему блоку
    const float *table_am = pStructureCh->buffer_am.read();
    const float *table_fm = pStructureCh->buffer_fm.read();
    const float *table_master = pStructureCh->buffer_master.read();

    if (!en_fm && !en_am) {
        for (int i = 0; i < num_frames; i++) {
            pStructureCh->phase_accumulator_carrier += r_c32;
            tempArrayElements[i] = volume * morph.sample(pStructureCh->phase_accumulator_carrier >> 22);
        }
    }

    if (!en_fm && en_am) {
        for (int i = 0; i < num_frames; i++) {
            pStructureCh->phase_accumulator_carrier += r_c32;

            pStructureCh->phase_accumulator_am += r_am32;
            tempArrayElements[i] = volume * morph.sample(pStructureCh->phase_accumulator_carrier >> 22)
                                   * (table_am[pStructureCh->phase_accumulator_am >> 22] * am_depth + 1.0f - am_depth);
        }
    }

    if (en_fm && !en_am) {

        for (int i = 0; i < num_frames; i++) {
            pStructureCh->phase_accumulator_fm += r_fm32;

            pStructureCh->phase_accumulator_carrier +=
                    static_cast<unsigned int>(convertHzToR(
                            table_fm[pStructureCh->phase_accumulator_fm >> 22], sampleRate));

            tempArrayElements[i] = volume * morph.sample(pStructureCh->phase_accumulator_carrier >> 22);
        }
    }

    if (en_fm && en_am) {

        for (int i = 0; i < num_frames; i++) {

            pStructureCh->phase_accumulator_fm += r_fm32;

            delta = MAX32/sampleRate;

            pStructureCh->phase_accumulator_carrier += static_cast<uint32_t>(static_cast<float>(delta) * table_fm[pStructureCh->phase_accumulator_fm >> 22]);
            pStructureCh->phase_accumulator_am += r_am32;

            tempArrayElements[i] = volume * morph.sample(pStructureCh->phase_accumulator_carrier >> 22) *
                           (table_am[pStructureCh->phase_accumulator_am >> 22] * am_depth + 1.0f - am_depth);
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
            target = table_master[pStructureCh->phase_accumulator_master >> 22];
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

    WaveTable *destination = nullptr;

    switch (modulation) {
        case 0 : {
            destination = &pStructureCh->buffer_carrier;
            break;
        }
        case 1 : {
            destination = &pStructureCh->buffer_am;
            break;
        }
        case 2 : {
            destination = &pStructureCh->buffer_fm;
            break;
        }
        case 3 : {
            destination = &pStructureCh->buffer_master;
            break;
        }
        case 4 :
        case 5 :
        case 6 : {
            destination = &pStructureCh->buffer_morph[modulation - 4];
            break;
        }

        default:
            break;
    }

    if (destination == nullptr) return;

    if (env->GetArrayLength(data) < WAVE_TABLE_SIZE) return;

    jfloat *elements = env->GetFloatArrayElements(data, nullptr);
    if (elements == nullptr) return;

    // Форма уезжает в свободный слот и публикуется целиком: рендер её увидит
    // только со следующего блока
    destination->write(elements);

    // JNI_ABORT: массив мы не меняли, копировать обратно нечего
    env->ReleaseFloatArrayElements(data, elements, JNI_ABORT);

}