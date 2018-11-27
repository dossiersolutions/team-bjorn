#define PIN_MAIN_BUTTON_LED 13
#define PIN_MAIN_BUTTON 5

void MainButtonModuleInit(){
  pinMode(PIN_MAIN_BUTTON, INPUT);
  pinMode(PIN_MAIN_BUTTON_LED, OUTPUT);
}

void MainButtonModuleNetworkSync(){
  int mainButtonValue = digitalRead(PIN_MAIN_BUTTON);
 
}
  
void MainButtonModuleLedSync(){  
  int mainButtonValue = digitalRead(PIN_MAIN_BUTTON);
  log(F("mainButtonValue"), mainButtonValue);
  int mainButtonLedValue = mainButtonValue;
  log(F("mainButtonLedValue"), mainButtonLedValue);
  digitalWrite(PIN_MAIN_BUTTON_LED, mainButtonLedValue);
}

void MainButtonModuleLedSuccess(){  
  digitalWrite(PIN_MAIN_BUTTON_LED, HIGH);
}
