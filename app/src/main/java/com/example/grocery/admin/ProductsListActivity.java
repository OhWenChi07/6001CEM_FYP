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

public class ProductsListActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private RecyclerView productsRv;
    private FirebaseAuth mFirebaseAuth;

    private ArrayList<AdminModelProducts> mProductsList;
    private AdminAdapterProducts mAdminAdapterProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_list);

        backBtn = findViewById(R.id.backBtn);
        productsRv = findViewById(R.id.productsRv);

        mFirebaseAuth = FirebaseAuth.getInstance();
        loadAllProducts();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadAllProducts() {
        mProductsList = new ArrayList<>();

        // Get all users with the "User" account type
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProductsList.clear();

                for (DataSnapshot sellerSnapshot : dataSnapshot.getChildren()) {
                    // Inside this loop, 'sellerSnapshot' represents each seller

                    // Check if the seller has a "Products" node
                    if (sellerSnapshot.hasChild("Products")) {
                        DataSnapshot productsSnapshot = sellerSnapshot.child("Products");

                        for (DataSnapshot productSnapshot : productsSnapshot.getChildren()) {
                            // Inside this loop, 'productSnapshot' represents each product
                            AdminModelProducts adminModelProducts = productSnapshot.getValue(AdminModelProducts.class);
                            mProductsList.add(adminModelProducts);
                        }
                    }
                }

                // Setup adapter
                mAdminAdapterProducts = new AdminAdapterProducts(ProductsListActivity.this, mProductsList);
                // Set the adapter
                productsRv.setAdapter(mAdminAdapterProducts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });
    }


}