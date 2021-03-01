package com.example.nwto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SendSMSActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "Send Message via SMS";

    private String mOwnerUID, mMessage;
    private ArrayList<String> mReceiverList;
    private EditText mEditMessage;
    private Button mButtonCancel, mButtonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_sms);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mOwnerUID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        ActivityCompat.requestPermissions(SendSMSActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);

        mEditMessage = (EditText) findViewById(R.id.edit_message);

        mButtonSend = (Button) findViewById(R.id.button_send);
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBySMS();
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

        mReceiverList = new ArrayList <String> ();
    }

    private void sendBySMS() {
        String message = mEditMessage.getText().toString();

        SmsManager mySmsManager = SmsManager.getDefault();
        mySmsManager.sendTextMessage("4165643931",null, message, null, null);

    }
}