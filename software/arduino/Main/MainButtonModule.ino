#define DEBUG_MAIN_BUTTON_MODULE false

#define PIN_MAIN_BUTTON_LED 13
#define PIN_MAIN_BUTTON 5

#define MAIN_BUTTON_USE_BACKLIGHT true
#define MAIN_BUTTON_USE_LED_ARRAY true

int mainButtonLastValueChangeTime = 0;
int mainButtonValue = 0;
int mainButtonValueLast = 0;

void MainButtonModuleInit(){
  pinMode(PIN_MAIN_BUTTON, INPUT);
  pinMode(PIN_MAIN_BUTTON_LED, OUTPUT);
}

int MainButtonModuleGetLastValueChangeTime(){
  return mainButtonLastValueChangeTime;
}

int MainButtonModuleHasValueChanged(){
  return mainButtonValue != mainButtonValueLast;
}

int MainButtonModuleGetValue(){
 return mainButtonValue;
}

void MainButtonModuleValueSync(){
  mainButtonValueLast = mainButtonValue;
  log(DEBUG_MAIN_BUTTON_MODULE, F("mainButtonValue"), mainButtonValue);
  mainButtonValue = MainButtonModuleReadValue();
  if(mainButtonValueLast != mainButtonValue){
    mainButtonLastValueChangeTime = millis();
  }
}

void MainButtonModuleLedSync(){  
  // sync backlight
  if(MAIN_BUTTON_USE_BACKLIGHT){
    int mainButtonValue = MainButtonModuleGetValue();
    digitalWrite(PIN_MAIN_BUTTON_LED, mainButtonValue);
  }

  // sync led array
  if(MAIN_BUTTON_USE_LED_ARRAY){
    if(mainButtonValue == HIGH){
      LedArrayModuleFullWithout(COLOR_WHITE, PotentiometerModuleGetStepValue() - 1);
    }
  }
}

int MainButtonModuleReadValue(){
  int value = digitalRead(PIN_MAIN_BUTTON);
  return value;
}

void MainButtonModuleLedSuccess(){  
  digitalWrite(PIN_MAIN_BUTTON_LED, HIGH);
}
