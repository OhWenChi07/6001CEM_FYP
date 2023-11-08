package com.example.grocery.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderedItem;
import com.example.grocery.models.ModelOrderedItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrderListDetailsActivity extends AppCompatActivity {

    private ImageButton backBtn, editBtn;
    private TextView orderIdTv, userIdTv, nameTv, emailTv, phoneTv, sellerIdTv, nameSellerTv, emailSellerTv, phoneSellerTv;
    private RecyclerView itemsRv;
    String orderId, orderBy, orderTo;
    private FirebaseAuth mFirebaseAuth;

    private ArrayList<ModelOrderedItem> mOrderedItemArrayList;
    private AdapterOrderedItem mAdapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list_details);

        backBtn = findViewById(R.id.backBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        userIdTv = findViewById(R.id.userIdTv);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        sellerIdTv = findViewById(R.id.sellerIdTv);
        nameSellerTv = findViewById(R.id.nameSellerTv);
        emailSellerTv = findViewById(R.id.emailSellerTv);
        phoneSellerTv = findViewById(R.id.phoneSellerTv);
        itemsRv = findViewById(R.id.itemsRv);

        //get data from intent
        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");
        orderTo = getIntent().getStringExtra("orderTo");

        mFirebaseAuth = FirebaseAuth.getInstance();
        // Load user details
        loadBuyerInfo();
        // Load seller details
        loadSellerInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadSellerInfo() {
        // Load seller information based on `orderTo`
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String uid = "" + dataSnapshot.child("uid").getValue();
                        String name = "" + dataSnapshot.child("name").getValue();
                        String email = "" + dataSnapshot.child("email").getValue();
                        String phone = "" + dataSnapshot.child("phoneNumber").getValue();

                        // Set seller info
                        sellerIdTv.setText(uid);
                        nameSellerTv.setText(name);
                        emailSellerTv.setText(email);
                        phoneSellerTv.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo() {
        // Load buyer information based on `orderBy`
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String uid = "" + dataSnapshot.child("uid").getValue();
                        String name = "" + dataSnapshot.child("name").getValue();
                        String email = "" + dataSnapshot.child("email").getValue();
                        String phone = "" + dataSnapshot.child("phoneNumber").getValue();

                        // Set buyer info
                        userIdTv.setText(uid);
                        nameTv.setText(name);
                        emailTv.setText(email);
                        phoneTv.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        // Load order details based on `orderId`
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get order info
                        String orderId = "" + dataSnapshot.child("orderId").getValue();

                        // Set order data
                        orderIdTv.setText(orderId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderedItems() {
        // Load ordered items based on `orderId`

        // Initialize the list
        mOrderedItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mOrderedItemArrayList.clear(); // Before loading items, clear the list
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                            // Add items to the list
                            mOrderedItemArrayList.add(modelOrderedItem);
                        }
                        // All items added to the list
                        // Setup the adapter
                        mAdapterOrderedItem = new AdapterOrderedItem(OrderListDetailsActivity.this, mOrderedItemArrayList);
                        // Set the adapter to your RecyclerView
                        itemsRv.setAdapter(mAdapterOrderedItem);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}