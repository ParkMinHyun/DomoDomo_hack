#include<SoftwareSerial.h>
#include <SPI.h>
#include <Adafruit_VS1053.h>
#include <SD.h>

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
  
unsigned long prev_LED1_Millis = 0;
bool led_version1 = false;
bool led_version2 = false;
int led_version1_State = LOW;
int blueTx=6;
int blueRx=5;
SoftwareSerial mySerial(blueTx,blueRx);

String myString="";
int bluetooth_serial_num = 99;

bool LED_1 = true;
bool LED_2 = false;
bool LED_3 = false;
bool LED_4 = false;
bool LED_put_off = false;




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
     Serial.println("Input value = "+ myString);
  }
  
  /* LED 버전1 Millis 함수로 구현 */
  if(current_LED1_Millis - prev_LED1_Millis >= 1000 && led_version1)
  { 
      led_version2 = false;
      LED_ver1();
      prev_LED1_Millis = current_LED1_Millis;
  }
  
  /* LED 버전2 함수로 구현 */
  if(current_LED1_Millis - prev_LED1_Millis >= 1000 && led_version2)
  {
      led_version1 = false;
      LED_ver2();
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
  else if(bluetooth_serial_num == 20)
  {
      led_version1 = true;
      led_version2 = false;
  }
  else if(bluetooth_serial_num == 21)
  {
     Serial.println("Input value = dddd");
      led_version2 = true;
      led_version1 = false;
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
  }
 */
  myString="";
  bluetooth_serial_num = 99;
}

void LED_ver1 ()
{
      for(int i=0; i<4; i++)
      {
        musicPlayer.GPIO_pinMode(i, OUTPUT);
      }
      if (led_version1_State == LOW)  //꺼져있으면
      {
        led_version1_State = HIGH;    //키고
      }
      else                            //켜져있으면
      {                       
        led_version1_State = LOW;     //끄고
      }
     
      for(int i=0; i<4; i++)
      {
        musicPlayer.GPIO_digitalWrite(i, led_version1_State);
      }
}

void LED_ver2 ()
{
     //prev_LED1_Millis
     for(int i=0; i<4; i++)
     {
        musicPlayer.GPIO_pinMode(i, OUTPUT);
     }

     if ( LED_1)
     {
        musicPlayer.GPIO_digitalWrite(0, HIGH); 
        LED_1 = false;
        LED_2 = true;
     }
     else if ( LED_2 )
     {
        musicPlayer.GPIO_digitalWrite(1, HIGH); 
        LED_2 = false;
        LED_3 = true;
     }  
     else if ( LED_3 )
     {
        musicPlayer.GPIO_digitalWrite(2, HIGH); 
        LED_3 = false;
        LED_4 = true;
     }
     else if ( LED_4 )
     {
        musicPlayer.GPIO_digitalWrite(3, HIGH); 
        LED_4 = false;
        LED_put_off = true;
     }
     else if ( LED_put_off )
     {  
        for(int i=0; i<4; i++)
        {  
            musicPlayer.GPIO_digitalWrite(i, LOW);
        }
        LED_1 = true;
     }
}
void LED_OFF()
{
     for(int i=0; i<4; i++)
     {
       musicPlayer.GPIO_digitalWrite(i, LOW);
     }
}

