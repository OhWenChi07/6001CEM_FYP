package com.example.grocery.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.ShopDetailsActivity;
import com.example.grocery.models.ModelCartItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    private Context mContext;
    private ArrayList<ModelCartItem> mCartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.mContext = context;
        this.mCartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_cart item.xml
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_cartitem, parent, false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, @SuppressLint("RecyclerView") int position) {
        //get data at position
        ModelCartItem modelCartItem = mCartItems.get(position);
        String id = modelCartItem.getId();
        String getpId = modelCartItem.getpId();
        String title = modelCartItem.getName();
        String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

        //set data
        holder.itemTitleTv.setText("" + title);
        holder.itemPriceTv.setText("" + cost);
        holder.itemQuantityTv.setText("[" + quantity + "]"); //example [2]
        holder.itemPriceEachTv.setText("" + price);

        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the unique identifier (product ID) for the item to be removed
                String productId = mCartItems.get(position).getpId();

                // Get the reference to the cart item in the database
                String uid = FirebaseAuth.getInstance().getUid();
                if (uid != null) {
                    DatabaseReference cartRef = FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(uid)
                            .child("Cart")
                            .child(productId);

                    // Remove the cart item from the database
                    cartRef.removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Item removed from the cart successfully
                                    Toast.makeText(mContext, "Item removed from cart.", Toast.LENGTH_SHORT).show();

                                    // Refresh the RecyclerView and update the total price
                                    mCartItems.remove(position);
                                    notifyItemRemoved(position);
                                    notifyDataSetChanged();

                                    // Calculate the new total price and update the UI
                                    ((ShopDetailsActivity) mContext).updateTotalPrice();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Error removing item from the cart
                                    Toast.makeText(mContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        //return number of records
        return mCartItems.size();
    }

    //view holder class
    class HolderCartItem extends RecyclerView.ViewHolder {

        //ui views of row_cart items.xml
        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv, itemRemoveTv;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            //init views
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);
        }
    }
}
