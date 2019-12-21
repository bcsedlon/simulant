#ifdef __IN_ECLIPSE__
//This is a automatic generated file
//Please do not modify this file
//If you touch this file your change will be overwritten during the next build
//This file has been generated on 2019-12-20 22:25:52

#include "Arduino.h"
#include "arduino.h"
#define SAMPLES 16
#define OUT_PULSE_PIN 	4
#define OUT_PT100_PIN 	5
#define OUT_I_PIN 		6
#define OUT_LED_PIN 	13
#define IN_PULSE_PIN	8
#define IN_ADJUST_PIN 	A6
#define IN_SENCE_PIN 	A7
#define IN_BATT_PIN 	A5
#include "FastX9CXXX.h"
#define X9_CS_PIN  9
#define X9_INC_PIN 10
#define X9_UD_PIN  11
extern FastX9C103 x9;
#include <EEPROM.h>
#define CALIBRATION_ADDRESS 4
#include "SoftwareSerial.h"
extern SoftwareSerial swSerial;
#include "TimerOne.h"
#include "filter.h"
extern ExponentialFilter<float> senceInFiltered;
extern unsigned long lastMillis;
extern float battIn;
extern int pwmOut;
extern unsigned long pulseOutDuration;
extern bool remoteControl;
extern bool ledOut;
extern char c;
extern float calibrationPar;
#include <avr/interrupt.h>

float analogRead(int pin, int samples) ;
ISR(PCINT0_vect) ;
void timerIsr() ;
void setup() ;
void loop() ;

#include "simulant.ino"


#endif
