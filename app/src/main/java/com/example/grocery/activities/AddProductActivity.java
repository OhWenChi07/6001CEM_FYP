package com.example.grocery.activities;

import android.Manifest;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.grocery.Constants;
import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt, descriptionEt;
    private TextView categoryTv, quantityEt, priceEt, discountedPriceEt, discountedNoteEt;
    private SwitchCompat discountSwitch;
    private Button addProductBtn;

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
        setContentView(R.layout.activity_add_product);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        productIconIv = findViewById(R.id.productIconIv);
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        categoryTv = findViewById(R.id.categoryTv);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountSwitch = findViewById(R.id.discountSwitch);
        discountedPriceEt = findViewById(R.id.discountedPriceEt);
        discountedNoteEt = findViewById(R.id.discountedNoteEt);
        addProductBtn = findViewById(R.id.addProductBtn);

        //on start is unchecked, so hide discountPriceEt, discountNoteEt
        discountedPriceEt.setVisibility(View.GONE);
        discountedNoteEt.setVisibility(View.GONE);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please Wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        //init permissions array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //if discountSwitch is checked: show discountPriceEt, discountNoteEt
        //if discountSwitch is not checked: hide discountPriceEt, discountNoteEt
        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    //checked, show discountPriceEt, discountNoteEt
                    discountedPriceEt.setVisibility(View.VISIBLE);
                    discountedNoteEt.setVisibility(View.VISIBLE);
                }
                else {
                    //unchecked, hide discountPriceEt, discountNoteEt
                    discountedPriceEt.setVisibility(View.GONE);
                    discountedNoteEt.setVisibility(View.GONE);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog to pick image
                showImagePickDialog();
            }
        });

        categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick category
                categoryDialog();
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //flow
                //1. Input data
                //2. Validate data
                //3. Add data to database
                inputData();
            }
        });
    }

    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;

    private void inputData() {
        //1. Input Data
        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked(); //true or false

        //2. Validate data
        if (TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Please enter the Product Title.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productDescription)){
            Toast.makeText(this, "Please enter the Product Description.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productCategory)){
            Toast.makeText(this, "Please choose the Product Category.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productQuantity)){
            Toast.makeText(this, "Please choose the Product Quantity.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this, "Please enter the Price.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (discountAvailable){
            //product is with discount
            discountPrice = discountedPriceEt.getText().toString().trim();
            discountNote = discountedNoteEt.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)){
                Toast.makeText(this, "Please enter the Discount Price.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(discountNote)){
                Toast.makeText(this, "Please enter the Discount Note.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            //product is without discount
            discountPrice = "0";
            discountNote = "";
        }

        addProduct();
    }

    private void addProduct() {
        //3. Add data to database
        mProgressDialog.setMessage("Adding Product. Please Wait.");
        mProgressDialog.show();

        final String timestamp = ""+System.currentTimeMillis();

        if (image_uri == null) {
            //upload without image

            //setup data to upload
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId", "" + timestamp);
            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDescription", "" + productDescription);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            hashMap.put("productIcon", ""); //no image, set empty
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("discountAvailable", "" + discountAvailable);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("uid", "" + mFirebaseAuth.getUid());

            //add to database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(mFirebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //added to database
                            mProgressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "Product Added Successfully!", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed added to database
                            mProgressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            //upload with image

            //first upload image to storage

            //name and path of image to be uploaded
            String filePathAndName = "product_images/" + "" + timestamp;

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
                                hashMap.put("productId", "" + timestamp);
                                hashMap.put("productTitle", "" + productTitle);
                                hashMap.put("productDescription", "" + productDescription);
                                hashMap.put("productCategory", "" + productCategory);
                                hashMap.put("productQuantity", "" + productQuantity);
                                hashMap.put("productIcon", "" + downloadImageUri);
                                hashMap.put("originalPrice", "" + originalPrice);
                                hashMap.put("discountPrice", "" + discountPrice);
                                hashMap.put("discountNote", "" + discountNote);
                                hashMap.put("discountAvailable", "" + discountAvailable);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("uid", "" + mFirebaseAuth.getUid());

                                //add to database
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(mFirebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //added to database
                                                mProgressDialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, "Product Added Successfully!", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed added to database
                                                mProgressDialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void clearData() {
        //clear data after uploading product
        titleEt.setText("");
        descriptionEt.setText("");
        categoryTv.setText("");
        quantityEt.setText("");
        priceEt.setText("");
        discountedPriceEt.setText("");
        discountedNoteEt.setText("");
        productIconIv.setImageResource(R.drawable.baseline_add_shopping_cart_primary);
        image_uri = null;
    }
    private void categoryDialog() {
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //get picked category
                        String category = Constants.productCategories[i];

                        //set picked category
                        categoryTv.setText(category);
                    }
                })
                .show();
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
                productIconIv.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image picked from camera

                productIconIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}