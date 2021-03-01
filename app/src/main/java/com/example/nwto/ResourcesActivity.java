package com.example.nwto;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ResourcesActivity extends AppCompatActivity {
    private static final String TAG = ResourcesActivity.class.getSimpleName();

    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;

    private CityApi cityApi;
    private String mUserPostalCode;
    private String mUserPostalCodeSpace;
    private double mUserLatitude;
    private double mUserLongitude;
    private ResourceAdapter mResourceAdapter;
    private List<Resource> mResources;

    private ProgressBar mProgressBar;
    private TextView mTextWardNumb, mTextAreaName;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        // Initializes Firebase Variables
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFireStore = FirebaseFirestore.getInstance();

        // Initializes Layout Variables
        mProgressBar = (ProgressBar) findViewById(R.id.resources_progressBar);
        mTextWardNumb = (TextView) findViewById(R.id.resources_textView_wardNumber);
        mTextAreaName = (TextView) findViewById(R.id.resources_textView_areaName);
        mRecyclerView = (RecyclerView) findViewById(R.id.resources_recyclerView);

        // Initializes Resources Adapter
        mResources = new ArrayList<>();
        mResourceAdapter = new ResourceAdapter(mResources);
        mRecyclerView.setAdapter(mResourceAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Testing TODO: remove
//        mUserLatitude = 43.7681507;
//        mUserLongitude = -79.4143751;
//        mUserPostalCode = "M2N6W8";
//        mUserPostalCodeSpace = "M2N 6W8";
//        cityApi = new CityApi();
//        getCustomResources();

        cityApi = new CityApi();
        getUserLocation();
    }

    private void getUserLocation() {
        String collectionName = getResources().getString(R.string.firestore_collection_users);
        String documentID = mUser.getUid();
        String documentField_coordinates = getResources().getString(R.string.firestore_users_coordinates);
        String documentField_postalCode = getResources().getString(R.string.firestore_users_postalCode);

        // reads the User's location and postal code
        mFireStore.collection(collectionName).document(documentID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<Double> coordinates = (ArrayList<Double>) document.get(documentField_coordinates);
                                String postalCode = (String) document.get(documentField_postalCode);
                                mUserLatitude = coordinates.get(0);
                                mUserLongitude = coordinates.get(1);
                                mUserPostalCodeSpace = postalCode;
                                mUserPostalCode =  postalCode.replaceAll("\\s+","");
                            }
                            Log.d(TAG, "getLocation: onComplete -> Success=" + "Lat:" + mUserLatitude + ", Long:" + mUserLongitude + ", PostalCode:"+mUserPostalCode);
                            getCustomResources(); // reads and updates the Government contact information
                        }
                        else Log.e(TAG, "getLocation: onComplete -> Fail", task.getException());
                    }
                });
    }

    private void getCustomResources() {
        cityApi.getWard(mUserLatitude, mUserLongitude, mTextWardNumb, mTextAreaName);
        cityApi.getResources(mUserPostalCode, mResources, mResourceAdapter, mProgressBar);
    }
}
