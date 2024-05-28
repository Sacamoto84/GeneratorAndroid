#include <jni.h>
#include <android/bitmap.h>
#include <cstring>
#include <thread>
#include <vector>
#include <mutex>
#include <iostream>
#include <vector>
#include <queue>
#include <thread>
#include <condition_variable>
#include <functional>
#include <future>
#include <omp.h>
#include "scope.h"







extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeCanvas_jniCanvas(JNIEnv *env, jobject thiz,
                                                                  jfloatArray big_pointn_l,
                                                                  jfloatArray big_pointn_r,
                                                                  jfloatArray buf_rn,
                                                                  jfloatArray buf_ln,
                                                                  jint w,
                                                                  jint h,
                                                                  jint max_pixel_buffer,
                                                                  jboolean is_one_two,
                                                                  jint start,
                                                                  jint end

) {

    float maxL;
    float maxR = h - 1.0f;
    float minR;
    if (is_one_two) {
        maxL = h - 1.0f;
        minR = 0.0f;
    } else {
        maxL = h / 2.0f;
        minR = h / 2.0f;
    }

    jsize temp1 = env->GetArrayLength(buf_rn) - 1;
    int temp2 = w - 1;
    int temp3 = 0;

    jfloat *bigPointnL = env->GetFloatArrayElements(big_pointn_l, nullptr);
    jfloat *bigPointnR = env->GetFloatArrayElements(big_pointn_r, nullptr);

    jfloat *bufRN = env->GetFloatArrayElements(buf_rn, nullptr);
    jfloat *bufLN = env->GetFloatArrayElements(buf_ln, nullptr);

    for (int x = start; x < end; x++) {
        int mapX = (x * temp1 / temp2);
        if (mapX < 0) mapX = 0;
        if (mapX > temp1) mapX = temp1;

        ////////
        for (int i = 0; i < max_pixel_buffer; i++) {
            int offset = mapX + i;
            if (offset > temp1) offset = temp1;
            temp3 = i * 2 + x * max_pixel_buffer * 2;
            *(bigPointnR + temp3) = x;
            *(bigPointnL + temp3) = x;
            *(bigPointnR + temp3 + 1) = (*(bufRN + offset) + 1.0f) * (maxR - minR) / 2.0f + minR;
            *(bigPointnL + temp3 + 1) = (*(bufLN + offset) + 1.0f) * maxL / 2.0f;
        }


    }

    env->ReleaseFloatArrayElements(big_pointn_l, bigPointnL, 0);
    env->ReleaseFloatArrayElements(big_pointn_r, bigPointnR, 0);

    env->ReleaseFloatArrayElements(buf_rn, bufRN, 0);
    env->ReleaseFloatArrayElements(buf_ln, bufLN, 0);

}


void routine(int start, int length, uint32_t *line, uint32_t width, jfloat *bigPointnL,
             jfloat *bigPointnR) {


    uint32_t end = start + length;
    for (jsize i = start; i < end; i += 2) {

        int xL = static_cast<int>(bigPointnL[i]);
        int yL = static_cast<int>(bigPointnL[i + 1]);

        int xR = static_cast<int>(bigPointnR[i]);
        int yR = static_cast<int>(bigPointnR[i + 1]);

        //if (x >= 0 && x < info.width && y >= 0 && y < info.height) {

        line[yL * width + xL] = 0x3F00FFFF;  // Устанавливаем черный цвет
        //line[(yL+1) * info.width + xL] = 0x3F00FFFF;  // Устанавливаем черный цвет
        line[yR * width + xR] = 0x3FFF00FF;  // Устанавливаем черный цвет
        //}
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeCanvas_jniCanvasBitmap(JNIEnv *env, jobject thiz,
                                                                        jfloatArray big_pointn_l,
                                                                        jfloatArray big_pointn_r,
                                                                        jobject bitmap,
                                                                        jboolean enable_l,
                                                                        jboolean enable_r,
                                                                        jint start,
                                                                        jint length
) {

    jfloat *bigPointnL = env->GetFloatArrayElements(big_pointn_l, nullptr);
    jfloat *bigPointnR = env->GetFloatArrayElements(big_pointn_r, nullptr);

    //    // Получаем указатель на пиксели Bitmap
    void *pixels;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        return;
    }

    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    }

    uint32_t *line = (uint32_t *) pixels;

    routine(start, length, line, info.width, bigPointnL, bigPointnR);
    ////    // Освобождаем ресурсы
    AndroidBitmap_unlockPixels(env, bitmap);

    env->ReleaseFloatArrayElements(big_pointn_l, bigPointnL, 0);
    env->ReleaseFloatArrayElements(big_pointn_r, bigPointnR, 0);
}




extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeCanvas_fill(JNIEnv *env, jobject thiz,
                                                             jfloatArray array, jint len) {
    // jfloat *point = env->GetFloatArrayElements(array, nullptr);



//    #pragma omp parallel for
//    for(uint32_t i = 0 ; i< len; i++)
//    {
//        point[i] = -1.0f;
//    }

    //   env->ReleaseFloatArrayElements(array, point, 0);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeCanvas_fillArrayWithZero(JNIEnv *env, jobject thiz,
                                                                          jfloatArray array,
                                                                          jint length

) {
    // Получаем указатель на элементы массива
    jfloat *body = env->GetFloatArrayElements(array, nullptr);

    if (body == NULL) {
        // Обработка ошибки
        return;
    }

    // Используем memset для заполнения нулями
    memset(body, 0, length * sizeof(jfloat));

    // Освобождаем ресурсы и синхронизируем изменения
    env->ReleaseFloatArrayElements(array, body, 0);

}





extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_generator2_features_scope_NativeCanvas_createNativeScope(JNIEnv *env,
                                                                          jobject thiz) {
    auto *ringBuffer = new Scope();
    return reinterpret_cast<jlong>(ringBuffer);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_scope_NativeCanvas_dectroyNativeScope(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong scope) {
    auto *ringBuffer = reinterpret_cast<Scope *>(scope);
    delete ringBuffer;
}
