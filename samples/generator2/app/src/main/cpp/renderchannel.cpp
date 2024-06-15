//
// Created by user on 12.06.2024.
//



#include <jni.h>


float convertHzToR(float hz, int sampleRate) {
    return (48000.0f / sampleRate) *
           (hz * 16384.0f / 3.798f * 2.0f * 1000.0 / 48.8 / 2.0 * 1000.0 / 988.0);
}

float map(float x, float in_min, float in_max, float out_min, float out_max) {
    return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}


extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_example_generator2_features_generator_RenderChannel_jniRenderChannel(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jobject ch,
                                                                              jint num_frames,
                                                                              jint sample_rate,
        //jfloatArray m_buffer,
                                                                              jint r_c,
                                                                              jint r_am,
                                                                              jint r_fm,
                                                                              jboolean en_ch,
                                                                              jboolean en_am,
                                                                              jboolean en_fm,
                                                                              jfloat volume,
                                                                              jfloat am_depth) {

    if (!en_ch) {
        // Создание нового массива jfloatArray
        jfloatArray floatArray = env->NewFloatArray(num_frames);
        if (floatArray == nullptr) {
            return nullptr; // Ошибка выделения памяти
        }
        return floatArray;
    }

    jclass cls = env->GetObjectClass(ch);

//    jfieldID fid = env->GetFieldID(cls, "ch", "I");

//    int channel = env->GetIntField(ch, fid);

//    // buffer_carrier
//    auto buffer_carrier = (jfloatArray) env->GetObjectField(ch,
//                                                            env->GetFieldID(cls, "buffer_carrier",
//                                                                            "[F"));

//    // buffer_am
//    auto buffer_am = (jfloatArray) env->GetObjectField(ch, env->GetFieldID(cls, "buffer_am", "[F"));
//
//    // buffer_fm
//    auto buffer_fm = (jfloatArray) env->GetObjectField(ch, env->GetFieldID(cls, "buffer_fm", "[F"));
//
//    // source_buffer_fm
//    auto source_buffer_fm = (jfloatArray) env->GetObjectField(ch, env->GetFieldID(cls,
//                                                                                  "source_buffer_fm",
//                                                                                  "[F"));


    // Получаем ID поля bufferAmDirect
    jfieldID bufferCarrierDirectFieldID = env->GetFieldID(cls, "buffer_carrier_direct", "Ljava/nio/FloatBuffer;");
    jfieldID bufferAmDirectFieldID = env->GetFieldID(cls, "buffer_am_direct", "Ljava/nio/FloatBuffer;");
    jfieldID bufferFmDirectFieldID = env->GetFieldID(cls, "buffer_fm_direct", "Ljava/nio/FloatBuffer;");

    // Получаем объект FloatBuffer
    jobject bufferCarrierObject = env->GetObjectField(ch, bufferCarrierDirectFieldID);
    jobject bufferAmObject = env->GetObjectField(ch, bufferAmDirectFieldID);
    jobject bufferFmObject = env->GetObjectField(ch, bufferFmDirectFieldID);

    // Получаем указатель на данные в прямом буфере
    auto* tempBuffer_carrier = static_cast<float*>(env->GetDirectBufferAddress(bufferAmObject));
    auto* tempBuffer_am = static_cast<float*>(env->GetDirectBufferAddress(bufferAmObject));
    auto* tempBuffer_fm = static_cast<float*>(env->GetDirectBufferAddress(bufferFmObject));



    // phase_accumulator_carrier
    jfieldID phase_accumulator_carrierFid = env->GetFieldID(cls, "phase_accumulator_carrier", "I");
    auto phase_accumulator_carrier = static_cast<unsigned int>(env->GetIntField(ch,
                                                                                phase_accumulator_carrierFid));

    // phase_accumulator_am
    jfieldID phase_accumulator_amFid = env->GetFieldID(cls, "phase_accumulator_am", "I");
    auto phase_accumulator_am = static_cast<unsigned int>(env->GetIntField(ch,
                                                                           phase_accumulator_amFid));

    // phase_accumulator_fm
    jfieldID phase_accumulator_fmFid = env->GetFieldID(cls, "phase_accumulator_fm", "I");
    auto phase_accumulator_fm = static_cast<unsigned int>(env->GetIntField(ch,
                                                                           phase_accumulator_fmFid));

    // Создание нового массива jfloatArray
    jfloatArray floatArray = env->NewFloatArray(num_frames);
    if (floatArray == nullptr) {
        return nullptr; // Ошибка выделения памяти
    }

    float *tempArrayElements = env->GetFloatArrayElements(floatArray, nullptr);

    // Создание временного массива для заполнения
    //auto *tempArray = new jfloat[num_frames]();

    //float tempArray[num_frames];

    //short *mBufferCarrierElements = env->GetShortArrayElements(buffer_carrier, nullptr);
    //short *mBufferAmElements = env->GetShortArrayElements(buffer_am, nullptr);
    //short *mBufferFmElements = env->GetShortArrayElements(buffer_fm, nullptr);

    //float *mBufferElements = env->GetFloatArrayElements(floatArray, nullptr);

    //float o;

    auto r_fm32 = (uint32_t) r_fm;
    auto r_am32 = (uint32_t) r_am;
    auto r_c32 = (uint32_t) r_c;


//    jfloat tempBuffer_carrier[1024];
//    env->GetFloatArrayRegion(buffer_carrier, 0, 1024, tempBuffer_carrier);

    if (!en_fm && !en_am) {
        for (int i = 0; i < num_frames; i++) {
            phase_accumulator_carrier += r_c32;
            tempArrayElements[i] = volume * tempBuffer_carrier[phase_accumulator_carrier >> 22];
        }
    }

    if (!en_fm && en_am) {
        //jfloat tempBuffer_am[1024];
        //env->GetFloatArrayRegion(buffer_am, 0, 1024, tempBuffer_am);

        for (int i = 0; i < num_frames; i++) {
            phase_accumulator_carrier += r_c32;

            phase_accumulator_am += r_am32;
            tempArrayElements[i] = volume * tempBuffer_carrier[phase_accumulator_carrier >> 22]
                           *
                           (tempBuffer_am[phase_accumulator_am >> 22] * am_depth + 1.0f - am_depth);
        }
    }

    if (en_fm && !en_am) {
        //jfloat tempBuffer_fm[1024];
        //env->GetFloatArrayRegion(buffer_fm, 0, 1024, tempBuffer_fm);

        for (int i = 0; i < num_frames; i++) {
            phase_accumulator_fm += r_fm32;
            phase_accumulator_carrier += static_cast<unsigned int>(convertHzToR(
                    tempBuffer_fm[phase_accumulator_fm >> 22], sample_rate));

            tempArrayElements[i] = volume * tempBuffer_carrier[phase_accumulator_carrier >> 22];
        }
    }

    //auto *p = reinterpret_cast<jfloat *>(&floatArray);

    if (en_fm && en_am) {
        //jfloat tempBuffer_am[1024];
        //env->GetFloatArrayRegion(buffer_am, 0, 1024, tempBuffer_am);

        //jfloat tempBuffer_fm[1024];
        //env->GetFloatArrayRegion(buffer_fm, 0, 1024, tempBuffer_fm);

        for (int i = 0; i < num_frames; i++) {
            phase_accumulator_fm += r_fm32;
            phase_accumulator_carrier += static_cast<unsigned int>(convertHzToR(
                    tempBuffer_fm[phase_accumulator_fm >> 22], sample_rate));

            phase_accumulator_am += r_am32;
            tempArrayElements[i] = volume * tempBuffer_carrier[phase_accumulator_carrier >> 22]
                           *
                           (tempBuffer_am[phase_accumulator_am >> 22] * am_depth + 1.0f - am_depth);
        }

    }


//        for (int i = 0; i < num_frames; i++) {
//            if (en_fm) {
//                phase_accumulator_fm += r_fm32;
//                phase_accumulator_carrier += static_cast<unsigned int>(convertHzToR(
//                        //mBufferFmElements[phase_accumulator_fm >> 22]
//                        tempBuffer_fm[phase_accumulator_fm >> 22]
//                        , sample_rate));
//            } else
//                phase_accumulator_carrier += r_c32;
//
//            if (en_am) {
//                phase_accumulator_am += r_am32;
//                o = volume * (
//                        //mBufferCarrierElements[phase_accumulator_carrier >> 22]
//                                     tempBuffer_carrier[phase_accumulator_carrier >> 22] - 2048.0f) / 2048.0f
//                    *
//
//                    ((
//                            //mBufferAmElements[phase_accumulator_am >> 22]
//                             tempBuffer_am[phase_accumulator_am >> 22]
//                             / 4095.0f) * am_depth + 1.0f -
//                     am_depth)
//
////                    map(mBufferAmElements[phase_accumulator_am >> 22] / 4095.0f, 0.0f, 1.0f,
////                        1.0f - am_depth, 1.0f)
//
//
//                        ;
//            }
//            else {
//                o = volume * (
//                        //mBufferCarrierElements[phase_accumulator_carrier >> 22]
//                                     tempBuffer_carrier[phase_accumulator_carrier >> 22]
//                        - 2048.0f) / 2048.0f;
//            }
//            tempArray[i] = o;
//        }


    env->SetIntField(ch, phase_accumulator_carrierFid, phase_accumulator_carrier);
    env->SetIntField(ch, phase_accumulator_amFid, phase_accumulator_am);
    env->SetIntField(ch, phase_accumulator_fmFid, phase_accumulator_fm);

    env->ReleaseFloatArrayElements(floatArray, tempArrayElements, 0);
    //env->ReleaseShortArrayElements(buffer_carrier, mBufferCarrierElements, 0);
    //env->ReleaseShortArrayElements(buffer_am, mBufferAmElements, 0);
    //env->ReleaseShortArrayElements(buffer_fm, mBufferFmElements, 0);

    //env->ReleaseFloatArrayElements(floatArray, mBufferElements, 0);

    // Заполнение jfloatArray данными из tempArray
  //  env->SetFloatArrayRegion(floatArray, 0, num_frames, tempArray);

    // Очистка временного массива
    //delete[] tempArray;

//    delete[] tempBuffer_carrier;
//    delete[] tempBuffer_am;
//    delete[] tempBuffer_fm;

    // Возвращение jfloatArray в Kotlin
    return floatArray;
}

