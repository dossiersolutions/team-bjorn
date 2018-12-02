#include <ESP8266WiFi.h>

#define DEBUG_NETWORK_MODULE false

#define NETWORK_MODULE_CONNECT_TIMEOUT 10000
#define NETWORK_MODULE_CONNECT_SLEEP 1

#define NETWORK_MODULE_SERVER_CONNECT_TIMEOUT 200
#define NETWORK_MODULE_SERVER_CONNECT_ERROR_TIMEOUT 10000

#define NETWORK_MODULE_SSID "Fryden58HO305"
#define NETWORK_MODULE_PASSWORD "99535552"
#define NETWORK_MODULE_SERVER "192.168.10.173"
#define NETWORK_MODULE_SERVER_PORT 38911

uint16_t messageCounter = 0;
int errorTime = 0;

void NetworkModuleConnect(boolean quick) {
  log(DEBUG_NETWORK_MODULE, F("connecting to wifi "));
  if(!quick){
    LedArrayModuleClear();
  }

  WiFi.begin(NETWORK_MODULE_SSID, NETWORK_MODULE_PASSWORD);
  int connectingTime = 0;
  while (!NetworkModuleIsConnected() && connectingTime <= NETWORK_MODULE_CONNECT_TIMEOUT) {
    delay(NETWORK_MODULE_CONNECT_SLEEP);
    connectingTime += NETWORK_MODULE_CONNECT_SLEEP;
    LedArrayModuleLoading(quick);
  }
  if(!quick){
    LedArrayModuleClear();
    delay(200);
  }
  
  boolean connected = NetworkModuleIsConnected();
  if (connected) {
    if(!quick){
      LedArrayModuleSuccess();
    }
    LedArrayModuleClear();
    log(DEBUG_NETWORK_MODULE, F("connected \n"));
  } else {
    if(!quick){
      LedArrayModuleError();
    }
    log(DEBUG_NETWORK_MODULE, F("error \n"));
  }
  connectingTime = 0;
}

void  NetworkModulePowerSaveSync(){
  int timeFromLastPotentiometerChange = millis() - PotentiometerModuleGetLastValueChangeTime();
  int timeFromLastButtonChange = millis() - MainButtonModuleGetLastValueChangeTime();
  int timeFromLastChange = min(timeFromLastPotentiometerChange, timeFromLastButtonChange);
  boolean hasInputChanged = MainButtonModuleHasValueChanged() || PotentiometerModuleHasValueChanged();
  boolean isConected = NetworkModuleIsConnected();

  if(hasInputChanged){
    if(WiFi.getMode() == 0){ //WIFI_OFF
      WiFi.forceSleepWake();
      delay(1);
      log(DEBUG_NETWORK_MODULE, F("turning on the wifi \n"));
      WiFi.mode(WIFI_STA);
      NetworkModuleConnect(true);
    }
  }
  
  if(timeFromLastChange > LED_ARRAY_POWER_SAVING_TIMEOUT){
    if(WiFi.getMode() == 1){ //WIFI_STA
      log(DEBUG_NETWORK_MODULE, F("turning off the wifi \n"));
      WiFi.mode(WIFI_OFF);
      delay(100);
      WiFi.forceSleepBegin();
      delay(100);
    }
  }
}

void NetworkModuleSync() {
  boolean buttonChanged = MainButtonModuleHasValueChanged();
  boolean potentiometerChanged = PotentiometerModuleHasValueChanged();
  if (buttonChanged || potentiometerChanged) {
    uint16_t id = 11;
    uint16_t buttonValue = (uint16_t) MainButtonModuleGetValue();
    uint16_t potentiometerState = (uint16_t) PotentiometerModuleGetValue();
    uint16_t potentiometerStep = (uint16_t) PotentiometerModuleGetStepValue();
    NetworkModuleSendStatusMessage(id, messageCounter++, buttonValue, potentiometerState, potentiometerStep);
  }
}

void NetworkModuleSendStatusMessage(uint16_t id, uint16_t messageCounter, uint16_t buttonState, uint16_t potentiometerState, uint16_t potentiometerStep) {
  // dont send any message for some time if error happened
  if (errorTime > 0 && millis() - errorTime < NETWORK_MODULE_SERVER_CONNECT_ERROR_TIMEOUT) {
    return; 
  }else {
    errorTime = 0;
  }

  // send message
  WiFiClient client;
  client.setTimeout(NETWORK_MODULE_SERVER_CONNECT_TIMEOUT);
  log(DEBUG_NETWORK_MODULE, F("connectingToServer "));
  if (client.connect(NETWORK_MODULE_SERVER, NETWORK_MODULE_SERVER_PORT)) {
    log(DEBUG_NETWORK_MODULE, F("OK \n"));
    uint8_t data[10];
    data[0] = (uint8_t) (id >> 8);
    data[1] = (uint8_t) (id & 0xff);

    data[2] = (uint8_t) (messageCounter >> 8);
    data[3] = (uint8_t) (messageCounter & 0xff);

    data[4] = (uint8_t) (buttonState >> 8);
    data[5] = (uint8_t) (buttonState & 0xff);

    data[6] = (uint8_t) (potentiometerState >> 8);
    data[7] = (uint8_t) (potentiometerState & 0xff);

    data[8] = (uint8_t) (potentiometerStep >> 8);
    data[9] = (uint8_t) (potentiometerStep & 0xff);
    
    client.write(data, 10);
    client.flush();
  } else {
    log(DEBUG_NETWORK_MODULE, F("ERROR\n"));
    errorTime = millis();
    LedArrayModuleError();
  }
}

boolean NetworkModuleIsConnected() {
  boolean connected = WiFi.status() == WL_CONNECTED;
  //  log(DEBUG_NETWORK_MODULE, F("connected"), connected);
  return connected;
}

