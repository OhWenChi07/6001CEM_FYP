package com.example.grocery.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderedItem;
import com.example.grocery.models.ModelOrderedItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn, editBtn;
    private TextView orderIdTv, dateTv, orderStatusTv, emailTv, phoneTv, totalItemsTv, amountTv, paymentMethodTv, addressTv;
    private RecyclerView itemsRv;
    String orderId, orderBy;
    
    private FirebaseAuth mFirebaseAuth;

    private ArrayList<ModelOrderedItem> mOrderedItemArrayList;
    private AdapterOrderedItem mAdapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        paymentMethodTv = findViewById(R.id.paymentMethodTv);
        addressTv = findViewById(R.id.addressTv);
        itemsRv = findViewById(R.id.itemsRv);

        //get data from intent
        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");

        mFirebaseAuth = FirebaseAuth.getInstance();
        loadBuyerInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //edit order status: In Progress, Completed, Cancelled
                editOrderStatusDialog();
            }
        });
    }

    private void editOrderStatusDialog() {
        //options to display in dialog
        String [] options = {"All", "In Progress", "Completed", "Cancelled"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //handle item clicks
                        String selectedOption = options[i];
                        editOrderStatus(selectedOption);
                    }
                })
                .show();
    }

    private void editOrderStatus(String selectedOption) {
        //setup data to put in firebase
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", "" + selectedOption);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mFirebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //status updated
                        Toast.makeText(OrderDetailsSellerActivity.this, "Order is now " + selectedOption, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating status, show reason
                        Toast.makeText(OrderDetailsSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadBuyerInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String email = "" + dataSnapshot.child("email").getValue();
                        String phone = "" + dataSnapshot.child("phoneNumber").getValue();

                        //set info
                        emailTv.setText(email);
                        phoneTv.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        //load detailed info of this order, based on order id
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mFirebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get order info
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
                        amountTv.setText("RM" + orderCost + "[Including Delivery Fee RM");
                        dateTv.setText(formatedDate);
                        addressTv.setText(address);
                        paymentMethodTv.setText(selectedPaymentMethod);

                        findAddress(latitude, longitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderedItems() {
        //load the products/items of order

        //init list
        mOrderedItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mFirebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
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
                        mAdapterOrderedItem = new AdapterOrderedItem(OrderDetailsSellerActivity.this, mOrderedItemArrayList);
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