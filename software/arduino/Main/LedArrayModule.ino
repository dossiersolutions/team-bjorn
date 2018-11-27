#include <Adafruit_NeoPixel.h>
#define PIN_LED_ARRAY 4
#define LED_ARRAY_NUM_PIXELS  8

#define COLOR_GREEN 0x00FF00
#define COLOR_YELLOW 0xFFFF00
#define COLOR_RED 0x00FF00
#define COLOR_NULL 0x000000
#define COLOR_WHITE 0xFFFFFF

Adafruit_NeoPixel strip = Adafruit_NeoPixel(LED_ARRAY_NUM_PIXELS, PIN_LED_ARRAY, NEO_GRBW + NEO_KHZ800);

void LedArrayModuleInit() {
  strip.begin();
}

void LedArrayModuleClear(){
   for(int i = 0; i<LED_ARRAY_NUM_PIXELS; i++){ 
    strip.setPixelColor(i, COLOR_NULL); strip.show();
  }
}

void LedArrayModuleLoading(){
  LedArrayModuleSetBrightness(10);
  LedArrayModuleKnightRider(3, 64, 2, COLOR_WHITE);
}

void LedArrayModuleSetBrightness(byte brightness){
  strip.setBrightness(brightness);
}

void LedArrayModuleSuccess(){
  LedArrayModuleSetBrightness(5);
  LedArrayModuleRow(64, COLOR_GREEN);
  delay(2000);
}

void LedArrayModuleError(){
  for(int i = 0; i<LED_ARRAY_NUM_PIXELS; i++){
    strip.setPixelColor(i, COLOR_RED); strip.show();
  }
  delay(3000);
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


void LedArrayModuleRow(uint16_t speed, uint32_t color){
 for (int count = 0; count<LED_ARRAY_NUM_PIXELS; count++) {
    strip.setPixelColor(count, color);
    strip.show();
    delay(speed);
  }
}
