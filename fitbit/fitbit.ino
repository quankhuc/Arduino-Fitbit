
//#include <SparkFun_MMA8452Q.h>

#include <Wire.h>
#include <Arduino.h>
#include "SparkFun_MMA8452Q.h"

MMA8452Q accel;

//Declaring keys

const byte MAGIC_NUMBER = 0x21;
const byte INFO_KEY = 0x30;
const byte ERROR_STRING = 0x31;
const byte TIMESTAMP_KEY = 0x32;
const byte PED_KEY = 0x35;
const byte BUTTON_KEY = 0x36;
const byte SLEEPTIME_KEY = 0x37;
const byte STEP_COUNT_KEY = 0x40;

//Declaring LED_PIN and BUTTON_PIN
const unsigned int BUTTON_PIN = 2;
const unsigned int LED_PIN1 = 3;
const unsigned int LED_PIN2 = 4;

//Declaring time variable for delata timing
//const unsigned long samplePeriod = 1000;
unsigned long nextSampleTime = 0;
unsigned long lastSwitchTime = 0;
const unsigned long switchDelay = 50;
unsigned long nextSwitchTime = 0;
unsigned long nextSendTime = 0;
const unsigned long sendPeriod = 200;



//Need to define state of the switch
boolean lastSwitchReading = HIGH;
boolean switchState = HIGH;

unsigned long sleepTime = 0;
int stepCount = 0;
long x_value;
byte x;
float y_value;
byte y;
float z_value;
byte z;
const unsigned long samplePeriod = 1000;
unsigned long lastDebounceTime = 0;
int debounceDelay = 50;
boolean lastResetReading = HIGH;
boolean resetState = HIGH;
boolean mode = LOW;

//Declaring states
enum State{
  pedometer,
  sleep,
};

//Declaring to be in the state of pedometer first
State currentState = pedometer; 

void setup(){
  Serial.begin(9600);
  pinMode(BUTTON_PIN, INPUT_PULLUP);
  pinMode(LED_PIN1, OUTPUT);
  pinMode(LED_PIN2, OUTPUT);
  Wire.begin();
  accel.begin();
}

void loop() {
  unsigned long currentTime = millis();
  //after a second, send current running time
  if(currentTime >= nextSampleTime){
    nextSampleTime += samplePeriod;
    //send the current running time
    sendTimestamp(currentTime); 
  }
  if(currentTime >= nextSampleTime){
    nextSampleTime += samplePeriod;
    sendSleepTime(millis()); 
  }
  if(currentTime >= nextSendTime){   
      nextSendTime += sendPeriod;
      sendpedometer();
      sendbutton();
   }
  checkReset();
}


void sendUlong(unsigned long value){
  Serial.write(value >> 24);
  Serial.write(value >> 16);
  Serial.write(value >> 8);
  Serial.write(value); 
}

void sendwordlength(int wordlength){
  Serial.write(wordlength >> 8);
//  Serial.println(wordlength >> 8);
  Serial.write(wordlength);
//  Serial.println(wordlength >> 8);
}

void sendTimestamp(unsigned long timestamp) {
  // Send magic number.
  Serial.write(MAGIC_NUMBER);

  // Send timestamp key.
  Serial.write(TIMESTAMP_KEY);

  // Send timestamp value.
  sendUlong(timestamp);
}

void sendSleepTime(unsigned long sleepTime) {
  // Send magic number.
  Serial.write(MAGIC_NUMBER);

  // Send timestamp key.
  Serial.write(SLEEPTIME_KEY);

  // Send timestamp value.
  sendUlong(sleepTime);
}

//need to have a function to convert float to byte
byte FloatstoBytes(float input){
  byte x = 0;
  x = input/0.1;
  return x;
  }

void sendpedometer(){
  Serial.write(MAGIC_NUMBER);
  Serial.write(PED_KEY);
  if(accel.available()){
    x_value = accel.getCalculatedX();
    x = FloatstoBytes(x_value);
    Serial.write(x);
    y_value = accel.getCalculatedY();
    y = FloatstoBytes(y_value);
    Serial.write(y);
    z_value = accel.getCalculatedZ();
    z = FloatstoBytes(z_value);
    Serial.write(z);
  }
}

void sendSleepTime(long sleepTimes) {
  Serial.write(MAGIC_NUMBER);
  Serial.write(SLEEPTIME_KEY);
  sendUlong(sleepTimes);
}

void sendinfo(char *value) {
  int i = 0;
    while(value[i] != '\0'){
    i++;
    }
  Serial.write(MAGIC_NUMBER);
  Serial.write(INFO_KEY);
  sendwordlength(i + 1);
  Serial.write(value);
}

void sendError(char *value) {
   int i = 0;
    while(value[i] != '\0'){
    i++;
    }
  Serial.write(MAGIC_NUMBER);
  Serial.write(ERROR_STRING);
  sendwordlength(i + 1);
  Serial.write(value);
}

void sendStepCount(int stepcount){
  Serial.write(MAGIC_NUMBER);
  Serial.write(STEP_COUNT_KEY);
  Serial.write(stepcount >> 8);
  Serial.write(stepcount);
}


//need a method to reset the sleep clock if there is a motion
void checkReset() {
  int reading = digitalRead(BUTTON_PIN);
  char steps[] = "Pedometer reset";
  char sleeps[] = "Sleep timer reset";
  if (lastResetReading != reading) {
    lastDebounceTime = millis();
  }
  if ((millis()-lastDebounceTime) > debounceDelay) {
    if (reading != resetState) {
      resetState = reading;
      if (resetState == LOW) {
            sendinfo(steps);
//            digitalWrite(LED_PIN1, LOW);
      }
      else {
            sendError(sleeps);
//            digitalWrite(LED_PIN2, LOW);
      } 
   }
 }
  lastResetReading = reading; 
}


void sendbutton(){ 
  boolean button_status = digitalRead(BUTTON_PIN);
//  Serial.print("THIS IS THE STATUS OF THE BUTTON: ");
//  Serial.println(button_status);
    if(button_status == LOW){
      boolean a = HIGH;
      Serial.write(MAGIC_NUMBER);
      Serial.write(BUTTON_KEY);
      Serial.write(a);
//    Serial.print("THIS IS THE BYTE INDICATING THE BUTTON IS PRESSED: ");
//    Serial.println(a);
      //Need to switch back mode to low when it was high and to high when it was low
      if(mode == LOW){
      mode = HIGH;
      }
      else{
        mode = LOW;
      }
      }
//      digitalWrite(LED_PIN2, HIGH);
//      digitalWrite(LED_PIN1, HIGH);
    else{
      boolean b = LOW;
      Serial.write(MAGIC_NUMBER);
      Serial.write(BUTTON_KEY);
      Serial.write(b);
//    Serial.print("THIS IS THE BYTE INDICATING THE BUTTON IS NOT PRESSED: ");
//    Serial.println(b);
//      digitalWrite(LED_PIN1, HIGH);
//      digitalWrite(LED_PIN2, LOW);
      if(mode == LOW){
        digitalWrite(LED_PIN1, HIGH);
        digitalWrite(LED_PIN2, LOW);
      }
      else{
        digitalWrite(LED_PIN1, LOW);
        digitalWrite(LED_PIN2, HIGH);
      }
      }
}
