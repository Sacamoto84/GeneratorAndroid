//
// Рендер режима «3-фаза» (по мотивам restim).
//

#ifndef GENERATOR2_THREEPHASE_H
#define GENERATOR2_THREEPHASE_H

#include <cstdint>
#include <vector>

/**
 * Состояние трёхфазного рендера между блоками. Пишет и читает только
 * аудиопоток.
 */
struct ThreePhaseState {
    uint32_t phase = 0;          // DDS-фаза несущей, полный оборот = 2^32
    float prevVolume = 0.0f;     // громкость конца прошлого блока — старт рампы
    std::vector<float> points;   // копия контрольных точек блока
    std::vector<float> bufL;     // переиспользуемые выходные буферы,
    std::vector<float> bufR;     // растут только при увеличении блока
};

/**
 * Посэмпловый рендер: точка (alpha, beta) из линейной интерполяции
 * контрольных точек, squeeze-матрица restim, калибровочная матрица k (2×2),
 * приглушение центра, рампа громкости по блоку, потолок по каналам.
 *
 * points — [a0, b0, a1, b1, …], точка j соответствует сэмплу min(j·step, numFrames).
 */
void threePhaseRender(ThreePhaseState &s, int numFrames, int sampleRate,
                      const float *points, int pointCount, int step,
                      const float *k, float carrierHz, float volume, float centerReductionPct,
                      float maxVolume0, float maxVolume1,
                      float *outL, float *outR);

#endif //GENERATOR2_THREEPHASE_H
