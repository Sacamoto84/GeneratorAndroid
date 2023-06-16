#include <jni.h>
#include <oboe/Oboe.h>
#include "HelloOboeEngine.h"
#include "logging_macros.h"
#include "generator.h"

extern std::vector<float> receive();

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_startListening(JNIEnv *env, jobject obj, jobject callback) {

//    jclass cls = env->GetObjectClass(callback);
//    jmethodID method = env->GetMethodID(cls, "onDataReceived", "([F)V");

//    while (true) {
//
//        std::vector<float> data = receive();
//
//        // создаем массив типа float
//        jfloatArray floatArray = env->NewFloatArray(512);
//
//        // заполняем массив данными
//        jfloat* arrayData = env->GetFloatArrayElements(floatArray, nullptr);
//        env->ReleaseFloatArrayElements(floatArray, arrayData, 0);
//
//        //env->CallVoidMethod(callback, method, str);
//
//        //env->DeleteLocalRef(str);
//
//        // вызываем метод, передавая ему массив
//        env->CallVoidMethod(obj, method, floatArray);
//
//        // удаляем массив
//        env->DeleteLocalRef(floatArray);
//
//    }

}


}