package com.example.nwto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.adapter.ContactAdapter;
import com.example.nwto.api.ResourceApi;
import com.example.nwto.model.Contact;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    private static final String TAG = "TAG: " + ContactsActivity.class.getSimpleName();

    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;

    private String mUserPostalCode;
    private double mUserLatitude;
    private double mUserLongitude;
    private ContactAdapter mContactAdapter;
    private List<Contact> mContacts;

    private ProgressBar mProgressBar;
    private CardView mTitleCardView;
    private TextView mTextWardNumb, mTextAreaName;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // Initializes Firebase Variables
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFireStore = FirebaseFirestore.getInstance();

        // Initializes Layout Variables
        mProgressBar = (ProgressBar) findViewById(R.id.resources_progressBar);
        mTitleCardView = (CardView) findViewById(R.id.resources_cardView_title);
        mTextWardNumb = (TextView) findViewById(R.id.resources_textView_wardNumber);
        mTextAreaName = (TextView) findViewById(R.id.resources_textView_areaName);
        mRecyclerView = (RecyclerView) findViewById(R.id.resources_recyclerView);
        startLoading(); // sets RecyclerView and CardView invisible and starts the progress bar

        // Initializes Resources Adapter
        mContacts = new ArrayList<>();
        mContactAdapter = new ContactAdapter(this, mContacts);
        mRecyclerView.setAdapter(mContactAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Go to Login page if not logged in
        if (mUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            getUserLocation(); // reads user location and updates the contact cards information
        }
    }

    private void startLoading() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mTitleCardView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        Collections.sort(mContacts);
        mContactAdapter.notifyDataSetChanged();
        mRecyclerView.setVisibility(View.VISIBLE);
        mTitleCardView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
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
                                mUserPostalCode = postalCode.replaceAll("\\s+", "");
                                Log.e(TAG, "onComplete: " + mUserLatitude + "," + mUserLongitude + ", " + postalCode);
                            }
                            Log.d(TAG, "getLocation: onComplete -> Success=" + "Lat:" + mUserLatitude + ", Long:" + mUserLongitude + ", PostalCode:" + mUserPostalCode);
                            updateResourceCards(); // reads and updates the contact cards information
                        } else Log.e(TAG, "getLocation: onComplete -> Fail", task.getException());
                    }
                });
    }

    private void updateResourceCards() {
        // updates Ward Number & Ward Name
        new ResourceApi() {
            @Override
            public void updateWardInfoCard(String wardNumb, String wardName) {
                mTextWardNumb.setText("Ward" + wardNumb); // updates the TextViews
                mTextAreaName.setText(wardName);
                // mTitleCardView.setVisibility(View.VISIBLE);
            }
        }.getMappingResource(mUserLatitude, mUserLongitude, 1);

        // updates Crime Prevention Officer & Police Division contact info
        new ResourceApi() {
            @Override
            public void updatePoliceDivisionContactCard(String divisionNumb) {
                readPoliceContactInfoFromFireStore(divisionNumb);
            }
        }.getMappingResource(mUserLatitude, mUserLongitude, 2);

        // updates Government Officials' contact info
        new ResourceApi() {
            @Override
            public void updateOfficialContactCard(String title, String name, String email, String phoneNumb) {
                int order = 0;
                switch (title) {
                    case "Councillor":
                        order = 2;
                        title = "City Councillor";
                        break;
                    case "MPP":
                        order = 3;
                        break;
                    case "MP":
                        order = 4;
                        break;
                }
                mContacts.add(new Contact(order, title, name, email, phoneNumb));
                mContactAdapter.notifyDataSetChanged();
            }
        }.getOfficialResource(mUserPostalCode);
    }

    private void readPoliceContactInfoFromFireStore(String divisionNumb) {
        String collectionName = "police_contact_info";
        String documentField_divisionAddress = "divisionAddress";
        String documentField_divisionEmail = "divisionEmail";
        String documentField_divisionPhone = "divisionPhone";
        String documentField_officerName = "officerName";
        String documentField_officerEmail = "officerEmail";
        String documentField_officerPhone = "officerPhone";

        // reads the police division and crime prevention officer info
        mFireStore.collection(collectionName).document(divisionNumb).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String divisionAddress = (String) document.get(documentField_divisionAddress);
                                String divisionEmail = (String) document.get(documentField_divisionEmail);
                                String divisionPhone = (String) document.get(documentField_divisionPhone);
                                String officerName = (String) document.get(documentField_officerName);
                                String officerEmail = (String) document.get(documentField_officerEmail);
                                String officerPhone = (String) document.get(documentField_officerPhone);
                                mContacts.add(new Contact(0, "Police Division " + divisionNumb, divisionAddress, divisionEmail, divisionPhone));
                                mContacts.add(new Contact(1, "Crime Prevention", officerName, officerEmail, officerPhone));
                                Log.d(TAG, "readPoliceContactInfoFromFireStore: onComplete -> Read Info Success");
                            }
                        } else
                            Log.e(TAG, "readPoliceContactInfoFromFireStore: onComplete -> Read Info Fail", task.getException());
                        stopLoading();
                    }
                });
    }


}
