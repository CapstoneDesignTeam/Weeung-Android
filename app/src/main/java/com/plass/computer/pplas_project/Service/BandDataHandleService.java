package com.plass.computer.pplas_project.Service;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.plass.computer.pplas_project.Login.LoginActivity;
import com.plass.computer.pplas_project.Patient.BandData;
import com.plass.computer.pplas_project.Patient.LoginPatientActivity;
import com.plass.computer.pplas_project.common.CustomDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BandDataHandleService extends Service {
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private List<String> pairedDevicesList;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private Handler bluetoothHandler;
    private ConnectedBluetoothThread connectedBluetoothThread;
    private BandData bandData;
    private GpsTracker gpsTracker;
    private String readMessage;
    private String latitude;
    private String longitude;
    private String bandMessage;
    private String pulse;
    private String temperature;
    private int check;
    private boolean pubServiceCheck;
    private CustomDialog customDialog;

    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BandDataHandleService() {

        bandData = BandData.getInstance();
        gpsTracker = GpsTracker.getInstance();
        readMessage = null;
        latitude = null;
        longitude = null;
        bandMessage = null;
        pulse = null;
        temperature = null;
        check = 0;
        pubServiceCheck = false;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        //////////////////////////////////블루투스 핸들러///////////////////////////////// 수신된 데이터를 읽어와 recieve에 써주는 메소드
        bluetoothHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == BT_MESSAGE_READ){

                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");

                        pulse = readMessage.split("%")[1];
                        temperature = readMessage.split("%")[0];
                        latitude = Double.toString(gpsTracker.getLatitude());
                        longitude = Double.toString(gpsTracker.getLongitude());

                        if(latitude!=null && longitude!=null){
                            check++;
                            ((LoginPatientActivity)(LoginPatientActivity.context)).setGpsConnectStatus("Connect");
                        } else {
                            check--;
                            ((LoginPatientActivity)(LoginPatientActivity.context)).setGpsConnectStatus("Not-Connect");
                        }
                        if(pulse!=null &&  temperature!=null){
                            check++;
                            ((LoginPatientActivity)LoginPatientActivity.context).setPulseView(pulse);
                            ((LoginPatientActivity)LoginPatientActivity.context).setTemperatureView(temperature);
                            ((LoginPatientActivity)(LoginPatientActivity.context)).setBandConnectStatus("Connect");
                        } else {
                            check--;
                            ((LoginPatientActivity)LoginPatientActivity.context).setPulseView("-");
                            ((LoginPatientActivity)LoginPatientActivity.context).setTemperatureView("-");
                            ((LoginPatientActivity)(LoginPatientActivity.context)).setBandConnectStatus("Check Device Connection");
                        }

                        /*if(check==2){   //밴드데이터와 gps수신 모두 성공적일 경우
                            if(pubServiceCheck==false){
                                Intent intent = new  Intent(LoginPatientActivity.context, PublishService.class);
                                startService(intent);
                                pubServiceCheck = true;
                            }
                        } else{         //하나라도 중간에 끊길경우 publish service종료
                            if(pubServiceCheck==true){
                                Intent intent = new  Intent(LoginPatientActivity.context, PublishService.class);
                                stopService(intent);
                            }
                        }*/

                        byte[] messageByte = (pulse+"%"+temperature+"%"+latitude+":"+longitude).getBytes("UTF-8");
                        bandMessage = new String(messageByte,"UTF-8");

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    Log.e("test","bandMessage : " + bandMessage);
                    Log.e("test","pulse : " + pulse);
                    Log.e("test", "temperature : " + temperature);
                    Log.e("test","latitude : " + latitude);
                    Log.e("test","longitude : " + longitude);
                    bandData.updateBandData(bandMessage);



                }
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        public BandDataHandleService getService(){
            return BandDataHandleService.this;
        }
    }

    //////////////////////////////////////////메소드 들////////////////////////////////////////////////
    public BluetoothAdapter getBluetoothAdapter(){
        return bluetoothAdapter;
    }
    public void bluetoothOn() {
        if(bluetoothAdapter == null) {                          //블루투스기능이 없는 기기인경우
            Toast.makeText(LoginPatientActivity.context, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        }
        else if(!bluetoothAdapter.isEnabled()){                 //블루투스가 꺼져 있는 경우
            showDialogForBlutoothServiceSetting(LoginPatientActivity.context);
        }
    }
    public void showDialogForBlutoothServiceSetting(Context context) {
        String title = "블루투스 서비스 비활성화";
        String message = "앱을 사용하기 위해서는\n블루투스 서비스가 필요합니다.\n"
                + "블루투스 설정을 수정하시겠습니까?";
        View.OnClickListener positiveListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((LoginPatientActivity)LoginPatientActivity.context).bluetoothActivityStart(bluetoothIntent);       //블루투스를 켠다.
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


       /* AlertDialog.Builder builder = new AlertDialog.Builder(LoginPatientActivity.context);
        builder.setTitle("블루투스 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 블루투스 서비스가 필요합니다.\n"
                + "블루투스 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {


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

    ////////////////////////////////////////////////////////페어링 디바이스 출력 메소드///////////////////////////////////////////
    public void listPairedDevices() {
        if (bluetoothAdapter.isEnabled()) {
            pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginPatientActivity.context);
                builder.setTitle("장치 선택");

                pairedDevicesList = new ArrayList<String>();
                for (BluetoothDevice device : pairedDevices) {
                    pairedDevicesList.add(device.getName());
                    //pairedDevicesList.add(device.getName() + "\n" + device.getAddress());
                }

                final CharSequence[] items = pairedDevicesList.toArray(new CharSequence[pairedDevicesList.size()]);
                pairedDevicesList.toArray(new CharSequence[pairedDevicesList.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(LoginPatientActivity.context, "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(LoginPatientActivity.context, "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    ////////////////////////////////////////////////디바이스 선택시 페어링 연결해주는 메소드//////////////////////////////////////
    public void connectSelectedDevice(String selectedDeviceName) {
        for(BluetoothDevice tempDevice : pairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            bluetoothSocket.connect();
            if(bluetoothSocket.isConnected()){
                ((LoginPatientActivity)LoginPatientActivity.context).setBandConnectStatus("Connect");
            }

            connectedBluetoothThread = new ConnectedBluetoothThread(bluetoothSocket);

            connectedBluetoothThread.setDaemon(true);
            connectedBluetoothThread.start();

            bluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(LoginPatientActivity.context, "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    //////////////////////////////////////데이터를 수신하는 쓰레드/////////////////////////////////////////
    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {       //생성자 : 소켓 초기화
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(LoginPatientActivity.context, "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[16];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();     //버퍼 허용가능량
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        bluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        /*   public void write(String str) {  //데이터 전송 메소드
               byte[] bytes = str.getBytes();
               try {
                   mmOutStream.write(bytes);
               } catch (IOException e) {
                   Toast.makeText(MainActivity.context, "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
               }
           }*/
        public void cancel() {          //블루투스소켓을 닫는 메소드
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(LoginPatientActivity.context, "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }

    }
}
