#ifndef OBOE_HELLO_OBOE_ENGINE_H
#define OBOE_HELLO_OBOE_ENGINE_H

#include <oboe/Oboe.h>

#include "SoundGenerator.h"
#include "LatencyTuningCallback.h"
#include "IRestartable.h"
#include "DefaultErrorCallback.h"

constexpr int32_t kBufferSizeAutomatic = 0;

class HelloOboeEngine : public IRestartable {

public:
    HelloOboeEngine();

    virtual ~HelloOboeEngine() = default;

    /**
     * Open and start a stream.
     * @return error or OK
     */
    oboe::Result start();

    /**
     * Stop and close the stream.
     */
    oboe::Result stop();

    // From IRestartable
    void restart() override;

    // These methods reset the underlying stream with new properties

    /**
     * Set the audio device which should be used for playback. Can be set to oboe::kUnspecified if
     * you want to use the default playback device (which is usually the built-in speaker if
     * no other audio devices, such as headphones, are attached).
     *
     * @param deviceId the audio device id, can be obtained through an {@link AudioDeviceInfo} object
     * using Java/JNI.
    */
    void setDeviceId(int32_t deviceId);

    void setChannelCount(int channelCount);

    void setAudioApi(oboe::AudioApi audioApi);

    void setBufferSizeInBursts(int32_t numBursts) const;

    /**
     * Calculate the current latency between writing a frame to the output stream and
     * the same frame being presented to the audio hardware.
     *
     * Here's how the calculation works:
     *
     * 1) Get the time a particular frame was presented to the audio hardware
     * @see AudioStream::getTimestamp
     * 2) From this extrapolate the time which the *next* audio frame written to the stream
     * will be presented
     * 3) Assume that the next audio frame is written at the current time
     * 4) currentLatency = nextFramePresentationTime - nextFrameWriteTime
     *
     * @return  Output Latency in Milliseconds
     */
    double getCurrentOutputLatencyMillis();

    bool isLatencyDetectionSupported();

    std::shared_ptr<SoundGenerator>        mAudioSource; //Там лежат калбеки
    std::unique_ptr<LatencyTuningCallback> mLatencyCallback;

    int32_t        mDeviceId = oboe::Unspecified; //Текущий номер устройства

    std::shared_ptr<oboe::AudioStream> mStream; //Сам аудиопоток


    int32_t        needAllData = 0; //Признак того что требуется получить все данные

private:
    oboe::Result reopenStream();
    oboe::Result openPlaybackStream();  //Постсроитель аудиопотока

    std::unique_ptr<DefaultErrorCallback> mErrorCallback;

    bool mIsLatencyDetectionSupported = false;

    int32_t        mChannelCount = oboe::Stereo;
    //oboe::AudioApi mAudioApi = oboe::AudioApi::Unspecified;
    oboe::AudioApi mAudioApi = oboe::AudioApi::OpenSLES;
    std::mutex     mLock;
};

#endif //OBOE_HELLO_OBOE_ENGINE_H
