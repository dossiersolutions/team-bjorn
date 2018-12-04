# Team Bjørn Workspace - thatButton
**For 3D (Dossier Developer Days) 2018**

---
## Included Software
### ButtonEmulatorJava

Software implementation of the button. Should work anywhere by running `javac ButtonEmulator.java` and `java ButtonEmulator`.

### ButtonServerJava

A button server implemented in Java. It requires Maven to build it `mvn install`. It is a multiplatform project and executable jar files can be found in `buttonserver-{os}-jar/target`.

### ButtonServerRuby

A button server that can be configured with json to perform actions when button events happen. It is written in Ruby, so it requires Ruby to run:

    cd button_server_ruby
    ruby server.rb

### ThatButton Arduino Project

ThatButton Arduino sowtware written for ESP8266 (Adafruit Feather HUZZAH ESP8266).

### DosseRacing 2018

A thatButton-enabled top-down racing game written in Ruby, using the Gosu game library.

**Running on Windows:**

* Must have at least Ruby version 2.3 and DevKit (to build packages)
* Install bundler: `gem install bundler`
* Run the game: `ruby racing.rb`

**Running on Linux:**

* Must have at least Ruby version 2.3
* Install dependencies: `sudo apt-get install build-essential libsdl2-dev libsdl2-ttf-dev libpango1.0-dev libgl1-mesa-dev libopenal-dev libsndfile-dev libmpg123-dev libgmp-dev`
* Install bundler: `gem install bundler`
* Run the game: `ruby racing.rb`

## Included Hardware
[See the documentation...](hardware)

## FAQ
### How to deploy latest version to server?

* Push changes to server
* `ruby deploy.rb <OPTIONAL GIT REFSPEC>`

### How does the protocol between thatButton and server look like?
v 1.0.
* buttonId - length: 2 bytes (uint16)
* messageCounter - length: 2 bytes (uint16)
* buttonState - length: 2 bytes (uint16) - value: 0 to 1 (where 0 = unpressed)
* potentiometerState - length: 2 bytes (uint16) - value: 0 to 1023
* potentiometerStep - length: 2 bytes (uint16) - value: 0 to 8

All the numbers are network order, unsigned, 16-bit integers.
