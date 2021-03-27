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
import com.jjoe64.graphview.series.DataPoint;

import org.osmdroid.util.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CrimeStatsActivity extends AppCompatActivity {
    private static final String TAG = "TAG: " + CrimeStatsActivity.class.getSimpleName();
    private int colorWhite, colorGreen, colorYellow, colorRed, colorHeader, colorBody;

    public static final String[] HEADER_MODE1 = new String[] {"Types", "2018-20 Average", "Growth", "Last Month"};
    public static final String[] HEADER_MODE2 = new String[] {"Types", "2018 Avg", "2019 Avg", "2020 Avg", "Last Month"};
    public static final String[] STUB = new String[] {"Assault", "Auto Theft", "Break and Enter", "Robbery", "Theft Over", "Sexual Violation", "Shooting"};
    public static final int[] YEARS = new int[] {2018, 2019, 2020};
    public static final String[] CRIMETYPES_YE = new String[] {"Assault", "Auto Theft", "Break and Enter", "Robbery", "Theft Over"};
    public static final String[] CRIMETYPES_YTD = new String[] {"Assault", "Auto Theft", "Break and Enter", "Robbery", "Theft Over", "Sexual Violation", "Shooting"};
    public static final String EMPTY_VALUE = "-";

    private boolean calculationYE = false, calculationYTD = false;

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
            // readStats_Testing(); // for Testing
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
                            buildComparisonsTables(HEADER_MODE1, STUB, HEADER_MODE2, STUB, YEARS, CRIMETYPES_YE, CRIMETYPES_YTD);// reads and updates for Crime Stats tables
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
                    sortRecentCrimesByDate();
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
                    sortRecentCrimesByDate();
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

    private void buildComparisonsTables(String[] header_mode1, String[] stub_mode1, String[] header_mode2, String[] stub_mode2,
                                        int[] years, String[] crimeTypes_YE, String[] crimeTypes_YTD) {
        // initializes table mode 1
        int rowNumb_mode1 = stub_mode1.length + 1;
        int colNumb_mode1 = header_mode1.length;
        for (int row = 0; row < rowNumb_mode1; row++) {
            for (int col = 0; col < colNumb_mode1; col++) {
                int index = row * colNumb_mode1 + col;
                if (row == 0) mTable_mode1.add(new TableBox(index, header_mode1[col], colorHeader));
                else if (col == 0) mTable_mode1.add(new TableBox(index, stub_mode1[row - 1], colorHeader));
                else mTable_mode1.add(new TableBox(index, EMPTY_VALUE, colorBody));
            }
        }

        // initializes table mode 2
        int rowNumb_mode2 = stub_mode2.length + 1;
        int colNumb_mode2 = header_mode2.length;
        for (int row = 0; row < rowNumb_mode2; row++) {
            for (int col = 0; col < colNumb_mode2; col++) {
                int index = row * colNumb_mode2 + col;
                if (row == 0) mTable_mode2.add(new TableBox(index, header_mode2[col], colorHeader));
                else if (col == 0) mTable_mode2.add(new TableBox(index, stub_mode2[row - 1], colorHeader));
                else mTable_mode2.add(new TableBox(index, EMPTY_VALUE, colorBody));
            }
        }

        // reads YE data and fills the tables
        new CrimeApi() {
            @Override
            public void processCrimes_YE(List<Crime> crimes) {
                // finds crime counts per year per crime type
                int duration = (years[years.length - 1] - years[0] + 1) * 12;
                int[][] crimeCounts = new int[years.length][crimeTypes_YE.length];
                // double[] avgOverAllYears = new double[crimeTypes_YE.length];
                // double[][] avgOverEachYear = new double[years.length][crimeTypes_YE.length];
                for (Crime crime : crimes) {
                    int year = Integer.parseInt(crime.getDate().substring(6)); // "MM-dd-yyyy"
                    String type = crime.getCategory();
                    int yearIndex = -1;
                    switch (year) {
                        case 2018:
                            yearIndex = 0;
                            break;
                        case 2019:
                            yearIndex = 1;
                            break;
                        case 2020:
                            yearIndex = 2;
                            break;
                        default:
                            break;
                    }
                    if (yearIndex == -1) {
                        Log.e(TAG, "processCrimes_YE: year out of range");
                        break;
                    }
                    for (int i = 0; i < crimeTypes_YE.length; i++) {
                        if (type.equals(crimeTypes_YE[i])) crimeCounts[yearIndex][i]++;
                    }
                }

                // Mode 1 Table Calculation
                for (int i = 0; i < crimeCounts[0].length; i++) { // crime type
                    double monthlyAvg = 0;
                    for (int j = 0; j < crimeCounts.length; j++) { // year
                        monthlyAvg += crimeCounts[j][i];
                    }
                    monthlyAvg /= ((double) duration);
                    // avgOverAllYears[i] = monthlyAvg;
                    mTable_mode1.get((i + 1) * colNumb_mode1 + 1).setText(String.format("%.1f", monthlyAvg));
                }

                // Mode 2 Table Calculation
                // finds monthly average of crime counts per year per crime type
                for (int i = 0; i < crimeCounts.length; i++) { // year
                    for (int j = 0; j < crimeCounts[0].length; j++) { // crime type
                        double monthlyAvg = crimeCounts[i][j] / 12.0;
                        // avgOverEachYear[i][j] = monthlyAvg;
                        mTable_mode2.get((j + 1) * colNumb_mode2 + (i + 1)).setText(String.format("%.1f", monthlyAvg));
                    }
                }

                calculationYE = true;
                if (calculationYE && calculationYTD) {
                    calculateGrowth(colNumb_mode1, rowNumb_mode1);
                    mTableAdapter_mode1.notifyDataSetChanged();
                    mTableAdapter_mode2.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.GONE); // finishes background tasks and brings the Recent Crime Fragment page to front
                    navigationView.setSelectedItemId(R.id.nav_crime_recent_events);
                }
            }
        }.queryYE(mUserRadius, mUserLatitude, mUserLongitude, -1, -1,
                  years[0], 1, 1, years[years.length - 1], 12, 31, null, null);


        // reads YTD data and fills the tables
        int[] lmPeriod = getLastMonthPeriod();
        new CrimeApi() {
            @Override
            public void processCrimes_YTD(List<Crime> crimes) {
                // finds crime counts of Last Month per crime type
                int[] crimeCounts = new int[crimeTypes_YTD.length];
                for (Crime crime : crimes) {
                    String type = crime.getCategory();
                    for (int i = 0; i < crimeTypes_YTD.length; i++) {
                        if (type.equals(crimeTypes_YTD[i])) crimeCounts[i]++;
                    }
                }

                int colIndex_mode1 = 3, colIndex_mode2 = 4;
                for (int j = 0; j < crimeCounts.length; j++) {
                    mTable_mode1.get((j + 1) * colNumb_mode1 + colIndex_mode1).setText(Integer.toString(crimeCounts[j]));
                    mTable_mode2.get((j + 1) * colNumb_mode2 + colIndex_mode2).setText(Integer.toString(crimeCounts[j]));
                }

                calculationYTD = true;
                if (calculationYE && calculationYTD) {
                    calculateGrowth(colNumb_mode1, rowNumb_mode1);
                    mTableAdapter_mode1.notifyDataSetChanged();
                    mTableAdapter_mode2.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.GONE); // finishes background tasks and brings the Recent Crime Fragment page to front
                    navigationView.setSelectedItemId(R.id.nav_crime_recent_events);
                }
            }
        }.queryYTD(mUserRadius, mUserLatitude, mUserLongitude, -1,
                lmPeriod[0], lmPeriod[1], lmPeriod[2], lmPeriod[3], lmPeriod[4], lmPeriod[5], null, null);
    }

    private void initializeColor() {
        colorWhite = getResources().getColor(R.color.white);
        colorHeader = getResources().getColor(R.color.white3);
        colorBody = getResources().getColor(R.color.white2);
        colorGreen = getResources().getColor(R.color.green);
        colorYellow = getResources().getColor(R.color.yellow);
        colorRed = getResources().getColor(R.color.red);
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

    private void calculateGrowth(int numbOfCols, int numbOfRows) {
        int threshold = 30;
        for (int i = 1; i < numbOfRows; i++) { // numbOfRows doesn't count the header row
            int rowIndex = numbOfCols * i;
            String avgMonthlyCountVal = mTable_mode1.get(rowIndex + 1).getText();
            if (avgMonthlyCountVal.equals(EMPTY_VALUE)) avgMonthlyCountVal = "0.0";
            String lastMonthCountVal = mTable_mode1.get(rowIndex + 3).getText();
            if (lastMonthCountVal.equals(EMPTY_VALUE)) lastMonthCountVal = "0.0";

            double avgMonthlyCount = Double.parseDouble(avgMonthlyCountVal);
            double lastMonthCount = Double.parseDouble(lastMonthCountVal);
            double growth = (lastMonthCount - avgMonthlyCount) / avgMonthlyCount * 100;
            int color = 0;
            if (growth < -threshold) color = colorGreen;
            else if (growth > threshold) color = colorRed;
            else color = colorYellow;

            TableBox growthBox = mTable_mode1.get(rowIndex + 2);
            if (avgMonthlyCount == 0) growthBox.setText("N/A");
            else {
                growthBox.setText(String.format("%.0f", growth) + "%");
                growthBox.setBackgroundColor(color);
            }
        }

        sortTableMode1ByGrowth(1, numbOfRows - 3); // excluding header, Sexual Violation and Shooting as there are no data for YE
    }

    private void sortRecentCrimesByDate() {
        Collections.sort(mCrimes, new Comparator<Crime>() {
            @Override
            public int compare(Crime crime1, Crime crime2) {
                String date1 = crime1.getDate();
                String date2 = crime2.getDate();
                int month1 = Integer.parseInt(date1.substring(0, 2));
                int day1 = Integer.parseInt(date1.substring(3, 5));
                int month2 = Integer.parseInt(date2.substring(0, 2));
                int day2 = Integer.parseInt(date2.substring(3, 5));

                if (month2 > month1) return 1;
                else if (month2 == month1) return Integer.compare(day2, day1);
                else return -1;
            }
        });
    }

    private void sortTableMode1ByGrowth(int startRow, int endRow) {
        if (startRow >= endRow) return;
        int colNumbs = HEADER_MODE1.length;
        int growthBoxIndex = 2;
        int standard = startRow;
        int pivot = startRow + 1;

        int index_pivot = colNumbs * pivot;
        int index_standard = colNumbs * standard;
        for (int i = startRow + 1; i <= endRow; i++) {
            int index_i = colNumbs * i;

            String growth_i_text = mTable_mode1.get(index_i + growthBoxIndex).getText();
            String growth_standard_text = mTable_mode1.get(index_standard + growthBoxIndex).getText();
            double growth_i = Double.parseDouble(growth_i_text.substring(0, growth_i_text.length() - 1));
            double growth_standard = Double.parseDouble(growth_standard_text.substring(0, growth_standard_text.length() - 1));

            if (growth_i > growth_standard) {
                // row_temp = row_i
                TableBox[] temp = new TableBox[colNumbs];
                for (int j = 0; j < colNumbs; j++) {
                    TableBox boxOfRow_i = mTable_mode1.get(index_i + j);
                    temp[j] = new TableBox(boxOfRow_i.getOrder(), boxOfRow_i.getText(), boxOfRow_i.getBackgroundColor());
                }

                // row_i = row_pivot
                for (int j = 0; j < colNumbs; j++) {
                    TableBox boxOfRow_pivot = mTable_mode1.get(index_pivot + j);
                    TableBox boxOfRow_i = mTable_mode1.get(index_i + j);
                    boxOfRow_i.setOrder(boxOfRow_pivot.getOrder());
                    boxOfRow_i.setText(boxOfRow_pivot.getText());
                    boxOfRow_i.setBackgroundColor(boxOfRow_pivot.getBackgroundColor());
                }

                // row_pivot = row_temp
                for (int j = 0; j < colNumbs; j++) {
                    TableBox boxOfRow_pivot = mTable_mode1.get(index_pivot + j);
                    TableBox boxOfRow_temp = temp[j];
                    boxOfRow_pivot.setOrder(boxOfRow_temp.getOrder());
                    boxOfRow_pivot.setText(boxOfRow_temp.getText());
                    boxOfRow_pivot.setBackgroundColor(boxOfRow_temp.getBackgroundColor());
                }

                pivot++;
                index_pivot = colNumbs * pivot;
            }
        }
        pivot--;
        index_pivot = colNumbs * pivot;

        // row_temp = row_pivot
        TableBox[] temp = new TableBox[colNumbs];
        for (int j = 0; j < colNumbs; j++) {
            TableBox boxOfRow_pivot = mTable_mode1.get(index_pivot + j);
            temp[j] = new TableBox(boxOfRow_pivot.getOrder(), boxOfRow_pivot.getText(), boxOfRow_pivot.getBackgroundColor());
        }

        // row_pivot = row_standard
        for (int j = 0; j < colNumbs; j++) {
            TableBox boxOfRow_pivot = mTable_mode1.get(index_pivot + j);
            TableBox boxOfRow_standard =  mTable_mode1.get(index_standard + j);
            boxOfRow_pivot.setOrder(boxOfRow_standard.getOrder());
            boxOfRow_pivot.setText(boxOfRow_standard.getText());
            boxOfRow_pivot.setBackgroundColor(boxOfRow_standard.getBackgroundColor());
        }

        // row_standard = row_temp
        for (int j = 0; j < colNumbs; j++) {
            TableBox boxOfRow_standard = mTable_mode1.get(index_standard + j);
            TableBox boxOfRow_temp = temp[j];
            boxOfRow_standard.setOrder(boxOfRow_temp.getOrder());
            boxOfRow_standard.setText(boxOfRow_temp.getText());
            boxOfRow_standard.setBackgroundColor(boxOfRow_temp.getBackgroundColor());
        }

        sortTableMode1ByGrowth(standard, pivot - 1);
        sortTableMode1ByGrowth(pivot + 1, endRow);
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
        for (int year : YEARS) {
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
