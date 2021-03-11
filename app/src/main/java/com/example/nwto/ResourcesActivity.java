package com.example.nwto;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.adapter.ResourceAdapter;
import com.example.nwto.api.CityApi;
import com.example.nwto.model.Resource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ResourcesActivity extends AppCompatActivity {
    private static final String TAG = "TAG: " + ResourcesActivity.class.getSimpleName();

    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;

    private CityApi cityApi;
    private String mUserPostalCode;
    private double mUserLatitude;
    private double mUserLongitude;
    private ResourceAdapter mResourceAdapter;
    private List<Resource> mResources;

    private ProgressBar mProgressBar;
    private CardView mTitleCardView;
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
        mTitleCardView = (CardView) findViewById(R.id.resources_cardView_title);
        mTextWardNumb = (TextView) findViewById(R.id.resources_textView_wardNumber);
        mTextAreaName = (TextView) findViewById(R.id.resources_textView_areaName);
        mRecyclerView = (RecyclerView) findViewById(R.id.resources_recyclerView);
        mTitleCardView.setVisibility(View.INVISIBLE);

        // Initializes Resources Adapter
        mResources = new ArrayList<>();
        mResourceAdapter = new ResourceAdapter(this ,mResources);
        mRecyclerView.setAdapter(mResourceAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
        cityApi.updateResourcesPage(mUserLatitude, mUserLongitude, mUserPostalCode, mTitleCardView, mTextWardNumb, mTextAreaName, mProgressBar, mResources, mResourceAdapter);
        cityApi.getNeighbourhood(mUserLatitude, mUserLongitude);
    }
}
