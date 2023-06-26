#ifndef SAMPLES_GENERATOR_H
#define SAMPLES_GENERATOR_H

#include <cstdint>
#include <atomic>
#include <math.h>
#include <memory>

#include "signal.h"

#include "logging_macros.h"

#include <Oscillator.h>


typedef struct {

    int ch; //Номер канала 1 2

    bool CH_EN;
    int AM_EN;
    int FM_EN;

    float Carrier_fr;         //Частота несущей

    float AM_fr;              //Часта модуляции

    //float FM_Base;

    float FM_Dev;
    char  FM_mod[20];       //Имя файла
    float FM_mod_fr;        //Часта модуляции

    //Буфферы
    uint16_t buffer_carrier[1024];
    uint16_t buffer_am[1024];
    uint16_t buffer_fm[1024];

    uint16_t source_buffer_fm[1024]; //Используется для перерасчета модуляции

    uint32_t rC;		     //Частота несущей 1
    uint32_t angle_cr;

    uint32_t rAM;
    uint32_t angle_m;

    uint32_t rFM;
    uint32_t angle_fm;
    float Volume; //Громкость 0..1

    //Аккумуляторы
    uint32_t phase_accumulator_carrier;
    uint32_t phase_accumulator_am;
    uint32_t phase_accumulator_fm;

    float mBuffer[4096];

    float AmDepth; //Глубина AM модуляции

    //Импульсный режим
    bool     impulseMode;       //true - включен импульсный режим, false режим генератора

    //Impulse
    uint32_t impulseGlobalTime; //Инкремент каждые 1/48000
    uint32_t impulseStartTime;  //Время с которого начался импульс

    uint32_t timeImp;           //Ширина импульса
    uint32_t timeImpPause;      //Ширина паузы





} _structure_ch;

extern _structure_ch CH1;
extern _structure_ch CH2;

extern bool Mono;
extern bool Invert;
extern bool shuffle; //Меняем местами левый и правый канал для стерео

extern void setToMono();   // Перевод в режим моно
extern void setToStereo(); // Перевод в режим стерео

extern void resetCarrierPhase();    //Сброс фазы несущей
extern void resetAllPhase();

extern bool enL;
extern bool enR;

extern float parameterFloat0;
extern float parameterFloat1;
extern float parameterFloat2;
extern float parameterFloat3;
extern float parameterFloat4;
extern float parameterFloat5;
extern float parameterFloat6;
extern float parameterFloat7;

extern int parameterInt0;
extern int parameterInt1;
extern int parameterInt2;
extern int parameterInt3;
extern int parameterInt4;
extern int parameterInt5;
extern int parameterInt6;
extern int parameterInt7;



#endif
