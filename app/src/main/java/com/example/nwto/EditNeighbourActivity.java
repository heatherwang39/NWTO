package com.example.nwto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class EditNeighbourActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "Edit Neighbour";

    private String mNeighbourID, mEmail, mPhoneNumber, mFullName;
    private EditText mEditFullName, mEditEmail, mEditPhoneNumber;
    private Button mButtonSave, mButtonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_neighbour);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        mEditFullName = (EditText) findViewById(R.id.edit_full_name);
        mEditEmail = (EditText) findViewById(R.id.edit_email);
        mEditPhoneNumber = (EditText) findViewById(R.id.edit_phone_number);

        loadNeighbour();

        mButtonSave = (Button) findViewById(R.id.button_save);
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNeighbour();
            }
        });

        mButtonCancel = (Button) findViewById(R.id.button_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadNeighbour() {
        if (getIntent().hasExtra("neighbourID")) {
            mNeighbourID = getIntent().getStringExtra("neighbourID");
            Log.d("Edit",mNeighbourID);
        }
        if (getIntent().hasExtra("fullName")) {
            mFullName = getIntent().getStringExtra("fullName");
            mEditFullName.setHint(mFullName);
            Log.d(TAG, "edit fullname" + mFullName);
        }
        if (getIntent().hasExtra("email")) {
            mEmail = getIntent().getStringExtra("email");
            mEditEmail.setHint(mEmail);
        }
        if (getIntent().hasExtra("phoneNumber")) {
            mPhoneNumber = getIntent().getStringExtra("phoneNumber");
            mEditPhoneNumber.setHint(mPhoneNumber);
        }
    }

    private void updateNeighbour() {
        if(mEditFullName.getText().toString().trim().length() > 0){
            mFullName = mEditFullName.getText().toString().trim();
            //convert first character of each word to be capitalized
            String[] split = mFullName.split("\\s+");
            mFullName = "";
            for(String word : split){
                word = word.substring(0, 1).toUpperCase() + word.substring(1);
                mFullName = mFullName + word + " ";
            }
            mFullName.trim();
        }

        if(mEditEmail.getText().toString().trim().length() > 0){
            mEmail = mEditEmail.getText().toString().trim();
        }
        if(mEditPhoneNumber.getText().toString().trim().length() > 0){
            mPhoneNumber = mEditPhoneNumber.getText().toString().trim();
        }

        DocumentReference documentReference = db.collection("neighbours").document(mNeighbourID);
        Map<String, Object> neighbourUpdate = new HashMap<>();
        neighbourUpdate.put("fullName", mFullName);
        neighbourUpdate.put("email", mEmail);
        neighbourUpdate.put("phoneNumber", mPhoneNumber);

        documentReference.set(neighbourUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Neighbour has been updated for user " + mNeighbourID);
                startActivity(new Intent(EditNeighbourActivity.this, NeighboursActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditNeighbourActivity.this, "Failed in updating neighbour.", Toast.LENGTH_SHORT).show();
            }
        });

    }




}
