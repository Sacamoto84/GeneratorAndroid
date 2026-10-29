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

/** Число float в params: K от/до (по 4), центр от/до. */
constexpr int THREE_PHASE_PARAMS = 10;

/**
 * Посэмпловый рендер: точка (alpha, beta) из линейной интерполяции
 * контрольных точек, squeeze-матрица restim, калибровочная матрица K (2×2),
 * приглушение центра, потолок по каналам. Громкость, K и центр линейно
 * рампируются по блоку — смена ползунка или калибровки не щёлкает.
 *
 * points — [a0, b0, a1, b1, …], точка j соответствует сэмплу min(j·step, numFrames).
 * params — [K₀ 4 шт., K₁ 4 шт., центр₀ %, центр₁ %], см. ThreePhaseEngine.blockParams.
 */
void threePhaseRender(ThreePhaseState &s, int numFrames, int sampleRate,
                      const float *points, int pointCount, int step,
                      const float *params, float carrierHz, float volume,
                      float maxVolume0, float maxVolume1,
                      float *outL, float *outR);

#endif //GENERATOR2_THREEPHASE_H
