void log(boolean debugEnabled, String name){
  if(DEBUG && debugEnabled){
    Serial.print(name);
  }
}

void log(boolean debugEnabled, String name, int value){
  if(DEBUG && debugEnabled){
    Serial.print(name);
    Serial.print(",");
    Serial.print(value);
    Serial.println(",");
  }
}

void logString(boolean debugEnabled, String name, char* value){
  if(DEBUG && debugEnabled){
    Serial.print(name);
    Serial.print(",");
    Serial.print(value);
    Serial.println(",");
  }
}
void logUint(boolean debugEnabled, String name, uint16_t value){
  if(DEBUG && debugEnabled){
    Serial.print(name);
    Serial.print(",");
    Serial.print(value);
    Serial.println(",");
  }
}

int nonBlockingDelayStartTime = 0;

boolean nonBlockingDelay(int delayTime){
  if(nonBlockingDelayStartTime == 0){
    nonBlockingDelayStartTime = millis();
    return false;
  }
  if(millis() > nonBlockingDelayStartTime + delayTime){
    nonBlockingDelayStartTime = 0;
    return true;
  }
  return false;
}

int getMedian(int a, int b , int c) {
    int x = a-b;
    int y = b-c;
    int z = a-c;
    if(x*y > 0) return b;
    if(x*z > 0) return c;
    return a;
}

