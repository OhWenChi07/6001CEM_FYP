package com.example.grocery.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.models.ModelReview;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview>{

    private Context mContext;
    private ArrayList<ModelReview> mReviewArrayList;

    public AdapterReview(Context context, ArrayList<ModelReview> reviewArrayList) {
        this.mContext = context;
        this.mReviewArrayList = reviewArrayList;
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_review.xml
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_review, parent, false);
        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {
        //get data at position
        ModelReview modelReview = mReviewArrayList.get(position);
        String uid = modelReview.getUid();
        String ratings = modelReview.getRatings();
        String review = modelReview.getReview();
        String timestamp = modelReview.getTimestamp();

        //need info of user name who wrote the review [can do using uid of user]
        loadUserDetails(modelReview, holder);

        //convert timestamp to proper format dd/MM/yyyy
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateFormat = DateFormat.format("dd/MM/yyyy", calendar).toString();

        //set data
        holder.ratingBar.setRating(Float.parseFloat(ratings));
        holder.reviewTv.setText(review);
        holder.dateTv.setText(dateFormat);
    }

    private void loadUserDetails(ModelReview modelReview, HolderReview holder) {
        //uid of user who wrote review
        String uid = modelReview.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get user info, use the same key names as in firebase
                        String name = "" + dataSnapshot.child("name").getValue();

                        //set data
                        holder.nameTv.setText(name);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return mReviewArrayList.size(); //return list size
    }

    //view holder class, holds views of recyclerview
    class HolderReview extends RecyclerView.ViewHolder{

        //views of row_review.xml
        private TextView nameTv, dateTv, reviewTv;
        private RatingBar ratingBar;

        public HolderReview(@NonNull View itemView) {
            super(itemView);

            //init views
            nameTv = itemView.findViewById(R.id.nameTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            reviewTv = itemView.findViewById(R.id.reviewTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
