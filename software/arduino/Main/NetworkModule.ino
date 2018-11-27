#include <ESP8266WiFi.h>


void NetworkModuleInit(char* ssid, char* password){
  WiFi.begin(ssid, password);
}

boolean NetworkModuleIsConnected(){
  boolean connected = WiFi.status() == WL_CONNECTED;
//  if(connected){
//    logString(F("IP address"), WiFi.localIP());
//  }
  log(F("connected"), connected);
  return connected;
}

