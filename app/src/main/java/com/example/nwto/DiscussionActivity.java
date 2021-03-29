package com.example.nwto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.nwto.fragment.DiscussionNeighbourhoodFragment;
import com.example.nwto.fragment.DiscussionPostFragment;
import com.example.nwto.fragment.DiscussionTipsFragment;
import com.example.nwto.fragment.DiscussionTorontoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class DiscussionActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    public static String uID, fullName, profilePic, neighbourhoodName;
    public static boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            uID = mAuth.getCurrentUser().getUid();
            loadProfile();
        }

        // Display the neighbourhood page when first open the discussion page
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new DiscussionTorontoFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_toronto:
                            selectedFragment = new DiscussionTorontoFragment();
                            break;
                        case R.id.nav_neighbours:
                            selectedFragment = new DiscussionNeighbourhoodFragment();
                            break;
                        case R.id.nav_tips:
                            selectedFragment = new DiscussionTipsFragment();
                            break;
                        case R.id.nav_post:
                            selectedFragment = new DiscussionPostFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.button_log_out) {
            try {
                // Sign out and then go to login page
                mAuth.signOut();
                startActivity(new Intent(DiscussionActivity.this, LoginActivity.class));
                Toast.makeText(this, "Logged Out!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadProfile() {
        //get user info, including fullName, profilePic
        DocumentReference documentReference = db.collection("users").document(uID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d("Discussion", "-->" + e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    fullName = documentSnapshot.getString("fullName");
                    profilePic = documentSnapshot.getString("displayPicPath");
                    neighbourhoodName = documentSnapshot.getString("neighbourhood");
                    isAdmin = documentSnapshot.getBoolean("isAdmin");
                    Log.d("Post:", neighbourhoodName);
                }
            }
        });
    }
}