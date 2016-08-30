#include<SoftwareSerial.h>
#include <SPI.h>
#include <Adafruit_VS1053.h>
#include <SD.h>

unsigned long prev_LED1_Millis = 0;
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
  
int blueTx=6;
int blueRx=5;
SoftwareSerial mySerial(blueTx,blueRx);

String myString="";
int bluetooth_serial_num = 99;

void setup() {
  mySerial.begin(9600);
  Serial.begin(9600);
  pinMode(2,INPUT_PULLUP);

  if (! musicPlayer.begin()) { 
      while (1);
  }
  
  SD.begin(CARDCS);    // initialise the SD card
  musicPlayer.setVolume(20,20);
  musicPlayer.useInterrupt(VS1053_FILEPLAYER_PIN_INT);  // DREQ int
}
  
void loop() {
  int play_stop_button = digitalRead(2);
  unsigned long current_LED1_Millis = millis(); 
  
  while(mySerial.available())
  {
    char myChar = (char)mySerial.read();
    myString = myString + myChar; 
    bluetooth_serial_num = myString.toInt();
    delay(10);
  }
  
  if(!myString.equals(""))
  {
     Serial.println("Input value = "+bluetooth_serial_num);
  }
  
  
  /* LED1 Millis 함수로 구현 */
  if(current_LED1_Millis - prev_LED1_Millis >= 1000)
  { 
    musicPlayer.GPIO_pinMode(0, OUTPUT);
    if (ledState == LOW){ //꺼져있으면
      ledState = HIGH; //키고
    } else {            //켜져있으면
      ledState = LOW;   //끄고
    }
     
    musicPlayer.GPIO_digitalWrite(0, ledState);
    prev_LED1_Millis = current_LED1_Millis;
  }

  if(bluetooth_serial_num == 0)
  {
     musicPlayer.startPlayingFile("001.mp3");
  }
  else if(bluetooth_serial_num == 1)
  {
     musicPlayer.startPlayingFile("002.mp3");
  }
  else if(bluetooth_serial_num == 2)
  {
     musicPlayer.startPlayingFile("002.mp3");
  }
  
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
    
    /*
  if (play_stop_button == 0) 
  {
    musicPlayer.startPlayingFile("001.mp3");
  }*/
 
  myString="";
  bluetooth_serial_num = 99;
}
