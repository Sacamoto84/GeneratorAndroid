//
// Создано пользователем 07.07.2024.
//

#ifndef GENERATOR2_AUDIOHISTORYBUFFER_H
#define GENERATOR2_AUDIOHISTORYBUFFER_H

#include <android/log.h>
#include <cstddef>
#include <cstring>
#include <jni.h>

#define MODULE_NAME "AUDIO_HISTORY_BUFFER"
#define LOGD(...) \
  __android_log_print(ANDROID_LOG_DEBUG, MODULE_NAME, __VA_ARGS__)
#define LOGE(...) \
  __android_log_print(ANDROID_LOG_ERROR, MODULE_NAME, __VA_ARGS__)

class AudioHistoryBuffer {
public:
    static constexpr std::size_t kCapacity = 3'000'000; // Около 12 МБ.
    static constexpr jint kMaxItemCount = 256;

    float bigBuffer[kCapacity];

    /**
     * Настраивает отображаемое окно истории и очищает предыдущие данные.
     * @param _itemSize количество float-значений в одном аудиопакете.
     * @param _itemCount число пакетов в отображаемом окне, 1..256.
     */
    bool clear(jint _itemSize, jint _itemCount) {
        if (!isValidConfiguration(_itemSize, _itemCount)) {
            return false;
        }

        itemSize = static_cast<std::size_t>(_itemSize);
        itemCount = static_cast<std::size_t>(_itemCount);
        std::memset(bigBuffer, 0, kCapacity * sizeof(float));
        wP = window();
        LOGD("clear(): itemSize=%d, itemCount=%d", _itemSize, _itemCount);
        return true;
    }

    bool add(const jfloat *data, jint len, jint _itemCount) {
        if (data == nullptr) {
            LOGE("add(): data is null");
            return false;
        }
        if (!isValidConfiguration(len, _itemCount)) {
            return false;
        }

        const auto incomingLength = static_cast<std::size_t>(len);
        if (itemSize != incomingLength ||
            itemCount != static_cast<std::size_t>(_itemCount)) {
            if (!clear(len, _itemCount)) {
                return false;
            }
        }

        // Хвост, который возвращает read(), должен быть одним непрерывным участком памяти.
        // Перед концом массива переносим последнее окно в его начало.
        if (wP > kCapacity - incomingLength) {
            const auto currentWindow = window();
            std::memmove(bigBuffer, bigBuffer + (wP - currentWindow),
                         currentWindow * sizeof(float));
            wP = currentWindow;
            LOGD("relocated history window");
        }

        std::memcpy(bigBuffer + wP, data, incomingLength * sizeof(float));
        wP += incomingLength;
        return true;
    }

    float *read() {
        const auto currentWindow = window();
        if (currentWindow == 0 || wP < currentWindow) {
            LOGE("read(): buffer is not configured");
            return nullptr;
        }
        return bigBuffer + (wP - currentWindow);
    }

    float *readSmall(jint len) {
        if (len <= 0 || static_cast<std::size_t>(len) > kCapacity ||
            wP < static_cast<std::size_t>(len)) {
            LOGE("readSmall(): invalid len=%d", len);
            return nullptr;
        }
        return bigBuffer + (wP - static_cast<std::size_t>(len));
    }

    std::size_t window() const {
        return itemSize * itemCount;
    }

private:
    bool isValidConfiguration(jint _itemSize, jint _itemCount) const {
        if (_itemSize <= 0) {
            LOGE("Invalid item size: %d", _itemSize);
            return false;
        }
        if (_itemCount <= 0 || _itemCount > kMaxItemCount) {
            LOGE("Invalid item count: %d (expected 1..%d)",
                 _itemCount, kMaxItemCount);
            return false;
        }

        const auto newItemSize = static_cast<std::size_t>(_itemSize);
        const auto newItemCount = static_cast<std::size_t>(_itemCount);
        if (newItemSize > kCapacity / newItemCount) {
            LOGE("Window exceeds buffer capacity");
            return false;
        }

        const auto newWindow = newItemSize * newItemCount;
        if (newWindow > kCapacity - newItemSize) {
            LOGE("Window and packet exceed buffer capacity: %zu + %zu > %zu",
                 newWindow, newItemSize, kCapacity);
            return false;
        }
        return true;
    }

    std::size_t wP = 0; // Текущая позиция записи.
    std::size_t itemSize = 0;
    std::size_t itemCount = 0;
};

#endif // GENERATOR2_AUDIOHISTORYBUFFER_H
