package com.example.grocery.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterPostSeller;
import com.example.grocery.models.ModelPostSeller;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CommunitySellerActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private TextView filteredPostsTv;
    private EditText searchPostEt;
    private RecyclerView postsRv;
    private FirebaseAuth mFirebaseAuth;
    private ProgressDialog mProgressDialog;

    FloatingActionButton fab;

    private ArrayList<ModelPostSeller> mPostSellerList;
    private AdapterPostSeller mAdapterPostSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_seller);

        FloatingActionButton fab;

        fab = findViewById(R.id.fab);
        filteredPostsTv = findViewById(R.id.filteredPostsTv);
        postsRv = findViewById(R.id.postsRv);
        backBtn = findViewById(R.id.backBtn);
        searchPostEt = findViewById(R.id.searchPostEt);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please Wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        mFirebaseAuth = FirebaseAuth.getInstance();
        loadAllPosts();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CommunitySellerActivity.this, AddPostSellerActivity.class);
                startActivity(intent);
//                finish();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        searchPostEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                mAdapterPostSeller.filter(query);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation2);
        bottomNavigationView.setSelectedItemId(R.id.bottom_community2);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bottom_home:
                    startActivity(new Intent(getApplicationContext(), MainSellerActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.bottom_community2:
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

    private void loadAllPosts() {
        mPostSellerList = new ArrayList<>();

        //get all posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Community_Post");
        reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //before getting reset list
                        mPostSellerList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            ModelPostSeller modelPostSeller = ds.getValue(ModelPostSeller.class);
                            mPostSellerList.add(modelPostSeller);
                        }
                        //setup adapter
                        mAdapterPostSeller = new AdapterPostSeller(CommunitySellerActivity.this, mPostSellerList);
                        //set adapter
                        postsRv.setAdapter(mAdapterPostSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
