package com.example.grocery.admin;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrderListActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private RecyclerView ordersRv;
    private FirebaseAuth mFirebaseAuth;

    private ArrayList<AdminModelOrders> mOrdersList;
    private AdminAdapterOrders mAdminAdapterOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        backBtn = findViewById(R.id.backBtn);
        ordersRv = findViewById(R.id.ordersRv);

        mFirebaseAuth = FirebaseAuth.getInstance();
        loadAllOrders();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadAllOrders() {
        mOrdersList = new ArrayList<>();

        // Get all users with the "User" account type
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@android.support.annotation.NonNull DataSnapshot dataSnapshot) {
                mOrdersList.clear();

                for (DataSnapshot sellerSnapshot : dataSnapshot.getChildren()) {
                    // Inside this loop, 'sellerSnapshot' represents each seller

                    // Check if the seller has a "Orders" node
                    if (sellerSnapshot.hasChild("Orders")) {
                        DataSnapshot ordersSnapshot = sellerSnapshot.child("Orders");

                        for (DataSnapshot productSnapshot : ordersSnapshot.getChildren()) {
                            // Inside this loop, 'productSnapshot' represents each product
                            AdminModelOrders adminModelOrders = productSnapshot.getValue(AdminModelOrders.class);
                            mOrdersList.add(adminModelOrders);
                        }
                    }
                }

                // Setup adapter
                mAdminAdapterOrders = new AdminAdapterOrders(OrderListActivity.this, mOrdersList);
                // Set the adapter
                ordersRv.setAdapter(mAdminAdapterOrders);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });
    }

}