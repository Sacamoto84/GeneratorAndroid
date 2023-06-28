#include <jni.h>
#include <oboe/Oboe.h>
#include "HelloOboeEngine.h"
#include "logging_macros.h"
#include "generator.h"

extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setParameterFloat(JNIEnv *env, jobject thiz,
                                                                     jlong engine_handle,
                                                                     jint index, jfloat value) {
    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engine_handle);

    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setParameterFloat: index:%d value:%f", index, value);

    if (index == 0) parameterFloat0 = value;
    if (index == 1) parameterFloat1 = value;
    if (index == 2) parameterFloat2 = value;
    if (index == 3) parameterFloat3 = value;
    if (index == 4) parameterFloat4 = value;
    if (index == 5) parameterFloat5 = value;
    if (index == 6) parameterFloat6 = value;
    if (index == 7) parameterFloat7 = value;

}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setParameterInt(JNIEnv *env, jobject thiz,
                                                                   jlong engine_handle, jint index,
                                                                   jint value) {
    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engine_handle);

    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setParameterInt: index:%d value:%d", index, value);

    if (index == 0) parameterInt0 = value;
    if (index == 1) parameterInt1 = value;
    if (index == 2) parameterInt2 = value;
    if (index == 3) parameterInt3 = value;
    if (index == 4) parameterInt4 = value;
    if (index == 5) parameterInt5 = value;
    if (index == 6) parameterInt6 = value;
    if (index == 7) parameterInt7 = value;
}