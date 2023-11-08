package com.example.grocery.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private ImageButton backBtn;

    private EditText passwordEt, newPasswordEt;

    private Button updateBtn;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        passwordEt = findViewById(R.id.passwordEt);
        newPasswordEt = findViewById(R.id.newPasswordEt);
        updateBtn = findViewById(R.id.updateBtn);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUser = mFirebaseAuth.getCurrentUser();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go back previous activity
                onBackPressed();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = passwordEt.getText().toString().trim();
                String newPassword = newPasswordEt.getText().toString().trim();

                if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(ChangePasswordActivity.this, "Please enter both the current and new passwords.", Toast.LENGTH_SHORT).show();
                } else if (newPassword.length() >= 8 && containsUppercase(newPassword)) {
                    changePassword(oldPassword, newPassword);
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "New Password must be at least 8 characters and one uppercase letter.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean containsUppercase(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    public void changePassword(String currentPassword, String newPassword) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);

        // Re-authenticate user
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Re-authentication successful, now change the password
                            currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Password changed successfully
                                                Toast.makeText(ChangePasswordActivity.this, "Password changed successfully.", Toast.LENGTH_SHORT).show();
                                                finish(); // Finish the activity or navigate to another screen.
                                            } else {
                                                // Password change failed
                                                Toast.makeText(ChangePasswordActivity.this, "Failed to change password. Please try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // Re-authentication failed
                            Toast.makeText(ChangePasswordActivity.this, "Re-authentication failed. Please check your current password.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}