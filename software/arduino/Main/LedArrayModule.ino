#include <Adafruit_NeoPixel.h>
#define PIN_LED_ARRAY 4
#define LED_ARRAY_NUM_PIXELS  8

#define LED_ARRAY_SUPER_BRIGHTNESS 100
#define LED_ARRAY_MAX_BRIGHTNESS 10
#define LED_ARRAY_MIN_BRIGHTNESS 1
#define LED_ARRAY_POWER_SAVING_TIMEOUT 120000

#define COLOR_NULL 0x000000
#define COLOR_LIME 0x00FF00
#define COLOR_GREEN 0x008000
#define COLOR_YELLOW 0xFFFF00
#define COLOR_RED 0xFF0000
#define COLOR_WHITE 0xFFFFFF
#define COLOR_CYAN 0x00FFFF
#define COLOR_BLUE 0x0000FF
#define COLOR_PURPLE 0xFF00FF
#define COLOR_PINK 0xFF1493
#define COLOR_MAROON 0x800000

const int COLORS[8] = {COLOR_GREEN, COLOR_YELLOW, COLOR_PURPLE, COLOR_RED, COLOR_LIME, COLOR_PINK, COLOR_BLUE, COLOR_MAROON};

Adafruit_NeoPixel strip = Adafruit_NeoPixel(LED_ARRAY_NUM_PIXELS, PIN_LED_ARRAY, NEO_GRBW + NEO_KHZ800);

boolean ledArrayPowerSaveMode = false;
int ledArrayModuleRowUnblockingCount = 0;

void LedArrayModuleInit() {
  strip.begin();
}

boolean LedArrayIsPowerSaveMode(){
  return ledArrayPowerSaveMode;
}

void LedArrayBrightnesSync(){
  int timeFromLastPotentiometerChange = millis() - PotentiometerModuleGetLastValueChangeTime();
  int timeFromLastButtonChange = millis() - MainButtonModuleGetLastValueChangeTime();
  int timeFromLastChange = min(timeFromLastPotentiometerChange, timeFromLastButtonChange);

  if(timeFromLastChange < 2000){
    LedArrayModuleSetBrightness(LED_ARRAY_MAX_BRIGHTNESS);
    ledArrayPowerSaveMode = false;
  }
  else if (timeFromLastChange < LED_ARRAY_POWER_SAVING_TIMEOUT){
    LedArrayModuleSetBrightness(LED_ARRAY_MIN_BRIGHTNESS);
    ledArrayPowerSaveMode = false;
  }
  else if(!ledArrayPowerSaveMode){
    LedArrayModuleClear();
    LedArrayModuleSetBrightness(0);
    ledArrayPowerSaveMode = true;
  }
}

void LedArrayModuleClear(){
   for(int i = 0; i<LED_ARRAY_NUM_PIXELS; i++){ 
    strip.setPixelColor(i, COLOR_NULL); strip.show();
  }
  ledArrayModuleRowUnblockingCount = 0; // hack
}

void LedArrayModuleClearOthersThan(int pixelNumber){
   for(int i = 0; i<LED_ARRAY_NUM_PIXELS; i++){ 
    if(i != pixelNumber){
      strip.setPixelColor(i, COLOR_NULL); strip.show();
    }
  }
}

void LedArrayModuleRow(uint16_t speed, uint32_t color){
 for (int count = 0; count<LED_ARRAY_NUM_PIXELS; count++) {
    strip.setPixelColor(count, color);
    strip.show();
    delay(speed);
  }
}


void LedArrayModuleRowUnblocking(uint16_t speed, uint32_t color){
 if(nonBlockingDelay(speed)){
    strip.setPixelColor(ledArrayModuleRowUnblockingCount++, color);
    strip.show();
 }
 if(ledArrayModuleRowUnblockingCount > LED_ARRAY_NUM_PIXELS){
    ledArrayModuleRowUnblockingCount = 0;
 }
}

void LedArrayModuleFullWithout(uint32_t color, int pixelNumber){
  for(int i = 0; i<LED_ARRAY_NUM_PIXELS; i++){
    if(i != pixelNumber){
      strip.setPixelColor(i, color); strip.show();
    }
  }
}

void LedArrayModuleSingleColor(int pixelNumber, uint32_t color){
  strip.setPixelColor(pixelNumber, color);
  strip.show();
}

void LedArrayModuleSingleColorPredefined(int pixelNumber){
  strip.setPixelColor(pixelNumber, COLORS[pixelNumber]);
  strip.show();
}

void LedArrayModuleSingleBlinking(int pixelNumber, uint32_t color){
  strip.setPixelColor(pixelNumber, color);
  strip.show();
}

void LedArrayModuleLoading(boolean unblocking){
  if(!unblocking){
    LedArrayModuleSetBrightness(LED_ARRAY_SUPER_BRIGHTNESS);
    LedArrayModuleKnightRider(3, 64, 2, COLOR_WHITE);
  }
  else{
    LedArrayModuleRowUnblocking(500, COLOR_WHITE);   
  }
}

void LedArrayModuleSetBrightness(byte brightness){
  strip.setBrightness(brightness);
}

void LedArrayModuleSuccess(){
  LedArrayModuleSetBrightness(LED_ARRAY_SUPER_BRIGHTNESS);
  LedArrayModuleRow(64, COLOR_GREEN);
  delay(1000);
  LedArrayModuleSetBrightness(LED_ARRAY_MIN_BRIGHTNESS);
}

void LedArrayModuleError(){
  LedArrayModuleSetBrightness(1);
  for(int i = 0; i<LED_ARRAY_NUM_PIXELS; i++){
    strip.setPixelColor(i, COLOR_RED); strip.show();
  }
  delay(2000);
  LedArrayModuleSetBrightness(LED_ARRAY_MIN_BRIGHTNESS);
}

void LedArrayModuleKnightRider(uint16_t cycles, uint16_t speed, uint8_t width, uint32_t color) {
  uint32_t old_val[LED_ARRAY_NUM_PIXELS]; // up to 256 lights!
  for(int i = 0; i < cycles; i++){
    for (int count = 1; count<LED_ARRAY_NUM_PIXELS; count++) {
      strip.setPixelColor(count, color);
      old_val[count] = color;
      for(int x = count; x>0; x--) {
        old_val[x-1] = LedArrayModuleDimColor(old_val[x-1], width);
        strip.setPixelColor(x-1, old_val[x-1]); 
      }
      strip.show();
      delay(speed);
    }
    for (int count = LED_ARRAY_NUM_PIXELS-1; count>=0; count--) {
      strip.setPixelColor(count, color);
      old_val[count] = color;
      for(int x = count; x<=LED_ARRAY_NUM_PIXELS ;x++) {
        old_val[x-1] = LedArrayModuleDimColor(old_val[x-1], width);
        strip.setPixelColor(x+1, old_val[x+1]);
      }
      strip.show();
      delay(speed);
    }
  }
}

uint32_t LedArrayModuleDimColor(uint32_t color, uint8_t width) {
   return (((color&0xFF0000)/width)&0xFF0000) + (((color&0x00FF00)/width)&0x00FF00) + (((color&0x0000FF)/width)&0x0000FF);
}


