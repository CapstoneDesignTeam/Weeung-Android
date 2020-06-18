package com.plass.computer.pplas_project.Patient;

import android.util.Log;

/**
 * Created by alsrh on 2019-08-21.
 */

public class BandData {
    private String pulse;
    private String temperature;
    private String location;
    private String latitude;
    private String longitude;
    private String bandMessage;
    private static BandData bandData = null;

    private BandData(String bandMessage){
        this.bandMessage = bandMessage;     //맥%체온%위도:경도:고도
        Log.e("test","BandData생성자");
        String [] data = bandMessage.split("%");

        this.pulse = data[0];
        this.temperature = data[1];
        this.location = data[2];
        this.latitude = location.split(":")[0];
        this.longitude = location.split(":")[1];
    }
    public static BandData getInstance(String bandMessage){
        if(bandData==null){
            bandData = new BandData(bandMessage);
        }
        return bandData;
    }
    public static BandData getInstance(){
        if(bandData==null){
            String message = "-%-%-:-";
            bandData = new BandData(message);
        }
        return bandData;
    }
    public void updateBandData(String bandMessage){
        this.bandMessage = bandMessage;     //맥%체온%위도:경도:고도

        String [] data = bandMessage.split("%");

        this.pulse = data[0];
        this.temperature = data[1];
        this.location = data[2];
        this.latitude = location.split(":")[0];
        this.longitude = location.split(":")[1];

    }
    public String getBandMessage(){
        return bandMessage;
    }
    public String getPulse(){
        return pulse;
    }
    public String getTemperature(){
        return temperature;
    }
    public String getLocation(){
        return location;
    }

}
