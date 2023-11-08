package com.example.grocery.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.FilterProductUser;
import com.example.grocery.R;
import com.example.grocery.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    private Context mContext;
    public ArrayList<ModelProduct> mProductsList, mfilterList;
    private FilterProductUser filter;

    //add new
    private FirebaseAuth mFirebaseAuth;
    private ProgressDialog mProgressDialog;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productsList) {
        this.mContext = context;
        this.mProductsList = productsList;
        this.mfilterList = productsList;

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_product_user, parent, false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {
        //get data
        ModelProduct modelProduct = mProductsList.get(position);
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String originalPrice = modelProduct.getOriginalPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String productTitle = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String productId = modelProduct.getProductId();
        String timestamp = modelProduct.getTimestamp();
        String productIcon = modelProduct.getProductIcon();

        //set data
        holder.titleTv.setText(productTitle);
        holder.descriptionTv.setText(productDescription);
        holder.discountedNoteTv.setText(discountNote);
        holder.discountedPriceTv.setText("RM"+discountPrice);
        holder.originalPriceTv.setText("RM"+originalPrice);

        if (discountAvailable.equals("true")) {
            //product is on discount
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            //add strike through on original price
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            //product is not on discount
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountedNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }

        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.baseline_add_shopping_cart_primary).into(holder.productIconIv);
        }
        catch (Exception e) {
            holder.productIconIv.setImageResource(R.drawable.baseline_add_shopping_cart_primary);
        }

        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add product to cart
                showQuantityDialog(modelProduct);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show product details
            }
        });
    }

    private double cost = 0;
    private double finalCost = 0;
    private int quantity = 0;

    private void showQuantityDialog(ModelProduct modelProduct) {
        //inflate layout for dialog
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_quantity, null);
        //init layout views
        ImageView productIv = view.findViewById(R.id.productIv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView pQuantityTv = view.findViewById(R.id.pQuantityTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView discountedNoteTv = view.findViewById(R.id.discountedNoteTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView priceDiscountedTv = view.findViewById(R.id.priceDiscountedTv);
        TextView finalPriceTv = view.findViewById(R.id.finalPriceTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);

        //get data from model
        String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescription();
        String discountNote = modelProduct.getDiscountNote();
        String image = modelProduct.getProductIcon();

        String price;
        if (modelProduct.getDiscountAvailable().equals("true")) {
            //product have discount
            price = modelProduct.getDiscountPrice();
            discountedNoteTv.setVisibility(View.VISIBLE);
            //add strike through on original price
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            //product do not have discount
            discountedNoteTv.setVisibility(View.GONE);
            priceDiscountedTv.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();
        }

        cost = Double.parseDouble(price.replaceAll("RM", ""));
        finalCost = Double.parseDouble(price.replaceAll("RM", ""));
        quantity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view);

        //set data
        try {
            Picasso.get().load(image).placeholder(R.drawable.baseline_shopping_cart_grey).into(productIv);
        }
        catch (Exception e) {
            productIv.setImageResource(R.drawable.baseline_shopping_cart_grey);
        }

        titleTv.setText("" + title);
        pQuantityTv.setText("" + productQuantity);
        descriptionTv.setText("" + description);
        discountedNoteTv.setText("" + discountNote);
        quantityTv.setText("" + quantity);
        originalPriceTv.setText("RM" + modelProduct.getOriginalPrice());
        priceDiscountedTv.setText("RM" + modelProduct.getDiscountPrice());
        finalPriceTv.setText("RM" + finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();

        //increase product quantity
        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalCost = finalCost + cost;
                quantity++;

                finalPriceTv.setText("RM" + finalCost);
                quantityTv.setText("" + quantity);
            }
        });

        //decrease product quantity, only if quantity is > 1
        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity>1) {
                    finalCost = finalCost - cost;
                    quantity --;

                    finalPriceTv.setText("RM" + finalCost);
                    quantityTv.setText("" + quantity);
                }
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleTv.getText().toString().trim();
                String priceEach = price;
                String totalPrice = finalPriceTv.getText().toString().trim().replace("", "");
                String quantity = quantityTv.getText().toString().trim();

                //add products
                addToCart(productId, title, priceEach, totalPrice, quantity);

                dialog.dismiss();
            }
        });
    }

    private int itemId = 1;

    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {
        // Generate a unique cart item ID
        String uid = mFirebaseAuth.getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Cart");
        String cartItemId = cartRef.push().getKey();

        // Create a HashMap to store cart item details
        HashMap<String, Object> cartItemMap = new HashMap<>();
        cartItemMap.put("productId", productId);
        cartItemMap.put("title", title);
        cartItemMap.put("priceEach", priceEach);
        cartItemMap.put("price", price);
        cartItemMap.put("quantity", quantity);

        // Add the cart item to the user's cart
        cartRef.child(cartItemId).setValue(cartItemMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Item added to the cart successfully
                        Toast.makeText(mContext, "Item added to cart", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error adding item to the cart
                        Toast.makeText(mContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public int getItemCount() {
        return mProductsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null) {
            filter = new FilterProductUser(this, mfilterList);
        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder {

        //uid views
        private ImageView productIconIv;
        private TextView discountedNoteTv, titleTv, descriptionTv, addToCartTv, discountedPriceTv, originalPriceTv;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);

            //init ui views
            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountedNoteTv = itemView.findViewById(R.id.discountedNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            addToCartTv = itemView.findViewById(R.id.addToCartTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
        }
    }
}
