#ifndef MYFFT_H
#define MYFFT_H

#include <cmath>
#include <cassert>
#include "fftw3.h"
#include "auformat.h"
#include "BufferIO.h"

#define REAL 0
#define IMAG 1

/**
 * Длина FFT. Вместе с FFT_DECIM (см. Decimator.h) задаёт размен время/частота,
 * обойти его нельзя: произведение разрешений всегда порядка единицы.
 *
 *   8192 @ 12 кГц -> шаг 1.46 Гц, окно 683 мс
 *   4096 @ 12 кГц -> шаг 2.93 Гц, окно 341 мс
 *   2048 @ 12 кГц -> шаг 5.86 Гц, окно 171 мс (как было до прореживания)
 */
#define LENPOINT 4096

class myFFT {

    BufferIODouble *m_pOutput = nullptr;

    /**
     * Указатель на входной массив комплексных чисел (типа fftwf_complex), которые будут подвергнуты преобразованию.
     */
    fftwf_complex *m_in = nullptr;

    /**
     * Указатель на выходной массив комплексных чисел (типа fftwf_complex), в который будут записаны результаты преобразования.
     */
    fftwf_complex *m_out = nullptr;

    /**
     * Длина массива данных (число точек, по которым будет выполняться преобразование Фурье). 4096
     */
    int m_length;

    float m_fftScaling;

    /**
     * Предрасчитанное окно длиной m_length. Считать окно на каждом кадре дорого:
     * Блэкман-Харрис это три косинуса на отсчёт, а кадров под сотню в секунду.
     */
    float *m_window = nullptr;

    /**
     * План FFT
     */
    fftwf_plan m_plan;

    void init(int length) {
        m_length = length;

        delete[] (m_window);
        m_window = new float[m_length];

        m_fftScaling = 0;
        for (int i = 0; i < m_length; i++) {
            m_window[i] = blackmanHarris(i);
            m_fftScaling += m_window[i];
        }

        //Выделение памяти для fftwf_complex float[2] * 4096
        m_in = (fftwf_complex *) fftwf_malloc(sizeof(fftwf_complex) * m_length);
        m_out = (fftwf_complex *) fftwf_malloc(sizeof(fftwf_complex) * m_length);

        //FFTW_FORWARD: Флаг, указывающий, что должно быть выполнено прямое преобразование Фурье.
        //FFTW_ESTIMATE: Флаг, указывающий, что FFTW должна использовать быстрый алгоритм для оценки плана, без выполнения тестовых преобразований для оптимизации.

        //Это функция из библиотеки FFTW, которая создаёт план для одномерного комплексного преобразования Фурье.
        m_plan = fftwf_plan_dft_1d(m_length, m_in, m_out, FFTW_FORWARD, FFTW_ESTIMATE);

        //Создать буфер из float размером m_length/2 или количество bins
        m_pOutput = new BufferIODouble(getBins());
        m_pOutput->clear();
    }

    void deinit() {
        delete (m_pOutput);
        m_pOutput = nullptr;

        delete[] (m_window);
        m_window = nullptr;

        fftwf_destroy_plan(m_plan);
        m_plan = nullptr;

        fftwf_free(m_out);
        m_out = nullptr;

        fftwf_free(m_in);
        m_in = nullptr;
    }

public:
    ~myFFT() {
        deinit();
    }

    float m_sampleRate;

    [[nodiscard]] BufferIODouble *getBufferIO() const
    {
        return m_pOutput;
    }


    virtual const char *GetName() const { return "FFT"; };

    void init(int length, float sampleRate) {
        m_sampleRate = sampleRate;
        init(length);
    }

    /**
     * Прочесть длину данных 4096
     * @return
     */
    [[nodiscard]] int getProcessedLength() const { return m_length; }

    [[nodiscard]] int getBins() const { return m_length / 2; }

    float hanning(int i) const {
        return static_cast<float>(0.50 - 0.50 * cos((2 * M_PI * i) / (m_length - 1.0)));
    }

    /**
     * Вычисляет значение Хэммингового окна, m_length длина окна
     * @param i индекс
     */
    [[nodiscard]] float hamming(int i) const {
        return static_cast<float>(0.54f - 0.46f * cos((2.0f * M_PI * i) /
                                                      (static_cast<float>(m_length) - 1.0f)));
    }

