#ifndef SCALEBUFFERBASE_H
#define SCALEBUFFERBASE_H

#define _USE_MATH_DEFINES
#include <cmath>
#include <algorithm>
#include "fft.h"
#include "scale.h"
#include "BufferIO.h"

class ScaleBufferBase
{
public:
    BufferIODouble *m_pOutput = nullptr;

public:
    virtual ~ScaleBufferBase()
    {
        delete(m_pOutput);
    };
    virtual void setOutputWidth(int outputWidth, float minFreq, float maxFreq) = 0;
    [[nodiscard]] virtual float XtoFreq(float x) const = 0;
    [[nodiscard]] virtual float FreqToX(float freq) const = 0;
    virtual void PreBuild(myFFT *pProc) = 0;
    virtual void Build(BufferIODouble *bufferIO, float volume) = 0;

    [[nodiscard]] BufferIODouble *GetBuffer() const
    {
        return m_pOutput;
    }

    [[maybe_unused]] [[nodiscard]] float *GetData() const
    {
        return m_pOutput->GetData();
    }

};

#endif