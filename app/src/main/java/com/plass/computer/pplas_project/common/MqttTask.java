package com.plass.computer.pplas_project.common;

import android.content.Context;
import android.util.Log;

import com.plass.computer.pplas_project.Login.LoginActivity;
import com.plass.computer.pplas_project.Patient.LoginPatientActivity;
import com.plass.computer.pplas_project.common.PatientData;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by alsrh on 2019-08-14.
 */

public class MqttTask {
    private MqttAndroidClient mqttAndroidClient;

    private String userID;
    private String authority;
    private Context context;

    public MqttTask(Context context,String userID, String authority){
        this.userID = userID;
        this.authority = authority;
        this.context = context;

        // mqttAndroidClient = new MqttAndroidClient(context, "tcp://" + "192.168.78.1" + ":1883", MqttClient.generateClientId());
        mqttAndroidClient = new MqttAndroidClient(context, "tcp://" + "116.126.97.126" + ":1883", MqttClient.generateClientId());
        //mqttAndroidClient = new MqttAndroidClient(context, "tcp://" + "113.198.84.52" + ":1883", MqttClient.generateClientId());

        mqttConnect();
        mqttSetCallback(mqttAndroidClient);
    }

    public MqttAndroidClient getMqttClient(){       //mqtt클라이언트 리턴
        return mqttAndroidClient;
    }

    public void mqttConnect(){          //mqtt커넥트
        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());    //mqtttoken 이라는것을 만들어 connect option을 달아줌

            token.setActionCallback(new IMqttActionListener() {
                @Override

                public void onSuccess(IMqttToken asyncActionToken) {

                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());    //연결에 성공한경우

                    Log.e("Connect_success", "Success");

                    try {
                        if(authority.equals("patient")){        //환자로 로그인 시
                            mqttAndroidClient.subscribe("user/"+authority+"/"+userID, 0);   //  user/patient/id 로 subscribe
                        } else if(authority.equals("ems")){
                            String []topic = {"user/"+authority, "user/"+authority+"/"+userID};
                            int [] qos = {0,0};
                           // mqttAndroidClient.subscribe("user/"+authority, 0);                // user.ems 로 subscribe
                            mqttAndroidClient.subscribe(topic,qos);
                        }



                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {   //연결에 실패한경우
                    Log.e("connect_fail", "Failure " + exception.toString());
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void mqttSetCallback(MqttAndroidClient mqttAndroidClient){

        mqttAndroidClient.setCallback(new MqttCallback() {  //클라이언트의 콜백을 처리하는부분
            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override

            public void messageArrived(String topic, MqttMessage message) throws Exception {    //모든 메시지가 올때 Callback method
                String mqttMessage = new String(message.getPayload());

                if (topic.equals("user/ems")) {     //구조대원으로서 환자의 메시지를 받을 때
                    new Message().information(context, "응급상황", "응급환자 발생");
                    //아이디%이름%맥%체온%위치

                    PatientData patientData = new PatientData(mqttMessage);

                } else if (topic.equals("user/ems/" + userID)) {

                    Message.information(context, "알림", "특정 ems호출 성공");

                } else if (topic.equals("user/patient/" + userID)) {     //환자로서 구조대원의 call Message를 받거나 로딩화면을 출력할 때
                    //구조대의 정보를 화면에 띄운다.

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

    }

    private DisconnectedBufferOptions getDisconnectedBufferOptions() {

        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();

        disconnectedBufferOptions.setBufferEnabled(true);

        disconnectedBufferOptions.setBufferSize(100);

        disconnectedBufferOptions.setPersistBuffer(true);

        disconnectedBufferOptions.setDeleteOldestMessages(false);

        return disconnectedBufferOptions;

    }


    private MqttConnectOptions getMqttConnectionOption() {

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

        mqttConnectOptions.setCleanSession(false);

        mqttConnectOptions.setAutomaticReconnect(true);

        mqttConnectOptions.setWill("aaa", "I am going offline".getBytes(), 1, true);

        return mqttConnectOptions;

    }

}
