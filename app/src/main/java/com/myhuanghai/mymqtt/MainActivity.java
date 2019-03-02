package com.myhuanghai.mymqtt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Button button;
    boolean state = true;//当前继电器开关状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.tv);
        button = findViewById(R.id.btn);

        final Client client = new Client();
        client.start(new String[]{"power"},new String[]{"temperature","state"},new PushCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("连接丢失");
                    }
                });
                cause.getCause().printStackTrace();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }

            @Override
            public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (topic.equals("temperature")){
                            textView.setText(""+ByteUtils.bytesToString(message.getPayload()));
                        }else if (topic.equals("state")){
                            state = ByteUtils.bytesToString(message.getPayload()).equals("state:1");
                            button.setText("电源："+(state?"on":"off"));
                        }
                    }
                });

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    client.publish("power",state?"0":"1");
                    state = !state;
                } catch (MqttException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
