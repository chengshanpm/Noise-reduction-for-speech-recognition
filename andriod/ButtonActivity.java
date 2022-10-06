package com.example.noisereductionspeechrecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ButtonActivity extends AppCompatActivity {
    private Button mBtn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button);
        mBtn2=(Button)findViewById(R.id.btn_button2);
        mBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到Button界面
                Intent intent = new Intent(ButtonActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}