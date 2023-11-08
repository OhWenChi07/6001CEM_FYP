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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddPostSellerActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSION = 2;

    private ImageButton backBtn;
    private EditText postTitleEt, postDescriptionEt;
    private ImageView postImageIv;
    private Button addBtn;

    private static final int IMAGE_PICK_CODE = 1000;
    private StorageReference storageReference;

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
    private Uri uri;

    private FirebaseAuth mFirebaseAuth;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post_seller);

        FirebaseApp.initializeApp(this);

        //init ui views
        postTitleEt = findViewById(R.id.postTitleEt);
        postDescriptionEt = findViewById(R.id.postDescriptionEt);
        postImageIv = findViewById(R.id.postImageIv);
        addBtn = findViewById(R.id.addBtn);
        backBtn = findViewById(R.id.backBtn);

        mFirebaseAuth = FirebaseAuth.getInstance();
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
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });
    }

    private String postTitle, postDescription;
    private void inputData() {

        //1. Input Data
        postTitle = postTitleEt.getText().toString().trim();
        postDescription = postDescriptionEt.getText().toString().trim();

        //2. Validate data
        if (TextUtils.isEmpty(postTitle)){
            Toast.makeText(this, "Please enter the Post Title.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(postDescription)){
            Toast.makeText(this, "Please enter the Post Description.", Toast.LENGTH_SHORT).show();
            return;
        }
        addPost();
    }

    private void addPost() {
        //3. Add data to database
        mProgressDialog.setMessage("Adding Post. Please Wait.");
        mProgressDialog.show();

        final String timestamp = ""+System.currentTimeMillis();

        if (image_uri == null) {
            //upload without image

            //setup data to upload
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("postId", "" + timestamp);
            hashMap.put("postTitle", "" + postTitle);
            hashMap.put("postDescription", "" + postDescription);
            hashMap.put("postImage", ""); //no image, set empty
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("uid", "" + mFirebaseAuth.getUid());

            //add to database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Community_Post");
            ref.child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //added to database
                            mProgressDialog.dismiss();
                            Toast.makeText(AddPostSellerActivity.this, "New Post Added Successfully!", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed added to database
                            mProgressDialog.dismiss();
                            Toast.makeText(AddPostSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            //upload with image

            //first upload image to storage

            //name and path of image to be uploaded
            String filePathAndName = "post_images/" + "" + timestamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded
                            //get url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {
                                //url of image received, uploaded to database
                                //setup data to upload
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("postId", "" + timestamp);
                                hashMap.put("postTitle", "" + postTitle);
                                hashMap.put("postDescription", "" + postDescription);
                                hashMap.put("postImage", "" + downloadImageUri);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("uid", "" + mFirebaseAuth.getUid());

                                //add to database
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Community_Post");
                                ref.child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //added to database
                                                mProgressDialog.dismiss();
                                                Toast.makeText(AddPostSellerActivity.this, "New Post Added Successfully!", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed added to database
                                                mProgressDialog.dismiss();
                                                Toast.makeText(AddPostSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            mProgressDialog.dismiss();
                            Toast.makeText(AddPostSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void clearData() {
        //clear data after uploading product
        postTitleEt.setText("");
        postDescriptionEt.setText("");
        postImageIv.setImageResource(R.drawable.baseline_placeholder_image_grey);
        image_uri = null;
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