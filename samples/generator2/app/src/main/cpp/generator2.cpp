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

#include <math.h>
#include <jni.h>
#include <android/bitmap.h>
#include "TFT.h"
#include <cstring> // Для функции memcpy
#include <cstdint> // Для типа данных uint16_t
#include <iostream>

TFT tft = TFT();







extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_FastBitmap_createBuffer(JNIEnv *env, jobject thiz, jint w, jint h) {
    if (tft.initialized == 0) {
        tft.createBuffer(w, h);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_FastBitmap_setPixel(JNIEnv *env, jobject thiz, jint x, jint y,
                                                jint color) {
    tft.setPixel(x, y, (uint16_t)color);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_FastBitmap_processBitmap(JNIEnv *env, jobject thiz, jobject bitmap) {

    AndroidBitmapInfo bitmapInfo;
    // Получаем указатель на пиксели Bitmap
    uint16_t *pixels;

//    // Получаем информацию о Bitmap
    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) < 0) {
        // Ошибка получения информации о Bitmap
        return;
    }

//    // Блокируем пиксели Bitmap для доступа
    if (AndroidBitmap_lockPixels(env, bitmap, reinterpret_cast<void **>(&pixels)) < 0) {
        // Ошибка блокировки пикселей Bitmap
        return;
    }

    uint16_t *p = reinterpret_cast<uint16_t *>(pixels);
    uint16_t *pp = tft.buffer16;

    // Копирование данных из исходного массива в целевой массив
    std::memcpy(p, pp, tft.WIDTH * tft.HEIGHT);

//    int lenP = (tft.WIDTH * tft.HEIGHT) / 2;
//    for (int i = 0; i < lenP; i++) {
//        p[i] = pp[i];
//    }


//    int len = tft.WIDTH * tft.HEIGHT;
//    for (int i = 0; i < len; ++i) {
//        pixels[i] =  tft.buffer16[i];
//    }

    // Здесь вы можете обрабатывать пиксели Bitmap, представленные в переменной pixels

    // Разблокируем пиксели Bitmap после окончания операций
    AndroidBitmap_unlockPixels(env, bitmap);

}
