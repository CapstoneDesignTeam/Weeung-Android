package com.plass.computer.pplas_project.Service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.plass.computer.pplas_project.Patient.LoginPatientActivity;

/**
 * Created by alsrh on 2019-09-02.
 */

public class GpsTracker extends Service implements LocationListener {

    private final Context mContext;
    Location location;
    double latitude;
    double longitude;
    private static GpsTracker gpsTracker;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;     //업데이트 기준 변동거리
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;          //업데이트 기준 시간
    protected LocationManager locationManager;


    private GpsTracker(Context context) {
        this.mContext = context;
        getLocation();
    }
    public static GpsTracker getInstance(){
        if(gpsTracker==null){
            gpsTracker = new GpsTracker(LoginPatientActivity.context);
        }
        return gpsTracker;
    }


    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); //gps사용가능 유무
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);     //네트워크 사용가능 유무

            if (!isGPSEnabled && !isNetworkEnabled) {       //둘다 불가능 일 경우

            } else {

                int hasFineLocationPermission = ContextCompat.checkSelfPermission(mContext,         //퍼미션 확인
                        Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION);


                if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                        hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) { //둘다 퍼미션 인정

                    ;
                } else                  //하나라도 퍼미션을 못받을경우 null반환
                    return null;


                if (isNetworkEnabled) {         //네트워크 사용가능일 경우


                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);      //마지막 위치 가져오기
                         if (location != null)
                        {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }


                if (isGPSEnabled)           //gps사용가능 일 경우
                {
                    if (location == null)       //네트워크로 해서 실패한 경우
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        if (locationManager != null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null)
                            {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.d("@@@", ""+e.toString());
        }

        return location;
    }

    public double getLatitude()
    {
        if(location != null)
        {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    public double getLongitude()
    {
        if(location != null)
        {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Toast.makeText(mContext,"onLocationChanged()호출\n"+
            "latitude : "+latitude +
            "\nlongitude : " + longitude,Toast.LENGTH_LONG).show();
        ((LoginPatientActivity)LoginPatientActivity.context).setGpsConnectStatus("Connect");
    }

    @Override
    public void onProviderDisabled(String provider)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }


    public void stopUsingGPS()
    {
        if(locationManager != null)
        {
            locationManager.removeUpdates(GpsTracker.this);
        }
    }


}
