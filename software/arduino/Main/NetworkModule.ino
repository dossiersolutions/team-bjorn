#include <ESP8266WiFi.h>

#define NETWORK_MODULE_CONNECT_TIMEOUT 10000
#define NETWORK_MODULE_CONNECT_SLEEP 100

boolean NetworkModuleConnect(char* ssid, char* password) {
  LedArrayModuleClear();
  LedArrayModuleLoading();
  WiFi.begin(ssid, password);
  int connectingTime = 0;
  while (!NetworkModuleIsConnected() && connectingTime <= NETWORK_MODULE_CONNECT_TIMEOUT) {
    delay(NETWORK_MODULE_CONNECT_SLEEP);
    connectingTime += NETWORK_MODULE_CONNECT_SLEEP;
    LedArrayModuleLoading();
  }
  LedArrayModuleClear();
  delay(200);
  boolean connected = NetworkModuleIsConnected();
  if(connected){
    LedArrayModuleSuccess();
    LedArrayModuleClear();
  }else{
    LedArrayModuleError();
  }
  return connected;
}

void NetworkModuleSendStatusMessage(){
  
}

boolean NetworkModuleIsConnected() {
  boolean connected = WiFi.status() == WL_CONNECTED;
  log(F("connected"), connected);
  return connected;
}

