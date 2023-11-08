package com.example.grocery.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditPostSellerActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText titleEt, descriptionEt;
    private ImageView postImageIv;
    private Button editPostBtn;

    private String postId;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //image picked uri
    private Uri image_uri;

    private FirebaseAuth mFirebaseAuth;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post_seller);

        //init ui views
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        postImageIv = findViewById(R.id.postImageIv);
        editPostBtn = findViewById(R.id.editPostBtn);
        backBtn = findViewById(R.id.backBtn);

        //get id of the product from intent
        postId = getIntent().getStringExtra("postId");

        mFirebaseAuth = FirebaseAuth.getInstance();
        loadPostSellerDetails(); //to set on views

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please Wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        //init permissions array
        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        postImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog to pick image
                showImagePickDialog();
            }
        });

        editPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //flow
                //1. Input data
                //2. Validate data
                //3. Edit and update data to database
                inputData();
            }
        });
    }

    private void loadPostSellerDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Community_Post");
        reference.child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get data
                        String postId = "" + dataSnapshot.child("postId").getValue();
                        String postTitle = "" + dataSnapshot.child("postTitle").getValue();
                        String postDescription = "" + dataSnapshot.child("postDescription").getValue();
                        String postImage = "" + dataSnapshot.child("postImage").getValue();
                        String timestamp = "" + dataSnapshot.child("timestamp").getValue();
                        String uid = "" + dataSnapshot.child("uid").getValue();

                        titleEt.setText(postTitle);
                        descriptionEt.setText(postDescription);

                        try {
                            Picasso.get().load(postImage).placeholder(R.drawable.baseline_image_grey).into(postImageIv);
                        }
                        catch (Exception e) {
                            postImageIv.setImageResource(R.drawable.baseline_image_grey);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String postTitle, postDescription;

    private void inputData() {

        //1. Input Data
        postTitle = titleEt.getText().toString().trim();
        postDescription = descriptionEt.getText().toString().trim();

        //2. Validate data
        if (TextUtils.isEmpty(postTitle)){
            Toast.makeText(this, "Please enter the Post Title.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(postDescription)){
            Toast.makeText(this, "Please enter the Post Description.", Toast.LENGTH_SHORT).show();
            return;
        }
        editSellerPost();
    }

    private void editSellerPost() {
        // Show progress
        mProgressDialog.setMessage("Editing Post. Please Wait");
        mProgressDialog.show();

        if (image_uri == null) {
            // Edit without image

            // Setup data in HashMap to edit
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("postTitle", postTitle);
            hashMap.put("postDescription", postDescription);

            // Update to the database
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Community_Post");

            // Check if the logged-in user is the owner of the post
            reference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String postOwnerUid = dataSnapshot.child("uid").getValue(String.class);

                        // Get the currently logged-in user's UID
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // Check if the current user is the post owner
                        if (currentUserId.equals(postOwnerUid)) {
                            // The current user is the post owner, allow the edit
                            reference.child(postId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            // Edit and update successfully
                                            mProgressDialog.dismiss();
                                            Toast.makeText(EditPostSellerActivity.this, "Edit Post Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Edit and update failed
                                            mProgressDialog.dismiss();
                                            Toast.makeText(EditPostSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // The current user is not the post owner, don't allow the edit
                            mProgressDialog.dismiss();
                            Toast.makeText(EditPostSellerActivity.this, "You are not the owner of this post. Cannot Edit Post Successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database errors if any
                    mProgressDialog.dismiss();
                    Toast.makeText(EditPostSellerActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Edit with an image

            //first upload image
            //image name and path on firebase storage
            String filePathAndMame = "post_images/" + "" + postId; //override previous image using same id

            //uploaded image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndMame);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded, get url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {
                                //setup data in hashmap to edit
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("postTitle", "" + postTitle);
                                hashMap.put("postDescription", "" + postDescription);
                                hashMap.put("postImage", "" + downloadImageUri);

                                // Update to the database
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Community_Post");

                                // Check if the logged-in user is the owner of the post
                                reference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            String postOwnerUid = dataSnapshot.child("uid").getValue(String.class);

                                            // Get the currently logged-in user's UID
                                            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                            // Check if the current user is the post owner
                                            if (currentUserId.equals(postOwnerUid)) {
                                                // The current user is the post owner, allow the edit
                                                reference.child(postId)
                                                        .updateChildren(hashMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                // Edit and update successfully
                                                                mProgressDialog.dismiss();
                                                                Toast.makeText(EditPostSellerActivity.this, "Edit Post Successfully", Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // Edit and update failed
                                                                mProgressDialog.dismiss();
                                                                Toast.makeText(EditPostSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                // The current user is not the post owner, don't allow the edit
                                                mProgressDialog.dismiss();
                                                Toast.makeText(EditPostSellerActivity.this, "You are not the owner of this post.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handle database errors if any
                                        mProgressDialog.dismiss();
                                        Toast.makeText(EditPostSellerActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //upload failed,
                            mProgressDialog.dismiss();
                            Toast.makeText(EditPostSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


        }
    }

    private void showImagePickDialog() {
        //options to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //handle item clicks
                        if (i==0) {
                            //camera clicked
                            if (checkCameraPermission()){
                                //permission granted
                                pickFromCamera();
                            }
                            else {
                                //permission not granted, request
                                requestCameraPermission();
                            }
                        }
                        else {
                            //gallery clicked
                            if (checkStoragePermission()){
                                //permission granted
                                pickFromGallery();
                            }
                            else {
                                //permission not granted, request
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();

    }

    private void pickFromGallery() {
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //intent to pick image from camera

        //using media store to pick high or original quality image
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image_Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        //return true or false
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //permissions allowed
                        pickFromCamera();
                    }
                    else {
                        //both or one of permissions denied
                        Toast.makeText(this, "Camera and Storage permissions are necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission allowed
                        pickFromGallery();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "Storage permission is necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //handle image pick results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image picked from gallery

                //save picked image uri
                image_uri = data.getData();

                //set image
                postImageIv.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image picked from camera

                postImageIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}