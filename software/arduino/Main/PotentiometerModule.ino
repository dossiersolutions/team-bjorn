#define PIN_POTENTIOMETER A0
#define POTENTIOMETER_MODULE_STABILIZATION_RANGE 5

int lastReading = 0;

void PotentiometerModuleInit(){
  pinMode(PIN_POTENTIOMETER, INPUT);
}

void PotentiometerModuleLedSync(){
  int potentiometerValue = PotentiometerModuleGetValue();
  log(F("potentiometerValue"), potentiometerValue);
  int potentiometerStep = PotentiometerModuleConvertValueToStep(potentiometerValue);
  log(F("potentiometerStep"), potentiometerStep);

  int ledNumber = potentiometerStep - 1;
  log(F("ledNumber"), ledNumber);
  if(ledNumber >= 0){
    LedArrayModuleClearOthersThan(ledNumber);
    LedArrayModuleSingleColorPredefined(ledNumber);
  }else{
    LedArrayModuleClear();
  }
}

int PotentiometerModuleGetValue(){
  int potentiometerValueRaw = analogRead(PIN_POTENTIOMETER);
  log(F("potentiometerValueRaw"), potentiometerValueRaw);
  int potentiometerValueStabilized = PotentiometerModuleStabilizeReadingValue(potentiometerValueRaw);
  log(F("potentiometerValueStabilized"), potentiometerValueStabilized);
  float potentiometerValueFloat = (1024.0/(845.0 - 12)) * (potentiometerValueStabilized - 12);
  int potentiometerValue = (int) potentiometerValueFloat;
  if(potentiometerValue > 1024){
    potentiometerValue = 1024;
  }
  if(potentiometerValue < 0){
    potentiometerValue = 0;
  }
  return potentiometerValue;
}

// returns value from 0 to 8 with every step taking cca 113 range
int PotentiometerModuleConvertValueToStep(int value){
  float divider = 1024.0/9.0;
  int stepValue = value/divider;
  return stepValue <= 8 ? stepValue : 8;
}

int PotentiometerModuleStabilizeReadingValue(int value){
  int stabilizedValue;
  if(value <= lastReading + POTENTIOMETER_MODULE_STABILIZATION_RANGE && value >= lastReading - POTENTIOMETER_MODULE_STABILIZATION_RANGE){
    stabilizedValue = lastReading;
  }
  else{
    stabilizedValue = value;
  }
  lastReading = value;
  return stabilizedValue;
}


