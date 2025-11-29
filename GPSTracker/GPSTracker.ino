#include <WiFi.h>
#include <WebSocketsClient.h>
#include <TinyGPSPlus.h>

const char* ssid = "양인계 (楊仁季)";
const char* password = "8fe8vk8s59dmrer";

WebSocketsClient webSocket;
TinyGPSPlus gps;
HardwareSerial SerialGPS(1);

void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {
  switch(type) {
    case WStype_CONNECTED:
      Serial.println("Connected to Socket.IO server");
      break;
    case WStype_DISCONNECTED:
      Serial.println("Disconnected");
      break;
    case WStype_TEXT:
      Serial.printf("Message from server: %s\n", payload);
      break;
  }
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

  // Connect to Node.js Socket.IO server (WebSocket transport)
  webSocket.begin("your-server-ip", 3000, "/socket.io/?EIO=4&transport=websocket");
  webSocket.onEvent(webSocketEvent);
}

void loop() {
  webSocket.loop();

  while (SerialGPS.available() > 0) {
    gps.encode(SerialGPS.read());
    if (gps.location.isUpdated()) {
      String payload = "{\"lat\": " + String(gps.location.lat(), 6) +
                       ", \"lng\": " + String(gps.location.lng(), 6) + "}";
      webSocket.sendTXT(payload);
      Serial.println("Sent: " + payload);
    }
  }
}
