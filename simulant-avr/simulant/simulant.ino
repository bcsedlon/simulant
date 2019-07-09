#include "arduino.h"

#define SAMPLES 16

#define OUT_PULSE_PIN 	4
#define OUT_PT100_PIN 	5
#define OUT_I_PIN 		6
#define OUT_LED_PIN 	13
#define IN_PULSE_PIN	8 // see interrupt PORTD
#define IN_ADJUST_PIN 	A6
#define IN_SENCE_PIN 	A7
#define IN_BATT_PIN 	A5

#include <EEPROM.h>
#define CALIBRATION_ADDRESS 4

//#include <SoftwareSerial.h>
#include "SoftwareSerial.h"
SoftwareSerial swSerial(2, 3); // RX, TX

#include <TimerOne.h>

//#include "libraries/PinChangeInt.h"
//#define NO_PORTB_PINCHANGES
//#define NO_PORTC_PINCHANGES
//#define NO_PORTD_PINCHANGES

#include "filter.h"
ExponentialFilter<float> senceInFiltered(16, 0);

unsigned long lastMillis, lastLedBlink, lastPulse;
float battIn, senceIn, adjustIn, f;
int pwmOut, counterIn;
unsigned long pulseOutDuration;
bool remoteControl;
bool ledOut, pulseOut;
char c;
float calibrationPar;

float analogRead(int pin, int samples) {
	float r = 0;
	for(int i = 0; i < samples; i++)
		r += analogRead(pin);
	r /= samples;
	return r;
}

//void counterIsr() {
//	counterIn++;
//}

#include <avr/interrupt.h>
ISR(PCINT0_vect)
{
	counterIn++;
}

void timerIsr() {
	//if(millis() - lastPulse > pulseOutDuration) {
	if(pwmOut > 0) {
		lastPulse = millis();
		digitalWrite(OUT_PULSE_PIN, pulseOut);
		pulseOut = not pulseOut;
	}
}

void setup() {
	analogReference(EXTERNAL);

	//EEPROM.begin(512);
	if(!EEPROM.read(0)) {
		EEPROM.get(CALIBRATION_ADDRESS, calibrationPar);
	}
	else {
		calibrationPar = 1.0;
		EEPROM.put(CALIBRATION_ADDRESS, calibrationPar);
		EEPROM.write(0, 0);
	}

	cli();
	PCICR |= 0b00000001; 	// enables Ports D Pin Change Interrupts
	PCMSK0 |= 0b00000001; 	// PCINT0
	//PCMSK1 |= 0b00001000; 	// PCINT11
	//PCMSK2 |= 0b00000001; 	// PCINT0
	sei();

	Serial.begin(9600);
	swSerial.begin(9600);

	Serial.println("\n\nSIMULANT");
	swSerial.println("\n\nSIMULANT");

	pinMode(IN_PULSE_PIN, INPUT);

	pinMode(OUT_I_PIN, OUTPUT);
	pinMode(OUT_PULSE_PIN, OUTPUT);
	pinMode(OUT_PT100_PIN, OUTPUT);
	pinMode(OUT_LED_PIN, OUTPUT);

	Timer1.initialize(1000000 / 2); // set a timer of length 100000 microseconds (or 0.1 sec - or 10Hz => the led will blink 5 times, 5 cycles of on-and-off, per second)
	Timer1.attachInterrupt(timerIsr);

	//attachInterrupt(digitalPinToInterrupt(IN_PULSE_PIN), counterIsr, RISING);
	//attachPinChangeInterrupt(IN_PULSE_PIN, counterIsr, FALLING);
}

void loop() {
	//if (swSerial.available()) {
	//  Serial.write(swSerial.read());
	//}
	//if (Serial.available()) {
	//  swSerial.write(Serial.read());
	//}

	//f = counterIn;
	//counterIn = 0;

	senceIn = analogRead(IN_SENCE_PIN, SAMPLES);
	senceInFiltered.Filter(senceIn);
	senceIn = senceInFiltered.Current();

	if(millis() - lastLedBlink > (255 - pwmOut) * 4) {
		lastLedBlink = millis();
		digitalWrite(OUT_LED_PIN, ledOut);
		ledOut = not ledOut;
	}

	//if(millis() - lastPulse > pulseOutDuration) {
	//	lastPulse = millis();
	//	digitalWrite(OUT_PULSE_PIN, pulseOut);
	//	pulseOut = not pulseOut;
	//}

	if(!remoteControl) {
		pwmOut = analogRead(IN_ADJUST_PIN, SAMPLES) / 4;
		analogWrite(OUT_I_PIN, pwmOut);
		analogWrite(OUT_PT100_PIN, 255 - pwmOut);
	}

	if (Serial.available()) {
		c = Serial.read();

		if(c == 'R') {
			remoteControl = false;
		}
		else if(c == 'S') {
			remoteControl = true;
			pwmOut = Serial.parseInt();
			//while(Serial.available())
			//int i = Serial.readBytesUntil();

			analogWrite(OUT_I_PIN, pwmOut);
			analogWrite(OUT_PT100_PIN, 255 -pwmOut);

			Serial.print('>');
			Serial.println(pwmOut);
			swSerial.print('>');
			swSerial.println(pwmOut);
		}
		else if(c =='C') {
			calibrationPar = Serial.parseFloat();
			EEPROM.put(CALIBRATION_ADDRESS, calibrationPar);
		}
		else {
			Serial.read();
		}
	}
	if (swSerial.available()) {
		c = swSerial.read();

		if(c == 'R') {
			remoteControl = false;
		}
		else if(c == 'S') {
			remoteControl = true;
			pwmOut = swSerial.parseInt();
			//while(swSerial.available())
			//int i = swSerial.read();

			analogWrite(OUT_I_PIN, pwmOut);
			analogWrite(OUT_PT100_PIN, pwmOut);

			analogWrite(OUT_I_PIN, pwmOut);
			analogWrite(OUT_PT100_PIN,255 - pwmOut);

			Serial.print('>');
			Serial.println(pwmOut);
			swSerial.print('>');
			swSerial.println(pwmOut);
		}
		else if(c =='C') {
			calibrationPar = swSerial.parseFloat();
			EEPROM.put(CALIBRATION_ADDRESS, calibrationPar);
		}
		else {
			swSerial.read();
		}

	}

	if(millis() - lastMillis > 1000) {


		f = (float)(counterIn / 2) / ((float)(millis() - lastMillis) / 1000.0);
		counterIn = 0;

		lastMillis = millis();

		battIn = analogRead(IN_BATT_PIN, SAMPLES);

		if(pwmOut == 0) {
			pulseOutDuration = 1000000 / 2;
			digitalWrite(OUT_PULSE_PIN, LOW);
		}
		else
			pulseOutDuration = (1000000.0 / (float)pwmOut) / 2.0;
		Timer1.setPeriod(pulseOutDuration);

		//int t1 = pulseIn(IN_PULSE_PIN, HIGH);
		//int t2 = pulseIn(IN_PULSE_PIN, LOW);
		//if((t1 + t2) > 0)
		//	f = 1000000.0 / float(t1 + t2);
		//else
		//	f = 0;

		Serial.print('S');
		Serial.println(senceIn);
		Serial.print('I');
		Serial.println(senceIn * calibrationPar);
		Serial.print('F');
		Serial.println(f);
		Serial.print('B');
		Serial.println(battIn);

		swSerial.print('S');
		swSerial.println(senceIn);
		swSerial.print('I');
		swSerial.println(senceIn * calibrationPar);
		swSerial.print('F');
		swSerial.println(f);
		swSerial.print('B');
		swSerial.println(battIn);
	}
}
