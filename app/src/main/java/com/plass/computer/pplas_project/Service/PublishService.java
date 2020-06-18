package com.plass.computer.pplas_project.Service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.plass.computer.pplas_project.Patient.BandData;
import com.plass.computer.pplas_project.Patient.LoginPatientActivity;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

public class PublishService extends Service {
    private BandData bandData;

    public PublishService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate(){
        super.onCreate();
        bandData = BandData.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        GetDataThread th = new GetDataThread();
        th.setDaemon(true);
        th.start();
        return super.onStartCommand(intent, flags, startId);
    }
    public class GetDataThread extends Thread{
        public GetDataThread(){
        }
        @Override
        public void run() {

            while (true) {
                try {
                    Thread.sleep(3000);
                    ((LoginPatientActivity) (LoginPatientActivity.context)).publishToServer(bandData.getBandMessage());
                    Log.e("test","thread is alive!");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
