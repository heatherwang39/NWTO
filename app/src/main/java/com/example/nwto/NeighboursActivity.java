package com.example.nwto;

import androidx.annotation.NonNull;
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

import com.example.nwto.adapter.NeighbourAdapter;
import com.example.nwto.model.Neighbour;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NeighboursActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "Neighbours";

    private String mOwnerUID;
    private Button mButtonAddContact, mButtonSendSMS, mButtonSendEmail;

    private RecyclerView mRecycleContactList;
    private ArrayList<Neighbour> mNeighbourList;
    private NeighbourAdapter mNeighbourAdapter;
    private GridLayoutManager mGridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neighbours);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mOwnerUID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();


        mButtonAddContact = (Button) findViewById(R.id.button_add_contact);
        mButtonAddContact.setOnClickListener(new View.OnClickListener() {
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
        mRecycleContactList = (RecyclerView) findViewById(R.id.recycler_contact_list);
        mGridLayoutManager = new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false);
        mRecycleContactList.setLayoutManager(mGridLayoutManager);
        loadContacts();
    }

    private void loadContacts() {
        mNeighbourList.clear();
        CollectionReference collectionReference = db.collection("contacts");
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
                        Log.d(TAG, "all contacts:" + mNeighbourList.toString());
                        mNeighbourAdapter = new NeighbourAdapter(NeighboursActivity.this, mNeighbourList);
                        mRecycleContactList.setAdapter(mNeighbourAdapter);
                        mRecycleContactList.setHasFixedSize(true);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.button_log_out) {
            try {
                // Sign out and then go to login page
                mAuth.signOut();
                startActivity(new Intent(NeighboursActivity.this, LoginActivity.class));
                Toast.makeText(this, "Logged Out!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}