#ifndef MYFFT_H
#define MYFFT_H

#include <cmath>
#include <cassert>
#include "Processor.h"
#include "fftw3.h"
#include "auformat.h"

#define REAL 0
#define IMAG 1

class myFFT : public Processor {
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
     * План FFT
     */
    fftwf_plan m_plan;

    float m_sampleRate;


    void init(int length) {
        m_length = length;

        m_fftScaling = 0;
        for (int i = 0; i < m_length; i++)
            m_fftScaling += hamming(i);

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

    [[nodiscard]] int getBins() const override { return m_length / 2; }

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
            val *= hamming(ii);

            assert(ii < m_length);
            m_in[ii][REAL] = val;
            m_in[ii][IMAG] = 0;
        }
    }


    void computePower(float decay)  {

        // Выполнение преобразования Фурье
        fftwf_execute(m_plan);

        float totalPower = 0;

        //Заполнение bins
        float *m_rout = m_pOutput->GetData();

        for (int i = 0; i < getBins(); i++) {
            float power = sqrt(m_out[i][REAL] * m_out[i][REAL] + m_out[i][IMAG] * m_out[i][IMAG]);
            power *= (2 / m_fftScaling);
            m_rout[i] = m_rout[i] * decay + power * (1.0f - decay);

            totalPower += power;
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
};

#endif