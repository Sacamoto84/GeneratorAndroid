/**
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#include <inttypes.h>
#include <memory>

#include <Oscillator.h>

#include "HelloOboeEngine.h"
#include "SoundGenerator.h"

#include <oboe/Oboe.h>


/**
 * Main audio engine for the HelloOboe sample. It is отвечает за:
 *
 * - Создание объекта обратного вызова, которое поставляется при построении аудиопотока и будет
 * вызывается, когда поток начинается
 * - Restarting the stream when user-controllable properties (Audio API, channel count etc) are
 * changed, and when the stream is disconnected (e.g. when headphones are attached)
 * - Calculating the audio latency of the stream
 *
 */
HelloOboeEngine::HelloOboeEngine()
        : mLatencyCallback(std::make_unique<LatencyTuningCallback>()),
        mErrorCallback(std::make_unique<DefaultErrorCallback>(*this)) {
    LOGD("~~~HelloOboeEngine::HelloOboeEngine()~~~");
}

double HelloOboeEngine::getCurrentOutputLatencyMillis() {

    if (!mIsLatencyDetectionSupported) return -1.0;

    std::lock_guard<std::mutex> lock(mLock);
    if (!mStream) return -1.0;

    oboe::ResultWithValue<double> latencyResult = mStream->calculateLatencyMillis();
    if (latencyResult) {
        return latencyResult.value();
    } else {
        LOGE("Error calculating latency: %s", oboe::convertToText(latencyResult.error()));
        return -1.0;
    }
}

//Установка буффера
void HelloOboeEngine::setBufferSizeInBursts(int32_t numBursts) const {
    LOGI("┌-----------------------------------------------------------┐");
    LOGI("│ Функция изменения размера буффера > setBufferSizeInBursts │");
    //std::lock_guard<std::mutex> lock(mLock);
    if (!mStream) return;

    //mStream->getFramesPerBurst() минимально возможный размер буффера
    LOGI( "│ Минимально возможный размер буффера      %5d            │", mStream->getFramesPerBurst());
    LOGI( "│ numBursts                                %5d            │", numBursts);

    //mLatencyCallback->setBufferTuneEnabled(numBursts == kBufferSizeAutomatic);

    //Установка размера буффера
    auto result = mStream->setBufferSizeInFrames( numBursts * mStream->getFramesPerBurst());

    if (result) {
        LOGI("│ Buffer размер буффера успещно изменен на %5d            │ ", result.value());
    } else {
        LOGW("│ Buffer размер буффера не может быть изменен, %5d        │", result.error());
    }
    LOGI("└-----------------------------------------------------------┘");
}

void HelloOboeEngine::setAudioApi(oboe::AudioApi audioApi) {
    LOGD("~~~HelloOboeEngine::setAudioApi(%d)~~~", audioApi);
    if (mAudioApi != audioApi) {
        mAudioApi = audioApi;
        reopenStream();
    }
}

void HelloOboeEngine::setChannelCount(int channelCount) {
    LOGD("~~~HelloOboeEngine::setChannelCount(%d)~~~", channelCount);
    if (mChannelCount != channelCount) {
        mChannelCount = channelCount;
        reopenStream();
    }
}

void HelloOboeEngine::setDeviceId(int32_t deviceId) {
    /////////////////////////////////////////
    //deviceId = 0;

    LOGI("┌---------------------------------------┐");
    LOGI("│ HelloOboeEngine::setDeviceId(%5d)   │", deviceId);
    LOGI("└---------------------------------------┘");

    if (mDeviceId != deviceId) {
        mDeviceId = deviceId;
        if (reopenStream() != oboe::Result::OK) {
            LOGW("Open stream failed, forcing deviceId to Unspecified");
            mDeviceId = oboe::Unspecified;
        }
    }
}

bool HelloOboeEngine::isLatencyDetectionSupported() {
    return mIsLatencyDetectionSupported;
}

