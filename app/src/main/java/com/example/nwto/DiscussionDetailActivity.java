package com.example.nwto;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class DiscussionDetailActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private static final String TAG = "Discussion Detail";

    private TextView mTextNameAndTime, mTextTopic, mTextContent, mTextNeighbourhood, mTextCrimeType;
    private ImageView mImagePostPic;
    private EditText mEditComment;
    private ImageView mButtonSendComment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion_detail);

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        mTextNameAndTime = (TextView) findViewById(R.id.text_name_and_time);
        mTextTopic = (TextView) findViewById(R.id.text_topic);
        mTextContent = (TextView) findViewById(R.id.text_content);
        mTextNeighbourhood = (TextView) findViewById(R.id.text_neighbourhood);
        mTextCrimeType = (TextView) findViewById(R.id.text_crime_type);
        mImagePostPic = (ImageView) findViewById(R.id.image_post_pic);
        mEditComment = (EditText) findViewById(R.id.edit_comment);
        mButtonSendComment = (ImageView) findViewById(R.id.button_send_comment);

        //show post info
        if (getIntent().hasExtra("nameAndTime")) {
            mTextNameAndTime.setText("Posted by: " + getIntent().getStringExtra("nameAndTime") + " ago");
        }
        if (getIntent().hasExtra("topic")) {
            mTextTopic.setText(getIntent().getStringExtra("topic"));
        }
        if (getIntent().hasExtra("content")) {
            mTextContent.setText(getIntent().getStringExtra("content"));
        }
        if (getIntent().hasExtra("neighbourhood")) {
            mTextNeighbourhood.setText("Neighbourhood: " + getIntent().getStringExtra("neighbourhood"));
        }
        if (getIntent().hasExtra("crimeType")) {
            mTextCrimeType.setText("Crime Type: " + getIntent().getStringExtra("crimeType"));
        }

        if (getIntent().hasExtra("postPic")) {
            Picasso.get().load(getIntent().getStringExtra("postPic")).into(mImagePostPic);
        }


    }
}