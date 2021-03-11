package com.example.nwto;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.adapter.CrimeAdapter;
import com.example.nwto.api.TPSApi;
import com.example.nwto.fragment.CrimeStatsFilterDialog;
import com.example.nwto.model.Crime;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrimeStatsActivity extends AppCompatActivity {
    private static final String TAG = "TAG: " + CrimeStatsActivity.class.getSimpleName();

    private TPSApi tpsApi;

    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;

    private double mUserLatitude;
    private double mUserLongitude;
    private int mUserFrequency;
    private int mUserRadius;

    private CrimeAdapter mCrimeAdapter;
    private List<Crime> mCrimes;

    private MaterialButton mFilterButton;
    private RecyclerView mCrimeRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crimestats);

        // Initializes Firebase Variables
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFireStore = FirebaseFirestore.getInstance();

        // Initializes Layout Variables
        mFilterButton = (MaterialButton) findViewById(R.id.crimestats_filter_button);
        mCrimeRecyclerView = (RecyclerView) findViewById(R.id.crimestats_crimes_recyclerView);

        mFilterButton.setOnClickListener(view -> openFilterDialog());

        // Initializes Crimes Adapter
        mCrimes = new ArrayList<>();
        mCrimeAdapter = new CrimeAdapter(this, mCrimes);
        mCrimeRecyclerView.setAdapter(mCrimeAdapter);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tpsApi = new TPSApi(mCrimes, mCrimeAdapter);

        getUserInfo();
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("radius", mUserRadius);
        data.put("frequency", mUserFrequency);

        return data;
    }

    private void openFilterDialog() {
        CrimeStatsFilterDialog.display(getSupportFragmentManager());
    }

    private void getUserInfo() {
        String collectionName = getResources().getString(R.string.firestore_collection_users);
        String documentID = mUser.getUid();
        String documentField_coordinates = getResources().getString(R.string.firestore_users_coordinates);
        String documentField_frequency = getResources().getString(R.string.firestore_users_frequency);
        String documentField_radius = getResources().getString(R.string.firestore_users_radius);

        // reads the User's location and postal code
        mFireStore.collection(collectionName).document(documentID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<Double> coordinates = (ArrayList<Double>) document.get(documentField_coordinates);
                                String frequency = (String) document.get(documentField_frequency);
                                String radius = (String) document.get(documentField_radius);
                                mUserLatitude = coordinates.get(0);
                                mUserLongitude = coordinates.get(1);
                                mUserFrequency =  Integer.parseInt(frequency);
                                mUserRadius = Integer.parseInt(radius);
                            }
                            Log.d(TAG, "getUserLocation: onComplete -> Success=" + "Lat:" + mUserLatitude + ", Long:" + mUserLongitude + ", Frequency:" + mUserFrequency + ", Radius:"+ mUserRadius);
                            getRecentCrimes(); // reads and updates the crime info
                        }
                        else Log.e(TAG, "getUserLocation: onComplete -> Fail", task.getException());
                    }
                });
    }

    private void getRecentCrimes() {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        calendar.add(Calendar.DATE, -mUserFrequency);
        Date oldDate = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String endDate = dateFormat.format(currentDate);
        String startDate = dateFormat.format(oldDate);
        Log.d(TAG, "getCrimeStats -> currentDate=" + endDate + ", oldDate=" + startDate);

        int endMonth = Integer.parseInt(endDate.substring(0, 2));
        int endDay = Integer.parseInt(endDate.substring(3, 5));
        int endYear = Integer.parseInt(endDate.substring(6));

        int startMonth = Integer.parseInt(startDate.substring(0, 2));
        int startDay = Integer.parseInt(startDate.substring(3, 5));
        int startYear = Integer.parseInt(startDate.substring(6));

        tpsApi.queryYTD(mUserRadius, mUserLatitude, mUserLongitude, -1, startYear, startMonth, startDay, endYear, endMonth, endDay, null, null);
//        tpsApi.queryYTD(-1, -1, -1, 32, 2021, 2, 25, 2021,3,1, null, null);
//        tpsApi.queryYE(1, 43.762148, -79.410010, -1, -1, 2018, 3, 2, 2018,3,3, null, null);
    }

}
