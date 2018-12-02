#define PIN_POTENTIOMETER A0
#define POTENTIOMETER_MODULE_STABILIZATION_DIFF 24

#define DEBUG_POTENTIOMETER_MODULE true

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
  int ledNumber = potentiometerStep - 1;
  if(ledNumber >= 0){
    if(MainButtonModuleGetValue() == LOW){
      LedArrayModuleClearOthersThan(ledNumber);
    }
    LedArrayModuleSingleColorPredefined(ledNumber);
  }else{
    if(MainButtonModuleGetValue() == LOW){
      LedArrayModuleClear();
    }
  } 
}

void PotentiometerModuleValueSync(){
  int potentiometerValueRaw = analogRead(PIN_POTENTIOMETER);
  log(DEBUG_POTENTIOMETER_MODULE, F("potentiometerValueRaw"), potentiometerValueRaw);
  
  int potentiometerValueAverage = PotentiometerModuleReadAverageValue();
  log(DEBUG_POTENTIOMETER_MODULE, F("potentiometerValueAverage"), potentiometerValueAverage);
  
  int potentiometerValueStabilized = PotentiometerModuleStabilizeValue(potentiometerValueAverage);
  log(DEBUG_POTENTIOMETER_MODULE, F("potentiometerValueStabilized"), potentiometerValueStabilized);
  
  int potentiometerValueNormalized = PotentiometerModuleNormalizeValue(potentiometerValueStabilized);
  log(DEBUG_POTENTIOMETER_MODULE, F("potentiometerValueNormalized"), potentiometerValueNormalized);
  
  int potentiometerStepValueCalc = PotentiometerModuleConvertValueToStep(potentiometerValueNormalized);
  log(DEBUG_POTENTIOMETER_MODULE, F("potentiometerStepValueCalc"), potentiometerStepValueCalc);
  
  potentiometerValueLast = potentiometerValue;
  potentiometerValue = potentiometerValueNormalized;
  potentiometerStepValue = potentiometerStepValueCalc;
}

int PotentiometerModuleReadAverageValue(){  
  int averageCount = 10;
  int average = 0;
  for(int i = 0; i < averageCount; ++i){
    average += analogRead(PIN_POTENTIOMETER);
    delay(1);
  }
  return average/averageCount;
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
  float valueFloat = (1023.0/(857.0 - 16)) * (value - 16);
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
  float divider = 1023.0/9.0;
  int stepValue = value/divider;
  return stepValue <= 8 ? stepValue : 8;
}


//const int runningAverageCount = 5;
//int runningAverageBuffer[runningAverageCount];
//int nextRunningAverage;
// 
//int PotentiometerModuleReadRunningAverageValue(int value){
//  runningAverageBuffer[nextRunningAverage++] = value;
//  if (nextRunningAverage >= runningAverageCount){
//    nextRunningAverage = 0; 
//  }
//  int runningAverage = 0;
//  for(int i = 0; i < runningAverageCount; ++i){
//    runningAverage += runningAverageBuffer[i];
//  }
//  return runningAverage / runningAverageCount;
//}

//int PotentiometerModuleReadMedianValue(){
//  int potentiometerValueMedian = getMedian(analogRead(PIN_POTENTIOMETER), analogRead(PIN_POTENTIOMETER), analogRead(PIN_POTENTIOMETER));
//  return potentiometerValueMedian;
//}

//int runningMedianLast = 0;
//int runningMedianLastLast = 0;
// 
//int PotentiometerModuleReadRunningMedianValue(int value){
//  int median = getMedian(value, runningMedianLastLast, runningMedianLast);
//  runningMedianLastLast = runningMedianLast;
//  runningMedianLast = value;
//  return median;
//}


