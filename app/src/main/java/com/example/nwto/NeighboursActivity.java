package com.example.nwto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nwto.adapter.NeighbourSwipeAdapter;
import com.example.nwto.model.Neighbour;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class NeighboursActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "Neighbours";

    private String mOwnerUID;
    public static boolean isAdmin;
    private Button mButtonAddNeighbour, mButtonSendSMS, mButtonSendEmail;

    private RecyclerView mRecycleNeighbourList;
    private NeighbourSwipeAdapter mNeighbourSwipeAdapter;
    public static ArrayList<Neighbour> mNeighbourList;
    private GridLayoutManager mGridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neighbours);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);// set drawable home icon

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();


        mButtonAddNeighbour = (Button) findViewById(R.id.button_add_neighbour);
        mButtonAddNeighbour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NeighboursActivity.this, AddNeighbourActivity.class));
            }
        });

        mButtonSendSMS = (Button) findViewById(R.id.button_send_sms);
        mButtonSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NeighboursActivity.this, SendSMSActivity.class));
            }
        });

        mButtonSendEmail = (Button) findViewById(R.id.button_send_email);
        mButtonSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NeighboursActivity.this, SendEmailActivity.class));
            }
        });

        //Set GridLayoutManager
        mNeighbourList = new ArrayList<Neighbour>();
        mRecycleNeighbourList = (RecyclerView) findViewById(R.id.recycler_neighbour_list);
        mGridLayoutManager = new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false);
        mRecycleNeighbourList.setLayoutManager(mGridLayoutManager);

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            mOwnerUID = mAuth.getCurrentUser().getUid();
            Log.d(TAG,mOwnerUID);
            loadNeighbours();
        }
    }

    private void loadNeighbours() {

        DocumentReference documentReference = db.collection("users").document(mOwnerUID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(NeighboursActivity.this, "Error while loading", Toast.LENGTH_SHORT);
                    Log.d(TAG, "-->" + e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    //check admin
                    isAdmin = documentSnapshot.getBoolean("isAdmin");
                    Log.i(TAG, "Show isAdmin successfully " + isAdmin);

                    //load neighbours
                    mNeighbourList.clear();


                    //load all authenticated User for admin
                    if (isAdmin) {
                        // Set the title of Action Bar
                        ActionBar actionBar = getSupportActionBar();
                        actionBar.setTitle("Registered Users");

                        CollectionReference collectionReference = db.collection("users");
                        collectionReference.orderBy("fullName")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String neighbourID = document.getId();
                                                String ownerUID = mOwnerUID;
                                                String fullName = document.getString("fullName");
                                                String email = document.getString("email");
                                                String phoneNumber = document.getString("phoneNumber");

                                                Neighbour neighbour = new Neighbour(neighbourID, ownerUID, fullName, email, phoneNumber);
                                                mNeighbourList.add(neighbour);
                                                Log.d(TAG, document.getId() + " => " + document.getData());
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                        Log.d(TAG, "all neighbours:" + mNeighbourList.toString());
                                        mNeighbourSwipeAdapter = new NeighbourSwipeAdapter(NeighboursActivity.this, mNeighbourList);
                                        mRecycleNeighbourList.setAdapter(mNeighbourSwipeAdapter);
                                        mNeighbourSwipeAdapter.setNeighbours(mNeighbourList);
//                        mNeighbourAdapter = new NeighbourAdapter(NeighboursActivity.this, mNeighbourList);
//                        mRecycleNeighbourList.setAdapter(mNeighbourAdapter);
                                        mRecycleNeighbourList.setHasFixedSize(true);
                                    }
                                });
                    } else {
                        CollectionReference collectionReference = db.collection("neighbours");
                        //load owned neighbours if not admin
                        collectionReference.orderBy("fullName")
                                .whereEqualTo("ownerUID", mOwnerUID)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Neighbour neighbour = document.toObject(Neighbour.class);
                                                mNeighbourList.add(neighbour);
                                                Log.d(TAG, document.getId() + " => " + document.getData());
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                        Log.d(TAG, "all neighbours:" + mNeighbourList.toString());
                                        mNeighbourSwipeAdapter = new NeighbourSwipeAdapter(NeighboursActivity.this, mNeighbourList);
                                        mRecycleNeighbourList.setAdapter(mNeighbourSwipeAdapter);
                                        mNeighbourSwipeAdapter.setNeighbours(mNeighbourList);
//                        mNeighbourAdapter = new NeighbourAdapter(NeighboursActivity.this, mNeighbourList);
//                        mRecycleNeighbourList.setAdapter(mNeighbourAdapter);
                                        mRecycleNeighbourList.setHasFixedSize(true);
                                    }
                                });
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        // hide search and add new button
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_log_out:
                try {
                    // Sign out and then go to login page
                    mAuth.signOut();
                    startActivity(new Intent(NeighboursActivity.this, LoginActivity.class));
                    Toast.makeText(this, "Logged Out!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}