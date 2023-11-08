package com.example.grocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.FilterOrderShop;
import com.example.grocery.R;
import com.example.grocery.activities.OrderDetailsSellerActivity;
import com.example.grocery.models.ModelOrderShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOrderShop> implements Filterable {

    private Context mContext;
    public ArrayList<ModelOrderShop> mOrderShopArrayList, mfilterList;
    private FilterOrderShop filter;

    public AdapterOrderShop(Context context, ArrayList<ModelOrderShop> orderShopArrayList) {
        this.mContext = context;
        this.mOrderShopArrayList = orderShopArrayList;
        this.mfilterList = orderShopArrayList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_order shop.xml
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_order_seller, parent, false);
        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {
        //get data at position
        ModelOrderShop modelOrderShop = mOrderShopArrayList.get(position);
        String orderId = modelOrderShop.getOrderId();
        String orderBy = modelOrderShop.getOrderBy();
        String orderCost = modelOrderShop.getOrderCost();
        String orderStatus = modelOrderShop.getOrderStatus();
        String orderTime = modelOrderShop.getOrderTime();
        String orderTo = modelOrderShop.getOrderTo();

        //load user info
        loadUserInfo(modelOrderShop, holder);

        //set data
        holder.amountTv.setText("Amount: RM" + orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("Order ID: " + orderId);
        //change order status text color
        if (orderStatus.equals("In Progress")) {
            holder.statusTv.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
        }
        else if (orderStatus.equals("Completed")) {
            holder.statusTv.setTextColor(mContext.getResources().getColor(R.color.green));
        }
        if (orderStatus.equals("Cancelled")) {
            holder.statusTv.setTextColor(mContext.getResources().getColor(R.color.red));
        }

        //convert time to proper format, exp. 10/10/2023 9:00 AM
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();

        holder.orderDateTv.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details (Order Details Seller Activity)
                Intent intent = new Intent(mContext, OrderDetailsSellerActivity.class);
                intent.putExtra("orderId", orderId); //to load order info
                intent.putExtra("orderBy", orderBy); //to load info of user who placed order
                mContext.startActivity(intent);
            }
        });
    }

    private void loadUserInfo(ModelOrderShop modelOrderShop, HolderOrderShop holder) {
        //to load email of the user: modelOrderShop.getOrderBy() contains uid of that user
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderShop.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String email = "" + dataSnapshot.child("email").getValue();
                        holder.emailTv.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return mOrderShopArrayList.size(); //return list size / number of records
    }

    @Override
    public Filter getFilter() {
        if (filter==null) {
            //init filter
            filter = new FilterOrderShop(this, mfilterList);
        }
        return filter;
    }

    //view holder class for row_order_seller.xml
    class HolderOrderShop extends RecyclerView.ViewHolder {

        //views of row_order seller.xml
        private TextView orderIdTv, orderDateTv, emailTv, amountTv, statusTv;

        public HolderOrderShop(@NonNull View itemView) {
            super(itemView);

            //init views
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            orderDateTv = itemView.findViewById(R.id.orderDateTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}
