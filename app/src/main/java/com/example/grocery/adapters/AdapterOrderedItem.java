package com.example.grocery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.models.ModelOrderedItem;

import java.util.ArrayList;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderedItem>{

    private Context mContext;
    private ArrayList<ModelOrderedItem> mOrderedItemArrayList;

    public AdapterOrderedItem(Context context, ArrayList<ModelOrderedItem> orderedItemArrayList) {
        this.mContext = context;
        this.mOrderedItemArrayList = orderedItemArrayList;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_ordered item.xml
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_ordereditem, parent, false);
        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItem holder, int position) {
        //get data at position
        ModelOrderedItem modelOrderedItem = mOrderedItemArrayList.get(position);
        String getpId = modelOrderedItem.getpId();
        String name = modelOrderedItem.getName();
        String cost = modelOrderedItem.getCost();
        String price = modelOrderedItem.getPrice();
        String quantity = modelOrderedItem.getQuantity();

        //set data
        holder.itemTitleTv.setText("" + name);
        holder.itemPriceTv.setText("" + cost);
        holder.itemQuantityTv.setText("[" + quantity + "]"); //example [2]
        holder.itemPriceEachTv.setText("" + price);
    }

    @Override
    public int getItemCount() {
        return mOrderedItemArrayList.size(); //return list size
    }

    //view holder class
    class HolderOrderedItem extends RecyclerView.ViewHolder {

        //views of row_ordered item.xml
        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv;

        public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);

            //init views
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
        }
    }
}
