package com.plass.computer.pplas_project.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.plass.computer.pplas_project.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by alsrh on 2019-08-03.
 */

public class CustomTask extends AsyncTask<String, Void, String> {

    private String sendMsg, recieveMsg;
    private Context context;

    ProgressDialog loadingDialog;

    public CustomTask(Context context){
        this.context = context;
        loadingDialog = new ProgressDialog(context, R.layout.loading_ani);

    }
    @Override
    protected void onPreExecute(){
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingDialog.setMessage("로딩중입니다..");

        loadingDialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
       // String jspUrl = "http://192.168.78.1:8080/MGJSP_Book/PPLAS/add.jsp";
        String jspUrl = "http://222.111.173.233/8080/PPLAS_MQTT-WebServer/add.jsp";
        //String jspUrl = "http://113.198.84.52:8080/PPLAS_MQTT-WebServer/add.jsp";
        HttpURLConnection conn=null;
        try {
            String tmp;
            URL url = new URL(jspUrl);

            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");

            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
            // type: 로그인, 회원가입 구별; authority: staff, admin, patient구별
            sendMsg = "accountID="+strings[0]+"&accountPassword="+strings[1]+"&accountName="+strings[2]+"&accountResidentID="+strings[3]+"&accountPhone="+strings[4]+
                    "&accountAuthority="+strings[5]+"&accountType="+strings[6];
            osw.write(sendMsg);
            osw.flush();

            if(conn.getResponseCode() == conn.HTTP_OK) {     //통신 성공
                InputStreamReader isr = new InputStreamReader(conn.getInputStream(),"UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                StringBuffer buffer = new StringBuffer();

                while((tmp=reader.readLine())!=null){
                    buffer.append(tmp);
                }
                recieveMsg = buffer.toString();
            }else{                                             //통신 에러
                Log.i("통신결과", conn.getResponseCode()+"에러");
            }

        } catch (MalformedURLException e) { e.printStackTrace(); } catch(IOException e){ e.printStackTrace(); }
        finally {
            if(conn!=null) conn.disconnect();
            Log.e("conn_disconnect","disconnect");
        }

        return recieveMsg;
    }

    @Override
    protected void onPostExecute(String result) {
        loadingDialog.dismiss();
        if(this.getStatus()== Status.RUNNING){
            this.cancel(true);
        }
        super.onPostExecute(result);
    }
}
