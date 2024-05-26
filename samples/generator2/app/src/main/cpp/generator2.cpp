// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("generator2");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("generator2")
//      }
//    }


float testfloat0[524288];
float testfloat1[524288];

#include <jni.h>
#include <cstring>
#include <memory>

// Определение класса FloatRingBuffer
class FloatRingBuffer {
public:
    FloatRingBuffer(int entrySize, int bufferSize)
            : entrySize(entrySize), bufferSize(bufferSize), start(0), end(0), isFull(false) {
        buffer = std::make_unique<float[]>(entrySize * bufferSize);
    }

    void add(const float *entry) {
        std::memcpy(buffer.get() + end * entrySize, entry, entrySize * sizeof(float));
        end = (end + 1) % bufferSize;
        if (isFull) {
            start = (start + 1) % bufferSize;
        } else if (end == start) {
            isFull = true;
        }
    }

    void toExternalFloatArray(float *result) const {
        if (isFull) {
            int part1Size = (bufferSize - start) * entrySize;
            std::memcpy(result, buffer.get() + start * entrySize, part1Size * sizeof(float));
            std::memcpy(result + part1Size, buffer.get(), end * entrySize * sizeof(float));
        } else {
            std::memcpy(result, buffer.get(), end * entrySize * sizeof(float));
        }
    }

private:
    int entrySize;
    int bufferSize;
    int start;
    int end;
    bool isFull;
    std::unique_ptr<float[]> buffer;
};


// Функции JNI для работы с буфером
extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_generator2_features_scope_NativeLib_createBuffer(JNIEnv *env, jobject,
                                                                  jint entrySize, jint bufferSize) {
    auto *ringBuffer = new FloatRingBuffer(entrySize, bufferSize);
    return reinterpret_cast<jlong>(ringBuffer);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeLib_addEntry(JNIEnv *env, jobject, jlong bufferPtr,
                                                              jfloatArray entry) {
    auto *ringBuffer = reinterpret_cast<FloatRingBuffer *>(bufferPtr);
    jfloat *entryElements = env->GetFloatArrayElements(entry, nullptr);
    ringBuffer->add(entryElements);
    env->ReleaseFloatArrayElements(entry, entryElements, 0);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeLib_toExternalFloatArray(JNIEnv *env, jobject,
                                                                          jlong bufferPtr,
                                                                          jfloatArray result) {
    auto *ringBuffer = reinterpret_cast<FloatRingBuffer *>(bufferPtr);
    jfloat *resultElements = env->GetFloatArrayElements(result, nullptr);
    ringBuffer->toExternalFloatArray(resultElements);
    env->ReleaseFloatArrayElements(result, resultElements, 0);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeLib_destroyBuffer(JNIEnv *env, jobject,
                                                                   jlong bufferPtr) {
    auto *ringBuffer = reinterpret_cast<FloatRingBuffer *>(bufferPtr);
    delete ringBuffer;
}









extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeLib_copyFloatArrayJNI(JNIEnv *env, jobject thiz,
                                                                       jfloatArray source,
                                                                       jfloatArray destination) {
    jsize length = env->GetArrayLength(source);
    jfloat *sourceElements = env->GetFloatArrayElements(source, nullptr);
    jfloat *destinationElements = env->GetFloatArrayElements(destination, nullptr);

    // Копирование данных
    std::memcpy(destinationElements, sourceElements, length * sizeof(jfloat));

    // Освобождение указателей
    env->ReleaseFloatArrayElements(source, sourceElements, JNI_ABORT);
    env->ReleaseFloatArrayElements(destination, destinationElements, 0);
}



extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeLib_testCopyJNI(JNIEnv *env, jobject thiz) {
    // TODO: implement testCopyJNI()
    std::memcpy(testfloat0, testfloat1, 524288 * sizeof(jfloat));
}
