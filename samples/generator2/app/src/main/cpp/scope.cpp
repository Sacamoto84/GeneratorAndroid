//
// Created by user on 28.05.2024.
//

#include <jni.h>
#include "scope.h"
#include <cstring>


/**
 * Разделить буфер на два массива и записать их в scope, замена pairFlatArray = bufSplit0.split(buf)
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeCanvas_split(JNIEnv *env, jobject thiz,
                                                              jlong scope, jfloatArray buf, jint length) {

    auto * _scope = reinterpret_cast<Scope *>(scope);
    jfloat *entryElements = env->GetFloatArrayElements(buf, nullptr);

    int halfSize = length / 2;

    if (_scope->sizeBufRf != halfSize) {
        //deallocateBuffers
        delete[] _scope-> bufRf;
        delete[] _scope-> bufLf;
        _scope-> bufRf = nullptr;
        _scope-> bufLf = nullptr;
        _scope->sizeBufRf = 0;
        //allocateBuffers
        _scope->sizeBufRf = halfSize;
        _scope-> bufRf = new float[halfSize];
        _scope->bufLf = new float[halfSize];
    }

    std::memset(_scope->bufRf, 0, halfSize * sizeof(float));
    std::memset(_scope->bufLf, 0, halfSize * sizeof(float));

    // Split the input buffer
    size_t index1 = 0;
    size_t index2 = 0;

    for (int i = 0; i < length; ++i) {
        if (i % 2 == 0) {
            _scope->bufLf[index1++] = entryElements[i];
        } else {
            _scope->bufRf[index2++] = entryElements[i];
        }
    }

    env->ReleaseFloatArrayElements(buf, entryElements, 0);
}



























//void Scope::calculatePixel(
//        jfloatArray big_pointn_l,
//        jfloatArray big_pointn_r
//        ) {
//
//
//
//    float maxL;
//    float maxR = h - 1.0f;
//    float minR;
//    if (is_one_two) {
//        maxL = h - 1.0f;
//        minR = 0.0f;
//    } else {
//        maxL = h / 2.0f;
//        minR = h / 2.0f;
//    }
//
//    jsize temp1 = env->GetArrayLength(buf_rn) - 1;
//    int temp2 = w - 1;
//    int temp3 = 0;
//
//    jfloat *bigPointnL = env->GetFloatArrayElements(big_pointn_l, nullptr);
//    jfloat *bigPointnR = env->GetFloatArrayElements(big_pointn_r, nullptr);
//
//    jfloat *bufRN = env->GetFloatArrayElements(buf_rn, nullptr);
//    jfloat *bufLN = env->GetFloatArrayElements(buf_ln, nullptr);
//
//    for (int x = start; x < end; x++) {
//        int mapX = (x * temp1 / temp2);
//        if (mapX < 0) mapX = 0;
//        if (mapX > temp1) mapX = temp1;
//
//        ////////
//        for (int i = 0; i < max_pixel_buffer; i++) {
//            int offset = mapX + i;
//            if (offset > temp1) offset = temp1;
//            temp3 = i * 2 + x * max_pixel_buffer * 2;
//            *(bigPointnR + temp3) = x;
//            *(bigPointnL + temp3) = x;
//            *(bigPointnR + temp3 + 1) = (*(bufRN + offset) + 1.0f) * (maxR - minR) / 2.0f + minR;
//            *(bigPointnL + temp3 + 1) = (*(bufLN + offset) + 1.0f) * maxL / 2.0f;
//        }
//
//
//    }
//
//
//    env->ReleaseFloatArrayElements(buf_rn, bufRN, 0);
//    env->ReleaseFloatArrayElements(buf_ln, bufLN, 0);
//
//
//
//
//
//
//
//}
