void log(String name, int value){
  if(DEBUG){
    Serial.print(name);
    Serial.print(": ");
    Serial.println(value);
  }
}

void logString(String name, char* value){
  if(DEBUG){
    Serial.print(name);
    Serial.print(": ");
    Serial.println(value);
  }
}
