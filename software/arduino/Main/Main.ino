#define PIN_BOARD_LED 0
#define DEBUG true


void setup() {
  Serial.begin(115200);
  delay(100);
  pinMode(PIN_BOARD_LED, OUTPUT);

  MainButtonModuleInit();
  PotentiometerModuleInit();
  LedArrayModuleInit();

  NetworkModuleConnect("Fryden58HO305", "99535552");
}

 
void loop() {
  MainButtonModuleLedSync();
  PotentiometerModuleLedSync();
  delay(10);
}
