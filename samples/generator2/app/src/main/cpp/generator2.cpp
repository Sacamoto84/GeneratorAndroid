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

#include <jni.h>
#include <cstring>
#include <memory>

#include "route/FloatRingBuffer.h"

// Функции JNI для работы с буфером

/**
 * Создание буфера
 */
extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_generator2_features_scope_NativeLib_createBuffer(JNIEnv *env, jobject,
                                                                  jint entrySize, jint bufferSize) {
    auto *ringBuffer = new FloatRingBuffer(entrySize, bufferSize);
    return reinterpret_cast<jlong>(ringBuffer);
}

/**
 * Добавить елемент размером entrySize
 */
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




