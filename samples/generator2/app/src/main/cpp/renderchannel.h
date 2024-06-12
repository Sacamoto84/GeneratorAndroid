//
// Created by user on 12.06.2024.
//

#ifndef GENERATOR2_RENDERCHANNEL_H
#define GENERATOR2_RENDERCHANNEL_H


#include <vector>

struct StructureCh {
    int ch;
    std::vector<uint16_t> buffer_carrier;
    std::vector<uint16_t> buffer_am;
    std::vector<uint16_t> buffer_fm;
    std::vector<uint16_t> source_buffer_fm;

    uint32_t phase_accumulator_carrier;
    uint32_t phase_accumulator_am;
    uint32_t phase_accumulator_fm;

    StructureCh() : ch(0),
                    buffer_carrier(1024),
                    buffer_am(1024),
                    buffer_fm(1024),
                    source_buffer_fm(1024),
                    phase_accumulator_carrier(0),
                    phase_accumulator_am(0),
                    phase_accumulator_fm(0) {}
};


class RenderChannel {
private:

    std::vector<float> mBuffer;
    float o;
    int sampleRate;

public:
    RenderChannel();

    std::vector<float> renderChanel(StructureCh* CH, int numFrames, int sampleRate);

private:
    float convertHzToR(float hz);
    float map(float x, float in_min, float in_max, float out_min, float out_max);
};





float RenderChannel::convertHzToR(float hz) {
    return (48000.0f / sampleRate) * (hz * 16384.0f / 3.798f * 2.0f * 1000.0 / 48.8 / 2.0 * 1000.0 / 988.0);
}

float RenderChannel::map(float x, float in_min, float in_max, float out_min, float out_max) {
    return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}



std::vector<float> RenderChannel::renderChanel(StructureCh *CH, int numFrames, int sampleRate) {
    return std::vector<float>();
}

#endif //GENERATOR2_RENDERCHANNEL_H
