package com.example.grocery.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.grocery.R;
import com.example.grocery.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {

    private CardView communityCv, customerCv, sellerCv, productsCv, ordersCv, logoutBtn;

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        communityCv = findViewById(R.id.communityCv);
        customerCv = findViewById(R.id.customerCv);
        sellerCv = findViewById(R.id.sellerCv);
        productsCv = findViewById(R.id.productsCv);
        ordersCv = findViewById(R.id.ordersCv);
        logoutBtn = findViewById(R.id.logoutBtn);

        mFirebaseAuth = FirebaseAuth.getInstance();

        communityCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, CommunityListActivity.class);
                startActivity(intent);
            }
        });

        customerCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, UserListActivity.class);
                startActivity(intent);
            }
        });

        sellerCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, SellerListActivity.class);
                startActivity(intent);
            }
        });

        productsCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, ProductsListActivity.class);
                startActivity(intent);
            }
        });

        ordersCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, OrderListActivity.class);
                startActivity(intent);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.dialog_logout_confirmation, null));
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFirebaseAuth.signOut();
                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(AdminActivity.this, "Logout Successfully.", Toast.LENGTH_SHORT).show();
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