    /**
     * Четырёхчленное окно Блэкмана-Харриса.
     *
     * Боковые лепестки -92 дБ против -43 дБ у Хэмминга. Для генератора это
     * важнее ширины главного лепестка: гармоники и интермодуляции перестают
     * тонуть в утечке от мощной основной частоты. Плата - главный лепесток
     * шириной 8 бинов вместо 4.
     */
    [[nodiscard]] float blackmanHarris(int i) const {
        const double n = (2.0 * M_PI * i) / (static_cast<double>(m_length) - 1.0);

        return static_cast<float>(0.35875
                                  - 0.48829 * cos(n)
                                  + 0.14128 * cos(2.0 * n)
                                  - 0.01168 * cos(3.0 * n));
    }

    /**
     * Заполнение входного буфера m_in данными
     * @param input
     * @param offsetDest
     * @param length
     */
    void convertShortToFFT(const AU_FORMAT *input, int offsetDest, int length) {
        assert(m_length >= offsetDest + length);

        for (int i = 0; i < length; i++) {
            float val = Uint16ToFloat(&input[i]);

            int ii = i + offsetDest;
            val *= m_window[ii];

            assert(ii < m_length);
            m_in[ii][REAL] = val;
            m_in[ii][IMAG] = 0;
        }
    }

    /**
    * Заполнение входного буфера m_in данными
    * @param input
    * @param offsetDest
    * @param length
    */
    void convertFloatToFFT(const float *input, int length) {
        for (int i = 0; i < length; i++) {
            float val = input[i];
            val *= m_window[i];
            m_in[i][REAL] = val;
            m_in[i][IMAG] = 0;
        }
    }



    void computePower(float decay)  {

        // Выполнение преобразования Фурье
        fftwf_execute(m_plan);

        //float totalPower = 0;

        //Заполнение bins
        float *m_rout = m_pOutput->GetData();

        for (int i = 0; i < getBins(); i++) {
            float power = sqrt(m_out[i][REAL] * m_out[i][REAL] + m_out[i][IMAG] * m_out[i][IMAG]);
            power *= (2 / m_fftScaling);
            m_rout[i] = m_rout[i] * decay + power * (1.0f - decay);

            //totalPower += power;
        }
    }


    float bin2Freq(int bin) const {
        if (bin == 0)
            return 0;

        return (m_sampleRate * (float) bin) / (float) getProcessedLength();
    }

    float freq2Bin(float freq) const {
        if (freq == 0)
            return 0;

        return (freq * (float) getProcessedLength()) / m_sampleRate;
    }

    /** Ширина одного бина в герцах. */
    [[nodiscard]] float binWidthHz() const {
        return m_sampleRate / (float) getProcessedLength();
    }

    /**
     * Находит пик в окне +-searchHz вокруг частоты freqHz и уточняет его
     * положение параболой по трём точкам логарифма магнитуды.
     *
     * Обычный поиск максимума даёт точность +-полбина. Парабола по логарифму
     * для гладкого окна снимает большую часть этой ошибки, доводя отсчёт до
     * сотых долей бина.
     *
     * @return частота пика в герцах либо -1, если данных нет
     */
    [[nodiscard]] float findPeak(float freqHz, float searchHz) const {
        if (m_pOutput == nullptr)
            return -1;

        const int bins = getBins();

        int first = (int) floorf(freq2Bin(freqHz - searchHz));
        int last = (int) ceilf(freq2Bin(freqHz + searchHz));

        // Крайние бины исключены: параболе нужны соседи с обеих сторон.
        if (first < 1) first = 1;
        if (last > bins - 2) last = bins - 2;
        if (last < first)
            return -1;

        const float *p = m_pOutput->GetData();

        int best = first;
        for (int i = first; i <= last; i++) {
            if (p[i] > p[best]) best = i;
        }

        const float a = logf(fmaxf(p[best - 1], 1e-20f));
        const float b = logf(fmaxf(p[best], 1e-20f));
        const float c = logf(fmaxf(p[best + 1], 1e-20f));

        const float denom = a - 2.0f * b + c;

        float delta = (denom != 0.0f) ? 0.5f * (a - c) / denom : 0.0f;

        // Вершина параболы вне соседних бинов означает, что это не пик,
        // а склон - смещение обрезаем, чтобы не увести отсчёт.
        if (delta < -0.5f) delta = -0.5f;
        if (delta > 0.5f) delta = 0.5f;

        return bin2Freq(best) + delta * binWidthHz();
    }
};

#endif