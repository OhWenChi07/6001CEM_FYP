package com.example.grocery.admin;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;

import java.util.ArrayList;
import java.util.Calendar;

public class AdminAdapterOrders extends RecyclerView.Adapter<AdminAdapterOrders.HolderAdminOrder>{

    private Context mContext;
    public ArrayList<AdminModelOrders> mOrdersList;

    public AdminAdapterOrders(Context context, ArrayList<AdminModelOrders> ordersList) {
        this.mContext = context;
        this.mOrdersList = ordersList;
    }

    @NonNull
    @Override
    public HolderAdminOrder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_admin_order_list, parent, false);
        return new HolderAdminOrder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdminOrder holder, int position) {
        //get data
        AdminModelOrders adminModelOrders = mOrdersList.get(position);
        String orderId = adminModelOrders.getOrderId();
        String orderCost = adminModelOrders.getOrderCost();
        String orderStatus = adminModelOrders.getOrderStatus();
        String orderTime = adminModelOrders.getOrderTime();
        String orderBy = adminModelOrders.getOrderBy();
        String orderTo = adminModelOrders.getOrderTo();

        //set data
        holder.amountTv.setText("Amount: RM" + orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("OrderID:" + orderId);
        //change order status text color
        if (orderStatus.equals("In Progress")) {
            holder.statusTv.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
        } else if (orderStatus.equals("Completed")) {
            holder.statusTv.setTextColor(mContext.getResources().getColor(R.color.green));
        } else if (orderStatus.equals("Cancelled")) {
            holder.statusTv.setTextColor(mContext.getResources().getColor(R.color.red));
        }

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy", calendar).toString(); //example: 16/06/2023

        holder.dateTv.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details (Order Details Seller Activity)
                Intent intent = new Intent(mContext, OrderListDetailsActivity.class);
                intent.putExtra("orderId", orderId); //to load order info
                intent.putExtra("orderBy", orderBy); //to load info of user who placed order
                intent.putExtra("orderTo", orderTo); //to load info of seller who received order
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mOrdersList.size();
    }

    class HolderAdminOrder extends RecyclerView.ViewHolder {

        //views of layout
        private TextView orderIdTv, dateTv, amountTv, statusTv;

        public HolderAdminOrder(@NonNull View itemView) {
            super(itemView);

            //init views of layout
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}