//Постсроитель аудиопотока
oboe::Result HelloOboeEngine::openPlaybackStream() {

    LOGI("│┌---------------------------------------┐");
    LOGI("││ HelloOboeEngine::openPlaybackStream() │");
    LOGI("│├---------------------------------------┤");
    LOGI("││ Функция Постсроитель аудиопотока      │");
    LOGI("│└---------------------------------------┘");

    oboe::AudioStreamBuilder builder;

    oboe::Result result =
         builder.setSharingMode(oboe::SharingMode::Exclusive) //Эксклюзивный доступ
         //.setSharingMode(oboe::SharingMode::Exclusive) //Эксклюзивный доступ
        ->setPerformanceMode(oboe::PerformanceMode::PowerSaving)
        ->setFormat(oboe::AudioFormat::Float) //16 бит или float
        //->setFormatConversionAllowed(true)
        ->setDataCallback(mLatencyCallback.get())
        ->setErrorCallback(mErrorCallback.get())
        ->setAudioApi(mAudioApi)
        ->setChannelCount(2) //Количество каналов
        ->setDeviceId(mDeviceId)
        ->setSampleRate(48000)
        ->setSampleRateConversionQuality(oboe::SampleRateConversionQuality::Low) //Улучшает качество звука
        ->openStream(mStream); //В конце открываем поток
    if (result == oboe::Result::OK) {
        mChannelCount = mStream->getChannelCount();
        mStream->setBufferSizeInFrames(mStream->getFramesPerBurst() ); //Размер буффера
    }

    setBufferSizeInBursts(6);

    return result;
}

void HelloOboeEngine::restart() {

    LOGI("┌----------------------------┐\n");
    LOGI("│ HelloOboeEngine::restart() │\n");
    LOGI("└----------------------------┘\n");

    // The stream will have already been closed by the error callback.
    mLatencyCallback->reset();

    start();
}

oboe::Result HelloOboeEngine::start() {

    LOGI("┌--------------------------┐");
    LOGI("│ HelloOboeEngine::start() │");
    LOGI("├--------------------------┘");

    std::lock_guard<std::mutex> lock(mLock);
    mIsLatencyDetectionSupported = false;
    auto result = openPlaybackStream(); //Постсроитель аудиопотока

    if (result == oboe::Result::OK){

        mAudioSource =  std::make_shared<SoundGenerator>(mStream->getSampleRate(), mStream->getChannelCount());

        mLatencyCallback->setSource(std::dynamic_pointer_cast<IRenderableAudio>(mAudioSource));

        LOGI("│ Stream открыт: AudioAPI = %d, channelCount = %d, deviceID = %d",
                 mStream->getAudioApi(),
                 mStream->getChannelCount(),
                 mStream->getDeviceId());

        LOGI("│ Запуск потока");
        result = mStream->start(); //Запуск потока

        if (result != oboe::Result::OK) {
            LOGI("| Error starting playback stream. Error: %s", oboe::convertToText(result));
            mStream->close();
            mStream.reset();
        } else {

            mIsLatencyDetectionSupported = (mStream->getTimestamp((CLOCK_MONOTONIC)) != oboe::Result::ErrorUnimplemented);

        }
    } else {

        LOGE("Error creating playback stream. Error: %s", oboe::convertToText(result));
    }

    LOGI("│ Требуем все данные");
    //Зартебываем тут все данные
    needAllData = 1; //Требуем все данные
    LOGI("└--------------------------┘");
    return result;
}

oboe::Result HelloOboeEngine::stop() {
    LOGD("~~~HelloOboeEngine::stop()~~~");
    oboe::Result result = oboe::Result::OK;
    // Stop, close and delete in case not already closed.
    std::lock_guard<std::mutex> lock(mLock);
    if (mStream) {
        result = mStream->stop();
        mStream->close();
        mStream.reset();
    }
    return result;
}

oboe::Result HelloOboeEngine::reopenStream() {
    LOGD("~~~HelloOboeEngine::reopenStream()~~~");
    if (mStream) {
        stop();
        return start();
    } else {
        return oboe::Result::OK;
    }
}







