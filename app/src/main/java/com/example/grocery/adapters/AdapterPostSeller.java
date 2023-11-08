package com.example.grocery.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.CommentPostActivity;
import com.example.grocery.activities.CommentRetrieveActivity;
import com.example.grocery.activities.EditPostSellerActivity;
import com.example.grocery.models.ModelPostSeller;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdapterPostSeller extends RecyclerView.Adapter<AdapterPostSeller.HolderPostSeller>{

    private Context mContext;
    public ArrayList<ModelPostSeller> mPostSellerList;
    private List<ModelPostSeller> originalList;

    public AdapterPostSeller(Context context, ArrayList<ModelPostSeller> postSellerList) {
        this.mContext = context;
        this.mPostSellerList = postSellerList;
        this.originalList = new ArrayList<>(postSellerList);
    }

    @NonNull
    @Override
    public HolderPostSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_post, parent, false);
        return new HolderPostSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPostSeller holder, int position) {
        //get data
        ModelPostSeller modelPostSeller = mPostSellerList.get(position);
        String id = modelPostSeller.getPostId();
        String uid = modelPostSeller.getUid();
        String title = modelPostSeller.getPostTitle();
        String description = modelPostSeller.getPostDescription();
        String image = modelPostSeller.getPostImage();
        String timestamp = modelPostSeller.getTimestamp();

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);

        try {
            Picasso.get().load(image).placeholder(R.drawable.baseline_image_grey).into(holder.postImageIv);
        }
        catch (Exception e) {
            holder.postImageIv.setImageResource(R.drawable.baseline_image_grey);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //handle item clicks, show item details (in bottom sheet)
                detailsBottomSheet(modelPostSeller); //here modelPost contains details of clicked post
            }
        });
    }

    private void detailsBottomSheet(ModelPostSeller modelPostSeller) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext);
        //inflate view for bottom
        View view = LayoutInflater.from(mContext).inflate(R.layout.bs_post_details, null);
        //set view to bottomsheet
        bottomSheetDialog.setContentView(view);

        //init views of bottomsheet
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton deleteBtn = view.findViewById(R.id.deleteBtn);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        ImageButton commentBtn = view.findViewById(R.id.commentBtn);
        ImageButton addCommentBtn = view.findViewById(R.id.addCommentBtn);
        ImageButton shareBtn = view.findViewById(R.id.shareBtn);
        ImageView postImageIv = view.findViewById(R.id.postImageIv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);

        //get data
        String id = modelPostSeller.getPostId();
        String uid = modelPostSeller.getUid();
        String title = modelPostSeller.getPostTitle();
        String description = modelPostSeller.getPostDescription();
        String image = modelPostSeller.getPostImage();
        String timestamp = modelPostSeller.getTimestamp();

        //set data
        titleTv.setText(title);
        descriptionTv.setText(description);
        try {
            Picasso.get().load(image).placeholder(R.drawable.baseline_image_grey).into(postImageIv);
        }
        catch (Exception e) {
            postImageIv.setImageResource(R.drawable.baseline_image_grey);
        }

        //show dialog
        bottomSheetDialog.show();

        //retrieve comment click
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                //open edit product activity, pass id of product
                Intent intent = new Intent(mContext, CommentRetrieveActivity.class);
                intent.putExtra("postUid", id);
                mContext.startActivity(intent);
            }
        });

        //add comment click
        addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                //open edit product activity, pass id of product
                Intent intent = new Intent(mContext, CommentPostActivity.class);
                intent.putExtra("postUid", id);
                mContext.startActivity(intent);
            }
        });

        //share click
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                // Create a share intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareText = "Check out this post: " + title + "\n\n" + description;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

                // Start the sharing activity
                mContext.startActivity(Intent.createChooser(shareIntent, "Share this post via"));
            }
        });

        //edit click
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                //open edit product activity, pass id of product
                Intent intent = new Intent(mContext, EditPostSellerActivity.class);
                intent.putExtra("postId", id);
                mContext.startActivity(intent);
            }
        });
        //delete click
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this post " + title + " ?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //delete
                                deletePostSeller(id); //id is the post id
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
        //back click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void deletePostSeller(String id) {
        // Delete the post using its ID

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Community_Post").child(id);

        // Check if the logged-in user is the owner of the post
        if (firebaseAuth.getCurrentUser() != null) {
            String currentUserId = firebaseAuth.getCurrentUser().getUid();

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get the UID of the post owner
                        String postOwnerUid = dataSnapshot.child("uid").getValue(String.class);

                        // Check if the current user is the post owner
                        if (currentUserId.equals(postOwnerUid)) {
                            // The current user is the post owner, allow deletion
                            reference.removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            // Post deleted successfully
                                            Toast.makeText(mContext, "Post Deleted Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Failed to delete the post
                                            Toast.makeText(mContext, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // The current user is not the post owner, don't allow deletion
                            Toast.makeText(mContext, "You are not the owner of this post. Cannot Delete Post Successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPostSellerList.size();
    }

    public void filter(String query) {
        query = query.toLowerCase(Locale.getDefault());
        mPostSellerList.clear();

        if (query.isEmpty()) {
            mPostSellerList.addAll(originalList); // If the query is empty, show all items
        } else {
            for (ModelPostSeller post : originalList) {
                String title = post.getPostTitle().toLowerCase(Locale.getDefault());
                String description = post.getPostDescription().toLowerCase(Locale.getDefault());

                if (title.contains(query) || description.contains(query)) {
                    mPostSellerList.add(post);
                }
            }
        }

        notifyDataSetChanged();
    }

    class HolderPostSeller extends RecyclerView.ViewHolder{

        private ImageView postImageIv;
        private TextView titleTv, descriptionTv;

        public HolderPostSeller(@NonNull View itemView) {
            super(itemView);

            postImageIv = itemView.findViewById(R.id.postImageIv);
            titleTv = itemView.findViewById(R.id.titleTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
        }
    }
}
