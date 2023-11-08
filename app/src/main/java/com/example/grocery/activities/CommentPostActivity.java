package com.example.grocery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CommentPostActivity extends AppCompatActivity {

    //uid views
    private ImageButton backBtn;
    private ImageView postImageIv;
    private TextView titleTv, descriptionTv;
    private EditText commentEt;
    private FloatingActionButton submitBtn;
    private String postUid; // The ID of the post being commented on

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_post);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        postImageIv = findViewById(R.id.postImageIv);
        titleTv = findViewById(R.id.titleTv);
        descriptionTv = findViewById(R.id.descriptionTv);
        commentEt = findViewById(R.id.commentEt);
        submitBtn = findViewById(R.id.submitBtn);

        //get post id from intent
        postUid = getIntent().getStringExtra("postUid");

        //load post info: post image, post title, post description
        loadPostInfo();

        mFirebaseAuth = FirebaseAuth.getInstance();

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
        String comment = "" + commentEt.getText().toString().trim();

        //for time of comment
        String timestamp = "" + System.currentTimeMillis();

        //setup data in hashmap
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + mFirebaseAuth.getUid());
        hashMap.put("comment", "" + comment);
        hashMap.put("timestamp", "" + timestamp);

        //add to db: Users > Post Uid > Comments
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Community_Post");
        ref.child(postUid).child("Comments").child(mFirebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //comment added to db
                        Toast.makeText(CommentPostActivity.this, "Comment Added Successfully.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //comment failed add to db
                        Toast.makeText(CommentPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPostInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Community_Post");
        ref.child(postUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get post info
                String postTitle = "" + dataSnapshot.child("postTitle").getValue();
                String postDescription = "" + dataSnapshot.child("postDescription").getValue();
                String postImage = "" + dataSnapshot.child("postImage").getValue();

                //set post info to ui
                titleTv.setText(postTitle);
                descriptionTv.setText(postDescription);
                try {
                    Picasso.get().load(postImage).placeholder(R.drawable.baseline_upload_grey).into(postImageIv);
                }
                catch (Exception e) {
                    postImageIv.setImageResource(R.drawable.baseline_upload_grey);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
