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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SellerListActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private RecyclerView sellersRv;
    private FirebaseAuth mFirebaseAuth;

    private ArrayList<AdminModelSeller> mSellerList;
    private AdminAdapterSeller mAdminAdapterSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_list);

        backBtn = findViewById(R.id.backBtn);
        sellersRv = findViewById(R.id.sellersRv);

        mFirebaseAuth = FirebaseAuth.getInstance();
        loadAllSellers();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadAllSellers() {
        mSellerList = new ArrayList<>();

        // Get all users with the "User" account type
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        Query query = ref.orderByChild("accountType").equalTo("Seller");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Before getting data, reset the list
                mSellerList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    AdminModelSeller adminModelSeller = ds.getValue(AdminModelSeller.class);
                    mSellerList.add(adminModelSeller);
                }
                // Setup adapter
                mAdminAdapterSeller = new AdminAdapterSeller(SellerListActivity.this, mSellerList);
                // Set the adapter
                sellersRv.setAdapter(mAdminAdapterSeller);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });
    }
}