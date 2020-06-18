package com.plass.computer.pplas_project.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.plass.computer.pplas_project.R;

import org.w3c.dom.Text;

/**
 * Created by alsrh on 2019-09-22.
 */

public class CustomDialog extends Dialog {
    private String title;
    private String message;
    private TextView dialogTitle;
    private TextView dialogMessage;
    private Button acceptButton;
    private Button denyButton;
    private ImageView dialogImage;
    private View.OnClickListener positiveListener;
    private View.OnClickListener negativeListener;

    public CustomDialog(@NonNull Context context, String title, String message, View.OnClickListener positiveListener, View.OnClickListener negativeListener) {
        super(context);

        this.title = title;
        this.message = message;
        this.positiveListener = positiveListener;
        this.negativeListener = negativeListener;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //다이얼로그 밖의 화면은 흐리게 만들어줌
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.custom_dialog);

        dialogTitle = (TextView)findViewById(R.id.dialogTitle);
        dialogMessage = (TextView)findViewById(R.id.dialogMessage);
        acceptButton = (Button)findViewById(R.id.acceptButton);
        denyButton = (Button)findViewById(R.id.denyButton);
        dialogImage = (ImageView)findViewById(R.id.dialogImage);

        dialogTitle.setText(title);
        dialogMessage.setText(message);
        acceptButton.setOnClickListener(positiveListener);
        denyButton.setOnClickListener(negativeListener);
        dialogImage.setImageResource(R.drawable.warning_mark);
    }
   /* @Override
    public void dismiss(){

    }*/
}
