//
// Создано пользователем 06.07.2024.
//

#include "FloatDirectBuffer.h"

#include <iostream>

AudioHistoryBuffer audioHistoryBuffer;

extern "C" {

JNIEXPORT void JNICALL
Java_JniFloatBuffer_addToBuffer(JNIEnv *env, jobject /* obj */, jlong bufferPtr,
                                jfloatArray data, jint offset, jint length) {
    float *buffer = reinterpret_cast<float *>(bufferPtr);
    if (buffer == nullptr || data == nullptr || offset < 0 || length < 0) {
        std::cerr << "Invalid buffer, array, offset, or length" << std::endl;
        return;
    }

    const jsize dataLength = env->GetArrayLength(data);
    if (offset > dataLength - length ||
        offset > static_cast<jint>(AudioHistoryBuffer::kCapacity) - length) {
        std::cerr << "Offset and length exceed array or buffer size" << std::endl;
        return;
    }

    jfloat *elements = env->GetFloatArrayElements(data, nullptr);
    if (elements == nullptr) {
        std::cerr << "Failed to access FloatArray" << std::endl;
        return;
    }

    std::memcpy(buffer + offset, elements, static_cast<std::size_t>(length) * sizeof(float));
    env->ReleaseFloatArrayElements(data, elements, JNI_ABORT);
}

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeFloatDirectBuffer_add(
        JNIEnv *env, jobject /* thiz */, jfloatArray data, jint len, jint item_count) {
    if (data == nullptr || len <= 0 || item_count <= 0 ||
        len > env->GetArrayLength(data)) {
        LOGE("add(): invalid JNI arguments len=%d, itemCount=%d", len, item_count);
        return;
    }

    jfloat *elements = env->GetFloatArrayElements(data, nullptr);
    if (elements == nullptr) {
        LOGE("add(): failed to access FloatArray");
        return;
    }

    audioHistoryBuffer.add(elements, len, item_count);
    env->ReleaseFloatArrayElements(data, elements, JNI_ABORT);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_generator2_features_scope_NativeFloatDirectBuffer_getByteBuffer(
        JNIEnv *env, jobject /* thiz */, jint /* len */) {
    float *buffer = audioHistoryBuffer.read();
    const auto window = audioHistoryBuffer.window();
    if (buffer == nullptr || window == 0) {
        LOGE("getByteBuffer(): buffer is not ready");
        return nullptr;
    }

    jobject byteBuffer = env->NewDirectByteBuffer(
            buffer, static_cast<jlong>(window * sizeof(float)));
    if (byteBuffer == nullptr) {
        LOGE("Failed to create ByteBuffer");
        return nullptr;
    }
    return byteBuffer;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_generator2_features_scope_NativeFloatDirectBuffer_getByteBufferSmallLissagu(
        JNIEnv *env, jobject /* thiz */, jint len) {
    float *buffer = audioHistoryBuffer.readSmall(len);
    if (buffer == nullptr) {
        LOGE("getByteBufferSmallLissagu(): buffer is not ready");
        return nullptr;
    }

    jobject byteBuffer = env->NewDirectByteBuffer(
            buffer, static_cast<jlong>(len) * sizeof(float));
    if (byteBuffer == nullptr) {
        LOGE("Failed to create ByteBuffer");
        return nullptr;
    }
    return byteBuffer;
}
