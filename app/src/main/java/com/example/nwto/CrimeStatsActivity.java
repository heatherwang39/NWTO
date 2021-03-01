package com.example.nwto;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class CrimeStatsActivity extends AppCompatActivity {
    private static final String TAG = "TAG: " + CrimeStatsActivity.class.getSimpleName();

    private TPSApi tpsApi;

    private double mUserLatitude;
    private double mUserLongitude;
    private int mUserRadius;

    private TextView mDummy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crimestats);

        // Initializes Layout Variables
        mDummy = (TextView) findViewById(R.id.crimestats_dummy); // Testing

        tpsApi = new TPSApi(mDummy);

        // Testing TODO: remove
        tpsApi.queryYTD(2, 43.787001, -79.417399, -1, 2021, 2, 25, 2021,3,1, null, null);
//        tpsApi.queryYTD(-1, -1, -1, 32, 2021, 2, 25, 2021,3,1, null, null);
//        tpsApi.queryYE(1, 43.762148, -79.410010, -1, -1, 2018, 3, 2, 2018,3,3, null, null);

    }
}
