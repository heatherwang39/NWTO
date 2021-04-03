package com.example.nwto;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nwto.model.Neighbour;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SendEmailActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "Send Email";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private String mOwnerUID, mSubject, mBody;
    private ArrayList<String> mEmailList;
    private ArrayList<Neighbour> mNeighbourList = NeighboursActivity.mNeighbourList;
    private EditText mEditSubject, mEditBody;
    private Button mButtonCancel, mButtonSend;
    private TextView mTextTakeAPicture, mTextPreview;
    private ImageView mImagePreview;
    private Uri mAttachmentUri;
    private Bitmap mImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_email);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mOwnerUID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        mEditSubject = findViewById(R.id.edit_subject);
        mEditBody = findViewById(R.id.edit_body);
        mTextPreview = findViewById(R.id.text_preview);

        mButtonSend = findViewById(R.id.button_send);
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendByEmail();
                Log.d(TAG, "Send message via Email.");
            }
        });

        mButtonCancel = findViewById(R.id.button_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTextTakeAPicture = findViewById(R.id.text_take_a_picture);
        mTextTakeAPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        mImagePreview = findViewById(R.id.image_preview);
        mEmailList = new ArrayList<String>();
    }

    private void sendByEmail() {
        String mSubject = mEditSubject.getText().toString();
        String mBody = mEditBody.getText().toString();

        //The subject and body can't be empty
        if (mSubject.isEmpty()) {
            mEditSubject.setError("Please enter a subject.");
            mEditSubject.requestFocus();
            return;
        }
        if (mBody.isEmpty()) {
            mEditBody.setError("Please enter the body");
            mEditBody.requestFocus();
            return;
        }

        //Get emails of the User's neighbours
        mEmailList.clear();
        for (Neighbour neighbour : mNeighbourList) {
            mEmailList.add(neighbour.getEmail());
            Log.d(TAG, neighbour.getEmail());
        }
        String emails[] = mEmailList.toArray(new String[mEmailList.size()]);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_EMAIL, emails);
        intent.putExtra(Intent.EXTRA_SUBJECT, mSubject);
        intent.putExtra(Intent.EXTRA_TEXT, mBody);
        if (mAttachmentUri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, mAttachmentUri);
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(SendEmailActivity.this, "There is no application that support this email action",
                    Toast.LENGTH_SHORT).show();
        }


//        Intent email = new Intent(Intent.ACTION_SENDTO);
//        email.setData(Uri.parse("mailto:"));
//        email.putExtra(Intent.EXTRA_EMAIL, emails);
//        email.putExtra(Intent.EXTRA_SUBJECT, mSubject);
//        email.putExtra(Intent.EXTRA_TEXT, mBody);

//        Log.d(TAG, "all emails:" + emails);
//
//        if (email.resolveActivity(getPackageManager()) != null) {
//            startActivity(email);
//        } else {
//            Toast.makeText(SendEmailActivity.this, "There is no application that support this email action",
//                    Toast.LENGTH_SHORT).show();
//        }

    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error when creating the Post File",
                        Toast.LENGTH_LONG).show();
            }
            if (photoFile != null) {
                mAttachmentUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mAttachmentUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "onActivityResult: Make Post Image Capture RESULT OK");
            try {
                mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mAttachmentUri);
                mImagePreview.setVisibility(View.VISIBLE);
                mImagePreview.setImageBitmap(mImageBitmap);
                mTextPreview.setVisibility(View.VISIBLE);
                Log.i(TAG, "onActivityResult ok: get postBitmap successfully");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "onActivityResult ok: get postBitmap unsuccessfully");
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_CANCELED) {
            Log.i(TAG, "onActivityResult: Make Post Image Capture RESULT CANCELLED");
        } else {
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

}