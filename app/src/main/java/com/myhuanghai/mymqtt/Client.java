package com.myhuanghai.mymqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;

public class Client {

    private static final String HOST = "tcp://47.104.142.113:61613";
    private static final String TOPIC = "home.status";
    private static final String clientid = "android";
    private static final String userName = "admin";
    private static final String passWord = "111220179";
    private HashMap<String, MqttTopic> topicList = new HashMap<>();


    void start(String[] publicTopics, String[] subscribeTopics, PushCallback pushCallback) {
        try {
            // host为主机名，clientid即连接MQTT的客户端ID，一般以唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            MqttClient client = new MqttClient(HOST, clientid, new MemoryPersistence());
            // MQTT的连接设置
            MqttConnectOptions options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            // 设置连接的用户名
            options.setUserName(userName);
            // 设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            // 设置回调
            client.setCallback(pushCallback);
            MqttTopic topic = client.getTopic(TOPIC);
            //setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
            options.setWill(topic, "close".getBytes(), 2, true);

            client.connect(options);
            //订阅消息
            int[] Qos = new int[subscribeTopics.length];
            for (int i=0;i<Qos.length;i++){
                Qos[i] = 1;
            }

            client.subscribe(subscribeTopics, Qos);
            for (String publicTopic : publicTopics) {
                topicList.put(publicTopic, client.getTopic(publicTopic));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String message) throws MqttException, UnsupportedEncodingException {

        MqttMessage msg = new MqttMessage();
        msg.setQos(2);
        msg.setRetained(true);
        msg.setPayload(ByteUtils.stringToByte(message));
        MqttTopic mqttTopic = topicList.get(topic);
        MqttDeliveryToken token = mqttTopic.publish(msg);
        token.waitForCompletion();
        System.out.println("message is published completely! "
                + token.isComplete());
    }

    public static void main(String[] args) {
    }
}
