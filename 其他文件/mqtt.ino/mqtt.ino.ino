
#include <DHTesp.h>
#include <WiFi.h>
#include <WiFiClient.h>
#include <PubSubClient.h>

#define DHT11PIN 18
#define RELAYPIN 27


const char* ssid = "ASUS";
const char* password = "jingai.love";
const char* mqtt_server = "47.104.142.113"; // 使用HIVEMQ 的信息中转服务
const char* mqtt_username = "admin";
const char* mqtt_password = "111220179";
const char* sub_topic = "power";                     // 订阅信息主题
const char* pub_topic_1 = "temperature";                     // 发布信息主题
const char* pub_topic_2 = "state";                     // 发布信息主题
const char* client_id = "esp32";                   // 标识当前设备的客户端编号
int state = 0;

DHTesp dht;
TaskHandle_t tempTaskHandle = NULL;
WiFiClient espClient;                                                         // 定义wifiClient实例
PubSubClient client(espClient);                                         // 定义PubSubClient的实例
long lastMsg = 0;                                                               // 记录上一次发送信息的时长

void setup() {
  pinMode(RELAYPIN, OUTPUT);                               // 定义继电器输出引脚
  Serial.begin(115200); 
  dht.setup(DHT11PIN, DHTesp::DHT11);
  setup_wifi();                                                                    //执行Wifi初始化，下文有具体描述
  client.setServer(mqtt_server, 61613);                              //设定MQTT服务器与使用的端口，1883是默认的MQTT端口
  client.setCallback(callback);                                          //设定回调方式，当ESP8266收到订阅消息时会调用此方法
}

void setup_wifi() {

  delay(10);
  // 板子通电后要启动，稍微等待一下让板子点亮
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);   // 打印主题信息
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]); // 打印主题内容
  }
  Serial.println();

  if ((char)payload[0] == '1') {
    digitalWrite(RELAYPIN, HIGH);   // 亮灯
    state = 1;
  } else {
    digitalWrite(RELAYPIN, LOW);   // 熄灯
    state = 0;
  }
  //发布电源状态消息
  char pub2[20];
  sprintf(pub2, "state:%d",state);
  client.publish(pub_topic_2, pub2);
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (client.connect(client_id,mqtt_username,mqtt_password)) {
      Serial.println("connected");
      // 连接成功时订阅主题
      client.subscribe(sub_topic);
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

void loop() {
  
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  long now = millis();
  if (now - lastMsg > 2000) {
    char pub1[20];
    TempAndHumidity lastValues = dht.getTempAndHumidity();
    Serial.println("Temperature: " + String(lastValues.temperature,0));
    Serial.println("Humidity: " + String(lastValues.humidity,0));
    lastMsg = now;
    sprintf(pub1, "Temperature:%f#Humidity:%f",lastValues.temperature,lastValues.humidity);
    client.publish(pub_topic_1, pub1);
  }
}
