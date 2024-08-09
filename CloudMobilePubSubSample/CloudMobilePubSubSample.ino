#include <ESP8266WiFi.h>
#include <PubSubClient.h>

// WiFi settings
const char *ssid = "IngSAC1";          // Replace with your WiFi name
const char *password = "Termita5432";  // Replace with your WiFi password

// MQTT Broker settings
const char *mqtt_broker = "broker.emqx.io";  // EMQX broker endpoint
//const char *mqtt_topic = "emqx/esp8266";     // MQTT topic
const char *mqtt_topic_iot = "topic/iot";        // MQTT topic
const char *mqtt_topic_mobile = "topic/mobile";  // MQTT topic
const char *mqtt_username = "emqx";              // MQTT username for authentication
const char *mqtt_password = "public";            // MQTT password for authentication
const int mqtt_port = 1883;                      // MQTT port (TCP)

WiFiClient espClient;
PubSubClient mqtt_client(espClient);

void connectToWiFi();

void connectToMQTTBroker();

void mqttCallback(char *topic, byte *payload, unsigned int length);

void setup() {
  Serial.begin(115200);
  connectToWiFi();
  mqtt_client.setServer(mqtt_broker, mqtt_port);
  mqtt_client.setCallback(mqttCallback);
  connectToMQTTBroker();
}

void connectToWiFi() {
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nConnected to the WiFi network");
}

void connectToMQTTBroker() {
  while (!mqtt_client.connected()) {
    String client_id = "esp8266-client-" + String(WiFi.macAddress());
    Serial.printf("Connecting to MQTT Broker as %s.....\n", client_id.c_str());
    if (mqtt_client.connect(client_id.c_str(), mqtt_username, mqtt_password)) {
      Serial.println("Connected to MQTT broker");
      mqtt_client.subscribe(mqtt_topic_mobile);
      // Publish message upon successful connection
      mqtt_client.publish(mqtt_topic_iot, "Hi EMQX I'm LM35 sensor");
    } else {
      Serial.print("Failed to connect to MQTT broker, rc=");
      Serial.print(mqtt_client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}

void mqttCallback(char *topic, byte *payload, unsigned int length) {
  Serial.print("Message received on topic: ");
  Serial.println(topic);
  Serial.print("Message:");

  String cmd = "";
  for (unsigned int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
    cmd += (char)payload[i];
  }

  Serial.println("\n----------command----------");
  Serial.println(cmd);
  Serial.println("-----------------------");
}

void loop() {
  if (!mqtt_client.connected()) {
    connectToMQTTBroker();
  }
  mqtt_client.loop();

  static unsigned long lastMsg = 0;
  unsigned long now = millis();
  float temp = 23.0;
  if (now - lastMsg > 30000) {
    lastMsg = now;
    String msg = "{timer:" + String(now) + ",temp:" + String(temp) + "}";
    Serial.print("Publishing message: ");
    Serial.println(msg);
    mqtt_client.publish(mqtt_topic_iot, msg.c_str());
  }
}
