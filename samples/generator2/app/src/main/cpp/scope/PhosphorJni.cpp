//
// JNI-привязка фосфорного аккумулятора.
//

#include "PhosphorGrid.h"
#include "FloatDirectBuffer.h"

#include <algorithm>
#include <cmath>
#include <jni.h>

PhosphorGrid phosphorGrid;

extern AudioHistoryBuffer audioHistoryBuffer;

namespace {

/**
 * Канал синхронизации — левый. Включается только в моно, где второй канал
 * повторяет первый: кадр встаёт сразу по обоим. В стерео каналы независимы,
 * один фронт удержал бы только свой — второй продолжил бы плыть, и разъезд
 * двух картинок читается хуже, чем общий дрейф.
 */
constexpr int kTriggerChannel = 0;

/** Доля пика, ниже которой сигнал считается взведённым для фронта. */
constexpr float kTriggerArmFraction = 0.25f;

/** Пик тише этого — тишина, синхронизировать не по чему. */
constexpr float kTriggerMinPeak = 1e-3f;

/**
 * Ищет последний переход через ноль снизу вверх в запасе перед окном.
 *
 * Окно истории отсчитывается от позиции записи, а та двигается целыми
 * пакетами. Длина пакета не кратна периоду сигнала, поэтому фаза на левом
 * краю кадра каждый раз другая — картинка плывёт. Привязка левого края к
 * фронту убирает это ровно так же, как триггер настоящего осциллографа.
 *
 * Берётся именно последний фронт запаса: он ближе всех к голове записи,
 * значит кадр отстаёт от свежих данных не больше чем на период сигнала.
 *
 * @param interleaved начало запаса, чередующиеся сэмплы двух каналов.
 * @param searchFrames длина запаса в стереокадрах.
 * @return смещение в кадрах от начала запаса; searchFrames, если фронта нет —
 * тогда кадр берётся без синхронизации, прямо от головы.
 */
std::size_t findTriggerOffset(const float *interleaved,
                              std::size_t searchFrames) {
    if (interleaved == nullptr || searchFrames < 2) {
        return searchFrames;
    }

    float peak = 0.0f;
    for (std::size_t i = 0; i < searchFrames; ++i) {
        peak = std::max(peak,
                        std::fabs(interleaved[i * 2 + kTriggerChannel]));
    }
    if (peak < kTriggerMinPeak) {
        return searchFrames;
    }

    // Гистерезис: перед фронтом сигнал обязан уйти заметно ниже нуля. Без
    // него шум у самой оси даёт пачку переходов подряд, и кадр цепляется
    // каждый раз за другой — то же дрожание, только мельче.
    const float armLevel = -peak * kTriggerArmFraction;

    std::size_t found = searchFrames;
    bool armed = false;
    for (std::size_t i = 0; i < searchFrames; ++i) {
        const float value = interleaved[i * 2 + kTriggerChannel];
        if (value < armLevel) {
            armed = true;
        } else if (armed && value >= 0.0f) {
            found = i;
            armed = false;
        }
    }
    return found;
}

int requestedColumns = 0;
int requestedLayout = 0;
bool requestedRollMode = true;
float requestedSweep = 1.0f;
bool requestedTrigger = false;
unsigned lastRebuiltSerial = 0;
bool hasRebuilt = false;

/**
 * Пересобирает конфигурацию, когда меняются запрошенные параметры или
 * размер окна истории. Окно известно только после первого аудиопакета,
 * поэтому проверка выполняется на каждом обращении.
 */
void ensureConfigured() {
    if (requestedColumns <= 0) {
        return;
    }
    std::size_t frames = audioHistoryBuffer.window() / 2;
    if (frames == 0) {
        return;
    }

    // Развёртка меньше единицы означает часть одного пакета. Сам буфер
    // истории дробить нельзя, он считает пакетами и хранит минимум один,
    // поэтому берём его хвост нужной длины.
    if (requestedSweep < 1.0f) {
        frames = static_cast<std::size_t>(
                static_cast<float>(frames) * requestedSweep);
        if (frames < 2) {
            frames = 2;
        }
    }

    phosphorGrid.configure(requestedColumns, requestedLayout, frames,
                           requestedRollMode);
}

} // namespace

extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativePhosphor_configure(
        JNIEnv * /* env */, jobject /* thiz */, jint columns, jint layout,
        jboolean rollMode, jfloat sweep, jboolean trigger) {
    requestedColumns = columns;
    requestedLayout = layout;
    requestedRollMode = (rollMode == JNI_TRUE);
    requestedSweep = sweep;
    // Сетку не трогает: триггер выбирает окно на стороне вызова rebuild(),
    // геометрия от него не зависит. Поэтому ensureConfigured() ниже увидит
    // прежние параметры и не станет чистить накопленное.
    requestedTrigger = (trigger == JNI_TRUE);
    ensureConfigured();
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_example_generator2_features_scope_NativePhosphor_update(
        JNIEnv *env, jobject /* thiz */) {
    ensureConfigured();

    if (phosphorGrid.isReady() && !phosphorGrid.isRollMode()) {
        // Пересобирать имеет смысл только когда пришли новые сэмплы: экран
        // обновляется втрое чаще, чем приходят пакеты, а каждая пересборка
        // это полная сетка в текстуру.
        const unsigned serial = phosphorGrid.packetSerial();
        if (!hasRebuilt || serial != lastRebuiltSerial) {
            lastRebuiltSerial = serial;
            hasRebuilt = true;

            // Длина берётся один раз: аудиопоток может сменить геометрию между
            // вызовами, и тогда указатель и длина разойдутся.
            //
            // Само содержимое окна аудиопоток может переписывать прямо во время
            // чтения — у AudioHistoryBuffer нет блокировок. Гонка принята:
            // массив фиксированного размера, выход за границы невозможен,
            // худшее последствие — один порванный кадр.
            // Берём ровно столько кадров, сколько настроено: при развёртке
            // меньше единицы это хвост одного пакета, иначе всё окно.
            const std::size_t frames = phosphorGrid.framesInWindow();
            if (frames > 0) {
                // Запас перед окном, в котором ищется фронт синхронизации.
                // Дальше окна назад уходить незачем: там кадр всё равно не
                // начнётся. Сколько истории есть на самом деле, знает только
                // буфер — сразу после переноса окна в начало массива за ним
                // не остаётся ничего, и тогда кадр идёт без синхронизации.
                // Нулевой запас означает её отключение: смещение выйдет нулевым.
                std::size_t searchFrames = 0;
                if (requestedTrigger) {
                    std::size_t spare = audioHistoryBuffer.available() / 2;
                    spare = (spare > frames) ? spare - frames : 0;
                    searchFrames = std::min(spare, frames);
                }

                const float *window = audioHistoryBuffer.readSmall(
                        static_cast<jint>((frames + searchFrames) * 2));
                if (window != nullptr) {
                    const std::size_t offset =
                            findTriggerOffset(window, searchFrames);
                    phosphorGrid.rebuild(window + offset * 2, frames);
                }
            }
        }
    }

    jint range[2] = {0, 0};
    phosphorGrid.takeDirtyRange(&range[0], &range[1]);

    // Конверсию в half-float делаем сами: драйверу отдавать float32 в
    // текстуру RG16F слишком дорого.
    phosphorGrid.packHalf(range[0], range[1]);

    jintArray result = env->NewIntArray(2);
    if (result == nullptr) {
        return nullptr;
    }
    env->SetIntArrayRegion(result, 0, 2, range);
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativePhosphor_invalidate(
        JNIEnv * /* env */, jobject /* thiz */) {
    phosphorGrid.markAllDirty();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_generator2_features_scope_NativePhosphor_gridBuffer(
        JNIEnv *env, jobject /* thiz */) {
    if (!phosphorGrid.isReady()) {
        return nullptr;
    }
    const jlong bytes = static_cast<jlong>(phosphorGrid.columns()) *
                        static_cast<jlong>(PhosphorGrid::kColumnStride) *
                        static_cast<jlong>(sizeof(std::uint16_t));
    return env->NewDirectByteBuffer(
            const_cast<std::uint16_t *>(phosphorGrid.halfData()), bytes);
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_example_generator2_features_scope_NativePhosphor_ringOffset(
        JNIEnv * /* env */, jobject /* thiz */) {
    return phosphorGrid.ringOffset();
}
