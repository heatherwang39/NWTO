package com.example.nwto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddContactActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "Add Contact";

    private String mOwnerUID;
    private EditText mEditFullName, mEditEmail, mEditPhoneNumber;
    private Button mButtonSave, mButtonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mOwnerUID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        mEditFullName = (EditText) findViewById(R.id.edit_full_name);
        mEditEmail = (EditText) findViewById(R.id.edit_email);
        mEditPhoneNumber = (EditText) findViewById(R.id.edit_phone_number);

        mButtonSave = (Button) findViewById(R.id.button_save);
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact();
                Log.d(TAG,"Added new contact.");
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

    private void addContact() {
        String mFullName = mEditFullName.getText().toString();
        String mEmail = mEditEmail.getText().toString().trim();
        String mPhoneNumber = mEditPhoneNumber.getText().toString().trim();

        if(mFullName.length()<1){
            mEditFullName.setError("Full Name can't be empty.");
            mEditFullName.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()){
            mEditEmail.setError("Valid Email Address is required!");
            mEditEmail.requestFocus();
            return;
        }

        if(mFullName.length()<1){ //TODO: add regex here
            mEditPhoneNumber.setError("Phone Number can't be empty.");
            mEditPhoneNumber.requestFocus();
            return;
        }

        Contact contact = new Contact(mOwnerUID, mFullName, mEmail, mPhoneNumber);

        db.collection("contacts")
                .add(contact)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Successfully added new contact!");
                        //go back to Neighbours page
                        startActivity(new Intent(AddContactActivity.this, NeighboursActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding a new contact", e);
                    }
                });

    }
}