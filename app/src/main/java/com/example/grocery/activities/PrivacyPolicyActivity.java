package com.example.grocery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.grocery.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private TextView contentTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        contentTv = findViewById(R.id.contentTv);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go back previous activity
                onBackPressed();
            }
        });
    }
}