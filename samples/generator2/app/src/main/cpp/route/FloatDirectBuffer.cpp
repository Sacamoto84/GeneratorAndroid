//
// Created by user on 06.07.2024.
//

#include "FloatDirectBuffer.h"

FloatDirectBuffer floatDirectBuffer;


extern "C" {

JNIEXPORT void JNICALL
Java_JniFloatBuffer_addToBuffer(JNIEnv *env, jobject obj, jlong bufferPtr, jfloatArray data, jint offset, jint length) {

    float *buffer = reinterpret_cast<float *>(bufferPtr);
    jsize dataLength = env->GetArrayLength(data);

    if (offset + length > dataLength) {
        std::cerr << "Offset and length exceed array size" << std::endl;
        return;
    }

    jfloat *elements = env->GetFloatArrayElements(data, nullptr);

    if (offset + length <= BUFFER_SIZE) {
        memcpy(buffer + offset, elements, length * sizeof(float));
    } else {
        std::cerr << "Buffer overflow" << std::endl;
    }

    env->ReleaseFloatArrayElements(data, elements, 0);
}




}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeFloatDirectBuffer_add(JNIEnv *env, jobject thiz,
                                                                       jfloatArray data, jint len,
                                                                       jint item_count) {
    jfloat *elements = env->GetFloatArrayElements(data, nullptr);
    floatDirectBuffer.add(elements, len, item_count);
    env->ReleaseFloatArrayElements(data, elements, 0);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_generator2_features_scope_NativeFloatDirectBuffer_getByteBuffer(JNIEnv *env,
                                                                                 jobject thiz,
                                                                                 jint len) {
    float *buffer = reinterpret_cast<float *>(floatDirectBuffer.read());

    // Create a direct ByteBuffer that shares the memory with the C++ buffer
    jobject byteBuffer = env->NewDirectByteBuffer(buffer, floatDirectBuffer.window() * sizeof(float));

    if (byteBuffer == nullptr) {
        LOGE ("Failed to create ByteBuffer");
        return nullptr;
    }

    return byteBuffer;
}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_generator2_features_scope_NativeFloatDirectBuffer_getByteBufferSmallLissagu(
        JNIEnv *env, jobject thiz, jint len) {

    float *buffer = reinterpret_cast<float *>(floatDirectBuffer.readSmall(len));

    // Create a direct ByteBuffer that shares the memory with the C++ buffer
    jobject byteBuffer = env->NewDirectByteBuffer(buffer, len * sizeof(float));

    if (byteBuffer == nullptr) {
        LOGE ("Failed to create ByteBuffer");
        return nullptr;
    }

    return byteBuffer;

}