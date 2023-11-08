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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CommunityListActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private RecyclerView postsRv;
    private FirebaseAuth mFirebaseAuth;

    private ArrayList<AdminModelPost> mPostList;
    private AdminAdapterPost mAdminAdapterPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_list);

        backBtn = findViewById(R.id.backBtn);
        postsRv = findViewById(R.id.postsRv);

        mFirebaseAuth = FirebaseAuth.getInstance();
        loadAllPosts();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadAllPosts() {
        mPostList = new ArrayList<>();

        //get all posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Community_Post");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //before getting reset list
                mPostList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    AdminModelPost adminModelPost = ds.getValue(AdminModelPost.class);
                    mPostList.add(adminModelPost);
                }
                //setup adapter
                mAdminAdapterPost = new AdminAdapterPost(CommunityListActivity.this, mPostList);
                //set adapter
                postsRv.setAdapter(mAdminAdapterPost);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}