#define PIN_POTENTIOMETER A0
#define POTENTIOMETER_MODULE_STABILIZATION_DIFF 6

int potentiometerLastReading = 0;

void PotentiometerModuleInit(){
  pinMode(PIN_POTENTIOMETER, INPUT);
}

void PotentiometerModuleLedSync(){
  int potentiometerValue = PotentiometerModuleGetValue();
  log(F("potentiometerValue"), potentiometerValue);
  int potentiometerStep = PotentiometerModuleConvertValueToStep(potentiometerValue);
  log(F("potentiometerStep"), potentiometerStep);

  int ledNumber = potentiometerStep - 1;
  if(ledNumber >= 0){
    LedArrayModuleClearOthersThan(ledNumber);
    LedArrayModuleSingleColorPredefined(ledNumber);
  }else{
    LedArrayModuleClear();
  }
}

int PotentiometerModuleGetValue(){
  int potentiometerValueAverage = PotentiometerModuleReadMedianValue();
  int potentiometerValueStabilized = PotentiometerModuleStabilizeReadingValue(potentiometerValueAverage);
  log(F("potentiometerValueStabilized"), potentiometerValueStabilized);
  float potentiometerValueFloat = (1024.0/(845.0 - 16)) * (potentiometerValueStabilized - 16);
  int potentiometerValue = (int) potentiometerValueFloat;
  if(potentiometerValue > 1024){
    potentiometerValue = 1024;
  }
  if(potentiometerValue < 0){
    potentiometerValue = 0;
  }
  return potentiometerValue;
}

int PotentiometerModuleReadMedianValue(){
  int potentiometerValueMedian = getMedian(analogRead(PIN_POTENTIOMETER), analogRead(PIN_POTENTIOMETER), analogRead(PIN_POTENTIOMETER));
  log(F("potentiometerValueMedian"), potentiometerValueMedian);
  return potentiometerValueMedian;
}

int PotentiometerModuleStabilizeReadingValue(int value){ 
  int difference = abs(value - potentiometerLastReading);
  log(F("potentiometerDifference"), difference);
  if(difference <= POTENTIOMETER_MODULE_STABILIZATION_DIFF){
    return potentiometerLastReading;
  }
  else{
    potentiometerLastReading = value;
    return value;
  }
}

// returns value from 0 to 8 with every step taking cca 113 range
int PotentiometerModuleConvertValueToStep(int value){
  float divider = 1024.0/9.0;
  int stepValue = value/divider;
  return stepValue <= 8 ? stepValue : 8;
}

