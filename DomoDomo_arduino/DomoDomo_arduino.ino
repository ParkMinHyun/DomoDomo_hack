<<<<<<< HEAD
#include<SoftwareSerial.h>
int blueTx=2;
int blueRx=3;
SoftwareSerial mySerial(blueTx,blueRx);
String myString="";
void setup() {
  Serial.begin(9600);
  mySerial.begin(9600);
=======
#include <SPI.h>
#include <Adafruit_VS1053.h>
#include <SD.h>

unsigned long prevMillis = 0;
int ledState = LOW;

#define BREAKOUT_RESET  9      // VS1053 reset pin (output)
#define BREAKOUT_CS     10     // VS1053 chip select pin (output)
#define BREAKOUT_DCS    8      // VS1053 Data/command select pin (output)

#define SHIELD_RESET  -1      // VS1053 reset pin (unused!)
#define SHIELD_CS     7      // VS1053 chip select pin (output)
#define SHIELD_DCS    6      // VS1053 Data/command select pin (output)

#define CARDCS 4     // Card chip select pin
#define DREQ 3       // VS1053 Data request, ideally an Interrupt pin

Adafruit_VS1053_FilePlayer musicPlayer = 
  Adafruit_VS1053_FilePlayer(BREAKOUT_RESET, BREAKOUT_CS, BREAKOUT_DCS, DREQ, CARDCS);
  
void setup() {
  Serial.begin(9600);
  pinMode(2,INPUT_PULLUP);

  if (! musicPlayer.begin()) { 
      while (1);
  }
  
  SD.begin(CARDCS);    // initialise the SD card
  musicPlayer.setVolume(20,20);
  musicPlayer.useInterrupt(VS1053_FILEPLAYER_PIN_INT);  // DREQ int
  
  Serial.println(F("Playing track 002"));
  musicPlayer.startPlayingFile("002.mp3");
>>>>>>> feature/AUDIO_LED
}
  
void loop() {
<<<<<<< HEAD
  while(mySerial.available())
  {
    char myChar = (char)mySerial.read();
    myString = myString + myChar; 
    delay(10);
  }
  if(!myString.equals(""))
  {
    Serial.println("Input value = "+myString);
    myString="";
  }
=======

  
  int play_stop_button = digitalRead(2);
  unsigned long currentMillis = millis(); 

  if(currentMillis - prevMillis >= 1000)
  { 
    musicPlayer.GPIO_pinMode(0, OUTPUT);
    if (ledState == LOW){ //꺼져있으면
      ledState = HIGH; //키고
    } else {            //켜져있으면
      ledState = LOW;   //끄고
    }
     
    musicPlayer.GPIO_digitalWrite(0, ledState);
    prevMillis = currentMillis;
  }
  /*
  if (play_stop_button == 0) 
  {
      if (! musicPlayer.paused()) 
      {
        musicPlayer.pausePlaying(true);
      } 
      else 
      { 
        musicPlayer.pausePlaying(false);
      }
    }
    */
  if (play_stop_button == 0) 
  {
    musicPlayer.startPlayingFile("001.mp3");
  }
  
  if (musicPlayer.stopped()) {
    Serial.println("Done playing music");
    while (1);
  }
  
>>>>>>> feature/AUDIO_LED
}
