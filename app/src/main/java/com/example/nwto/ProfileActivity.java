package com.example.nwto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import javax.annotation.Nullable;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private static final String TAG = "Profile";

    private TextView mTextFullName, mTextEmail, mTextAddress, mTextRadius, mTextFrequency;
    private ImageView mImageProfile;
    private String mUID;
    private Button mButtonLogOut, mButtonEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mUID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        // Initialize Storage
        storage = FirebaseStorage.getInstance();

        mTextFullName = (TextView) findViewById(R.id.text_full_name);
        mTextEmail = (TextView) findViewById(R.id.text_email);
        mTextAddress = (TextView) findViewById(R.id.text_address);
        mTextRadius = (TextView) findViewById(R.id.text_radius);
        mTextFrequency = (TextView) findViewById(R.id.text_frequency);
        mImageProfile = (ImageView) findViewById(R.id.image_profile);

        mButtonLogOut = (Button) findViewById(R.id.button_log_out);
        mButtonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Sign out and then go to login page
                    mAuth.signOut();
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mButtonEdit = (Button) findViewById(R.id.button_edit);
        mButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"EDIT");
            }
        });

        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        }else{
            mUID = mAuth.getCurrentUser().getUid();
            showProfile();
        }
    }


    private void showProfile() {
        DocumentReference documentReference = db.collection("users").document(mUID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(ProfileActivity.this,"Error while loading", Toast.LENGTH_SHORT);
                    Log.d(TAG,"-->"+e.toString());
                    return;
                }

                if(documentSnapshot.exists()){
                    String fullName = documentSnapshot.getString("fullName");
                    mTextFullName.setText(fullName);
                    String email = documentSnapshot.getString("email");
                    mTextEmail.setText(email);
                    String address = documentSnapshot.getString("address");
                    mTextAddress.setText(address);
                    String radius = documentSnapshot.getString("radius");
                    mTextFullName.setText(radius);
                    String frequency = documentSnapshot.getString("frequency");
                    mTextFrequency.setText(radius);

                    String profilePic = documentSnapshot.getString("displayPicPath");
                    Glide.with(ProfileActivity.this).load(profilePic).into(mImageProfile);

                    Log.i("Profile","Show profile successfully"+fullName+" "+mUID);
                }
            }
        });
    }

}