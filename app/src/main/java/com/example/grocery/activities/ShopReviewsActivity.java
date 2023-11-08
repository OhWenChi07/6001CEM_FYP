package com.example.grocery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterReview;
import com.example.grocery.models.ModelReview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn;
    private TextView shopNameTv, ratingsTv;
    private RatingBar ratingBar;
    private RecyclerView reviewsRv;

    private FirebaseAuth mFirebaseAuth;
    private ArrayList<ModelReview> mReviewArrayList;
    private AdapterReview mAdapterReview;
    private String shopUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        shopNameTv = findViewById(R.id.shopNameTv);
        ratingBar = findViewById(R.id.ratingBar);
        ratingsTv = findViewById(R.id.ratingsTv);
        reviewsRv = findViewById(R.id.reviewsRv);

        //get shop uid from intent
        shopUid = getIntent().getStringExtra("shopUid");

        mFirebaseAuth = FirebaseAuth.getInstance();
        loadShopDetails(); //for shop name
        loadReviews(); //for reviews list, avg rating

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid)
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

    private float ratingSum = 0;
    private void loadReviews() {
        //init list
        mReviewArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding data into it
                        mReviewArrayList.clear();
                        ratingSum = 0;
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue()); //exp: 4.5
                            ratingSum = ratingSum + rating; //for avg rating, add(addition of) all ratings, and divide it by number of reviews

                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            mReviewArrayList.add(modelReview);
                        }
                        //setup adapter
                        mAdapterReview = new AdapterReview(ShopReviewsActivity.this, mReviewArrayList);
                        //set adapter
                        reviewsRv.setAdapter(mAdapterReview);

                        long numberOfReviews = dataSnapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        ratingsTv.setText(String.format("%.2f", avgRating) + " [" + numberOfReviews + "]"); //exp 4.7[10]
                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}