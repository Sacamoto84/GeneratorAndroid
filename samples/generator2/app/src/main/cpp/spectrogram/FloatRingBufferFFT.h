//
// Created by user on 26.06.2024.
//

#ifndef GENERATOR2_FLOATRINGBUFFER_H
#define GENERATOR2_FLOATRINGBUFFER_H

#include <jni.h>
#include <cstring>
#include <memory>
#include <mutex>
#include <stdexcept>

// Определение класса FloatRingBufferFFT
class FloatRingBufferFFT {
public:

    FloatRingBufferFFT(int bufferSize)
            :  bufferSize(bufferSize), head(0), tail(0), is_full(false) {
        buffer = std::make_unique<float[]>(bufferSize);
    }

    // Добавить массив float в буфер
    void add(const float* data, size_t data_size) {
        std::lock_guard<std::mutex> lock(mutex);
        if (data_size > bufferSize) {
            throw std::overflow_error("Data size is larger than buffer capacity.");
        }
        for (size_t i = 0; i < data_size; ++i) {
            buffer[tail] = data[i];
            tail = (tail + 1) % bufferSize;
            if (is_full) {
                head = (head + 1) % bufferSize;
            }
            is_full = tail == head;
        }
    }

    // Прочитать блок данных из буфера
    void read(float* dest, size_t block_size) {
        std::lock_guard<std::mutex> lock(mutex);
        if (block_size > bufferSize || block_size > sizeUnlocked()) {
            throw std::underflow_error("Not enough data in the buffer.");
        }

        for (size_t i = 0; i < block_size; ++i) {
            dest[i] = buffer[head];
            head = (head + 1) % bufferSize;
            is_full = false;
        }
    }

    // Прочитать блок данных из буфера не изменяя положения указателей
    bool peek(float* dest, size_t block_size) {
        std::lock_guard<std::mutex> lock(mutex);

        if (block_size > sizeUnlocked()) {
            return false;
        }

        size_t _head = head;
        size_t _tail = tail;

        for (size_t i = 0; i < block_size; ++i) {
            dest[i] = buffer[_head];
            _head = (_head + 1) % bufferSize;
        }

        return true;
    }

    /**
     * Переместить указатели на step
     * @param step
     * @return
     */
    bool gotoNext(int step){
        std::lock_guard<std::mutex> lock(mutex);
        if (step > sizeUnlocked()) {
            return false;
        }
        head = (head + step) % bufferSize;
        return true;
    }

    // Получить текущее количество элементов в буфере
    [[nodiscard]] size_t size() const {
        std::lock_guard<std::mutex> lock(mutex);
        return sizeUnlocked();
    }

    // Keeps a complete FFT window stable while audio data is being appended.
    bool peekAndAdvance(float* dest, size_t block_size, size_t step) {
        std::lock_guard<std::mutex> lock(mutex);
        if (block_size > sizeUnlocked() || step > sizeUnlocked()) {
            return false;
        }

        size_t readHead = head;
        for (size_t i = 0; i < block_size; ++i) {
            dest[i] = buffer[readHead];
            readHead = (readHead + 1) % bufferSize;
        }

        head = (head + step) % bufferSize;
        is_full = false;
        return true;
    }

private:
    [[nodiscard]] size_t sizeUnlocked() const {
        if (is_full) {
            return bufferSize;
        } else if (tail >= head) {
            return tail - head;
        } else {
            return bufferSize - head + tail;
        }
    }

private:
    int bufferSize; //Количество порций порции данных

    size_t head;
    size_t tail;
    bool is_full;

    mutable std::mutex mutex;

    std::unique_ptr<float[]> buffer;
};

#endif //GENERATOR2_FLOATRINGBUFFER_H
