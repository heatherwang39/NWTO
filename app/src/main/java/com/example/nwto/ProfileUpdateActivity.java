package com.example.nwto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nwto.adapter.PlaceAutoSuggestAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class ProfileUpdateActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser mCurrentUser;
    private FirebaseStorage storage;

    private static final String TAG = "Profile Update";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private TextView mTextFullName, mTextEmail, mTextRadius, mTextFrequency;
    private SeekBar mSeekBarRadius, mSeekBarFrequency;
    private ImageView mImageProfile, mImageCamera;
    private Button mButtonLogOut, mButtonSave, mButtonCancel;
    private AutoCompleteTextView mAutoCompleteAddress;
    private String mUID, mAddress, mPostalCode, mRadius, mFrequency, mProfilePic;
    private double mLatitude, mLongitude;
    private Bitmap mImageBitmap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        // Initialize Storage
        storage = FirebaseStorage.getInstance();

        mTextFullName = (TextView) findViewById(R.id.text_full_name);
        mTextEmail = (TextView) findViewById(R.id.text_email);
        mTextRadius = (TextView) findViewById(R.id.text_radius);
        mTextFrequency = (TextView) findViewById(R.id.text_frequency);

        mSeekBarRadius = (SeekBar) findViewById(R.id.seek_bar_radius);
        mSeekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRadius = String.valueOf(progress);
                mTextRadius.setText(mRadius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mSeekBarFrequency = (SeekBar) findViewById(R.id.seek_bar_frequency);
        mSeekBarFrequency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFrequency = String.valueOf(progress);
                mTextFrequency.setText(mFrequency);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        mAutoCompleteAddress = findViewById(R.id.auto_complete_address);
        mAutoCompleteAddress.setAdapter(new PlaceAutoSuggestAdapter(ProfileUpdateActivity.this,android.R.layout.simple_list_item_1));
        mAutoCompleteAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                convertAddress();
            }
        });

        mImageProfile = (ImageView) findViewById(R.id.image_profile);
        mImageCamera = (ImageView) findViewById(R.id.image_camera);
        mImageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        mButtonLogOut = (Button) findViewById(R.id.button_log_out);
        mButtonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Sign out and then go to login page
                    mAuth.signOut();
                    startActivity(new Intent(ProfileUpdateActivity.this, MainActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mButtonSave = (Button) findViewById(R.id.button_save);
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUpdates();
                Log.d(TAG,"SAVE");
            }
        });

        mButtonCancel = (Button) findViewById(R.id.button_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(ProfileUpdateActivity.this, MainActivity.class));
        }else{
            mCurrentUser = mAuth.getCurrentUser();
            mUID = mAuth.getCurrentUser().getUid();
            loadProfile();
        }
    }


    //Load the Profile first, so the user can only change some of the attributes
    private void loadProfile() {
        DocumentReference documentReference = db.collection("users").document(mUID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(ProfileUpdateActivity.this,"Error while loading", Toast.LENGTH_SHORT);
                    Log.d(TAG,"-->"+e.toString());
                    return;
                }

                if(documentSnapshot.exists()){
                    String fullName = documentSnapshot.getString("fullName");
                    mTextFullName.setText(fullName);
                    String email = documentSnapshot.getString("email");
                    mTextEmail.setText(email);
                    //Initialize the mAddress, mPostalCode, mRadius, mFrequency, mProfilePic variable (they can be updated later)
                    mAddress = documentSnapshot.getString("address");
                    mAutoCompleteAddress.setHint(mAddress);
                    mPostalCode = documentSnapshot.getString("postalCode");

                    List<Double> coordinates = (ArrayList<Double>) documentSnapshot.get("coordinates");
                    mLatitude = coordinates.get(0);
                    mLongitude = coordinates.get(1);

                    mRadius = documentSnapshot.getString("radius");
                    mTextRadius.setText(mRadius);
                    mSeekBarRadius.setProgress(Integer.parseInt(mRadius));

                    //TODO: change to set Text, and change the check empty after click save
                    mFrequency = documentSnapshot.getString("frequency");
                    mTextFrequency.setText(mFrequency);
                    mSeekBarFrequency.setProgress(Integer.parseInt(mFrequency));

                    mProfilePic = documentSnapshot.getString("displayPicPath");
                    Glide.with(ProfileUpdateActivity.this).load(mProfilePic).into(mImageProfile);

                    Log.i(TAG,"Load profile successfully"+fullName+" "+mUID);
                }
            }
        });
    }

    private void saveUpdates() {
        //check whether user did modified
        if(mImageBitmap != null) upload(mImageBitmap);

        DocumentReference documentReference = db.collection("users").document(mUID);
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("address", mAddress);
        userUpdate.put("postalCode",mPostalCode);
        userUpdate.put("coordinates", Arrays.asList(mLatitude,mLongitude));
        userUpdate.put("radius", mRadius);
        userUpdate.put("frequency",mFrequency);

        documentReference.set(userUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Profile has been updated for user " + mUID);
                startActivity(new Intent(ProfileUpdateActivity.this, ProfileActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileUpdateActivity.this, "Failed in updating profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mImageBitmap = (Bitmap) extras.get("data");
            mImageProfile.setImageBitmap(mImageBitmap);
        } else {
            Log.i(TAG, "takePictureIntent onActivityResult: RESULT CANCELLED");
            Toast.makeText(ProfileUpdateActivity.this, "Take Picture Cancelled.",Toast.LENGTH_SHORT).show();
        }
    }

    private void upload(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        StorageReference reference = storage.getReference().child("profileImages").child(mUID+".jpeg");

        reference.putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess: upload profile pic to storage"+uri);
                        updateProfilePic(uri);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "OnFailure: upload profile pic to storage", e.getCause());
            }
        });
    }

    private void updateProfilePic(Uri uri) {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
        mCurrentUser.updateProfile(request).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileUpdateActivity.this, "Image Updated!", Toast.LENGTH_SHORT).show();
                String profilePic = mCurrentUser.getPhotoUrl().toString();
                DocumentReference documentReference = db.collection("users").document(mUID);
                Map<String, Object> userUpdate = new HashMap<>();
                userUpdate.put("displayPicPath", profilePic);
                documentReference.set(userUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Profile Pic has been updated for user " + mUID);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileUpdateActivity.this, "Failed in uploading profile pic.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void convertAddress() {
        mAddress = mAutoCompleteAddress.getText().toString();
        Log.d("Address : ",mAddress);
        LatLng latLng = getLatLngFromAddress(mAddress);
        if(latLng!=null) {
            Log.d("Lat Lng : ", " " + latLng.latitude + " " + latLng.longitude);
            mLatitude = latLng.latitude;
            mLongitude = latLng.longitude;
            Address address = getAddressFromLatLng(latLng);
            if(address!=null) {
                mPostalCode = address.getPostalCode();
                Log.d("Pin Code : ",""+mPostalCode);
            }
            else {
                Log.d("Address","Address Not Found");
            }
        }
        else {
            Log.d("Lat Lng","Lat Lng Not Found");
        }
    }

    private LatLng getLatLngFromAddress(String address){

        Geocoder geocoder=new Geocoder(ProfileUpdateActivity.this);
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(address, 1);
            if(addressList!=null){
                Address singleAddress=addressList.get(0);
                LatLng latLng = new LatLng(singleAddress.getLatitude(),singleAddress.getLongitude());
                return latLng;
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    private Address getAddressFromLatLng(LatLng latLng){
        Geocoder geocoder=new Geocoder(ProfileUpdateActivity.this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
            if(addresses!=null){
                Address address=addresses.get(0);
                return address;
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}