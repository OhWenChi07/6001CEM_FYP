package com.example.grocery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.grocery.R;

public class CustomerSupportActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText subjectEt, contentEt, emailEt;
    private Button supportBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_support);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        subjectEt = findViewById(R.id.subjectEt);
        contentEt = findViewById(R.id.contentEt);
        emailEt = findViewById(R.id.emailEt);
        supportBtn = findViewById(R.id.supportBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go back previous activity
                onBackPressed();
            }
        });

        supportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject, content, to_email;
                subject = subjectEt.getText().toString();
                content = contentEt.getText().toString();
                to_email = emailEt.getText().toString();

                if (subject.isEmpty() || to_email.isEmpty()) {
                    Toast.makeText(CustomerSupportActivity.this, "Subject and email are required", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendEmail(subject, content, to_email);
                }
            }
        });
    }

    public void sendEmail(String subject, String content, String to_email) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{to_email});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Choose email client:"));
    }
}