package com.example.nwto;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DiscussionPostActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private static final String TAG = "Post Discussion";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SELECT = 2;

    private TextView mTextTakePicture, mTextSelectFromGallery;
    private EditText mEditTopic, mEditContent;
    private ImageView mImagePreview;
    private Button mButtonPost;
    private Spinner mSpinnerCrimeType;
    private CheckBox mCheckBoxNeighbourhood, mCheckBoxIsTip;
    private String mUID, mFullName, mProfilePic, mCurrentPhotoPath, mTimeStamp, mTopic, mContent, mNeighbourhoodName;
    private String mCrimeType = "N/A";
    private String mPostNeighbourhoodName = "Toronto";
    private Bitmap mImageBitmap;
    private Uri postURI;
    private ProgressBar mProgressBar;
    private boolean mIsAdmin, mIsTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion_post);

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        // Initialize Storage
        storage = FirebaseStorage.getInstance();

        mTextTakePicture = (TextView) findViewById(R.id.text_take_picture);
        mTextSelectFromGallery = (TextView) findViewById(R.id.text_select_from_gallery);
        mImagePreview = (ImageView) findViewById(R.id.image_preview);
        mEditTopic = (EditText) findViewById(R.id.edit_topic);
        mEditContent = (EditText) findViewById(R.id.edit_content);
        mButtonPost = (Button) findViewById(R.id.button_post);
        mSpinnerCrimeType = (Spinner) findViewById(R.id.spinner_crime_type);
        mCheckBoxNeighbourhood = (CheckBox) findViewById(R.id.checkbox_neighbourhood);
        mCheckBoxIsTip = (CheckBox) findViewById(R.id.checkbox_isTip);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Get the variable from DiscussionActivity
        mUID = DiscussionActivity.uID;
        mFullName = DiscussionActivity.fullName;
        mProfilePic = DiscussionActivity.profilePic;
        mNeighbourhoodName = DiscussionActivity.neighbourhoodName;
        mIsAdmin = DiscussionActivity.isAdmin;
        mIsTip = false;

        if (mIsAdmin) {
            mCheckBoxIsTip.setVisibility(View.VISIBLE);
            mCheckBoxIsTip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((CompoundButton) view).isChecked()) {
                        mIsTip = true;
                    } else {
                        mIsTip = false;
                    }
                }
            });
        }

        mCheckBoxNeighbourhood.setText("Post to: " + mNeighbourhoodName);

        mTextTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        mTextSelectFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(DiscussionPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DiscussionPostActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_SELECT);
                } else {
                    selectFromGallery();
                }
            }
        });

        mButtonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    makePost();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.crime_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCrimeType.setAdapter(adapter);
        mSpinnerCrimeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                mCrimeType = parent.getItemAtPosition(position).toString();
                Toast.makeText(parent.getContext(), mCrimeType, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mCheckBoxNeighbourhood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CompoundButton) view).isChecked()) {
                    mPostNeighbourhoodName = mNeighbourhoodName;
                } else {
                    mPostNeighbourhoodName = "Toronto";
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_SELECT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectFromGallery();
            }
        }
    }

    private void selectFromGallery() {
        // Pick images from Gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_SELECT);
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
                postURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, postURI);
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
                mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), postURI);
                mImagePreview.setImageBitmap(mImageBitmap);

                Log.i(TAG, "onActivityResult ok: get postBitmap successfully");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "onActivityResult ok: get postBitmap unsuccessfully");
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_CANCELED) {
            Log.i(TAG, "onActivityResult: Make Post Image Capture RESULT CANCELLED");
        } else if (requestCode == REQUEST_IMAGE_SELECT && resultCode == Activity.RESULT_OK && data != null) {
            postURI = data.getData();
            try {
                mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), postURI);
                mImagePreview.setImageBitmap(mImageBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void makePost() throws IOException {
        mProgressBar.setVisibility(View.VISIBLE);

        //check the topic is not empty
        mTopic = mEditTopic.getText().toString();
        mContent = mEditContent.getText().toString();
        mTimeStamp = String.valueOf(System.currentTimeMillis());
        if (mTopic.length() < 1) {
            mEditTopic.setError("Please enter a topic.");
            mEditTopic.requestFocus();
            return;
        }
        if (mContent.length() < 1) {
            mEditContent.setError("Please enter the content.");
            mEditContent.requestFocus();
            return;
        }

        //check if imageBitmap exists
        if (mImageBitmap != null) {
            //downscale the imageBitmap
            double ratio =  (double) mImageBitmap.getWidth() /  (double) mImageBitmap.getHeight();
            Log.d("float",String.valueOf(mImageBitmap.getWidth())+"  "+ String.valueOf(mImageBitmap.getWidth())+ " " +String.valueOf(ratio));
            Bitmap finalBitmap = Bitmap.createScaledBitmap(mImageBitmap, 1024,  (int)(1024/ratio), true);

            //upload
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            final StorageReference postReference = storage.getReference().child("photos").child(mUID + "/" + mTimeStamp + ".jpeg");

            postReference.putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    postReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d(TAG, "onSuccess: get uri " + uri);
                            addToUserPosts(uri);
                            mProgressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(DiscussionPostActivity.this, DiscussionActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "OnFailure: ", e.getCause());
                    mProgressBar.setVisibility(View.GONE);
                }
            });
        } else {
            Uri uri = null;
            addToUserPosts(uri);
            Intent intent = new Intent(this, DiscussionActivity.class);
            startActivity(intent);
        }

    }

    private void addToUserPosts(Uri uri) {
        Map<String, Object> post = new HashMap<>();

        //I don't use custom object post here because all field names are converted to lowercase automatically, like uid and timestamp
        post.put("ownerUID", mUID);
        post.put("fullName", mFullName);
        post.put("profilePic", mProfilePic);
        post.put("postPic", String.valueOf(uri));
        post.put("timeStamp", mTimeStamp);
        post.put("topic", mTopic);
        post.put("content", mContent);
        post.put("neighbourhood", mPostNeighbourhoodName);
        post.put("crimeType", mCrimeType);
        post.put("isTip", mIsTip);

        //create a new post and store it in firestore
        db.collection("posts")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(DiscussionPostActivity.this, "Made a new Post", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "On Success: addToUserPosts" + documentReference.getId());
                        mProgressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error addToUserPosts", e);
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }

}
