package com.example.nwto;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.io.Resources;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourcesActivity extends AppCompatActivity {
    private static final String TAG = ResourcesActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;

    private CityApi cityApi;
    private String mUserPostalCode;
    private String mUserPostalCodeSpace;
    private double mUserLatitude;
    private double mUserLongitude;

    private TextView mTextWardNumb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        // Initialize Firebase Variables
        mAuth = FirebaseAuth.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFireStore = FirebaseFirestore.getInstance();

        // Initialize Layout Variables
        mTextWardNumb = (TextView) findViewById(R.id.resources_wardNumber);

//        mUserLatitude = 43.7681507;
//        mUserLongitude = -79.4143751;
//        mUserPostalCode = "M2N6W8";
//        mUserPostalCodeSpace = "M2N 6W8";
//        cityApi.getWard(mUserLatitude, mUserLongitude, mTextWardNumb);
        cityApi = new CityApi();
        getUserLocation();
    }

    private void getUserLocation() {
        String collectionName = getResources().getString(R.string.firestore_collection_users);
        String documentID = mUser.getUid();
        String documentField_coordinates = getResources().getString(R.string.firestore_users_coordinates);
        String documentField_postalCode = getResources().getString(R.string.firestore_users_postalCode);

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
                            cityApi.getWard(mUserLatitude, mUserLongitude, mTextWardNumb);
                        }
                        else Log.e(TAG, "getLocation: onComplete -> Fail", task.getException());
                    }
                });
    }





}
