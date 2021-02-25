package com.example.nwto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class NavigationActivity extends AppCompatActivity {

    private ImageView mImageNavProfile, mImageNavNeighbours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

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
    }
}