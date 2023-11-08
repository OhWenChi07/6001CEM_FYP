package com.example.grocery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterCommentPost;
import com.example.grocery.models.ModelCommentPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CommentRetrieveActivity extends AppCompatActivity {

    //uid views
    private ImageButton backBtn;
    private ImageView postImageIv;
    private TextView titleTv, descriptionTv;
    private RecyclerView commentRv;

    private FirebaseAuth mFirebaseAuth;
    private ArrayList<ModelCommentPost> mCommentPostArrayList;
    private AdapterCommentPost mAdapterCommentPost;
    private String postUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_retrieve);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        postImageIv = findViewById(R.id.postImageIv);
        titleTv = findViewById(R.id.titleTv);
        descriptionTv = findViewById(R.id.descriptionTv);
        commentRv = findViewById(R.id.commentRv);

        //get post id from intent
        postUid = getIntent().getStringExtra("postUid");

        mFirebaseAuth = FirebaseAuth.getInstance();
        loadPostDetails(); //for post image, post title, post description
        loadComments(); //for comment list

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadComments() {
        //init list
        mCommentPostArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Community_Post");
        ref.child(postUid).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding data into it
                        mCommentPostArrayList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {

                            ModelCommentPost modelCommentPost = ds.getValue(ModelCommentPost.class);
                            mCommentPostArrayList.add(modelCommentPost);
                        }
                        //setup adapter
                        mAdapterCommentPost = new AdapterCommentPost(CommentRetrieveActivity.this, mCommentPostArrayList);
                        //set adapter
                        commentRv.setAdapter(mAdapterCommentPost);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadPostDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Community_Post");
        ref.child(postUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String postTitle = "" + dataSnapshot.child("postTitle").getValue();
                        String postDescription = "" + dataSnapshot.child("postDescription").getValue();
                        String postImage = "" + dataSnapshot.child("postImage").getValue();

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