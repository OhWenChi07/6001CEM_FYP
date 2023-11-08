package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.example.grocery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // make full screen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        mFirebaseAuth = FirebaseAuth.getInstance();

        // start login activity after 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                if (user==null) {
                    //user not logged in start login activity
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
                else {
                    //user is logged in, check user type
                    checkUserType();
                }
            }
        }, 1000);
    }

    private void checkUserType(){
        //if user is seller, start admin main screen
        //if user is user, start customer main screen

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mFirebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String accountType = "" + dataSnapshot.child("accountType").getValue();
                        if (accountType.equals("Seller")){
                            //user is seller(admin)
                            startActivity(new Intent(SplashActivity.this, MainSellerActivity.class));
                            finish();
                        }
                        else {
                            //user is buyer(customer)
                            startActivity(new Intent(SplashActivity.this, MainUserActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}