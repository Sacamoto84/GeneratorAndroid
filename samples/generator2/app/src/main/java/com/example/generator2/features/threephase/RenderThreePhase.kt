package com.example.generator2.features.threephase

/**
 * Обёртка нативного рендера режима «3-фаза» (`cpp/generator/threephase.cpp`).
 * Состояние (фаза несущей, рампа громкости) живёт в нативе.
 */
object RenderThreePhase {

    init {
        System.loadLibrary("plasma")
    }

    /**
     * Отрендерить [numFrames] сэмплов в [outL]/[outR].
     *
     * @param points контрольные точки [ThreePhaseEngine.points], [pointCount] штук
     * @param k матрица калибровки [ThreePhaseMath.channelMatrix]
     * @param maxVolume0 потолок левого канала (Config)
     * @param maxVolume1 потолок правого канала (Config)
     */
    external fun jniRender(
        numFrames: Int,
        sampleRate: Int,
        points: FloatArray,
        pointCount: Int,
        step: Int,
        k: FloatArray,
        carrierHz: Float,
        volume: Float,
        centerReductionPct: Float,
        maxVolume0: Float,
        maxVolume1: Float,
        outL: FloatArray,
        outR: FloatArray,
    )
}
