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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private RecyclerView customersRv;
    private FirebaseAuth mFirebaseAuth;

    private ArrayList<AdminModelUser> mUserList;
    private AdminAdapterUser mAdminAdapterUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        backBtn = findViewById(R.id.backBtn);
        customersRv = findViewById(R.id.customersRv);

        mFirebaseAuth = FirebaseAuth.getInstance();
        loadAllUsers();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadAllUsers() {
        mUserList = new ArrayList<>();

        // Get all users with the "User" account type
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        Query query = ref.orderByChild("accountType").equalTo("User");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Before getting data, reset the list
                mUserList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    AdminModelUser adminModelUser = ds.getValue(AdminModelUser.class);
                    mUserList.add(adminModelUser);
                }
                // Setup adapter
                mAdminAdapterUser = new AdminAdapterUser(UserListActivity.this, mUserList);
                // Set the adapter
                customersRv.setAdapter(mAdminAdapterUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });
    }
}