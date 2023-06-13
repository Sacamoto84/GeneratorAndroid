#include <jni.h>
#include <oboe/Oboe.h>
#include "HelloOboeEngine.h"
#include "logging_macros.h"
#include "generator.h"



//Установка ширины импульса
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setImpulseWidthTime(JNIEnv *env, jobject thiz,
                                                                       jlong engine_handle, jint ch,
                                                                       jint width) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engine_handle);

    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setImpulseWidthTime: CH:%d width:%d", ch, width);

    if (ch)
        CH2.timeImp = width;
    else
        CH1.timeImp = width;

}

//Установка паузы импульса
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setImpulsePauseTime(JNIEnv *env, jobject thiz,
                                                                       jlong engine_handle, jint ch,
                                                                       jint width) {
    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engine_handle);

    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:ImpulsePauseTime: CH:%d pause:%d", ch, width);

    if (ch)
        CH2.timeImpPause = width;
    else
        CH1.timeImpPause = width;

}


