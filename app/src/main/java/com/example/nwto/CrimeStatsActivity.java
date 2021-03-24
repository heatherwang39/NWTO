package com.example.nwto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.nwto.adapter.CrimeAdapter;
import com.example.nwto.adapter.TableAdapter;
import com.example.nwto.api.CrimeApi;
import com.example.nwto.api.ResourceApi;
import com.example.nwto.fragment.CrimeMapFragment;
import com.example.nwto.fragment.CrimeRecentEventsFragment;
import com.example.nwto.fragment.CrimeStatsFragment;
import com.example.nwto.model.Crime;
import com.example.nwto.model.TableBox;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CrimeStatsActivity extends AppCompatActivity {
    private static final String TAG = "TAG: " + CrimeStatsActivity.class.getSimpleName();
    private int colorWhite, colorGreen, colorYellow, colorRed, colorBlack, colorAccent;
    public static final String[] STUB = new String[]{"Assault", "Auto Theft", "Break and Enter", "Robbery", "Theft Over"}; // YE crime types
    public static final int NUMB_COL_MODE1 = 4;
    public static final int[] COLUMN_MODE2 = new int[]{2018, 2019, 2020}; // years
    public static final int NUMB_COL_MODE2 = COLUMN_MODE2.length + 1;

    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;

    private double mUserLatitude, mUserLongitude;
    private int mUserFrequency, mUserRadius, mDivisionNumb;
    private String mPremiseType, mCrimeType;
    private boolean mFilterByLocation;

    private CrimeAdapter mCrimeAdapter;
    private List<Crime> mCrimes;
    private List<List<GeoPoint>> mPoliceBoundaries;
    private List<String> mPoliceDivisionNames;

    private List<TableBox> mTable_mode1, mTable_mode2;
    private TableAdapter mTableAdapter_mode1, mTableAdapter_mode2;

    BottomNavigationView navigationView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crimestats);

        mProgressBar = (ProgressBar) findViewById(R.id.crimestats_progressBar);

        // Initializes Firebase Variables
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFireStore = FirebaseFirestore.getInstance();

        // Initializes bottom navigation view
        navigationView = findViewById(R.id.crimestats_navigationView);
        navigationView.setOnNavigationItemSelectedListener(new NavigationListener());
        // navigationView.setSelectedItemId(R.id.nav_crime_recent_events);

        // Initializes variables for Filter page
        mDivisionNumb = -1;
        mPremiseType = null;
        mCrimeType = null;
        mFilterByLocation = true;

        // Initializes Crimes Adapter for Recent Crimes page
        mCrimes = new ArrayList<>();
        mCrimeAdapter = new CrimeAdapter(this, mCrimes);

        // Initializes Division Boundary info variables for Crime Map page
        mPoliceBoundaries = new ArrayList<>();
        mPoliceDivisionNames = new ArrayList<>();

        // Initializes for Crime Stats page
        mTable_mode1 = new ArrayList<>();
        mTableAdapter_mode1 = new TableAdapter(this, mTable_mode1);
        mTable_mode2 = new ArrayList<>();
        mTableAdapter_mode2 = new TableAdapter(this, mTable_mode2);

        // Go to Login page if not logged in
        if (mUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            initializeColor();
            readUserInfo(); // for Recent Crimes Fragment
            readPoliceBoundaries(); // for Crime Map Fragment
//        readStats_Testing(); // for Testing
        }

    }

    private class NavigationListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.nav_crime_recent_events:
                    selectedFragment = new CrimeRecentEventsFragment();
                    break;
                case R.id.nav_crime_map:
                    selectedFragment = new CrimeMapFragment();
                    break;
                case R.id.nav_crime_comparisons:
                    selectedFragment = new CrimeStatsFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.crimestats_frameLayout, selectedFragment).commit();
            return true;
        }
    }

    // GETTERS //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public GeoPoint getStartingPoint() {
        if (!mFilterByLocation && mCrimes.size() != 0)
            return new GeoPoint(mCrimes.get(0).getLatitude(), mCrimes.get(0).getLongitude());
        else return new GeoPoint(mUserLatitude, mUserLongitude);
    }

    public GeoPoint getUserLocation() {
        return new GeoPoint(mUserLatitude, mUserLongitude);
    }

    public List<Crime> getCrimes() {
        return mCrimes;
    }

    public CrimeAdapter getCrimeAdapter() {
        return mCrimeAdapter;
    }

    public List<List<GeoPoint>> getPoliceBoundaries() {
        return mPoliceBoundaries;
    }

    public List<String> getPoliceDivisionNames() {
        return mPoliceDivisionNames;
    }

    public TableAdapter getTableAdapter_mode1() {
        return mTableAdapter_mode1;
    }

    public TableAdapter getTableAdapter_mode2() {
        return mTableAdapter_mode2;
    }

    public List<TableBox> getTable_mode1() {
        return mTable_mode1;
    }

    public List<TableBox> getTable_mode2() {
        return mTable_mode2;
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

        readRecentCrimes();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void readUserInfo() {
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
                                mUserFrequency = Integer.parseInt(frequency);
                                mUserRadius = Integer.parseInt(radius);
                            }
                            Log.d(TAG, "readUserInfo: onComplete -> Success=" + "Lat:" + mUserLatitude + ", Long:" + mUserLongitude + ", Frequency:" + mUserFrequency + ", Radius:" + mUserRadius);
                            readRecentCrimes(); // reads and updates the recent crime info
                            readStatsMode1(STUB); // reads and updates for Crime Stats table mode 1
                            readStatsMode2(STUB, COLUMN_MODE2);
                        } else Log.e(TAG, "readUserInfo: onComplete -> Fail", task.getException());
                    }
                });
    }

    private void readRecentCrimes() {
        // calculates start date and end date
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        calendar.add(Calendar.DATE, -mUserFrequency);
        Date oldDate = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String endDate = dateFormat.format(currentDate);
        String startDate = dateFormat.format(oldDate);
        Log.d(TAG, "readRecentCrimes -> currentDate=" + endDate + ", oldDate=" + startDate);

        int endMonth = Integer.parseInt(endDate.substring(0, 2));
        int endDay = Integer.parseInt(endDate.substring(3, 5));
        int endYear = Integer.parseInt(endDate.substring(6));

        int startMonth = Integer.parseInt(startDate.substring(0, 2));
        int startDay = Integer.parseInt(startDate.substring(3, 5));
        int startYear = Integer.parseInt(startDate.substring(6));

        // queries YTD server and updates the Recent Crimes Fragment page
        mCrimes.clear();
        if (mFilterByLocation) { // search by the User's location
            new CrimeApi() {
                @Override
                public void processCrimes_YTD(List<Crime> crimes) {
                    if (crimes.size() == 0)
                        mCrimes.add(new Crime(0, "No Results Found", "", "", "", "", 0, 0));
                    else
                        mCrimes.addAll(crimes);
                    mCrimeAdapter.notifyDataSetChanged();
                }
            }.queryYTD(mUserRadius, mUserLatitude, mUserLongitude, -1, startYear, startMonth, startDay, endYear, endMonth, endDay, mPremiseType, mCrimeType);
        } else { // search by the selected Police Division Number
            new CrimeApi() {
                @Override
                public void processCrimes_YTD(List<Crime> crimes) {
                    if (crimes.size() == 0)
                        mCrimes.add(new Crime(0, "No Results Found", "", "", "", "", 0, 0));
                    else
                        mCrimes.addAll(crimes);
                    mCrimeAdapter.notifyDataSetChanged();
                }
            }.queryYTD(-1, -1, -1, mDivisionNumb, startYear, startMonth, startDay, endYear, endMonth, endDay, mPremiseType, mCrimeType);
        }

        // tpsApi.queryYTD(-1, -1, -1, 32, 2021, 2, 25, 2021,3,1, null, null);
        // tpsApi.queryYE(1, 43.762148, -79.410010, -1, -1, 2018, 3, 2, 2018,3,3, null, null);
    }

    private void readPoliceBoundaries() {
        new ResourceApi() {
            @Override
            public void processPoliceDivisionBoundaries(List<Record> records) {
                for (Record record : records) {
                    List<List<Double>> polygonCoordinates = record.getCoordinates();
                    List<GeoPoint> geoPoints = new ArrayList<>();
                    for (List<Double> coordinates : polygonCoordinates) {
                        GeoPoint geoPoint = new GeoPoint(coordinates.get(1), coordinates.get(0));
                        geoPoints.add(geoPoint);
                    }
                    mPoliceBoundaries.add(geoPoints);
                    mPoliceDivisionNames.add(record.getAreaName());
                }
            }
        }.getMappingResource(mUserLatitude, mUserLongitude, 4);
    }

    private void readStatsMode1(String[] crimeTypes_YE) {
        // YTD_CRIME={Assault, Auto Theft, Break and Enter, Homicide, Robbery, Sexual Violation, Shooting, Theft Over}
        // String[] crimeTypes_YE = new String[] {"Assault", "Auto Theft", "Break and Enter", "Robbery", "Theft Over"}; // YE crime types
        String[] header = new String[]{"2018-20 Average", "Growth", "Last Month"}; // excluding (0, 0)

        int[] lmPeriod = getLastMonthPeriod();
        int[] avgPeriod = new int[]{2018, 1, 1, 2020, 12, 31};
        int duration = (avgPeriod[3] - avgPeriod[0] + 1) * 12;
        int totalNumbTableBoxes = NUMB_COL_MODE1 * (crimeTypes_YE.length + 1);
        mTable_mode1.clear();

        // Row 0, Col 0: Table's First Entry
        mTable_mode1.add(new TableBox(0, "Types", colorWhite));

        // Col 0 & 2
        for (int i = 0; i < crimeTypes_YE.length; i++) {
            mTable_mode1.add(new TableBox((i + 1) * NUMB_COL_MODE1, crimeTypes_YE[i], colorWhite)); // Col 0: Table Stub
            mTable_mode1.add(new TableBox((2 + NUMB_COL_MODE1 * (i + 1)), "", colorWhite)); // Col 2: place holder for Growth column
        }

        // Row 0: Table header
        for (int i = 0; i < header.length; i++)
            mTable_mode1.add(new TableBox(i + 1, header[i], colorWhite));

        if (mFilterByLocation) {
            // Col 1: 2018-2019 Monthly Average for each crime type
            new CrimeApi() {
                @Override
                public void processCrimes_YE(List<Crime> crimes) {
                    // finds crime counts of 2018-2019 per crime type
                    int[] crimeCounts = new int[crimeTypes_YE.length];
                    for (Crime crime : crimes) {
                        String type = crime.getCategory();
                        for (int i = 0; i < crimeTypes_YE.length; i++)
                            if (type.equals(crimeTypes_YE[i])) crimeCounts[i]++;
                    }

                    // calculates average monthly crime counts
                    int colIndex = 1;
                    for (int j = 0; j < crimeCounts.length; j++) {
                        double monthlyAvg = crimeCounts[j] / ((double) duration);
                        mTable_mode1.add(new TableBox(colIndex + NUMB_COL_MODE1 * (j + 1), String.format("%.1f", monthlyAvg), colorWhite));
                    }

                    if (mTable_mode1.size() == totalNumbTableBoxes)
                        calculateGrowth(crimeTypes_YE.length);
                }
            }.queryYE(mUserRadius, mUserLatitude, mUserLongitude, -1, -1,
                    avgPeriod[0], avgPeriod[1], avgPeriod[2], avgPeriod[3], avgPeriod[4], avgPeriod[5], null, null);

            // Col 3: Last Month count for each crime type TODO: handle January's last month
            new CrimeApi() {
                @Override
                public void processCrimes_YTD(List<Crime> crimes) {
                    // finds crime counts of Last Month per crime type
                    int[] crimeCounts = new int[crimeTypes_YE.length];
                    for (Crime crime : crimes) {
                        String type = crime.getCategory();
                        for (int i = 0; i < crimeTypes_YE.length; i++)
                            if (type.equals(crimeTypes_YE[i])) crimeCounts[i]++;
                    }

                    int colIndex = 3;
                    for (int j = 0; j < crimeCounts.length; j++)
                        mTable_mode1.add(new TableBox(colIndex + NUMB_COL_MODE1 * (j + 1), Integer.toString(crimeCounts[j]), colorWhite));

                    if (mTable_mode1.size() == totalNumbTableBoxes)
                        calculateGrowth(crimeTypes_YE.length);
                }
            }.queryYTD(mUserRadius, mUserLatitude, mUserLongitude, -1,
                    lmPeriod[0], lmPeriod[1], lmPeriod[2], lmPeriod[3], lmPeriod[4], lmPeriod[5], null, null);
        } else {
            // case for filtering by division
            // include this method in setFilterParams() to update as the User changes filter params
        }
    }

    private void readStatsMode2(String[] crimeTypes_YE, int[] years) {
        // String[] crimeTypes_YE = new String[] {"Assault", "Auto Theft", "Break and Enter", "Robbery", "Theft Over"}; // YE crime types
        // int[] years = new int[] {2017, 2018, 2019};
        int duration = 12;
        mTable_mode2.clear();

        // Row 0, Col 0: Table's First Entry
        mTable_mode2.add(new TableBox(0, "Types", colorWhite));

        // Col 0: Table Stub
        for (int i = 0; i < crimeTypes_YE.length; i++)
            mTable_mode2.add(new TableBox((i + 1) * NUMB_COL_MODE2, crimeTypes_YE[i], colorWhite));

        if (mFilterByLocation) {
            // Row 0: Table Header (Year)
            for (int i = 0; i < years.length; i++) {
                int colIndex = i + 1;
                int year = years[i];
                mTable_mode2.add(new TableBox(colIndex, Integer.toString(year) + " AVG", colorWhite));
            }

            // Col 2 to End: monthly average for each crime type per Year
            new CrimeApi() {
                @Override
                public void processCrimes_YE(List<Crime> crimes) {
                    // finds crime counts per year per crime type
                    int[][] crimeCounts = new int[years.length][crimeTypes_YE.length];
                    for (Crime crime : crimes) {
                        int year = Integer.parseInt(crime.getDate().substring(6)); // "MM-dd-yyyy"
                        String type = crime.getCategory();
                        for (int i = 0; i < years.length; i++) {
                            if (year == years[i]) {
                                for (int j = 0; j < crimeTypes_YE.length; j++)
                                    if (type.equals(crimeTypes_YE[j])) crimeCounts[i][j]++;
                            }
                        }
                    }

                    // finds monthly average of crime counts per year per crime type
                    for (int i = 0; i < crimeCounts.length; i++) {
                        for (int j = 0; j < crimeCounts[0].length; j++) {
                            double monthlyAvg = crimeCounts[i][j] / ((double) duration);
                            int colIndex = i + 1;
                            int rowIndex = j + 1;
                            mTable_mode2.add(new TableBox(colIndex + NUMB_COL_MODE2 * (rowIndex), String.format("%.1f", monthlyAvg), colorWhite));
                        }
                    }

                    Collections.sort(mTable_mode2);
                    mTableAdapter_mode2.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.GONE); // finishes background tasks and brings the Recent Crime Fragment page to front
                    navigationView.setSelectedItemId(R.id.nav_crime_recent_events);
                }
            }.queryYE(mUserRadius, mUserLatitude, mUserLongitude, -1, -1,
                    years[0], 1, 1, years[years.length - 1], 12, 31, null, null);
        } else {
            // case for filtering by division
            // include this method in setFilterParams() to update as the User changes filter params
        }
    }

    private void initializeColor() {
        colorWhite = getResources().getColor(R.color.white);
        colorGreen = getResources().getColor(R.color.green);
        colorYellow = getResources().getColor(R.color.yellow);
        colorRed = getResources().getColor(R.color.red);
        colorBlack = getResources().getColor(R.color.black);
        colorAccent = getResources().getColor(R.color.colorAccent);
    }

    private int[] getLastMonthPeriod() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DATE, 1);
        Date firstDayPrevMonth = calendar.getTime();
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        Date lastDayPrevMonth = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String endDate = dateFormat.format(lastDayPrevMonth);
        String startDate = dateFormat.format(firstDayPrevMonth);

        int endMonth = Integer.parseInt(endDate.substring(0, 2));
        int endDay = Integer.parseInt(endDate.substring(3, 5));
        int endYear = Integer.parseInt(endDate.substring(6));

        int startMonth = Integer.parseInt(startDate.substring(0, 2));
        int startDay = Integer.parseInt(startDate.substring(3, 5));
        int startYear = Integer.parseInt(startDate.substring(6));

        return new int[]{startYear, startMonth, startDay, endYear, endMonth, endDay};
    }

    private void calculateGrowth(int numbOfRows) {
        Collections.sort(mTable_mode1);
        int threshold = 30;
        for (int i = 0; i < numbOfRows; i++) { // numbOfRows doesn't count the header row
            int rowIndex = NUMB_COL_MODE1 * (i + 1);
            double avgMonthlyCount = Double.parseDouble(mTable_mode1.get(rowIndex + 1).getText());
            double lastMonthCount = Double.parseDouble(mTable_mode1.get(rowIndex + 3).getText());
            double growth = (lastMonthCount - avgMonthlyCount) / avgMonthlyCount * 100;
            int color = 0;
            if (growth < -threshold) color = colorGreen;
            else if (growth > threshold) color = colorRed;
            else color = colorYellow;

            TableBox growthBox = mTable_mode1.get(rowIndex + 2);
            growthBox.setText(String.format("%.0f", growth) + "%");
            growthBox.setBackgroundColor(color);
        }

        // mProgressBar.setVisibility(View.GONE);
        mTableAdapter_mode1.notifyDataSetChanged();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void readStats_Testing() {
        Random random = new Random();

        mTable_mode1.add(new TableBox(0, "Types", colorWhite));
        mTable_mode1.add(new TableBox(1, "2018-19 Average", colorWhite));
        mTable_mode1.add(new TableBox(2, "Growth", colorWhite));
        mTable_mode1.add(new TableBox(3, "Last Month", colorWhite));

        for (String type : STUB) {
            mTable_mode1.add(new TableBox(0, type, colorWhite));
            mTable_mode1.add(new TableBox(0, String.format("%.2f", random.nextDouble() * 100), colorWhite));
            mTable_mode1.add(new TableBox(0, String.format("%.2f", random.nextDouble() * 200 - 50), colorWhite));
            mTable_mode1.add(new TableBox(0, String.format("%.2f", random.nextDouble() * 100), colorWhite));
        }

        mTable_mode2.add(new TableBox(0, "Types", colorWhite));
        for (int year : COLUMN_MODE2) {
            mTable_mode2.add(new TableBox(1, Integer.toString(year) + " AVG", colorWhite));
        }

        for (String type : STUB) {
            mTable_mode2.add(new TableBox(0, type, colorWhite));
            mTable_mode2.add(new TableBox(0, String.format("%.2f", random.nextDouble() * 50), colorWhite));
            mTable_mode2.add(new TableBox(0, String.format("%.2f", random.nextDouble() * 50), colorWhite));
            mTable_mode2.add(new TableBox(0, String.format("%.2f", random.nextDouble() * 50), colorWhite));
            mTable_mode2.add(new TableBox(0, String.format("%.2f", random.nextDouble() * 50), colorWhite));
        }

        mProgressBar.setVisibility(View.GONE);
        mTableAdapter_mode1.notifyDataSetChanged();
        mTableAdapter_mode2.notifyDataSetChanged();
        navigationView.setSelectedItemId(R.id.nav_crime_recent_events);
    }
}
