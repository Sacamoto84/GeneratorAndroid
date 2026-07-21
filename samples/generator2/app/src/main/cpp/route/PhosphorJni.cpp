//
// JNI-привязка фосфорного аккумулятора.
//

#include "PhosphorGrid.h"
#include "FloatDirectBuffer.h"

#include <jni.h>

PhosphorGrid phosphorGrid;

extern AudioHistoryBuffer audioHistoryBuffer;

namespace {

int requestedColumns = 0;
int requestedLayout = 0;
bool requestedRollMode = true;
float requestedSweep = 1.0f;
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
        jboolean rollMode, jfloat sweep) {
    requestedColumns = columns;
    requestedLayout = layout;
    requestedRollMode = (rollMode == JNI_TRUE);
    requestedSweep = sweep;
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
            const float *window = audioHistoryBuffer.readSmall(
                    static_cast<jint>(frames * 2));
            if (window != nullptr && frames > 0) {
                phosphorGrid.rebuild(window, frames);
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
