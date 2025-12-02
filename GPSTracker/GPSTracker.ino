#include <ArduinoWebsockets.h>
#include <TinyGPSPlus.h>
#include <WiFi.h>
#include "time.h"

using namespace websockets;

const char* ssid = "양인계 (楊仁季)";
const char* password = "8fe8vk8s59dmrer";
const char* websockets_server = "ws://echo.websocket.org:8080";
const char* ntpServer = "pool.ntp.org";
const long gmtOffset_sec = 7 * 3600; // Jakarta GMT+7
const int daylightOffset_sec = 0;

WebsocketClient client;
TinyGPSPlus gps;
HardwareSerial SerialGPS(1);

String getTimestamp() {
  struct tm timeinfo;

  if(!getLocalTime(&timeinfo)) return "N/A";

  char buffer[30];
  strftime(buffer, sizeof(buffer), "%Y-%m-%dT%H:%M:%S%z", &timeinfo);

  return String(buffer);
}

void setup() {
  Serial.begin(115200);
  SerialGPS.begin(9600, SERIAL_8N1, 16, 17); // RX=16, TX=17

  WiFi.begin(ssid, password);
  while(WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("WiFi connected");
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);

  // Connect to Node.js Socket.IO server (WebSocket transport)
  client.connect(websockets_server);
}

void loop() {
  client.poll();

  while(SerialGPS.available() > 0) {
    gps.encode(SerialGPS.read());
    if(gps.location.isUpdated()) {
      String payload = "{ \"lat\": " + String(gps.location.lat(), 6) +
                       ", \"lng\": " + String(gps.location.lng(), 6) +
                       ", \"time\": \"" + getTimestamp() + "\" }";
      client.send(payload);
      Serial.println("Sent: " + payload);
      delay(1000);
    }
  }
}
