//
// Created by user on 26.06.2024.
//

#ifndef GENERATOR2_FLOATRINGBUFFER_H
#define GENERATOR2_FLOATRINGBUFFER_H

#include <jni.h>
#include <cstring>
#include <memory>

// Определение класса FloatRingBuffer
class FloatRingBuffer {
public:
    FloatRingBuffer(int entrySize, int bufferSize)
            : entrySize(entrySize), bufferSize(bufferSize), start(0), end(0), isFull(false) {

        buffer = std::make_unique<float[]>(entrySize * bufferSize);

    }

    void add(const float *entry) {

        std::memcpy(buffer.get() + end * entrySize, entry, entrySize * sizeof(float));
        end = (end + 1) % bufferSize;
        if (isFull) {
            start = (start + 1) % bufferSize;
        } else if (end == start) {
            isFull = true;
        }

    }

    void toExternalFloatArray(float *result) const {
        if (isFull) {
            int part1Size = (bufferSize - start) * entrySize;
            std::memcpy(result, buffer.get() + start * entrySize, part1Size * sizeof(float));
            std::memcpy(result + part1Size, buffer.get(), end * entrySize * sizeof(float));
        } else {
            std::memcpy(result, buffer.get(), end * entrySize * sizeof(float));
        }
    }

private:
    int entrySize;  //Размер порции данных
    int bufferSize; //Количество порций порции данных
    int start;
    int end;
    bool isFull;
    std::unique_ptr<float[]> buffer;
};

#endif //GENERATOR2_FLOATRINGBUFFER_H
