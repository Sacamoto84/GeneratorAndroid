/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <oboe/Oboe.h>
#include "HelloOboeEngine.h"
#include "logging_macros.h"
#include "generator.h"

extern "C" {










JNIEXPORT jlong JNICALL
Java_com_example_generator2_PlaybackEngine_native_1createEngine(JNIEnv *env, jclass /*unused*/) {
    // We use std::nothrow so `new` returns a nullptr if the engine creation fails
    HelloOboeEngine *engine = new(std::nothrow) HelloOboeEngine();
    if (engine == nullptr) {
        LOGD("Could not instantiate HelloOboeEngine");
        return 0;
    }
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT jint JNICALL
Java_com_example_generator2_PlaybackEngine_native_1startEngine(
        JNIEnv *env,
        jclass,
        jlong engineHandle) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    return static_cast<jint>(engine->start());
}

JNIEXPORT jint JNICALL
Java_com_example_generator2_PlaybackEngine_native_1stopEngine(
        JNIEnv *env,
        jclass,
        jlong engineHandle) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    return static_cast<jint>(engine->stop());
}

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1deleteEngine(
        JNIEnv *env,
        jclass,
        jlong engineHandle) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    engine->stop();
    delete engine;
}

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setAudioApi(
        JNIEnv *env,
        jclass type,
        jlong engineHandle,
        jint audioApi) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    oboe::AudioApi api = static_cast<oboe::AudioApi>(audioApi);
    engine->setAudioApi(api);
}

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setAudioDeviceId(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jint deviceId) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->setDeviceId(deviceId);
}

JNIEXPORT jint JNICALL
Java_com_example_generator2_PlaybackEngine_native_1getAudioDeviceId(
        JNIEnv *env,
        jclass,
        jlong engineHandle
) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return -1;
    }

    while (engine->mStream == nullptr)
    {}

    return static_cast<jint>(engine->mStream->getDeviceId());
}

//Прочесть признак того что требуются новые данные
JNIEXPORT jint JNICALL
Java_com_example_generator2_PlaybackEngine_native_1getAllData(
        JNIEnv *env,
        jclass,
        jlong engineHandle
) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return -1;
    }

    return static_cast<jint>(engine->needAllData);
}

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1resetAllData(
        JNIEnv *env,
        jclass type,
        jlong engineHandle,
        jint channelCount) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->needAllData = 0;
}


JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setChannelCount(
        JNIEnv *env,
        jclass type,
        jlong engineHandle,
        jint channelCount) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->setChannelCount(channelCount);
}

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setBufferSizeInBursts(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jint bufferSizeInBursts) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->setBufferSizeInBursts(bufferSizeInBursts);
}


JNIEXPORT jdouble JNICALL
Java_com_example_generator2_PlaybackEngine_native_1getCurrentOutputLatencyMillis(
        JNIEnv *env,
        jclass,
        jlong engineHandle) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine is null, you must call createEngine before calling this method");
        return static_cast<jdouble>(-1.0);
    }
    return static_cast<jdouble>(engine->getCurrentOutputLatencyMillis());
}

JNIEXPORT jboolean JNICALL
Java_com_example_generator2_PlaybackEngine_native_1isLatencyDetectionSupported(
        JNIEnv *env,
        jclass type,
        jlong engineHandle) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine is null, you must call createEngine before calling this method");
        return JNI_FALSE;
    }
    return (engine->isLatencyDetectionSupported() ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setDefaultStreamValues(
        JNIEnv *env,
        jclass type,
        jint sampleRate,
        jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}



//Мои функции


JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1resetAllPhase(
        JNIEnv *env,
        jclass,
        jlong engineHandle
        ){

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:resetAllPhase");

    resetAllPhase();

}


JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setShuffle(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jboolean value) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setShuffle: %d", value);
    shuffle = value;
}

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setEnL(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jboolean value) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setEnL: %d", value);
    enL = value;
}

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setEnR(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jboolean value) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setEnR: %d", value);
    enR = value;
}

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setMono(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jboolean mono) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setMono: %d", mono);

    if (mono)
    {
        setToMono();
        resetAllPhase();
    }
    else
    {
        setToStereo();
        resetAllPhase();
    }

}

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setInvertPhase(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jboolean invert) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setInvertPhase: %d", invert);

    Invert = invert;

}

//CH EN
JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setCH_1EN(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jint CH,
        jboolean EN) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setCH_1EN: CH:%d EN:%d", CH, EN);

    {
        if (CH)
            CH2.CH_EN = EN;
        else
            CH1.CH_EN = EN;
    }
}

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setCH_1AMEN(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jint CH,
        jboolean EN) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setCH_1AMEN: CH:%d EN:%d", CH, EN);

    //if (engine->mAudioSource->mGenerator)
    {
        if (CH)
            CH2.AM_EN = EN;
        else
            CH1.AM_EN = EN;
    }
}

JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setCH_1FMEN(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jint CH,
        jboolean EN) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setCH_1FMEN: CH:%d EN:%d", CH, EN);

    //if (engine->mAudioSource->mGenerator)
    {
        if (CH)
            CH2.FM_EN = EN;
        else
            CH1.FM_EN = EN;
    }
}


