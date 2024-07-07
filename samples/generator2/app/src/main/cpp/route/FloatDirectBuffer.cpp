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