#define PIN_BOARD_LED 0
#define DEBUG true


void setup() {
  Serial.begin(115200);
  delay(100);
  pinMode(PIN_BOARD_LED, OUTPUT);

  MainButtonModuleInit();
  PotentiometerModuleInit();
  LedArrayModuleInit();
  
  LedArrayModuleClear();
  LedArrayModuleLoading();
  NetworkModuleInit("Fryden58HO305", "99535552");
  while(!NetworkModuleIsConnected()){
    LedArrayModuleLoading();  
  }
  LedArrayModuleClear();
  LedArrayModuleSuccess();
  LedArrayModuleClear();
}

 
void loop() {
  MainButtonModuleLedSync();
  
//  delay(100);
}