//Установить громкость
JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setVolume(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jint CH,
        jfloat value
) {
    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setVolume: CH:%d fr%f", CH, value);

    if (CH == 0)
        CH1.Volume = value;
    else
        CH2.Volume = value;

}


//Изменить частоту несущей
JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setCH_1Carrier_1fr(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jint CH,
        jfloat fr
) {
    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setCH_Carrier_fr: CH:%d fr%f", CH, fr);

    //if (engine->mAudioSource->mGenerator)



    if (CH == 0) {
        CH1.Carrier_fr = fr;
        engine->mAudioSource->CreateFM_CH1();
    } else {
        CH2.Carrier_fr = fr;
        engine->mAudioSource->CreateFM_CH2();
    }





}

//Изменить частоту AM
JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setCH_1AM_1fr(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jint CH,
        jfloat fr
) {
    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setCH_AM_fr: CH:%d FR%f", CH, fr);

    //if (engine->mAudioSource->mGenerator)
    {
        if (CH == 0)
            CH1.AM_fr = fr;
        else
            CH2.AM_fr = fr;
    }
}

//Изменить частоту FM
JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setCH_1FM_1fr(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jint CH,
        jfloat fr
) {
    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setCH_FM_fr: CH:%d FR%f", CH, fr);

    //if (engine->mAudioSource->mGenerator)
    {
        if (CH == 0)
            CH1.FM_mod_fr = fr;
        else
            CH2.FM_mod_fr = fr;
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setCH_1Send_1Buffer(JNIEnv *env, jclass clazz,
                                                                       jlong engineHandle, jint CH,
                                                                       jint mod, jbyteArray buf) {

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setCH_Send_Buffer: CH:%d mod%d", CH, mod);

    jbyte *arrayBody = env->GetByteArrayElements(buf, 0);
    jsize theArrayLengthJ = env->GetArrayLength(buf);

    auto *starter16 = (uint16_t *) arrayBody;

    int i = (int) theArrayLengthJ;
    if (i != 2048) {
        LOGI("!ERROR JNI:Send: buf != 2048");
        return;
    }

    if (CH == 0) {

        if (mod == 0) {
            for (i = 0; i < 1024; i++) {
                CH1.buffer_carrier[i] = starter16[i];
            }
        }

        if (mod == 1)  //AM
        {
            for (i = 0; i < 1024; i++)
                CH1.buffer_am[i] = starter16[i];
        }

        if (mod == 2) //FM
        {
            for (i = 0; i < 1024; i++)
                CH1.source_buffer_fm[i] = starter16[i];

            engine->mAudioSource->CreateFM_CH1();

        }
    } else {
        if (mod == 0) {
            for (i = 0; i < 1024; i++)
                CH2.buffer_carrier[i] = starter16[i];
        }

        if (mod == 1) {
            for (i = 0; i < 1024; i++)
                CH2.buffer_am[i] = starter16[i];
        }

        if (mod == 2) //FM
        {
            for (i = 0; i < 1024; i++)
                CH2.source_buffer_fm[i] = starter16[i];

            engine->mAudioSource->CreateFM_CH2();
        }
    }

}


//JNIEXPORT void JNICALL
//Java_com_example_generator2_PlaybackEngine_native_1setCH_1FM_1Base(JNIEnv *env,
//                                                                   jclass clazz,
//                                                                   jlong engineHandle,
//                                                                   jint CH, float fr) {
//
//
//    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
//    if (engine == nullptr) {
//        LOGE("Engine handle is invalid, call createHandle() to create a new one");
//        return;
//    }
//
//    LOGI("JNI:setCH_FM_Base: CH:%d FR%f", CH, fr);
//
//    //if (engine->mAudioSource->mGenerator)
//    {
//        if (CH == 0) {
//            CH1.FM_Base = fr;
//            engine->mAudioSource->CreateFM_CH1();
//        } else {
//            CH2.FM_Base = fr;
//            engine->mAudioSource->CreateFM_CH2();
//        }
//    }
//
//
//}




JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setCH_1FM_1Dev(JNIEnv *env,
                                                                  jclass clazz,
                                                                  jlong engineHandle,
                                                                  jint CH, jfloat fr) {


    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setCH_FM_Dev: CH:%d FR%f", CH, fr);

    //if (engine->mAudioSource->mGenerator)
    {

        if (CH == 0) {
            CH1.FM_Dev = fr;
            engine->mAudioSource->CreateFM_CH1();
        } else {
            CH2.FM_Dev = fr;
            engine->mAudioSource->CreateFM_CH2();
        }
    }
}


}





extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_PlaybackEngine_native_1setAmDepth(JNIEnv *env, jobject thiz,
                                                              jlong engine_handle, jint ch,
                                                              jfloat depth) {
    // TODO: implement native_setAmDepth()

    HelloOboeEngine *engine = reinterpret_cast<HelloOboeEngine *>(engine_handle);

    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    LOGI("JNI:setAmDepth: CH:%d depth:%f", ch, depth);

    if (ch)
        CH2.AmDepth = depth;
    else
        CH1.AmDepth = depth;



}


