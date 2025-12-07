

# GPS Tracker with ESP32 and Neo-6M GPS Module


## Microcontroller(s) and Module(s) used:

- ESP32 Dev Kit
- Neo-6M GPS


## These can be found and installed in Arduino IDE

### Board:
- esp32 by Espressif Systems

### Libraries:
- Async TCP by ESP32Async
- ESP Async Web Server by ESP32Async
- TinyGPSPlus by Mikal Hart


#### Select ESP32 Dev Module as the Board to Verify or Upload the code during the development.


## Additional USB to UART Bridge VCP Drivers

When the microcontroller is connected to the laptop or PC, Arduino IDE may not detect any connected port right away. One possible reason is the lack of supported USB driver, especially on Windows. The driver that support the microcontrollers with CP210x chipset can be found on the link below.

https://www.silabs.com/software-and-tools/usb-to-uart-bridge-vcp-drivers?tab=downloads


## For more details on setting up the hardware, please refer to this article by CircuitDigest below

https://circuitdigest.com/microcontroller-projects/simple-gps-tracker-using-esp32-visualize-data-on-map
