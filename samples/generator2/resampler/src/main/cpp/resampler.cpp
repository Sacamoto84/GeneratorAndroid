#include <jni.h>
#include <string>
#include "smarc.h"

#define BUF_SIZE 8192

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_resampler_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}




int outBufSize = 0;
double outbuf[512000];

void resampleOne(int fsin, int fsout, double *inbuf, int len)
{

    //int fsin = 48000; // input samplerate
    //int fsout = 44100; // output samplerate
    double bandwidth = 0.95;  // bandwidth
    double rp = 0.1; // passband ripple factor
    double rs = 140; // stopband attenuation
    double tol = 0.000001; // tolerance

    // initialize smarc filter
    struct PFilter *pfilt = smarc_init_pfilter(fsin, fsout, bandwidth, rp, rs, tol, NULL, 0);

    //if (pfilt == NULL) return;

    // initialize smarc filter state
    struct PState *pstate = smarc_init_pstate(pfilt);

    // initialize buffers

    const int OUT_BUF_SIZE = (int) smarc_get_output_buffer_size(pfilt, len);

    //double *outbuf = static_cast<double *>(malloc(OUT_BUF_SIZE * sizeof(double)));

    outBufSize = OUT_BUF_SIZE;

    //int written = 0;

    // read input signal block into inbuf
    //     -----read = read_my_input_signal(inbuf,BUF_SIZE);


    // resample signal block
    smarc_resample(pfilt, pstate, inbuf, len, outbuf, OUT_BUF_SIZE);


    // do what you want with your output
    //-----write_my_resampled_signal(outbuf, written);


    // you are done with converting your signal.
    // If you want to reuse the same converter to process another signal
    // just reset the state:
    //
    // smarc_reset_pstate(pstate,pfilt);
    //

    // release buffers
    //free(inbuf);
    //free(outbuf);

    // release smarc filter state
    smarc_destroy_pstate(pstate);

    // release smarc filter
    smarc_destroy_pfilter(pfilt);

}

struct Pair {
    double* doubleArray;
    int intValue;
};

void resample(int fsin, int fsout) {
    //int fsin = 48000; // input samplerate
    //int fsout = 44100; // output samplerate
    double bandwidth = 0.95;  // bandwidth
    double rp = 0.1; // passband ripple factor
    double rs = 140; // stopband attenuation
    double tol = 0.000001; // tolerance

    // initialize smarc filter
    struct PFilter *pfilt = smarc_init_pfilter(fsin, fsout, bandwidth, rp, rs, tol, NULL, 0);
    if (pfilt == NULL) return;

    // initialize smarc filter state
    struct PState *pstate = smarc_init_pstate(pfilt);

    // initialize buffers
    const int IN_BUF_SIZE = BUF_SIZE;
    const int OUT_BUF_SIZE = (int) smarc_get_output_buffer_size(pfilt, IN_BUF_SIZE);

    double *inbuf = static_cast<double *>(malloc(IN_BUF_SIZE * sizeof(double)));
    double *outbuf = static_cast<double *>(malloc(OUT_BUF_SIZE * sizeof(double)));

    int read = 0;
    int written = 0;

    // resample audio
    while (1) {

        // read input signal block into inbuf
        //     -----read = read_my_input_signal(inbuf,BUF_SIZE);
        if (read == 0) {
            // reached end of file, have to flush last values
            break;
        }

        // resample signal block
        written = smarc_resample(pfilt, pstate, inbuf, read, outbuf, OUT_BUF_SIZE);

        // do what you want with your output
        //-----write_my_resampled_signal(outbuf, written);
    }

    // flushing last values
    while (1) {
        written = smarc_resample_flush(pfilt, pstate, outbuf, OUT_BUF_SIZE);

        // do what you want with your output
        //------write_my_resampled_signal(outbuf, written);

        // if written<OUT_BUF_SIZE then there will be no more output
        if (written < OUT_BUF_SIZE) break;
    }

    // you are done with converting your signal.
    // If you want to reuse the same converter to process another signal
    // just reset the state:
    //
    // smarc_reset_pstate(pstate,pfilt);
    //

    // release buffers
    free(inbuf);
    free(outbuf);

    // release smarc filter state
    smarc_destroy_pstate(pstate);

    // release smarc filter
    smarc_destroy_pfilter(pfilt);

}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_resampler_NativeLib_resampleOneImpl(JNIEnv *env, jobject thiz, jint fr_in,
                                                     jint fr_out, jdouble buf, jint len) {
    resampleOne(fr_in, fr_out, &buf,  len);

    // Создание jdoubleArray и копирование данных
    jdoubleArray doubleArray = env->NewDoubleArray(outBufSize);
    env->SetDoubleArrayRegion(doubleArray, 0, outBufSize, outbuf);

    return doubleArray;
}