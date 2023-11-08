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

public class AdminAdapterUser extends RecyclerView.Adapter<AdminAdapterUser.HolderAdminUser>{

    private Context mContext;
    public ArrayList<AdminModelUser> mUserList;

    public AdminAdapterUser(Context context, ArrayList<AdminModelUser> userList) {
        this.mContext = context;
        this.mUserList = userList;
    }

    @NonNull
    @Override
    public HolderAdminUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_admin_user_list, parent, false);
        return new HolderAdminUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdminUser holder, int position) {
        //get data
        AdminModelUser adminModelUser = mUserList.get(position);
        String id = adminModelUser.getUserId();
        String uid = adminModelUser.getUid();
        String name = adminModelUser.getName();
        String email = adminModelUser.getEmail();
        String phoneNumber = adminModelUser.getPhoneNumber();
        String timestamp = adminModelUser.getTimestamp();

        //set data
        holder.nameTv.setText(name);
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
                                deleteUser(uid); //id is the user id
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

    private void deleteUser(String uid) {

        Toast.makeText(mContext, "Deleting Customer with Customer ID" + uid, Toast.LENGTH_SHORT).show();
        // Delete user using their UID

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // User deleted successfully
                        Toast.makeText(mContext, "Customer Deleted Successfully", Toast.LENGTH_SHORT).show();
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
        return mUserList.size();
    }

    class HolderAdminUser extends RecyclerView.ViewHolder{

        private TextView nameTv, emailTv, phoneTv;
        private Button deleteBtn;

        public HolderAdminUser(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
