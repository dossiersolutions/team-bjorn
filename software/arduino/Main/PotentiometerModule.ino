#define PIN_POTENTIOMETER A0

void PotentiometerModuleInit(){
  pinMode(PIN_POTENTIOMETER, INPUT);
}

void PotentiometerModuleNetworkSync(){
  int potentiometerValueRaw = analogRead(PIN_POTENTIOMETER);
  log(F("potentiometerValueRaw"), potentiometerValueRaw);
  float potentiometerValueFloat = (1024.0/848.0) * analogRead(PIN_POTENTIOMETER);
  int potentiometerValue = (int) potentiometerValueFloat;
  log(F("potentiometerValue"), potentiometerValue);
}
