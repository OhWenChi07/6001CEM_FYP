package com.example.grocery.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.ArrayList;

public class AdminAdapterSeller extends RecyclerView.Adapter<AdminAdapterSeller.HolderAdminSeller>{

    private Context mContext;
    public ArrayList<AdminModelSeller> mSellerList;

    public AdminAdapterSeller(Context context, ArrayList<AdminModelSeller> sellerList) {
        this.mContext = context;
        this.mSellerList = sellerList;
    }

    @NonNull
    @Override
    public HolderAdminSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_admin_seller_list, parent, false);
        return new HolderAdminSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdminSeller holder, int position) {
        //get data
        AdminModelSeller adminModelSeller = mSellerList.get(position);
        String id = adminModelSeller.getSellerId();
        String uid = adminModelSeller.getUid();
        String name = adminModelSeller.getName();
        String shopName = adminModelSeller.getShopName();
        String email = adminModelSeller.getEmail();
        String phoneNumber = adminModelSeller.getPhoneNumber();
        String timestamp = adminModelSeller.getTimestamp();

        //set data
        holder.nameTv.setText(name);
        holder.shopNameTv.setText(shopName);
        holder.emailTv.setText(email);
        holder.phoneTv.setText(phoneNumber);

        // Set an OnClickListener for the delete button
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this user " + name + " ?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //delete
                                deleteSeller(uid); //id is the seller id
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

    private void deleteSeller(String uid) {

        Toast.makeText(mContext, "Deleting Seller with Seller ID" + uid, Toast.LENGTH_SHORT).show();
        // Delete user using their UID

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // User deleted successfully
                        Toast.makeText(mContext, "Seller Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // User deletion failed
                        Toast.makeText(mContext, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mSellerList.size();
    }

    class HolderAdminSeller extends RecyclerView.ViewHolder{

        private TextView nameTv, shopNameTv, emailTv, phoneTv;
        private Button deleteBtn;

        public HolderAdminSeller(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.nameTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
