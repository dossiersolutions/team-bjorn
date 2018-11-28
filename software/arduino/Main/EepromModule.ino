#include <EEPROM.h>
#define EEPROM_MIN_ADDR 0
#define EEPROM_MAX_ADDR 1024

#define DEBUG_EEPROM false

boolean EepromModuleWriteString(String data, int address){
  if(DEBUG_EEPROM){
    Serial.println();
    Serial.print(F("EepromModule: Writting "));
    Serial.print(data.length());
    Serial.print(F(" bytes  to "));
    Serial.print(address);
    Serial.println(F("..."));
  }

  char cbuff[data.length() + 1];
  data.toCharArray(cbuff, data.length() + 1);
  
  boolean done = eeprom_write_string(address, cbuff);
  if(DEBUG_EEPROM){
    Serial.print(F("EepromModule: Done"));
  }
  return done;
}

String EepromModuleReadString(int address, int length){
  if(DEBUG_EEPROM){
    Serial.println();
    Serial.println(F("EepromModule: Reading..."));
  }
  char cbuff[length+1];
  eeprom_read_string(address, cbuff, length+1);
  String stemp(cbuff);
  if(DEBUG_EEPROM){
    Serial.println(stemp);
  }
  return stemp;
}

void EepromModuleWriteByte(byte data, int address){
  if(DEBUG_EEPROM){
    Serial.println();
    Serial.print(F("EepromModule: Writting 1 byte to "));
    Serial.print(address);
    Serial.println(F("..."));
  }
  EEPROM.write(address, data);
}


byte EepromModuleReadByte(int address){
  if(DEBUG_EEPROM){
    Serial.println();
    Serial.println(F("EepromModule: Reading..."));
  }
  byte b = EEPROM.read(address);
  if(DEBUG_EEPROM){
    Serial.println(b);
  }
  return EEPROM.read(address);
}

 
// Returns true if the address is between the
// minimum and maximum allowed values, false otherwise.
//
// This function is used by the other, higher-level functions
// to prevent bugs and runtime errors due to invalid addresses.
boolean eeprom_is_addr_ok(int addr) {
  return ((addr >= EEPROM_MIN_ADDR) && (addr <= EEPROM_MAX_ADDR));
}
 
 
// Writes a sequence of bytes to eeprom starting at the specified address.
// Returns true if the whole array is successfully written.
// Returns false if the start or end addresses aren't between
// the minimum and maximum allowed values.
// When returning false, nothing gets written to eeprom.
boolean eeprom_write_bytes(int startAddr, const byte* array, int numBytes) {
  // counter
  int i;
 
  // both first byte and last byte addresses must fall within
  // the allowed range
  if (!eeprom_is_addr_ok(startAddr) || !eeprom_is_addr_ok(startAddr + numBytes)) {
    return false;
  }
 
  for (i = 0; i < numBytes; i++) {
    EEPROM.write(startAddr + i, array[i]);
  }
 
  return true;
}
 
 
// Writes a string starting at the specified address.
// Returns true if the whole string is successfully written.
// Returns false if the address of one or more bytes fall outside the allowed range.
// If false is returned, nothing gets written to the eeprom.
boolean eeprom_write_string(int addr, const char* string) {
 
  int numBytes; // actual number of bytes to be written
 
  //write the string contents plus the string terminator byte (0x00)
  numBytes = strlen(string) + 1;
 
  return eeprom_write_bytes(addr, (const byte*)string, numBytes);
}
 
 
// Reads a string starting from the specified address.
// Returns true if at least one byte (even only the string terminator one) is read.
// Returns false if the start address falls outside the allowed range or declare buffer size is zero.
//
// The reading might stop for several reasons:
// - no more space in the provided buffer
// - last eeprom address reached
// - string terminator byte (0x00) encountered.
boolean eeprom_read_string(int addr, char* buffer, int bufSize) {
  byte ch; // byte read from eeprom
  int bytesRead; // number of bytes read so far
 
  if (!eeprom_is_addr_ok(addr)) { // check start address
    return false;
  }
 
  if (bufSize == 0) { // how can we store bytes in an empty buffer ?
    return false;
  }
 
  // is there is room for the string terminator only, no reason to go further
  if (bufSize == 1) {
    buffer[0] = 0;
    return true;
  }
 
  bytesRead = 0; // initialize byte counter
  ch = EEPROM.read(addr + bytesRead); // read next byte from eeprom
  buffer[bytesRead] = ch; // store it into the user buffer
  bytesRead++; // increment byte counter
 
  // stop conditions:
  // - the character just read is the string terminator one (0x00)
  // - we have filled the user buffer
  // - we have reached the last eeprom address
  while ( (ch != 0x00) && (bytesRead < bufSize) && ((addr + bytesRead) <= EEPROM_MAX_ADDR) ) {
    // if no stop condition is met, read the next byte from eeprom
    ch = EEPROM.read(addr + bytesRead);
    buffer[bytesRead] = ch; // store it into the user buffer
    bytesRead++; // increment byte counter
  }
 
  // make sure the user buffer has a string terminator, (0x00) as its last byte
  if ((ch != 0x00) && (bytesRead >= 1)) {
    buffer[bytesRead - 1] = 0;
  }
 
  return true;
}
