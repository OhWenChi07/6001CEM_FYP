package com.example.grocery.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.models.ModelCommentPost;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterCommentPost extends RecyclerView.Adapter<AdapterCommentPost.HolderCommentPost>{

    private Context mContext;
    private ArrayList<ModelCommentPost> mCommentPostArrayList;

    public AdapterCommentPost(Context context, ArrayList<ModelCommentPost> commentPostArrayList) {
        this.mContext = context;
        this.mCommentPostArrayList = commentPostArrayList;
    }

    @NonNull
    @Override
    public HolderCommentPost onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_comment_post item.xml
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_comment_post, parent, false);
        return new HolderCommentPost(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCommentPost holder, int position) {
        //get data at position
        ModelCommentPost modelCommentPost = mCommentPostArrayList.get(position);
        String uid = modelCommentPost.getUid();
        String comment = modelCommentPost.getComment();
        String timestamp = modelCommentPost.getTimestamp();

        //need info of user name who wrote the review [can do using uid of user]
        loadUserDetails(modelCommentPost, holder);

        //convert timestamp to proper format dd/MM/yyyy
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateFormat = DateFormat.format("dd/MM/yyyy", calendar).toString();

        //set data
        holder.commentTv.setText(comment);
        holder.dateTv.setText(dateFormat);
    }

    private void loadUserDetails(ModelCommentPost modelCommentPost, HolderCommentPost holder) {
        //uid of user who wrote review
        String uid = modelCommentPost.getUid();

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
        return mCommentPostArrayList.size(); //return list size
    }

    //view holder class, holds views of recyclerview
    class HolderCommentPost extends RecyclerView.ViewHolder {

        //views of row_comment_post.xml
        private TextView nameTv, dateTv, commentTv;

        public HolderCommentPost(@NonNull View itemView) {
            super(itemView);

            //init views
            nameTv = itemView.findViewById(R.id.nameTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            commentTv = itemView.findViewById(R.id.commentTv);
        }
    }
}
