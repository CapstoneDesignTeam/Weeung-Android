package com.plass.computer.pplas_project.common;

/**
 * Created by alsrh on 2019-08-01.
 */

public class PatientData {

    private String mqttMessage;
    private String patientName;
    private String patientID;             //x
    private String patientPulse;
    private String patientTemperature;
    
    private String latitude;
    private String longitude;
    private String altitude;            //x
    private String patientLocation;

    private boolean check;

    public PatientData(String mqttMessage){

        this.mqttMessage = mqttMessage;

        String [] data = mqttMessage.split("%");        //아이디%이름%심박%체온%위치

        this.patientID = data[0];
        this.patientName =  data[1];
        this.patientPulse = data[2];
        this.patientTemperature = data[3];
        this.patientLocation = data[4];

        String [] location = patientLocation.split(":");
        this.latitude = location[0];
        this.longitude = location[1];
        this.altitude = location[2];

        this.check = false;
    }

    public String getMqttMessage(){
        return mqttMessage;
    }
    public String getPulse(){
        return patientPulse;
    }
    public String getTemperature(){
        return patientTemperature;
    }
    public String getName(){
        return patientName;
    }
    public String getID(){
        return patientID;
    }
    public void setCheck(){
        this.check = true;
    }
    public boolean getCheck(){
        return check;
    }
}
