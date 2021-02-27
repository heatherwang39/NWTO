package com.example.nwto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NeighboursActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "Neighbours";

    private String mOwnerUID;
    private Button mButtonLogOut, mButtonAddContact, mButtonWriteMessage;
    private ImageView mImageNav;

    private RecyclerView mRecycleContactList;
    private ArrayList<Contact> mContactList;
    private ContactAdapter mContactAdapter;
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

        mImageNav = (ImageView) findViewById(R.id.image_nav);
        mImageNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NeighboursActivity.this, NavigationActivity.class));
            }
        });

        mButtonLogOut = (Button) findViewById(R.id.button_log_out);
        mButtonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Sign out and then go to login page
                    mAuth.signOut();
                    startActivity(new Intent(NeighboursActivity.this, MainActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mButtonAddContact = (Button) findViewById(R.id.button_add_contact);
        mButtonAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NeighboursActivity.this, AddContactActivity.class));
            }
        });

        mButtonWriteMessage = (Button) findViewById(R.id.button_write_message);
        mButtonWriteMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NeighboursActivity.this, WriteMessageActivity.class));
            }
        });

        //Set GridLayoutManager
        mContactList = new ArrayList <Contact> ();
        mRecycleContactList = (RecyclerView) findViewById(R.id.recycler_contact_list);
        mGridLayoutManager = new GridLayoutManager(this, 1,GridLayoutManager.VERTICAL,false);
        mRecycleContactList.setLayoutManager(mGridLayoutManager);
        loadContacts();
    }

    private void loadContacts() {
        mContactList.clear();
        CollectionReference collectionReference = db.collection("contacts");
        collectionReference.orderBy("fullName")
                .whereEqualTo("ownerUID",mOwnerUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Contact contact = document.toObject(Contact.class);
                                mContactList.add(contact);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        Log.d(TAG, "all contacts:" + mContactList.toString());
                        mContactAdapter = new ContactAdapter(NeighboursActivity.this, mContactList);
                        mRecycleContactList.setAdapter(mContactAdapter);
                        mRecycleContactList.setHasFixedSize(true);
                    }
                });
    }
}