package com.example.nwto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

public class NavigationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private ImageView mImageNavProfile, mImageNavNeighbours, mImageNavDiscussion, mImageNavResources, mImageNavCrimeStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        mImageNavProfile = (ImageView) findViewById(R.id.image_nav_profile);
        mImageNavProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NavigationActivity.this, ProfileActivity.class));
            }
        });

        mImageNavNeighbours = (ImageView) findViewById(R.id.image_nav_neighbours);
        mImageNavNeighbours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NavigationActivity.this, NeighboursActivity.class));
            }
        });

        mImageNavDiscussion = (ImageView) findViewById(R.id.image_nav_discussion);
        mImageNavDiscussion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NavigationActivity.this, DiscussionActivity.class));
            }
        });

        mImageNavResources = (ImageView) findViewById(R.id.image_nav_resources);
        mImageNavResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NavigationActivity.this, ContactsActivity.class));
            }
        });

        mImageNavCrimeStats = (ImageView) findViewById(R.id.image_nav_crimestats);
        mImageNavCrimeStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NavigationActivity.this, CrimeStatsActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}