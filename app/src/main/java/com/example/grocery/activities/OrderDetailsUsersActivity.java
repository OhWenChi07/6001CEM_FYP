package com.example.grocery.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUsersActivity extends AppCompatActivity {

    private String orderTo, orderId;

    //ui views
    private ImageButton backBtn, writeReviewBtn;
    private TextView orderIdTv, dateTv, orderStatusTv, shopNameTv, totalItemsTv, amountTv, addressTv, paymentMethodTv;
    private RecyclerView itemsRv;

    private FirebaseAuth mFirebaseAuth;

    private ArrayList<ModelOrderedItem> mOrderedItemArrayList;
    private AdapterOrderedItem mAdapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_users);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        writeReviewBtn = findViewById(R.id.writeReviewBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        paymentMethodTv = findViewById(R.id.paymentMethodTv);
        itemsRv = findViewById(R.id.itemsRv);

        //get these values through intent on AdapterOrderUser
        Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo"); //orderTo contains uid of the shop where we placed order
        orderId = intent.getStringExtra("orderId");

        mFirebaseAuth = FirebaseAuth.getInstance();
        loadShopInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(OrderDetailsUsersActivity.this, WriteReviewActivity.class);
                intent1.putExtra("shopUid", orderTo); //to write review to a shop, must have uid of shop
                startActivity(intent1);
            }
        });

    }

    private void loadOrderedItems() {
        //init list
        mOrderedItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mOrderedItemArrayList.clear(); //before loading items clear list
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                            //add to list
                            mOrderedItemArrayList.add(modelOrderedItem);
                        }
                        //all items added to list
                        //setup adapter
                        mAdapterOrderedItem = new AdapterOrderedItem(OrderDetailsUsersActivity.this, mOrderedItemArrayList);
                        //set adapter
                        itemsRv.setAdapter(mAdapterOrderedItem);

                        //set items count
                        totalItemsTv.setText("" + dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        //load order details
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get data
                        String orderBy = "" + dataSnapshot.child("orderBy").getValue();
                        String orderCost = "" + dataSnapshot.child("orderCost").getValue();
                        String orderId = "" + dataSnapshot.child("orderId").getValue();
                        String orderStatus = "" + dataSnapshot.child("orderStatus").getValue();
                        String orderTime = "" + dataSnapshot.child("orderTime").getValue();
                        String orderTo = "" + dataSnapshot.child("orderTo").getValue();
                        String deliveryFee = "" + dataSnapshot.child("deliveryFee").getValue();
                        String latitude = "" + dataSnapshot.child("latitude").getValue();
                        String longitude = "" + dataSnapshot.child("longitude").getValue();
                        String address = "" + dataSnapshot.child("address").getValue();
                        String selectedPaymentMethod = "" + dataSnapshot.child("selectedPaymentMethod").getValue();

                        //convert timestamp to proper format
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatedDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString(); //exp. 10/10/2023 9:00 AM

                        //order status
                        if (orderStatus.equals("In Progress")) {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        }
                        else if (orderStatus.equals("Completed")) {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.green));
                        }
                        if (orderStatus.equals("Cancelled")) {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.red));
                        }

                        //set data
                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("RM" + orderCost + "[Including Delivery Fee]");
                        dateTv.setText(formatedDate);
                        paymentMethodTv.setText(selectedPaymentMethod);
                        addressTv.setText(address);

                        findAddress(latitude, longitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopInfo() {
        //get shop info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String shopName = "" + dataSnapshot.child("shopName").getValue();
                        shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        //find address, country, state, city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);

            String address = addresses.get(0).getAddressLine(0); //complete address
            addressTv.setText(address);
        }
        catch (Exception e) {

        }
    }
}