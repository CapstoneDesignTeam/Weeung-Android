package com.plass.computer.pplas_project.Manager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.plass.computer.pplas_project.R;

public class ManagerSettingActivity extends FragmentActivity {
    private Context context;

    private Button addPatientButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_setting);
        context = this;

        addPatientButton = findViewById(R.id.addPatientButton);
        addPatientButton.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Intent intent = null;
            switch(v.getId()){
                case R.id.addPatientButton:
                    intent = new Intent(context, AddPatientActivity.class);
                    startActivity(intent); break;
            }

        }
    };

}
