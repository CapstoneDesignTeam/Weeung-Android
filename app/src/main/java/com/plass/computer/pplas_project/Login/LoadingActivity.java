package com.plass.computer.pplas_project.Login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import static java.lang.Thread.sleep;

/**
 * Created by alsrh on 2019-09-14.
 */

public class LoadingActivity extends Activity {
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);

        try {
            sleep(2500);
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




    }
}
