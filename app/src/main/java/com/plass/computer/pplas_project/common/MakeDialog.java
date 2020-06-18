package com.plass.computer.pplas_project.common;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Created by alsrh on 2019-05-28.
 */
////////////////////////////AlertDialog.Builder를 리턴하는 static메소드를 가진 클래스/////////////////////////////
public class MakeDialog {
    static AlertDialog.Builder builder;

    static public AlertDialog.Builder makeDialog(Context context,String title,String message){
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        if(message!="")
            builder.setMessage(message);
        return builder;
    }
  /*  static public void setTitle(String title){
        builder.setTitle("title");
    }
    static public void setMessage(String message){
        builder.setMessage(message);
    }*/
}
