package com.example.grocery.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.grocery.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class SettingSellerActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private Button privacyBtn, changePasswordBtn, customerSupportBtn, logoutBtn;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_seller);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        privacyBtn = findViewById(R.id.privacyBtn);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        customerSupportBtn = findViewById(R.id.customerSupportBtn);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation2);
        bottomNavigationView.setSelectedItemId(R.id.bottom_support2);

        mFirebaseAuth = FirebaseAuth.getInstance();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go back previous activity
                onBackPressed();
            }
        });

        privacyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingSellerActivity.this, PrivacyPolicyActivity.class);
                startActivity(intent);
            }
        });

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingSellerActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        customerSupportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingSellerActivity.this, CustomerSupportActivity.class);
                startActivity(intent);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bottom_home:
                    startActivity(new Intent(getApplicationContext(), MainSellerActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.bottom_community2:
                    startActivity(new Intent(getApplicationContext(), CommunitySellerActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.bottom_profile:
                    startActivity(new Intent(getApplicationContext(), ProfileEditSellerActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.bottom_support2:
                    return true;
            }
            return false;
        });
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.dialog_logout_confirmation, null));
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFirebaseAuth.signOut();
                Intent intent = new Intent(SettingSellerActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(SettingSellerActivity.this, "Logout Successfully.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}