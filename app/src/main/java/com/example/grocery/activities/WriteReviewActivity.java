package com.example.grocery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {

    //uid views
    private ImageButton backBtn;
    private TextView shopNameTv;
    private RatingBar ratingBar;
    private EditText reviewEt;
    private FloatingActionButton submitBtn;

    private String shopUid;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        shopNameTv = findViewById(R.id.shopNameTv);
        ratingBar = findViewById(R.id.ratingBar);
        reviewEt = findViewById(R.id.reviewEt);
        submitBtn = findViewById(R.id.submitBtn);

        mFirebaseAuth = FirebaseAuth.getInstance();

        //get shop uid from intent
        shopUid = getIntent().getStringExtra("shopUid");

        //load shop info: shop name
        loadShopInfo();

        //if user has written review to this shop, load it
        loadMyReview();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //input data
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });

    }

    private void inputData() {
        String ratings = "" + ratingBar.getRating();
        String review = "" + reviewEt.getText().toString().trim();

        //for time of review
        String timestamp = "" + System.currentTimeMillis();

        //setup data in hashmap
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + mFirebaseAuth.getUid());
        hashMap.put("ratings", "" + ratings);
        hashMap.put("review", "" + review);
        hashMap.put("timestamp", "" + timestamp);

        //add to db: Users > Shop Uid > Ratings
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(mFirebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //review added to db
                        Toast.makeText(WriteReviewActivity.this, "Review Added Successfully.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //review failed add to db
                        Toast.makeText(WriteReviewActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMyReview() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(mFirebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            //my review is available in this shop

                            //get review details
                            String uid = "" + dataSnapshot.child("uid").getValue();
                            String ratings = "" + dataSnapshot.child("ratings").getValue();
                            String review = "" + dataSnapshot.child("review").getValue();
                            String timestamp = "" + dataSnapshot.child("timestamp").getValue();

                            //set review details to our ui
                            float myRating = Float.parseFloat(ratings);
                            ratingBar.setRating(myRating);
                            reviewEt.setText(review);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get shop info
                String shopName = "" + dataSnapshot.child("shopName").getValue();

                //set shop info to ui
                shopNameTv.setText(shopName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}