package com.example.grocery.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.Constants;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterCartItem;
import com.example.grocery.adapters.AdapterProductUser;
import com.example.grocery.models.ModelCartItem;
import com.example.grocery.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ShopDetailsActivity extends AppCompatActivity {

    //declare ui views
    private ImageView shopIv;
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv, deliveryFeeTv, addressTv, filteredProductsTv;
    private ImageButton callBtn, cartBtn, backBtn, filterProductBtn, reviewsBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private RatingBar ratingBar;

    private String shopUid;
    private String myLatitude, myLongitude, myPhoneNumber;
    private String shopName, shopEmail, shopPhone, shopAddress, shopLatitude, shopLongitude;
    public String deliveryFee;

    private FirebaseAuth mFirebaseAuth;
    private ProgressDialog mProgressDialog;

    private ArrayList<ModelProduct> mProductsList;
    private AdapterProductUser mAdapterProductUser;
    private Context mContext;

    //cart
    private ArrayList<ModelCartItem> mCartItemList;
    private AdapterCartItem mAdapterCartItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        mContext = this;

        //init ui views
        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        callBtn = findViewById(R.id.callBtn);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        productsRv = findViewById(R.id.productsRv);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        ratingBar = findViewById(R.id.ratingBar);

        //init progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please Wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        //get uid of the shop from intent
        shopUid = getIntent().getStringExtra("shopUid");
        mFirebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews(); //avg rating, set on rating bar

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go previous activity
                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show cart dialog
                showCartDialog();
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialPhone();
            }
        });

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    mAdapterProductUser.getFilter().filter(charSequence);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Please choose the Product Category")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //get selected item
                                String selected = Constants.productCategories1[i];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All")) {
                                    //load all
                                    loadShopProducts();
                                }
                                else {
                                    //load filtered products
                                    mAdapterProductUser.getFilter().filter(selected);
                                }
                            }
                        })
                        .show();
            }
        });

        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pass shop uid to show its reviews
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ratingSum = 0;
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue()); //exp: 4.5
                            ratingSum = ratingSum + rating; //for avg rating, add(addition of) all ratings, and divide it by number of reviews
                        }
                        long numberOfReviews = dataSnapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public double allTotalPrice = 0.00;
    //need to access these views in adapter so making public
    public TextView sTotalTv, dFeeTv, allTotalPriceTv;
    private void showCartDialog() {
        // Initialize Firebase Database reference
        String uid = mFirebaseAuth.getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Cart");

        // Initialize list to store cart items
        mCartItemList = new ArrayList<>();

        // Set up Firebase Realtime Database listener to retrieve cart items
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the previous list of cart items
                mCartItemList.clear();

                // Initialize total price
                allTotalPrice = 0.00;

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    // Get cart item data from Firebase
                    String id = ds.child("productId").getValue(String.class);
                    String name = ds.child("title").getValue(String.class);
                    String price = ds.child("priceEach").getValue(String.class);
                    String cost = ds.child("price").getValue(String.class);
                    String quantity = ds.child("quantity").getValue(String.class);

                    // Calculate total price
                    allTotalPrice += Double.parseDouble(cost.replace("RM", ""));

                    // Create a ModelCartItem object
                    ModelCartItem modelCartItem = new ModelCartItem(
                            id,
                            id, // Can set it to the product ID as a placeholder
                            name,
                            price,
                            cost,
                            quantity
                    );

                    // Add the cart item to the list
                    mCartItemList.add(modelCartItem);
                }

                // Update the cart dialog with the new data
                updateCartDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                Toast.makeText(mContext, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Show the cart dialog
        updateCartDialog();
    }

    private Spinner paymentMethodSpinner; // Declare the Spinner
    private void updateCartDialog() {
        // Inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);

        // Initialize views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);
        paymentMethodSpinner = view.findViewById(R.id.paymentMethodSpinner);

        // Set shop name
        shopNameTv.setText(shopName);

        // Set up adapter
        mAdapterCartItem = new AdapterCartItem(mContext, mCartItemList);
        cartItemsRv.setAdapter(mAdapterCartItem);

        // Set delivery fee and total price
        dFeeTv.setText("RM" + deliveryFee);
        sTotalTv.setText("RM" + String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("RM" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("RM", ""))));

        // Initialize the Spinner with payment methods (you can customize this list)
        String[] paymentMethods = {"Credit Card", "Cash on Delivery"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paymentMethods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodSpinner.setAdapter(adapter);

        // Set a listener for the Spinner
        paymentMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Retrieve the selected payment method
                String selectedPaymentMethod = paymentMethods[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // Show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });

        //place order
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedPaymentMethod = paymentMethodSpinner.getSelectedItem().toString();
                // Check if a payment method is selected, and use it for order submission.
                if (selectedPaymentMethod.isEmpty()) {
                    Toast.makeText(ShopDetailsActivity.this, "Please select the payment method", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mCartItemList.size() == 0) {
                    //cart list is empty
                    Toast.makeText(ShopDetailsActivity.this, "No item in cart.", Toast.LENGTH_SHORT).show();
                    return; //do not proceed further
                }

                submitOrder(selectedPaymentMethod);
            }
        });
    }

    private void submitOrder(String selectedPaymentMethod) {
        //show progress dialog
        mProgressDialog.setTitle("Placing Order. Please Wait.");
        mProgressDialog.show();

        //for order id and order time
        String timestamp = "" + System.currentTimeMillis();

        // Get the total cost of the order
        String cost = allTotalPriceTv.getText().toString().trim().replace("RM", ""); //remove RM if contains

        //add latitude, longitude of user to each order | delete previous orders from firebase or add manually to them

        //setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", "" + timestamp);
        hashMap.put("orderTime", "" + timestamp);
        hashMap.put("orderStatus", "" + "In Progress"); //In Progress/ Completed/ Cancelled
        hashMap.put("orderCost", "" + cost);
        hashMap.put("orderBy", "" + mFirebaseAuth.getUid());
        hashMap.put("orderTo", "" + shopUid);
        hashMap.put("latitude", "" + myLatitude);
        hashMap.put("longitude", "" + myLongitude);
        hashMap.put("selectedPaymentMethod", selectedPaymentMethod);

        //add to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order info added now add order items
                        for (int i=0; i < mCartItemList.size(); i++) {
                            String pId = mCartItemList.get(i).getpId();
                            String id = mCartItemList.get(i).getId();
                            String name = mCartItemList.get(i).getName();
                            String cost = mCartItemList.get(i).getCost();
                            String price = mCartItemList.get(i).getPrice();
                            String quantity = mCartItemList.get(i).getQuantity();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }
                        mProgressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed Successfully.", Toast.LENGTH_SHORT).show();

                        //after placing order open order details page
                        Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                        intent.putExtra("orderTo", shopUid);
                        intent.putExtra("orderId", timestamp);
                        intent.putExtra("paymentMethod", selectedPaymentMethod); // Pass the selected payment method
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        mProgressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    } //create row_order_user.xml for recyclerview

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(mFirebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            //get user data
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            myPhoneNumber = ""+ds.child("phoneNumber").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();
                            myLatitude = ""+ds.child("latitude").getValue();
                            myLongitude = ""+ds.child("longitude").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get shop data
                String name = "" + dataSnapshot.child("name").getValue();
                shopName = "" + dataSnapshot.child("shopName").getValue();
                shopEmail = "" + dataSnapshot.child("email").getValue();
                shopPhone = "" + dataSnapshot.child("phoneNumber").getValue();
                shopAddress = "" + dataSnapshot.child("address").getValue();
                shopLatitude = "" + dataSnapshot.child("latitude").getValue();
                shopLongitude = "" + dataSnapshot.child("longitude").getValue();
                deliveryFee = "" + dataSnapshot.child("deliveryFee").getValue();
                String shopOpen = "" + dataSnapshot.child("shopOpen").getValue();

                //set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery Fee: RM" + deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if (shopOpen.equals("true")) {
                    openCloseTv.setText("Open");
                }
                else {
                    openCloseTv.setText("Closed");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopProducts() {
        //init list
        mProductsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding items
                        mProductsList.clear();

                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            mProductsList.add(modelProduct);
                        }
                        //setup adapter
                        mAdapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, mProductsList);
                        //set adapter
                        productsRv.setAdapter(mAdapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void updateTotalPrice() {
        double totalPrice = 0.0;

        // Calculate the total price by iterating through the cart items
        for (ModelCartItem cartItem : mCartItemList) {
            double itemCost = Double.parseDouble(cartItem.getCost().replace("RM", ""));
            totalPrice += itemCost;
        }

        // Update the UI with the new total price
        allTotalPrice = totalPrice;
        sTotalTv.setText("RM" + String.format("%.2f", totalPrice));

        // Calculate the final total price including delivery fee
        double finalTotalPrice = totalPrice + Double.parseDouble(deliveryFee.replace("RM", ""));
        allTotalPriceTv.setText("RM" + String.format("%.2f", finalTotalPrice));
    }

}