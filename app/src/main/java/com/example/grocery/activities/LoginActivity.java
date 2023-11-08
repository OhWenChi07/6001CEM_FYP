package com.example.grocery.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.grocery.R;
import com.example.grocery.admin.AdminActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    // UI views
    private EditText emailEt, passwordEt;
    private TextView forgotTv, noAccountTv;
    private Button loginBtn;

    private FirebaseAuth mFirebaseAuth;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login1);

        //init UI views
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        forgotTv = findViewById(R.id.forgotTv);
        noAccountTv = findViewById(R.id.noAccountTv);
        loginBtn = findViewById(R.id.loginBtn);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please Wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
            }
        });

        forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
    }

    private String email, password;

    private void loginUser(){
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();

        //1. Login Email Validation
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Empty Email Address. Please insert again!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern. Please Try Again!", Toast.LENGTH_SHORT).show();
            return;
        }
        //2. Login Password Validation
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Empty Password. Please Insert Again!", Toast.LENGTH_SHORT).show();
        }

        mProgressDialog.setMessage("Logging In.");
        mProgressDialog.show();

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //logged in successfully
                        makeMeOnline();
                        Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to login
                        mProgressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeMeOnline() {
        //after login, make user online
        mProgressDialog.setMessage("Checking User.");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "true");

        //update value to database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mFirebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update successfully
                        checkUserType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update
                        mProgressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserType(){
        //if user is seller, start admin main screen
        //if user is user, start customer main screen

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(mFirebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String accountType = "" + ds.child("accountType").getValue();
                            if (accountType.equals("Seller")){
                                mProgressDialog.dismiss();
                                //user is seller
                                startActivity(new Intent(LoginActivity.this, MainSellerActivity.class));
                                finish();
                            } else if (accountType.equals("Admin")) {
                                // User is an admin
                                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                            }
                            else {
                                mProgressDialog.dismiss();
                                //user is buyer(customer)
                                startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}