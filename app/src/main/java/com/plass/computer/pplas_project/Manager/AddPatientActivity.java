package com.plass.computer.pplas_project.Manager;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.plass.computer.pplas_project.R;
import com.plass.computer.pplas_project.common.CustomTask;
import com.plass.computer.pplas_project.common.Message;

import java.util.concurrent.ExecutionException;

public class AddPatientActivity extends FragmentActivity {

    private EditText joinName;
    private EditText residentID;
    private EditText joinID;
    private EditText joinPW;
    private EditText joinPWCheck;
    private EditText phoneNumber;
    private Button add;
    private Button back;

    Context context;
    Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);
        setTitle("환자 추가");
        context = this;

        joinName = findViewById(R.id.joinName);
        residentID = findViewById(R.id.residentID);
        joinID = findViewById(R.id.joinID);
        joinPW = findViewById(R.id.joinPW);
        joinPWCheck = findViewById(R.id.joinPWCheck);
        phoneNumber = findViewById(R.id.phoneNumber);
        add = findViewById(R.id.add);
        back = findViewById(R.id.back);

        message = new Message();

        add.setOnClickListener(onClickListener);
        back.setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            switch(v.getId()){
                case R.id.add:
                    String id = joinID.getText().toString();
                    String pw = joinPW.getText().toString();
                    String pwCh = joinPWCheck.getText().toString();
                    String name = joinName.getText().toString();
                    String resident_id = residentID.getText().toString();
                    String phone = phoneNumber.getText().toString();
                    if(id.equals("") || pw.equals("") || pwCh.equals("") || name.equals("") || resident_id.equals("")
                            || phone.equals("")){
                        message.information(context,"경고","모든 입력란을 채워주세요");
                    } else {
                        if(pw.equals(pwCh)){
                            try {
                                String result = new CustomTask(context).execute(id,pw,name,resident_id,phone,"patient","join").get();
                                if (result.equals("joinOK")) {            //아이디가 존재하지 않을 경우
                                    message.information(context, "알림", "회원가입이 정상적으로 이루어졌습니다.");
                                    joinID.setText("");
                                    joinPW.setText("");
                                    joinPWCheck.setText("");
                                }
                                else if(result.equals("exist")){       //이미 존재하는 계정일 경우
                                    message.information(context, "알림", "이미 존재하는 계정입니다.");
                                    joinID.setText("");
                                    joinPW.setText("");
                                    joinPWCheck.setText("");
                                }
                                else if(result.equals("imposible")){
                                    message.information(context, "알림", "계정은 1인당 한 개까지 만들 수 있습니다");
                                    joinID.setText("");
                                    joinPW.setText("");
                                    joinPWCheck.setText("");
                                }
                            } catch (InterruptedException e) {e.printStackTrace(); } catch (ExecutionException e) {e.printStackTrace();}
                        } else {
                            message.information(context,"알림","비밀번호가 일치하지 않습니다.");
                            joinPW.setText("");
                            joinPWCheck.setText("");
                        }
                    }
                    break;
                case R.id.back: finish();break;
            }
        }
    };
}
