#define PIN_BOARD_LED 0
#define DEBUG true


void setup() {
  Serial.begin(115200);
  delay(100);
  pinMode(PIN_BOARD_LED, OUTPUT);

  MainButtonModuleInit();
  PotentiometerModuleInit();
  LedArrayModuleInit();

  NetworkModuleConnect();
}

 
void loop() {
  if(NetworkModuleIsConnected()){
    // read and store values
    MainButtonModuleValueSync();
    PotentiometerModuleValueSync();
  
    // synchronize values and lights
    if(MainButtonModuleGetValue() == LOW){
      PotentiometerModuleLedSync();
    }
    MainButtonModuleLedSync();

    NetworkModuleSync();
  }
  delay(10);
}
