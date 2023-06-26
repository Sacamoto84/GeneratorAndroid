#ifndef SAMPLES_SOUNDGENERATOR_H
#define SAMPLES_SOUNDGENERATOR_H

#include <logging_macros.h>
#include <vector>

#include "generator.h"

#include "signal.h"

extern void send(const std::vector<float> &value);

class SoundGenerator : public IRenderableAudio {
public:

    SoundGenerator(int32_t sampleRate, int32_t channelCount) {

        LOGI("_-_ SoundGenerator::SoundGenerator Constructor_-_");
        init();
        LOGI("_-_ SoundGenerator::SoundGenerator sampleRate %d channelCount %d _-_", sampleRate,
             channelCount);
    }

    ~SoundGenerator() = default;

    SoundGenerator(SoundGenerator &&other) = default;

    SoundGenerator &operator=(SoundGenerator &&other) = default;

    //Рендер звука
    void renderAudio(float *audioData, int32_t numFrames) override {

        //LOGI("numFrames:%d",numFrames);

        //CH1.Volume = 0.65F;
        //CH2.Volume = 0.55F;

        if (!Mono) {

            //stereo
            if (!CH1.impulseMode)
                renderChanel(&CH1, numFrames);
            else
                //renderImpulseChanel(&CH1, numFrames);
                renderImpulseSine50Chanel(&CH1, numFrames);


            if (!CH2.impulseMode)
                renderChanel(&CH2, numFrames);
            else
                //renderImpulseChanel(&CH2, numFrames);

                renderImpulseSine50Chanel(&CH2, numFrames);

            //Нормальный режим
            if (!shuffle)
                for (int i = 0; i < numFrames; i++) {
                    if (enL) audioData[i * 2] = CH1.mBuffer[i]; else audioData[i * 2] = 0;
                    if (enR) audioData[i * 2 + 1] = CH2.mBuffer[i]; else audioData[i * 2 + 1] = 0;
                }
            else
                for (int i = 0; i < numFrames; i++) {
                    if (enL) audioData[i * 2] = CH2.mBuffer[i]; else audioData[i * 2] = 0;
                    if (enR) audioData[i * 2 + 1] = CH1.mBuffer[i]; else audioData[i * 2 + 1] = 0;
                }


        } else {
            //Mono
            renderChanel(&CH1, numFrames);

            if (!Invert) {

                for (int i = 0; i < numFrames; i++) {
                    if (enL)
                        audioData[i * 2] = CH1.mBuffer[i];
                    else
                        audioData[i * 2] = 0;

                    if (enR)
                        audioData[i * 2 + 1] = CH1.mBuffer[i];
                    else
                        audioData[i * 2 + 1] = 0;
                }

            } else {
                //Invert
                for (int i = 0; i < numFrames; i++) {

                    if (enL)
                        audioData[i * 2] = CH1.mBuffer[i];
                    else
                        audioData[i * 2] = 0;

                    if (enR)
                        audioData[i * 2 + 1] = CH1.mBuffer[i] * (-1.0f);
                    else
                        audioData[i * 2 + 1] = 0;

                }
            }
        }

        std::vector<float> data(audioData, audioData + numFrames);
        send(data);

    }

    //std::unique_ptr<float[]> mBuffer = std::make_unique<float[]>(4096);

    //Импульсный режим
    void renderImpulseChanel(_structure_ch *CH, int numFrames) {

        float O = 0.0F;
        int timeImp = CH->timeImp;
        int timeImpPause = CH->timeImpPause;

        for (int i = 0; i < numFrames; i++) {

            if (CH->CH_EN) {

                int deltaTime = CH->impulseGlobalTime - CH->impulseStartTime;

                if ((deltaTime >= 0) && (deltaTime < timeImp))
                    O = 1.0F;

                if ((deltaTime >= timeImp) && (deltaTime < timeImp + timeImpPause))
                    O = 0;

                if ((deltaTime >= timeImp + timeImpPause) &&
                    (deltaTime < 2 * timeImp + timeImpPause))
                    O = -1.0F;

                if (deltaTime >= 2 * timeImp + timeImpPause)
                    O = 0;


            } else
                O = 0;

            CH->mBuffer[i] = O;

            CH->impulseGlobalTime++;

            if (CH->impulseGlobalTime % 4800 == 0)
                CH->impulseStartTime = CH->impulseGlobalTime;

        }

    }

//    enum class State
//    {
//        NotStarted,
//        Working,
//        Shutdown
//    };





