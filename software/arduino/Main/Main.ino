#define PIN_BOARD_LED 0
#define DEBUG true


void setup() {
  Serial.begin(115200);
  delay(100);
  pinMode(PIN_BOARD_LED, OUTPUT);

  MainButtonModuleInit();
  PotentiometerModuleInit();
  LedArrayModuleInit();

  NetworkModuleConnect(true);
}

 
void loop() {
//  if(NetworkModuleIsConnected()){
    // read and store values
    MainButtonModuleValueSync();
    PotentiometerModuleValueSync();

    // update the brightness of ledArray based on the input
    LedArrayBrightnesSync();
    // synchronize values and lights
    PotentiometerModuleLedSync();
    MainButtonModuleLedSync();

    NetworkModulePowerSaveSync();

    // sync state with button server
    NetworkModuleSync();
//  }
  delay(10);
}
