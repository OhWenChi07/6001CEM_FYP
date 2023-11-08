package com.example.grocery.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText emailEt;
    private Button forgotBtn;

    private FirebaseAuth mFirebaseAuth;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        backBtn = findViewById(R.id.backBtn);
        emailEt = findViewById(R.id.emailEt);
        forgotBtn = findViewById(R.id.forgotBtn);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please Wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        forgotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPassword();
            }
        });
    }

    private String email;
    private void forgotPassword(){
        email = emailEt.getText().toString().trim();

        //1. Forgot Password Email Validation
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Empty Email Address. Please insert again!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email Address. Please Insert Again!", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog.setMessage("Sending instructions to reset password.");
        mProgressDialog.show();

        mFirebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //instruction sent
                        mProgressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset instructions sent to your email", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //instruction failed to send
                        mProgressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}