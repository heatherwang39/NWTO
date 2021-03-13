package com.example.nwto;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.nwto.adapter.CrimeAdapter;
import com.example.nwto.api.TPSApi;
import com.example.nwto.fragment.CrimeFilterDialog;
import com.example.nwto.fragment.CrimeMapFragment;
import com.example.nwto.fragment.CrimeRecentEventsFragment;
import com.example.nwto.fragment.CrimeStatsFragment;
import com.example.nwto.model.Crime;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.util.GeoPoint;

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

    private double mUserLatitude, mUserLongitude;
    private int mUserFrequency, mUserRadius, mDivisionNumb;
    private String mPremiseType, mCrimeType;
    private boolean mFilterByLocation;

    private CrimeAdapter mCrimeAdapter;
    private List<Crime> mCrimes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crimestats);

        // Initializes Firebase Variables
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFireStore = FirebaseFirestore.getInstance();

        // Initializes bottom navigation view
        BottomNavigationView navigationView = findViewById(R.id.crimestats_navigationView);
        navigationView.setOnNavigationItemSelectedListener(new NavigationListener());
        navigationView.setSelectedItemId(R.id.nav_crime_recent_events);

        // Initializes variables for Filter page
        mDivisionNumb = -1;
        mPremiseType = null;
        mCrimeType = null;
        mFilterByLocation = true;

        // Initializes Crimes Adapter
        mCrimes = new ArrayList<>();
        mCrimeAdapter = new CrimeAdapter(this, mCrimes);
        tpsApi = new TPSApi(mCrimes, mCrimeAdapter);

        getUserInfo();
    }

    private class NavigationListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()){
                case R.id.nav_crime_recent_events:
                    selectedFragment = new CrimeRecentEventsFragment();
                    break;
                case R.id.nav_crime_map:
                    selectedFragment = new CrimeMapFragment();
                    break;
                case R.id.nav_crime_stats:
                    selectedFragment = new CrimeStatsFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.crimestats_frameLayout, selectedFragment).commit();
            return true;
        }
    }

    public GeoPoint getStartingPoint() {
        if (!mFilterByLocation && mCrimes.size() != 0) return new GeoPoint(mCrimes.get(0).getLatitude(), mCrimes.get(0).getLongitude());
        else return new GeoPoint(mUserLatitude, mUserLongitude);
    }

    public List<Crime> getCrimes() {
        return mCrimes;
    }

    public CrimeAdapter getCrimeAdapter() {
        return mCrimeAdapter;
    }

    public void openFilterDialog() {
        // TODO: remove
        CrimeFilterDialog.display(getSupportFragmentManager());
    }

    public Map<String, Object> getFilterParams() {
        // sends filter parameters to CrimeStatsFilterDialog
        Map<String, Object> data = new HashMap<>();

        data.put(getResources().getString(R.string.crimefilter_radius), mUserRadius);
        data.put(getResources().getString(R.string.crimefilter_frequency), mUserFrequency);
        data.put(getResources().getString(R.string.crimefilter_filterByLocation), mFilterByLocation);
        data.put(getResources().getString(R.string.crimefilter_divisionNumber), mDivisionNumb);
        data.put(getResources().getString(R.string.crimefilter_premiseType), mPremiseType);
        data.put(getResources().getString(R.string.crimefilter_crimeType), mCrimeType);
        return data;
    }

    public void setFilterParams(int userRadius, int userFrequency, boolean FilterByLocation, int division, String premiseType, String crimeType) {
        // receives filter parameters from CrimeStatsFilterDialog
        mUserRadius = userRadius;
        mUserFrequency = userFrequency;
        mFilterByLocation = FilterByLocation;
        mDivisionNumb = division;
        mPremiseType = premiseType;
        mCrimeType = crimeType;

        getRecentCrimes();
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
        // calculates start date and end date
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

        // queries YTD server and outputs the results
        if (mFilterByLocation)
            tpsApi.queryYTD(mUserRadius, mUserLatitude, mUserLongitude, -1, startYear, startMonth, startDay, endYear, endMonth, endDay, mPremiseType, mCrimeType);
        else
            tpsApi.queryYTD(-1, -1, -1, mDivisionNumb, startYear, startMonth, startDay, endYear, endMonth, endDay, mPremiseType, mCrimeType);
        // tpsApi.queryYTD(-1, -1, -1, 32, 2021, 2, 25, 2021,3,1, null, null);
        // tpsApi.queryYE(1, 43.762148, -79.410010, -1, -1, 2018, 3, 2, 2018,3,3, null, null);
    }
}