    //Импульсный режим Sine 50Hz
    void renderImpulseSine50Chanel(_structure_ch *CH, int numFrames) {

        float O;

        int valueFr = constrain(parameterInt0, 1, 50); //Частота импульсов
        float valueTimeImp = constrain(parameterFloat0, 0.5F, 5.0F); //Время импульсов

        for (int i = 0; i < numFrames; i++) {

            if (parameterInt2 == 1) {
                parameterInt2 = 0;
                CH->impulse50StartFireTime = CH->impulseGlobalTime;
            }

            if ((CH->impulseGlobalTime % (int) (48000 / (valueFr)) == 0) &&
                ((CH->impulseGlobalTime - CH->impulse50StartFireTime) <= (int)(valueTimeImp * 48000.0F)))
                CH->impulseStartTime = CH->impulseGlobalTime;

            if (CH->CH_EN) {

                int deltaTime = CH->impulseGlobalTime - CH->impulseStartTime;

                if (deltaTime < 960)
                    O = CH->Volume * (float) (SINE_960[deltaTime] - 2048) / 2048.0F;
                else
                    O = 0;

            } else
                O = 0;

            CH->mBuffer[i] = O;

            CH->impulseGlobalTime++;

        }

    }

    void renderChanel(_structure_ch *CH, int numFrames) {

        float O;

        CH->rC = (uint32_t) convertHzToR(CH->Carrier_fr);
        CH->rAM = (uint32_t) convertHzToR(CH->AM_fr);
        CH->rFM = (uint32_t) convertHzToR(CH->FM_mod_fr);

        //std::fill_n(CH->mBuffer, numFrames, 0);

        for (int i = 0; i < numFrames; i++) {

            if (CH->CH_EN) {

                if (CH->FM_EN) {
                    CH->phase_accumulator_fm = CH->phase_accumulator_fm + CH->rFM;
                    CH->phase_accumulator_carrier = CH->phase_accumulator_carrier +
                                                    (uint32_t) convertHzToR(
                                                            CH->buffer_fm[CH->phase_accumulator_fm
                                                                    >> 22]);
                    //+ (uint32_t) (
                    //(CH->buffer_fm[CH->phase_accumulator_fm >> 22]) * 1000.0F * 3000.0F/33.21F );
                } else
                    CH->phase_accumulator_carrier += CH->rC;


                if (CH->AM_EN) {
                    CH->phase_accumulator_am = CH->phase_accumulator_am + CH->rAM;

                    O = CH->Volume
                        * (float) (CH->buffer_carrier[CH->phase_accumulator_carrier >> 22] -
                                   2048.0F) / 2048.0F  //-1..1




                        //* ((float) CH->buffer_am[CH->phase_accumulator_am >> 22] / 4095.0F); //0..1

                        *
                        map(((float) CH->buffer_am[CH->phase_accumulator_am >> 22] / 4095.0F), 0.0F,
                            1.0F, 1.0F - CH->AmDepth, 1.0F);


                } else
                    O = CH->Volume
                        * (float) (CH->buffer_carrier[CH->phase_accumulator_carrier >> 22] -
                                   2048.0F) / 2048.0F;

            } else
                O = 0;

            CH->mBuffer[i] = O;
        }

    }


    void init() {
        LOGI("-----------------------");
        LOGI("---init()---");
        LOGI("-----------------------");
        int i;
        i = 0;
        CH1.CH_EN = 0;
        CH2.CH_EN = 0;

        for (i = 0; i < 1024; i++) {
            CH1.buffer_carrier[i] = Ramp_1024[i];
            CH2.buffer_carrier[i] = Ramp_1024[i];
            CH1.buffer_am[i] = Ramp_1024[i];
            CH2.buffer_am[i] = Ramp_1024[i];
        }
        CH1.Carrier_fr = 1000;
        CH2.Carrier_fr = 1000;
        CH1.AM_fr = 10.0;
        CH2.AM_fr = 10.0;
        for (i = 0; i < 1024; i++) {
            CH1.buffer_fm[i] = 2500;
            CH2.buffer_fm[i] = 2500;
        }

        CH1.Volume = 0.65F;
        CH2.Volume = 0.55F;

        setToStereo();
        resetAllPhase();

        enL = true;
        enR = true;

        CH1.ch = 1;
        CH2.ch = 2;

    }

    //std::unique_ptr<uint16_t[]> buffer_carrier1 = std::make_unique<uint16_t[]>(1024);

    float convertHzToR(float hz) {
        hz = hz * 16384.0F / 3.798F * 2.0F * 1000.0 / 48.8 / 2.0 * 1000.0 / 988.0;
        return hz;
    }

    void CreateFM_CH1(void) {
        int x, y;
        int i = 0;
        x = CH1.Carrier_fr - CH1.FM_Dev;
        y = CH1.FM_Dev * 2;
        for (i = 0; i < 1024; i++)
            CH1.buffer_fm[i] = x + (y * CH1.source_buffer_fm[i] / 4095.0F);
    }

    void CreateFM_CH2(void) {
        int x, y;
        int i = 0;
        x = CH2.Carrier_fr - CH2.FM_Dev;
        y = CH2.FM_Dev * 2;
        for (i = 0; i < 1024; i++)
            CH2.buffer_fm[i] = x + (y * CH2.source_buffer_fm[i] / 4095.0F);
    }

    float map(float x, long in_min, float in_max, float out_min, float out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }


    template<class T>
    const T &constrain(const T &x, const T &a, const T &b) {
        if (x < a) {
            return a;
        } else if (b < x) {
            return b;
        } else
            return x;
    }

};


#endif
