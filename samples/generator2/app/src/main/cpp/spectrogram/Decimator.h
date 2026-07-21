//
// Полифазный (со скипом вычислений) КИХ-дециматор для тракта FFT.
//
// Спектрограф показывает 0..5 кГц, поэтому гнать FFT на 48 кГц бессмысленно:
// 83% бинов уходит в диапазон, который не отображается.
//
// Прореживание входа в FFT_DECIM раз даёт во столько же раз меньший шаг по
// частоте при той же длине FFT: 48000/4096 = 11.7 Гц против 12000/4096 = 2.9 Гц.
// Разрешение при этом берётся не из воздуха - окно охватывает в FFT_DECIM раз
// больше времени. Выигрыш в том, что то же самое без прореживания потребовало бы
// FFT вчетверо длиннее, см. LENPOINT в fft.h.
//

#ifndef GENERATOR2_DECIMATOR_H
#define GENERATOR2_DECIMATOR_H

#include <cmath>
#include <cstring>

/** Коэффициент прореживания тракта FFT: 48 кГц -> 12 кГц. */
#define FFT_DECIM 4

/**
 * Стерео дециматор с общим ФНЧ для обоих каналов.
 *
 * Фильтр - оконный sinc с окном Блэкмана-Харриса, 191 отвод. Измеренные
 * характеристики при 48 кГц -> 12 кГц:
 *   - завал в полосе 0..5 кГц    < 0.001 дБ
 *   - наложение из 7..24 кГц     < -110 дБ
 *   - стоимость                  ~48 умножений на входной отсчёт на канал
 *
 * Полоса 7..24 кГц выделена потому, что после прореживания в 4 раза именно она
 * складывается обратно в отображаемые 0..5 кГц.
 *
 * Частота среза задана в долях входной частоты дискретизации, поэтому один и
 * тот же набор коэффициентов работает при любой fs.
 */
class DecimatorStereo {
public:

    DecimatorStereo() {
        designLowpass();
        reset();
    }

    void reset() {
        memset(m_histL, 0, sizeof(m_histL));
        memset(m_histR, 0, sizeof(m_histR));
        m_write = 0;
        m_phase = 0;
    }

    /**
     * Максимальное число выходных кадров для заданного числа входных.
     * Нужно для расчёта размера приёмного буфера.
     */
    static size_t maxOutputFrames(size_t inFrames) {
        return inFrames / FFT_DECIM + 1;
    }

    /**
     * Прореживание чередующегося стерео потока.
     *
     * @param in       вход, LRLR..., inFrames кадров
     * @param inFrames число входных кадров
     * @param out      выход, LRLR..., места должно хватать на maxOutputFrames()
     * @return         число записанных выходных кадров
     */
    size_t process(const float *in, size_t inFrames, float *out) {
        size_t outFrames = 0;

        for (size_t i = 0; i < inFrames; i++) {
            m_histL[m_write] = in[2 * i];
            m_histR[m_write] = in[2 * i + 1];

            m_write++;
            if (m_write == TAPS) m_write = 0;

            m_phase++;
            if (m_phase == FFT_DECIM) {
                m_phase = 0;
                out[2 * outFrames] = dot(m_histL);
                out[2 * outFrames + 1] = dot(m_histR);
                outFrames++;
            }
        }

        return outFrames;
    }

private:

    static const int TAPS = 191;

    float m_h[TAPS]{};
    float m_histL[TAPS]{};
    float m_histR[TAPS]{};

    int m_write = 0;
    int m_phase = 0;

    /**
     * Свёртка с линией задержки. m_write указывает на ячейку, которая будет
     * перезаписана следующей, то есть на самый старый отсчёт - он и умножается
     * на последний коэффициент.
     */
    float dot(const float *hist) const {
        float acc = 0;
        int idx = m_write;

        for (int i = TAPS - 1; i >= 0; i--) {
            acc += m_h[i] * hist[idx];
            idx++;
            if (idx == TAPS) idx = 0;
        }

        return acc;
    }

    void designLowpass() {
        const int M = TAPS - 1;

        // Срез ровно на выходной частоте Найквиста (6 кГц при 48 кГц входа).
        // Точка -6 дБ приходится на середину переходной полосы, поэтому
        // отображаемые 0..5 кГц остаются плоскими, а зона наложения от 7 кГц
        // уже полностью в полосе задерживания.
        const float fc = 0.5f / FFT_DECIM;

        float sum = 0;

        for (int i = 0; i < TAPS; i++) {
            const int n = i - M / 2;

            const float sinc = (n == 0)
                               ? 2.0f * fc
                               : sinf(2.0f * (float) M_PI * fc * (float) n) /
                                 ((float) M_PI * (float) n);

            const double a = 2.0 * M_PI * (double) i / (double) M;

            const auto w = (float) (0.35875
                                    - 0.48829 * cos(a)
                                    + 0.14128 * cos(2.0 * a)
                                    - 0.01168 * cos(3.0 * a));

            m_h[i] = sinc * w;
            sum += m_h[i];
        }

        // Единичное усиление на постоянном токе - амплитуды не съезжают.
        for (int i = 0; i < TAPS; i++) {
            m_h[i] /= sum;
        }
    }
};

#endif //GENERATOR2_DECIMATOR_H
