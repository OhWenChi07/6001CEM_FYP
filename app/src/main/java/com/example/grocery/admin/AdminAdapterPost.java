package com.example.grocery.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdminAdapterPost extends RecyclerView.Adapter<AdminAdapterPost.HolderAdminPost>{

    private Context mContext;
    public ArrayList<AdminModelPost> mPostList;

    public AdminAdapterPost(Context context, ArrayList<AdminModelPost> postList) {
        this.mContext = context;
        this.mPostList = postList;
    }

    @NonNull
    @Override
    public HolderAdminPost onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_admin_post, parent, false);
        return new HolderAdminPost(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdminPost holder, int position) {
        //get data
        AdminModelPost adminModelPost = mPostList.get(position);
        String id = adminModelPost.getPostId();
        String uid = adminModelPost.getUid();
        String title = adminModelPost.getPostTitle();
        String description = adminModelPost.getPostDescription();
        String image = adminModelPost.getPostImage();
        String timestamp = adminModelPost.getTimestamp();

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);

        try {
            Picasso.get().load(image).placeholder(R.drawable.baseline_image_grey).into(holder.postImageIv);
        }
        catch (Exception e) {
            holder.postImageIv.setImageResource(R.drawable.baseline_image_grey);
        }

        // Set an OnClickListener for the delete button
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this post " + title + " ?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //delete
                                deletePost(id); //id is the post id
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //cancel, dismiss dialog
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    private void deletePost(String id) {

        Toast.makeText(mContext, "Deleting Post with Post ID" + id, Toast.LENGTH_SHORT).show();
        //delete product using its id

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Community_Post");
        reference.child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //product delete successfully
                        Toast.makeText(mContext, "Post Delete Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //product failed delete
                        Toast.makeText(mContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    class HolderAdminPost extends RecyclerView.ViewHolder{

        private ImageView postImageIv;
        private TextView titleTv, descriptionTv;
        private Button deleteBtn;

        public HolderAdminPost(@NonNull View itemView) {
            super(itemView);

            postImageIv = itemView.findViewById(R.id.postImageIv);
            titleTv = itemView.findViewById(R.id.titleTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
