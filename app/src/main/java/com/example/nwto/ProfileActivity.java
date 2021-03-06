package com.example.nwto;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import javax.annotation.Nullable;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "Profile";

    private TextView mTextFullName, mTextEmail, mTextAddress, mTextRadius, mTextFrequency, mTextPhoneNumber,mTextNeighbourhood;
    private ImageView mImageProfile;
    private String mUID;
    private Button mButtonEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);// set drawable home icon

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        mTextFullName = (TextView) findViewById(R.id.text_full_name);
        mTextEmail = (TextView) findViewById(R.id.text_email);
        mTextPhoneNumber = (TextView) findViewById(R.id.text_phone_number);
        mTextAddress = (TextView) findViewById(R.id.text_address);
        mTextNeighbourhood = (TextView) findViewById(R.id.text_neighbourhood);
        mTextRadius = (TextView) findViewById(R.id.text_radius);
        mTextFrequency = (TextView) findViewById(R.id.text_frequency);
        mImageProfile = (ImageView) findViewById(R.id.image_profile);

        mButtonEdit = (Button) findViewById(R.id.button_edit);
        mButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ProfileUpdateActivity.class));
                Log.d(TAG, "Editing Profile.");
            }
        });

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        } else {
            mUID = mAuth.getCurrentUser().getUid();
            loadProfile();
        }
    }


    private void loadProfile() {
        DocumentReference documentReference = db.collection("users").document(mUID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(ProfileActivity.this, "Error while loading", Toast.LENGTH_SHORT);
                    Log.d(TAG, "-->" + e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("fullName");
                    mTextFullName.setText(fullName);
                    String email = documentSnapshot.getString("email");
                    mTextEmail.setText(email);
                    String phoneNumber = documentSnapshot.getString("phoneNumber");
                    mTextPhoneNumber.setText(phoneNumber);
                    String address = documentSnapshot.getString("address");
                    mTextAddress.setText(address);
                    String neighbourhoodName = documentSnapshot.getString("neighbourhood");
                    mTextNeighbourhood.setText(neighbourhoodName);
                    String radius = documentSnapshot.getString("radius");
                    mTextRadius.setText(radius);
                    String frequency = documentSnapshot.getString("frequency");
                    mTextFrequency.setText(frequency);

                    String profilePic = documentSnapshot.getString("displayPicPath");
                    if(profilePic!=null){
                        Glide.with(ProfileActivity.this).load(profilePic).into(mImageProfile);
                    }

                    Log.i(TAG, "Show profile successfully" + fullName + " " + mUID);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        // hide search and add new button
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_log_out:
                try {
                    // Sign out and then go to login page
                    mAuth.signOut();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    Toast.makeText(this, "Logged Out!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}