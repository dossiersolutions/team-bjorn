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

int getMedian(int a, int b , int c) {
    int x = a-b;
    int y = b-c;
    int z = a-c;
    if(x*y > 0) return b;
    if(x*z > 0) return c;
    return a;
}
