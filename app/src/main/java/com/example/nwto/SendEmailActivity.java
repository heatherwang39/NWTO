package com.example.nwto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SendEmailActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "Send Email";

    private String mOwnerUID, mSubject, mBody;
    private ArrayList<String> mEmailList;
    private EditText mEditSubject, mEditBody;
    private Button mButtonCancel, mButtonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_email);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mOwnerUID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        mEditSubject = (EditText) findViewById(R.id.edit_subject);
        mEditBody = (EditText) findViewById(R.id.edit_body);

        mButtonSend = (Button) findViewById(R.id.button_send);
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendByEmail();
                Log.d(TAG,"Send message via Email.");
            }
        });

        mButtonCancel = (Button) findViewById(R.id.button_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mEmailList = new ArrayList <String> ();
    }

    private void sendByEmail() {
        String mSubject = mEditSubject.getText().toString();
        String mBody = mEditBody.getText().toString();

        //The subject and body can't be empty
        if(mSubject.isEmpty()){
            mEditSubject.setError("Please enter a subject.");
            mEditSubject.requestFocus();
            return;
        }
        if(mBody.isEmpty()){
            mEditBody.setError("Please enter the body");
            mEditBody.requestFocus();
            return;
        }

        //Get emails of the User's neighbours
        mEmailList.clear();
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
                                mEmailList.add(contact.getEmail());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                        String emails[]=mEmailList.toArray(new String[mEmailList.size()]);
                        Intent email = new Intent(Intent.ACTION_SENDTO);
                        email.putExtra(Intent.EXTRA_EMAIL, emails);
                        email.putExtra(Intent.EXTRA_SUBJECT, mSubject);
                        email.putExtra(Intent.EXTRA_TEXT, mBody);
                        //        email.setType("message/rfc822");
                        email.setData(Uri.parse("mailto:"));

                        Log.d(TAG, "all emails:" + emails);

                        if (email.resolveActivity(getPackageManager()) != null) {
                            startActivity(email);
                        } else {
                            Toast.makeText(SendEmailActivity.this, "There is no application that support this email action",
                                    Toast.LENGTH_SHORT).show();
                        }
//                       startActivity(new Intent(WriteMessageActivity.this, NeighboursActivity.class));
                    }
                });


    }
}