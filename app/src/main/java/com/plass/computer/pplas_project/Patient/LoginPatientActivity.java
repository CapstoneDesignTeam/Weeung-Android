package com.plass.computer.pplas_project.Patient;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.plass.computer.pplas_project.R;
import com.plass.computer.pplas_project.Service.BandDataHandleService;
import com.plass.computer.pplas_project.common.CustomDialog;
import com.plass.computer.pplas_project.common.CustomTask;
import com.plass.computer.pplas_project.common.MqttTask;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.ExecutionException;

public class LoginPatientActivity extends FragmentActivity {

    public static Context context;

    private TextView nameView;
    private TextView pulseView;
    private TextView temperatureView;
    private TextView bandConnectStatus;
    private TextView gpsConnectStatus;
    private String patientID;
    private String patientName;
    private MqttTask mqttTask;
    private MqttAndroidClient mqttAndroidClient;
    private BandDataHandleService bService;
    private boolean pBound = false;
    private CustomDialog customDialog;

    private BandData bandData;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;        //퍼미션요청 코드
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};    //요청할 퍼미션들
    final static int BT_REQUEST_ENABLE = 1;


    //////////////////////////////////////////////////////  onCreate()  ////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_patient);
        context = this;

        nameView = findViewById(R.id.patientName);
        pulseView = findViewById(R.id.pulse);
        temperatureView = findViewById(R.id.temperature);
        bandConnectStatus = findViewById(R.id.bandConnectStatus);
        gpsConnectStatus = findViewById(R.id.gpsConnectStatus);

        bandData = BandData.getInstance();

        pulseView.setText(bandData.getPulse());
        temperatureView.setText(bandData.getTemperature());

        bandConnectStatus.setOnClickListener(new View.OnClickListener(){            //블루투스 연결 버튼
            @Override
            public void onClick(View v) {
                if(!bService.getBluetoothAdapter().isEnabled()){        //블루투스가 꺼져있을 경우
                    bService.bluetoothOn();
                } else {
                    bService.listPairedDevices();
                }

            }
        });
        gpsConnectStatus.setOnClickListener(new View.OnClickListener(){             //gps연결 버튼
            @Override
            public void onClick(View v) {
                if (!checkLocationServicesStatus()) {       //안드로이드의 위치정보가 꺼져있는 경우
                    showDialogForLocationServiceSetting();
                }else {
                    checkRunTimePermission();                //퍼미션 체크 메소드
                }
            }
        });

        patientID = getIntent().getStringExtra("userID");       //환자 아이디

        mqttTask = new MqttTask(context,patientID,"patient");
        mqttAndroidClient = mqttTask.getMqttClient();

        try {
            String result = new CustomTask(context).execute(patientID,"","","","","","searchName").get();

            if(result.contains("findName=")){
                patientName = result.split("=")[1];             //환자 이름
            }
        } catch (InterruptedException e) {e.printStackTrace();} catch (ExecutionException e) {e.printStackTrace();}
    }
    /////////////////////////////////////////////////// onStart(), onStop() ////////////////////////////////////////////
    protected void onStart(){
        super.onStart();

        Intent bServiceIntent = new Intent(context, BandDataHandleService.class);   //BandDataHandleService 실행
        bindService(bServiceIntent, mConn,context.BIND_AUTO_CREATE);

    }
    protected void onStop(){
        super.onStop();
        if(pBound){
            unbindService(mConn);
            pBound = false;
        }
    }
    ////////////////////////////////////////////밴드데이터 핸들 서비스 커넥션 객체////////////////////////////////
    ServiceConnection mConn = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bService = ((BandDataHandleService.LocalBinder)service).getService();        //서비스에서 밴드로부터 데이터를 받아 객체화 시킨다.
            nameView.setText(patientName);
            pBound = true;
            if(bService.getBluetoothAdapter()==null){
                Toast.makeText(LoginPatientActivity.context, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
            } else if(!bService.getBluetoothAdapter().isEnabled()) {   //블루투스가 꺼져있는 경우
                bService.bluetoothOn();
            } else {    //블루투스가 켜져있는 경우
                bService.listPairedDevices();
            }

            if (!checkLocationServicesStatus()) {       //안드로이드의 위치정보가 꺼져있는 경우
                showDialogForLocationServiceSetting();
            }else {
                checkRunTimePermission();                //퍼미션 체크 메소드
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            pBound = false;
        }
    };
    //PublishService에서 호출하는 메소드
    public void publishToServer(String message) {
        try {
            mqttAndroidClient.publish("user/patient/"+patientID,message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


      /*
     * 여기서부터는 블루투스 서비스 관련 메소드들
     */

    /////////////////////////////////////onActivityResult: 블루투스를 켜준다////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    Toast.makeText(context, "블루투스 활성화", Toast.LENGTH_LONG).show();
                    setBandConnectStatus("Connect Device!");
                    bService.listPairedDevices();

                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    Toast.makeText(context, "블루투스 취소", Toast.LENGTH_LONG).show();
                    setBandConnectStatus("NotConnect");
                }
                break;

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                    checkRunTimePermission();
                    return;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /////////////////////////////////////////서비스에서 호출하는 메소드////////////////////////////////////////
    public void bluetoothActivityStart(Intent bluetoothIntent){
        startActivityForResult(bluetoothIntent, BT_REQUEST_ENABLE);
    }
    public void setBandConnectStatus(String text){
        bandConnectStatus.setText(text);
        if(text.equals("Connect")){
            bandConnectStatus.setTextColor(Color.WHITE);
        } else if(text.equals("Not-Connect")){
            bandConnectStatus.setTextColor(Color.DKGRAY);
        }
    }
    public void setGpsConnectStatus(String text){
        gpsConnectStatus.setText(text);
        if(text.equals("Connect")){
            gpsConnectStatus.setTextColor(Color.WHITE);
        } else if(text.equals("Not-Connect")){
            gpsConnectStatus.setTextColor(Color.DKGRAY);
        }
    }
    public void setPulseView(String text){
        pulseView.setText(text);
        if(text.equals("-")){
            pulseView.setTextColor(Color.DKGRAY);
        } else{
            pulseView.setTextColor(Color.WHITE);
        }

    }
    public void setTemperatureView(String text){
        temperatureView.setText(text);
        if(text.equals("-")){
            temperatureView.setTextColor(Color.DKGRAY);
        } else{
            temperatureView.setTextColor(Color.WHITE);
        }
    }







    /*
     * 여기서부터는 GPS관련 메소드.
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            boolean check_result = true;    // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            for (int result : grandResults) {    // 모든 퍼미션을 허용했는지 체크합니다.
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                //위치 값을 가져올 수 있음
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(LoginPatientActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();

                }else {
                    Toast.makeText(LoginPatientActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(LoginPatientActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(LoginPatientActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginPatientActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(LoginPatientActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(LoginPatientActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(LoginPatientActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        String title = "위치 서비스 비활성화";
        String message = "앱을 사용하기 위해서는\n위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하시겠습니까?";
        View.OnClickListener positiveListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
                gpsConnectStatus.setText("Connect");
                gpsConnectStatus.setTextColor(Color.WHITE);
                customDialog.dismiss();
            }
        };
        View.OnClickListener negativeListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        };
        customDialog = new CustomDialog(context, title,message,positiveListener,negativeListener);
        customDialog.setCancelable(false);
        customDialog.getWindow().setGravity(Gravity.CENTER);
        customDialog.show();

    /*    AlertDialog.Builder builder = new AlertDialog.Builder(LoginPatientActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
                gpsConnectStatus.setText("Connect");
                gpsConnectStatus.setTextColor(Color.WHITE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();*/

    }
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}
