package com.example.grocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.ShopDetailsActivity;
import com.example.grocery.models.ModelShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop> {

    private Context mContext;
    public ArrayList<ModelShop> shopsList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopsList) {
        this.mContext = context;
        this.shopsList = shopsList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_shop.xml
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_shop, parent, false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        //get data
        ModelShop modelShop = shopsList.get(position);
        String accountType = modelShop.getAccountType();
        String address = modelShop.getAddress();
        String city = modelShop.getCity();
        String country = modelShop.getCountry();
        String deliveryFee = modelShop.getDeliveryFee();
        String email = modelShop.getEmail();
        String latitude = modelShop.getLatitude();
        String longitude = modelShop.getLongitude();
        String online = modelShop.getOnline();
        String name = modelShop.getName();
        String phoneNumber = modelShop.getPhoneNumber();
        String uid = modelShop.getUid(); //not sure need to put final in the front, final String uid = modelShop.getUid();
        String timestamp = modelShop.getTimestamp();
        String shopOpen = modelShop.getShopOpen();
        String state = modelShop.getState();
        String shopName = modelShop.getShopName();

        loadReviews(modelShop, holder); //load avg rating, set to rating bar

        //set data
        holder.shopNameTv.setText(shopName);
        holder.phoneTv.setText(phoneNumber);
        holder.addressTv.setText(address);
        //check if shop owner online
        if (online.equals("true")) {
            //shop owner is online
            holder.onlineIv.setVisibility(View.VISIBLE);
        }
        else {
            //shop owner is offline
            holder.onlineIv.setVisibility(View.GONE);
        }
        //check if shop open
        if (shopOpen.equals("true")) {
            //shop is open
            holder.shopClosedTv.setVisibility(View.GONE);
        }
        else {
            //shop is close
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }

        //handle click listener, show shop details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ShopDetailsActivity.class);
                intent.putExtra("shopUid", uid);
                mContext.startActivity(intent);
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews(ModelShop modelShop, HolderShop holder) {

        String shopUid = modelShop.getUid();

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

                        holder.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        //return number of records
        return shopsList.size();
    }

    //view holder
    class HolderShop extends RecyclerView.ViewHolder {

        //uid views of row_shop.xml
        private ImageView onlineIv;
        private TextView shopClosedTv, shopNameTv, phoneTv, addressTv;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            //init uid views
            onlineIv = itemView.findViewById(R.id.onlineIv);
            shopClosedTv = itemView.findViewById(R.id.shopClosedTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            addressTv = itemView.findViewById(R.id.addressTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
