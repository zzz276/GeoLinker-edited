#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <TinyGPSPlus.h>
#include <WiFi.h>
#include "time.h"

const char* ssid = "양인계 (楊仁季)";
const char* password = "8fe8vk8s59dmrer";
const char* ntpServer = "pool.ntp.org";
const long gmtOffset_sec = 7 * 3600; // Jakarta GMT+7
const int daylightOffset_sec = 0;

AsyncWebServer server(80);
AsyncWebSocket ws("/ws");
HardwareSerial SerialGPS(1);
TinyGPSPlus gps;

String lastJson = "";

String getTimestamp() {
  struct tm timeinfo;

  if(!getLocalTime(&timeinfo)) return "N/A";

  char buffer[30];
  strftime(buffer, sizeof(buffer), "%Y-%m-%dT%H:%M:%S%z", &timeinfo);

  return String(buffer);
}

void buildGpsJson() {
  if (gps.location.isValid()) {
    lastJson = "{";
    lastJson += "\"lat\": " + String(gps.location.lat(), 6) + ",";
    lastJson += "\"lng\": " + String(gps.location.lng(), 6) + ",";
    lastJson += "\"time\": \"" + getTimestamp() + "\"";
    lastJson += "}";
  }
}

void sendGpsData() {
  buildGpsJson();
  if (lastJson.length() > 0) {
    ws.textAll(lastJson); // broadcast to all connected clients
    Serial.println("Sent: " + lastJson);
  }
}

void onWsEvent(AsyncWebSocket *server, AsyncWebSocketClient *client, AwsEventType type, void *arg, uint8_t *data, size_t len) {
  if (type == WS_EVT_CONNECT) {
    Serial.printf("Client %u connected\n", client->id());
    client->text("{\"status\":\"connected\"}");
  } else if (type == WS_EVT_DATA) {
    String msg = "";
    for (size_t i = 0; i < len; i++) { msg += (char) data[i]; }

    Serial.printf("Received from client %u: %s\n", client->id(), msg.c_str());
    if (msg == "SYNC") {
      // Respond immediately with cached JSON
      if (lastJson.length() > 0) {
        client->text(lastJson);
        Serial.println("SYNC response: " + lastJson);
      } else { client->text("{\"error\":\"No valid GPS data\"}"); }
    }
  } else if (type == WS_EVT_DISCONNECT) { Serial.printf("Client %u disconnected\n", client->id()); }
}

void setup() {
  Serial.begin(115200);
  SerialGPS.begin(9600, SERIAL_8N1, 16, 17); // RX=16, TX=17
  delay(1000);

  Serial.println("Connecting to WiFi...");
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  while(WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.print("\nWiFi connected. IP: ");
    Serial.println(WiFi.localIP());
  }

  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);

  ws.onEvent(onWsEvent);
  server.addHandler(&ws);

  // Optional: a simple HTTP index to verify connectivity
  // server.on("/", HTTP_GET, [](AsyncWebServerRequest *request) { request->send(200, "text/plain", "ESP32 WebSocket server is running. Connect to ws://<ESP32_IP>/ws"); });

  server.begin();
  Serial.println("WebSocket server started on /ws");
}

void loop() {
  while(SerialGPS.available() > 0) { gps.encode(SerialGPS.read()); }

  // Send GPS data every 2 seconds if valid
  static unsigned long lastSend = 0;

  if (millis() - lastSend > 2000) {
    sendGpsData();
    lastSend = millis();
  }
}
