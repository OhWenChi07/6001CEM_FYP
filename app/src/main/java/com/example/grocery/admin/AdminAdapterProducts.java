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

public class AdminAdapterProducts extends RecyclerView.Adapter<AdminAdapterProducts.HolderAdminProduct>{

    private Context mContext;
    public ArrayList<AdminModelProducts> mProductsList;

    private FirebaseAuth mFirebaseAuth;

    public AdminAdapterProducts(Context context, ArrayList<AdminModelProducts> productsList) {
        this.mContext = context;
        this.mProductsList = productsList;

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderAdminProduct onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_admin_product_list, parent, false);
        return new HolderAdminProduct(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdminProduct holder, int position) {
        //get data
        AdminModelProducts adminModelProducts = mProductsList.get(position);
        String id = adminModelProducts.getProductId();
        String uid = adminModelProducts.getUid();
        String discountNote = adminModelProducts.getDiscountNote();
        String discountPrice = adminModelProducts.getDiscountPrice();
        String originalPrice = adminModelProducts.getOriginalPrice();
        String productCategory = adminModelProducts.getProductCategory();
        String productDescription = adminModelProducts.getProductDescription();
        String productTitle = adminModelProducts.getProductTitle();
        String productIcon = adminModelProducts.getProductIcon();
        String timestamp = adminModelProducts.getTimestamp();

        //set data
        holder.titleTv.setText(productTitle);
        holder.descriptionTv.setText(productDescription);
        holder.categoryTv.setText(productCategory);
        holder.discountedNoteTv.setText(discountNote);
        holder.discountedPriceTv.setText("RM"+discountPrice);
        holder.originalPriceTv.setText("RM"+originalPrice);

        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.baseline_add_shopping_cart_primary).into(holder.productIconIv);
        }
        catch (Exception e) {
            holder.productIconIv.setImageResource(R.drawable.baseline_add_shopping_cart_primary);
        }

        // Set an OnClickListener for the delete button
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this product " + productTitle + " ?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //delete
                                deleteProduct(uid); //id is the product id
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

    private void deleteProduct(String uid) {

        Toast.makeText(mContext, "Deleting Product with Product ID" + uid, Toast.LENGTH_SHORT).show();
        //delete product using its id

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(uid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //product delete successfully
                        mProductsList.remove(uid); // Remove the item from the list
                        Toast.makeText(mContext, "Product Delete Successfully", Toast.LENGTH_SHORT).show();
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
        return mProductsList.size();
    }

    class HolderAdminProduct extends RecyclerView.ViewHolder {

        //uid views
        private ImageView productIconIv;
        private TextView discountedNoteTv, titleTv, descriptionTv, categoryTv, discountedPriceTv, originalPriceTv;

        private Button deleteBtn;

        public HolderAdminProduct(@NonNull View itemView) {
            super(itemView);

            //init ui views
            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountedNoteTv = itemView.findViewById(R.id.discountedNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            categoryTv = itemView.findViewById(R.id.categoryTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
