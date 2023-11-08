package com.example.grocery.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.Constants;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderShop;
import com.example.grocery.adapters.AdapterProductSeller;
import com.example.grocery.models.ModelOrderShop;
import com.example.grocery.models.ModelProduct;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainSellerActivity extends AppCompatActivity {

    private TextView nameTv, shopNameTv, emailTv, tabProductsTv, tabOrdersTv, filteredProductsTv, filteredOrdersTv;
    private EditText searchProductEt;
    private ImageButton logoutBtn, filterProductBtn, filterOrderBtn, reviewsBtn;
    private RelativeLayout productsRl, ordersRl;
    private RecyclerView productsRv, ordersRv;

    private FirebaseAuth mFirebaseAuth;
    private ProgressDialog mProgressDialog;

    FloatingActionButton fab;

    private ArrayList<ModelProduct> mProductList;
    private AdapterProductSeller mAdapterProductSeller;

    private ArrayList<ModelOrderShop> mOrderShopArrayList;
    private AdapterOrderShop mAdapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        FloatingActionButton fab;

        fab = findViewById(R.id.fab);

        nameTv = findViewById(R.id.nameTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        emailTv = findViewById(R.id.emailTv);
        tabProductsTv = findViewById(R.id.tabProductsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        searchProductEt = findViewById(R.id.searchProductEt);
        logoutBtn = findViewById(R.id.logoutBtn);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        productsRv = findViewById(R.id.productsRv);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        filterOrderBtn = findViewById(R.id.filterOrderBtn);
        ordersRv = findViewById(R.id.ordersRv);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation2);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please Wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        mFirebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        loadAllOrders();

        showProductsUI();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainSellerActivity.this, AddProductActivity.class);
                startActivity(intent);
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
                    mAdapterProductSeller.getFilter().filter(charSequence);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make offline
                //sign out
                //go to login activity
                mFirebaseAuth.signOut();
                checkUser();
            }
        });

        tabProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load products
                showProductsUI();
            }
        });

        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load orders
                showOrdersUI();
            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Please choose the Product Category")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //get selected item
                                String selected = Constants.productCategories1[i];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All")) {
                                    //load all
                                    loadAllProducts();
                                }
                                else {
                                    //load filtered products
                                    loadFilteredProducts(selected);
                                }
                            }
                        })
                        .show();
            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //options to display in dialog
                String [] options = {"All", "In Progress", "Completed", "Cancelled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter All the Orders")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle item clicks
                                if (which==0) {
                                    //All clicked
                                    filteredOrdersTv.setText("Showing All Orders");
                                    mAdapterOrderShop.getFilter().filter(""); // show all orders
                                }
                                else {
                                    String optionClicked = options[which];
                                    filteredOrdersTv.setText("Showing " + optionClicked + " Orders"); //exp: Showing Completed Orders
                                    mAdapterOrderShop.getFilter().filter(optionClicked);
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
                Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", "" + mFirebaseAuth.getUid());
                startActivity(intent);
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bottom_home:
                    return true;
                case R.id.bottom_community2:
                    startActivity(new Intent(MainSellerActivity.this, CommunitySellerActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.bottom_profile:
                    startActivity(new Intent(getApplicationContext(), ProfileEditSellerActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.bottom_support2:
                    startActivity(new Intent(getApplicationContext(), SettingSellerActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
            }
            return false;
        });
    }

    private void loadAllOrders() {
        //init array list
        mOrderShopArrayList = new ArrayList<>();

        //load orders of shop
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mFirebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding new data in it
                        mOrderShopArrayList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            //add to list
                            mOrderShopArrayList.add(modelOrderShop);
                        }
                        //setup adapter
                        mAdapterOrderShop = new AdapterOrderShop(MainSellerActivity.this, mOrderShopArrayList);
                        //set adapter
                        ordersRv.setAdapter(mAdapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredProducts(String selected) {
        mProductList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(mFirebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //before getting reset list
                        mProductList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {

                            String productCategory = ""+ds.child("productCategory").getValue();

                            //if selected category matches product category then add in list
                            if (selected.equals(productCategory)) {
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                mProductList.add(modelProduct);
                            }
                        }
                        //setup adapter
                        mAdapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, mProductList);
                        //set adapter
                        productsRv.setAdapter(mAdapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProducts() {
        mProductList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(mFirebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //before getting reset list
                        mProductList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            mProductList.add(modelProduct);
                        }
                        //setup adapter
                        mAdapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, mProductList);
                        //set adapter
                        productsRv.setAdapter(mAdapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void showProductsUI() {
        //show products ui and hide orders ui
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.black));
        tabProductsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void showOrdersUI() {
        //show orders ui and hide products ui
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.white));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);
    }

    private void checkUser(){
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
            finish();
        }
        else {
            loadMyInfo();
        }
    }

    private void loadMyInfo(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(mFirebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            //get data from db
                            String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String email = ""+ds.child("email").getValue();
                            String shopName = ""+ds.child("shopName").getValue();

                            //set data to ui
                            nameTv.setText(name);
                            shopNameTv.setText(shopName);
                            emailTv.setText(email);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}