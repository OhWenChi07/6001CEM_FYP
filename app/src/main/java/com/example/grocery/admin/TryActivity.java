package com.example.grocery.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.grocery.R;
import com.example.grocery.activities.MainUserActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class TryActivity extends AppCompatActivity {

    private EditText emailEt, passwordEt;

    private Button registerBtn;

    private FirebaseAuth mFirebaseAuth;
    private Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try);

        mFirebaseAuth = FirebaseAuth.getInstance();

        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //register user
                inputData();
            }
        });
    }

    private String email, password;
    private void inputData() {
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();

        createAccount();
    }

    private void createAccount() {

        //create account
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account created
                        saverFirebaseData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed create account
                        Toast.makeText(TryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saverFirebaseData() {
        String timestamp = ""+System.currentTimeMillis();

        if (image_uri==null){
            //setup data to save
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + mFirebaseAuth.getUid());
            hashMap.put("email", "" + email);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("accountType", "Admin");
            hashMap.put("online", "true");

            //save to database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(mFirebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //database updated
                            startActivity(new Intent(TryActivity.this, MainUserActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed update database
                            startActivity(new Intent(TryActivity.this, MainUserActivity.class));
                            finish();
                        }
                    });
        }
    }
}
