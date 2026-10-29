//
// Рендер режима «3-фаза» (по мотивам restim).
//
// Формулы — порт stim_math/threephase.py, эталон на Kotlin —
// ThreePhaseMath.sample. Электроды: N (верх круга), L, R; каналы:
// L = V_N − V_L, R = V_N − V_R.
//
#include <jni.h>
#include <algorithm>
#include <cmath>
#include "threephase.h"

namespace {

constexpr int SINE_SIZE = 1024;
constexpr uint32_t QUARTER_TURN = 0x40000000u;

/** Синус на полный оборот + одна точка, чтобы интерполяции не нужен перенос индекса. */
struct SineTable {
    float v[SINE_SIZE + 1];

    SineTable() {
        for (int i = 0; i <= SINE_SIZE; i++)
            v[i] = (float) std::sin(2.0 * M_PI * i / SINE_SIZE);
    }
};

const SineTable sineTable;

inline float sineAt(uint32_t phase) {
    const uint32_t idx = phase >> 22;
    const float frac = (float) (phase & 0x3FFFFFu) * (1.0f / 4194304.0f);
    return sineTable.v[idx] + (sineTable.v[idx + 1] - sineTable.v[idx]) * frac;
}

ThreePhaseState state;

} // namespace

void threePhaseRender(ThreePhaseState &s, int numFrames, int sampleRate,
                      const float *points, int pointCount, int step,
                      const float *params, float carrierHz, float volume,
                      float maxVolume0, float maxVolume1,
                      float *outL, float *outR) {

    //Громкость и потолок не выше 1 даже при битом пресете
    volume = std::clamp(volume, 0.0f, 1.0f);
    maxVolume0 = std::clamp(maxVolume0, 0.0f, 1.0f);
    maxVolume1 = std::clamp(maxVolume1, 0.0f, 1.0f);

    //Калибровка: от прошлого блока к текущему
    const float *k0 = params;
    const float *k1 = params + 4;
    const float c0 = std::clamp(params[8], 0.0f, 20.0f) / 100.0f;
    const float c1 = std::clamp(params[9], 0.0f, 20.0f) / 100.0f;

    const auto dPhase = (uint32_t) (4294967296.0 / sampleRate * carrierHz);

    const float v0 = s.prevVolume;
    const float dv = volume - v0;

    for (int i = 0; i < numFrames; i++) {

        //Доля пройденного блока для рамп громкости и калибровки
        const float u = (float) (i + 1) / (float) numFrames;

        //Позиция: линейно между контрольными точками
        const int j = i / step;
        const int j1 = std::min(j + 1, pointCount - 1);
        const int s0 = j * step;
        const int len = std::min(step, numFrames - s0);
        const float f = (float) (i - s0) / (float) len;
        float a = points[2 * j] + (points[2 * j1] - points[2 * j]) * f;
        float b = points[2 * j + 1] + (points[2 * j1 + 1] - points[2 * j + 1]) * f;

        //В единичный круг
        float r = std::sqrt(a * a + b * b);
        if (r > 1.0f) {
            a /= r;
            b /= r;
            r = 1.0f;
        }

        //squeeze restim; знак t12 как у них, вместе с их TODO
        const float t11 = (2.0f - r + a) * 0.5f;
        const float t12 = -b * 0.5f;
        const float t22 = (2.0f - r - a) * 0.5f;

        const float x = sineAt(s.phase + QUARTER_TURN); //cos
        const float y = sineAt(s.phase);                //sin
        s.phase += dPhase;

        const float sa = t11 * x + t12 * y;
        const float sb = t12 * x + t22 * y;

        //Строки K — выпуклая смесь строк с нормой ≤ 1, поэтому и рампа не выходит за 1
        const float l = (k0[0] + (k1[0] - k0[0]) * u) * sa + (k0[1] + (k1[1] - k0[1]) * u) * sb;
        const float rr = (k0[2] + (k1[2] - k0[2]) * u) * sa + (k0[3] + (k1[3] - k0[3]) * u) * sb;

        const float c = c0 + (c1 - c0) * u;
        const float g = (v0 + dv * u) * (1.0f - c * (1.0f - r));

        outL[i] = std::clamp(l * g * maxVolume0, -1.0f, 1.0f);
        outR[i] = std::clamp(rr * g * maxVolume1, -1.0f, 1.0f);
    }

    s.prevVolume = volume;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_features_threephase_RenderThreePhase_jniRender(
        JNIEnv *env, jobject thiz,
        jint num_frames, jint sample_rate,
        jfloatArray points, jint point_count, jint step,
        jfloatArray params, jfloat carrier_hz, jfloat volume,
        jfloat max_volume0, jfloat max_volume1,
        jfloatArray out_l, jfloatArray out_r) {

    if (num_frames <= 0 || point_count < 1 || step <= 0 || sample_rate <= 0) return;

    ThreePhaseState &s = state;

    //Буферы растут только при увеличении блока — без аллокаций в обычном режиме
    const auto pointFloats = (size_t) point_count * 2;
    if (s.points.size() < pointFloats) s.points.resize(pointFloats);
    if (s.bufL.size() < (size_t) num_frames) {
        s.bufL.resize(num_frames);
        s.bufR.resize(num_frames);
    }

    env->GetFloatArrayRegion(points, 0, (jsize) pointFloats, s.points.data());
    float p[THREE_PHASE_PARAMS];
    env->GetFloatArrayRegion(params, 0, THREE_PHASE_PARAMS, p);

    threePhaseRender(s, num_frames, sample_rate,
                     s.points.data(), point_count, step,
                     p, carrier_hz, volume,
                     max_volume0, max_volume1,
                     s.bufL.data(), s.bufR.data());

    env->SetFloatArrayRegion(out_l, 0, num_frames, s.bufL.data());
    env->SetFloatArrayRegion(out_r, 0, num_frames, s.bufR.data());
}
