# Team Bjørn Workspace - thatButton
**For 3D (Dossier Developer Days) 2018**

---

#### Protocol (button -> server) ver. 1.0

* buttonnId - length: 2 bytes (uint16)
* messageCounter - length: 2 bytes (uint16)
* buttonState - length: 2 bytes (uint16) - value: 0 to 1 (where 0 = unpressed)
* potentiometerState - length: 2 bytes (uint16) - value: 6 to 848
* potentiometerStep - length: 2 bytes (uint16) - value: 0 to 8

#### ButtonEmulator.java

Software implementation of the button. Should work anywhere by running `javac ButtonEmulator.java` and `java ButtonEmulator`.

#### button_server_ruby

A button server that can be configured with json to perform actions when button events happen. Written in ruby.

It is written in Ruby, so it requires Ruby to run. Run it like this:

    cd button_server_ruby
    ruby server.rb
