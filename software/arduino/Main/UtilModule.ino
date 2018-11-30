void log(boolean debugEnabled, String name){
  if(DEBUG && debugEnabled){
    Serial.print(name);
  }
}

void log(boolean debugEnabled, String name, int value){
  if(DEBUG && debugEnabled){
    Serial.print(name);
    Serial.print(": ");
    Serial.println(value);
  }
}

void logString(boolean debugEnabled, String name, char* value){
  if(DEBUG && debugEnabled){
    Serial.print(name);
    Serial.print(": ");
    Serial.println(value);
  }
}
void logUint(boolean debugEnabled, String name, uint16_t value){
  if(DEBUG && debugEnabled){
    Serial.print(name);
    Serial.print(": ");
    Serial.println(value);
  }
}

int getMedian(int a, int b , int c) {
    int x = a-b;
    int y = b-c;
    int z = a-c;
    if(x*y > 0) return b;
    if(x*z > 0) return c;
    return a;
}

