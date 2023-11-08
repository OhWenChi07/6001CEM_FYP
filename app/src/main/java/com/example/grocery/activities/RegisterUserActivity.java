package com.example.grocery.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener {

    private ImageButton backBtn, gpsBtn;
    private EditText nameEt, phoneEt, countryEt, stateEt, cityEt, addressEt, emailEt, passwordEt,
            confirmPasswordEt;
    private Button registerBtn;
    private TextView registerSellerTv;

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 300;

    //permission arrays
    private String[] locationPermissions;
    private String[] storagePermissions;

    private double latitude, longitude;

    private LocationManager mLocationManager;

    private FirebaseAuth mFirebaseAuth;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        countryEt = findViewById(R.id.countryEt);
        stateEt = findViewById(R.id.stateEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        confirmPasswordEt = findViewById(R.id.confirmPasswordEt);
        registerBtn = findViewById(R.id.registerBtn);
        registerSellerTv = findViewById(R.id.registerSellerTv);

        //init permissions array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        mFirebaseAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please Wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //detect current location
                if (checkLocationPermission()) {
                    //already allowed
                    detectLocation();
                } else {
                    //not allowed, request
                    requestLocationPermission();
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //register user
                inputData();
            }
        });

        registerSellerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open register seller activity
                startActivity(new Intent(RegisterUserActivity.this, RegisterSellerActivity.class));
            }
        });
    }

    private String fullName, phoneNumber, country, state, city, address, email, password, confirmPassword;
    private void inputData(){
        //input data
        fullName = nameEt.getText().toString().trim();
        phoneNumber = phoneEt.getText().toString().trim();
        country = countryEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        city = cityEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();
        confirmPassword = confirmPasswordEt.getText().toString().trim();

        //validate data
        // 1. Full Name Validation
        if (TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Empty Full Name. Please insert again!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!fullName.matches("^[a-zA-Z]+$")) {
            Toast.makeText(this, "Username must contain alphabets only.", Toast.LENGTH_SHORT).show();
            return;
        }
        // 2. Phone Number Validation
        if (TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this, "Empty Phone Number. Please insert again!", Toast.LENGTH_SHORT).show();
            return;
        } else if (phoneNumber.length() < 10) {
            Toast.makeText(this, "Phone Number must be at least 10 digits.", Toast.LENGTH_SHORT).show();
            return;
        } else if (!phoneNumber.matches("^[0-9]+$")) {
            Toast.makeText(this, "Phone Number must contain only digits (0-9).", Toast.LENGTH_SHORT).show();
            return;
        }
        // 3. Country, State, City Validation
        if (latitude == 0.0 || longitude == 0.0){
            Toast.makeText(this, "Empty Field. Please click GPS button to detect location.", Toast.LENGTH_SHORT).show();
            return;
        }
        // 4. Email Address Validation
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Empty Email Address. Please insert again!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern. Please Try Again!", Toast.LENGTH_SHORT).show();
            return;
        }
        // 5. Password Validation
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Empty Password. Please insert again!", Toast.LENGTH_SHORT).show();
            return;
        } else if (password.length() < 8 || !containsUppercase(password)) {
            Toast.makeText(this, "Password must be at least 8 characters and contain at least one uppercase letter.", Toast.LENGTH_SHORT).show();
            return;
        }
        // 6. Confirm Password Validation
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Empty Confirm Password. Please insert again!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(confirmPassword)){
            Toast.makeText(this, "Password does not match. Please insert again!", Toast.LENGTH_SHORT).show();
            return;
        }

        createAccount();
    }

    // Function to check for at least one uppercase letter in the password
    private boolean containsUppercase(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    private void createAccount(){
        mProgressDialog.setMessage("Creating Account. Please Wait.");
        mProgressDialog.show();

        //create account
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account created
                        saverFirebaseData();
                        Toast.makeText(RegisterUserActivity.this, "Register an Account Successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed create account
                        mProgressDialog.dismiss();
                        Toast.makeText(RegisterUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saverFirebaseData(){
        mProgressDialog.setMessage("Saving Account Info. Please Wait");

        String timestamp = ""+System.currentTimeMillis();

        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + mFirebaseAuth.getUid());
        hashMap.put("email", "" + email);
        hashMap.put("name", "" + fullName);
        hashMap.put("phoneNumber", "" + phoneNumber);
        hashMap.put("country", "" + country);
        hashMap.put("state", "" + state);
        hashMap.put("city", "" + city);
        hashMap.put("address", "" + address);
        hashMap.put("latitude", "" + latitude);
        hashMap.put("longitude", "" + longitude);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("accountType", "User");
        hashMap.put("online", "true");

        //save to database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mFirebaseAuth.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //database updated
                        mProgressDialog.dismiss();
                        startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed update database
                        mProgressDialog.dismiss();
                        startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                        finish();
                    }
                });
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait", Toast.LENGTH_LONG).show();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private boolean checkLocationPermission(){

        boolean result = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }
    private void findAddress() {
        //find address, country, state, city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0); //complete address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set addresses
            countryEt.setText(country);
            stateEt.setText(state);
            cityEt.setText(city);
            addressEt.setText(address);
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    public void onProviderEnabled(String provider) {

    }
    public void onProviderDisabled(String provider) {
        //gps location disabled
        Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        //permission allowed
                        detectLocation();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "Location permission is necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}