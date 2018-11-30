#define PIN_POTENTIOMETER A0
#define POTENTIOMETER_MODULE_STABILIZATION_DIFF 10

#define DEBUG_POTENTIOMETER_MODULE false

int potentiometerLastValueChangeTime = 0;
int potentiometerLastReading = 0;
int potentiometerValueLast = 0;
int potentiometerValue = 0;
int potentiometerStepValue = 0;

void PotentiometerModuleInit(){
  pinMode(PIN_POTENTIOMETER, INPUT);
}

int PotentiometerModuleGetLastValueChangeTime(){
  return potentiometerLastValueChangeTime;
}

boolean PotentiometerModuleHasValueChanged(){
  return potentiometerValue != potentiometerValueLast;
}

int PotentiometerModuleGetValue(){
  return potentiometerValue;
}

int PotentiometerModuleGetStepValue(){
  return potentiometerStepValue;
}

void PotentiometerModuleLedSync(){
  int potentiometerStep = PotentiometerModuleGetStepValue();
  int timeFromLastChange = millis() - PotentiometerModuleGetLastValueChangeTime();

  if(timeFromLastChange < 2000){
    LedArrayModuleSetBrightness(LED_ARRAY_MAX_BRIGHTNESS);
  }
  else{
    LedArrayModuleSetBrightness(LED_ARRAY_MIN_BRIGHTNESS);
  }
  
  int ledNumber = potentiometerStep - 1;
  if(ledNumber >= 0){
    LedArrayModuleClearOthersThan(ledNumber);
    LedArrayModuleSingleColorPredefined(ledNumber);
  }else{
    LedArrayModuleClear();
  }
}

void PotentiometerModuleValueSync(){
  int potentiometerValueMedian = PotentiometerModuleReadMedianValue();
  log(DEBUG_POTENTIOMETER_MODULE, F("potentiometerValueMedian"), potentiometerValueMedian);
  int potentiometerValueStabilized = PotentiometerModuleStabilizeValue(potentiometerValueMedian);
  log(DEBUG_POTENTIOMETER_MODULE, F("potentiometerValueStabilized"), potentiometerValueStabilized);
  int potentiometerValueNormalized = PotentiometerModuleNormalizeValue(potentiometerValueStabilized);
  log(DEBUG_POTENTIOMETER_MODULE, F("potentiometerValueNormalized"), potentiometerValueNormalized);
  int potentiometerStepValueCalc = PotentiometerModuleConvertValueToStep(potentiometerValueNormalized);
  log(DEBUG_POTENTIOMETER_MODULE, F("potentiometerStepValueCalc"), potentiometerStepValueCalc);
  
  potentiometerValueLast = potentiometerValue;
  potentiometerValue = potentiometerValueNormalized;
  potentiometerStepValue = potentiometerStepValueCalc;
}

int PotentiometerModuleReadMedianValue(){
  int potentiometerValueMedian = getMedian(analogRead(PIN_POTENTIOMETER), analogRead(PIN_POTENTIOMETER), analogRead(PIN_POTENTIOMETER));
  return potentiometerValueMedian;
}

int PotentiometerModuleStabilizeValue(int value){ 
  int difference = abs(value - potentiometerLastReading);
  log(DEBUG_POTENTIOMETER_MODULE, F("potentiometerDifference"), difference);
  if(difference <= POTENTIOMETER_MODULE_STABILIZATION_DIFF){
    return potentiometerLastReading;
  }
  else{
    potentiometerLastValueChangeTime = millis();
    potentiometerLastReading = value;
    return value;
  }
}

int PotentiometerModuleNormalizeValue(int value){ 
  float valueFloat = (1023.0/(845.0 - 16)) * (value - 16);
  int valueInt = (int) valueFloat;
  if(valueInt > 1023){
    valueInt = 1023;
  }
  if(valueInt < 0){
    valueInt = 0;
  }
  return valueInt;
}

// returns value from 0 to 8 with every step taking cca 113 range
int PotentiometerModuleConvertValueToStep(int value){
  float divider = 1024.0/9.0;
  int stepValue = value/divider;
  return stepValue <= 8 ? stepValue : 8;
}

