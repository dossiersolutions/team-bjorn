# Team Bjørn Workspace - thatButton
**For 3D (Dossier Developer Days) 2018**

---

#### Protocol (button -> server) ver. 1.0

* buttonnId - length: 2 bytes (uint16)
* messageCounter - length: 2 bytes (uint16)
* buttonState - length: 2 bytes (uint16) - value: 0 to 1 (where 0 = unpressed)
* potentiometerState - length: 2 bytes (uint16) - value: 0 to 1023
* potentiometerStep - length: 2 bytes (uint16) - value: 0 to 8

#### ButtonEmulator.java

Software implementation of the button. Should work anywhere by running `javac ButtonEmulator.java` and `java ButtonEmulator`.

#### button_test_server.rb

A very basic test server for the button protocol. Does nothing except log the received messages to console. Requires Ruby to run. Run it like this: `ruby button_test_server.rb`.
