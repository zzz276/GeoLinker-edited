# GeoLinker-edited
## GPS Tracker with ESP32 and Neo-6M GPS Module

### Microcontroller(s) and Module(s) used:
ESP32
Neo-6M GPS


### These can be found and installed in Arduino IDE
#### Board:
esp32 by Espressif Systems

#### Libraries:
ArduinoJson by Benoit Blanchon
GeoLinker by Jobit Joseph, Circuit Digest


### Select ESP32 Dev Module as the Board to Verify or Upload the code during the development.


### Additional USB to UART Bridge VCP Drivers
When the microcontroller is connected to the laptop or PC, Arduino IDE may not detect any connected port. One possible reason is the lack of supported USB driver, especially on Windows. The driver that support the microcontrollers with CP210x chipset can be found in the liink below.
https://www.silabs.com/software-and-tools/usb-to-uart-bridge-vcp-drivers?tab=downloads


More details in this article below
https://circuitdigest.com/microcontroller-projects/simple-gps-tracker-using-esp32-visualize-data-on-map
