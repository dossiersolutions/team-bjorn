#include <ESP8266WiFi.h>

#define DEBUG_NETWORK_MODULE false

#define NETWORK_MODULE_CONNECT_TIMEOUT 10000
#define NETWORK_MODULE_CONNECT_SLEEP 100

#define NETWORK_MODULE_SERVER_CONNECT_TIMEOUT 200
#define NETWORK_MODULE_SERVER_CONNECT_ERROR_TIMEOUT 4000

#define NETWORK_MODULE_SSID "Fryden58HO305"
#define NETWORK_MODULE_PASSWORD "99535552"
#define NETWORK_MODULE_SERVER "192.168.10.173"
#define NETWORK_MODULE_SERVER_PORT 38911

uint16_t messageCounter = 0;
int errorTime = 0;

void NetworkModuleConnect() {
  LedArrayModuleClear();
  LedArrayModuleLoading();
  WiFi.begin(NETWORK_MODULE_SSID, NETWORK_MODULE_PASSWORD);
  int connectingTime = 0;
  while (!NetworkModuleIsConnected() && connectingTime <= NETWORK_MODULE_CONNECT_TIMEOUT) {
    delay(NETWORK_MODULE_CONNECT_SLEEP);
    connectingTime += NETWORK_MODULE_CONNECT_SLEEP;
    LedArrayModuleLoading();
  }
  LedArrayModuleClear();
  delay(200);
  
  boolean connected = NetworkModuleIsConnected();
  if (connected) {
    LedArrayModuleSuccess();
    LedArrayModuleClear();
  } else {
    LedArrayModuleError();
  }
}

void NetworkModuleSync() {
  boolean buttonChanged = MainButtonModuleHasValueChanged();
  log(DEBUG_NETWORK_MODULE, F("buttonChanged"), buttonChanged);
  boolean potentiometerChanged = PotentiometerModuleHasValueChanged();
  log(DEBUG_NETWORK_MODULE, F("potentiometerChanged"), potentiometerChanged);
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
  log(DEBUG_NETWORK_MODULE, F("connectingToClient "));
  if (client.connect(NETWORK_MODULE_SERVER, NETWORK_MODULE_SERVER_PORT)) {
    log(DEBUG_NETWORK_MODULE, F("OK"));
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
    log(DEBUG_NETWORK_MODULE, F("ERROR"));
    errorTime = millis();
    LedArrayModuleError();
  }
}



boolean NetworkModuleIsConnected() {
  boolean connected = WiFi.status() == WL_CONNECTED;
  //  log(DEBUG_NETWORK_MODULE, F("connected"), connected);
  return connected;
}

