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
JNIEXPORT void JNICALL
Java_com_example_generator2_features_generator_RenderChannel_jniRenderChannel(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jobject ch,
                                                                              jint num_frames,
                                                                              jint sample_rate,
                                                                              jfloatArray m_buffer,
                                                                              jint r_c,
                                                                              jint r_am,
                                                                              jint r_fm,
                                                                              jboolean en_ch,
                                                                              jboolean en_am,
                                                                              jboolean en_fm,
                                                                              jfloat volume,
                                                                              jfloat am_depth) {
    jclass cls = env->GetObjectClass(ch);

    jfieldID fid = env->GetFieldID(cls, "ch", "I");

    int channel = env->GetIntField(ch, fid);

    // buffer_carrier
    fid = env->GetFieldID(cls, "buffer_carrier", "[S");
    jshortArray buffer_carrier = (jshortArray) env->GetObjectField(ch, fid);

    // buffer_am
    fid = env->GetFieldID(cls, "buffer_am", "[S");
    jshortArray buffer_am = (jshortArray) env->GetObjectField(ch, fid);

    // buffer_fm
    fid = env->GetFieldID(cls, "buffer_fm", "[S");
    jshortArray buffer_fm = (jshortArray) env->GetObjectField(ch, fid);

    // source_buffer_fm
    fid = env->GetFieldID(cls, "source_buffer_fm", "[S");
    jshortArray source_buffer_fm = (jshortArray) env->GetObjectField(ch, fid);

    // phase_accumulator_carrier
    jfieldID phase_accumulator_carrierFid = env->GetFieldID(cls, "phase_accumulator_carrier", "I");
    uint32_t phase_accumulator_carrier = static_cast<unsigned int>(env->GetIntField(ch, phase_accumulator_carrierFid));

    // phase_accumulator_am
    jfieldID phase_accumulator_amFid= env->GetFieldID(cls, "phase_accumulator_am", "I");
    uint32_t phase_accumulator_am = static_cast<unsigned int>(env->GetIntField(ch, phase_accumulator_amFid));

    // phase_accumulator_fm
    jfieldID phase_accumulator_fmFid = env->GetFieldID(cls, "phase_accumulator_fm", "I");
    uint32_t phase_accumulator_fm = static_cast<unsigned int>(env->GetIntField(ch, phase_accumulator_fmFid));

    short *mBufferCarrierElements = env->GetShortArrayElements(buffer_carrier, nullptr);
    short *mBufferAmElements = env->GetShortArrayElements(buffer_am, nullptr);
    short *mBufferFmElements = env->GetShortArrayElements(buffer_fm, nullptr);

    float *mBufferElements = env->GetFloatArrayElements(m_buffer, nullptr);

    float o;

    uint32_t r_fm32 = (uint32_t) r_fm;
    uint32_t r_am32 = (uint32_t) r_am;
    uint32_t r_c32 = (uint32_t) r_c;
//
    for (int i = 0; i < num_frames; ++i) {

        if (en_ch) {

            if (en_fm) {

                phase_accumulator_fm += r_fm32;

                phase_accumulator_carrier += static_cast<unsigned int>(convertHzToR(
                        mBufferFmElements[phase_accumulator_fm >> 22], sample_rate));

            } else
                phase_accumulator_carrier += r_c32;


            if (en_am) {

                phase_accumulator_am += r_am32;

                o = volume * (
                        mBufferCarrierElements[phase_accumulator_carrier >> 22]
                        - 2048.0f) / 2048.0f
                    *

                        ((mBufferAmElements[phase_accumulator_am >> 22] / 4095.0f) * am_depth + 1.0f - am_depth)


//                    map(mBufferAmElements[phase_accumulator_am >> 22] / 4095.0f, 0.0f, 1.0f,
//                        1.0f - am_depth, 1.0f)


                        ;


            } else {
                o = volume * (mBufferCarrierElements[phase_accumulator_carrier >> 22] - 2048.0f) /
                    2048.0f;
            }
        } else
            o = 0.0f;
//
//
        mBufferElements[i] = o;
//
    }
//
//


    env->SetIntField(ch, phase_accumulator_carrierFid, phase_accumulator_carrier);
    env->SetIntField(ch, phase_accumulator_amFid, phase_accumulator_am);
    env->SetIntField(ch, phase_accumulator_fmFid, phase_accumulator_fm);

    env->ReleaseShortArrayElements(buffer_carrier, mBufferCarrierElements, 0);
    env->ReleaseShortArrayElements(buffer_am, mBufferAmElements, 0);
    env->ReleaseShortArrayElements(buffer_fm, mBufferFmElements, 0);

    env->ReleaseFloatArrayElements(m_buffer, mBufferElements, 0);
}

