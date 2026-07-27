package com.example.generator2.features.audio

/**
 * Собирает стереоинтерлив LRLR для AudioTrack: чётный сэмпл кадра — левое ухо.
 * shuffle = true меняет уши местами.
 */
fun interleaveOut(outL: FloatArray, outR: FloatArray, shuffle: Boolean): FloatArray =
    if (shuffle) bufMerge(outR, outL) else bufMerge(outL, outR)
