package com.example.nwto.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.nwto.DiscussionActivity;
import com.example.nwto.ProfileUpdateActivity;
import com.example.nwto.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DiscussionPostFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser mCurrentUser;
    private FirebaseStorage storage;

    private static final String TAG = "Post Discussion";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private EditText mEditTopic, mEditContent;
    private ImageView mImageUpload;
    private Button mButtonPost;
    private String mUID, mFullName,mProfilePic, mCurrentPhotoPath, mTimeStamp, mTopic, mContent;
    private Bitmap mImageBitmap;
    private Uri postURI;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_discussion_post,container,false);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mUID = mAuth.getCurrentUser().getUid();
        mFullName = mAuth.getCurrentUser().getDisplayName();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        // Initialize Storage
        storage = FirebaseStorage.getInstance();

        mImageUpload = (ImageView) rootView.findViewById(R.id.image_upload) ;
        mEditTopic = (EditText) rootView.findViewById(R.id.edit_topic);
        mEditContent = (EditText) rootView.findViewById(R.id.edit_content);
        mButtonPost = (Button) rootView.findViewById(R.id.button_post);

        mImageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
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


        return rootView;
    }


    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getActivity(), "Error when creating the Post File",
                        Toast.LENGTH_LONG).show();
            }
            if (photoFile != null) {
                postURI = FileProvider.getUriForFile(getActivity(),"com.example.android.fileprovider",photoFile);
//                getCtx().setPostUri(postURI);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, postURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            Log.i(TAG, "onActivityResult: Make Post Image Capture RESULT OK");
            try {
                mImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), postURI);
                mImageUpload.setImageBitmap(mImageBitmap);

                Log.i(TAG, "onActivityResult ok: get postBitmap successfully");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "onActivityResult ok: get postBitmap unsuccessfully");
            }}else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_CANCELED){
            Log.i(TAG, "onActivityResult: Make Post Image Capture RESULT CANCELLED");
        }else{
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
        //check the topic is not empty
        mTopic = mEditTopic.getText().toString();
        mContent = mEditContent.getText().toString();
        if(mTopic.length()<1){
            mEditTopic.setError("Please enter a topic.");
            mEditTopic.requestFocus();
            return;
        }
        if(mContent.length()<1){
            mEditContent.setError("Please enter the content.");
            mEditContent.requestFocus();
            return;
        }

        //check if postBitmap exists
        if(mImageBitmap != null){
            // crop the picture to square
            Bitmap square = cropToSquare(mImageBitmap);

            //Rotate the image
            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            Bitmap rotatedBitmap = null;
            Matrix matrix = new Matrix();
            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
            }
            rotatedBitmap = Bitmap.createBitmap(square, 0, 0, square.getWidth(), square.getHeight(),
                    matrix, true);

            // Downscale to 1024*1024
            Bitmap finalBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 1024, 1024, true);

            //upload
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            mTimeStamp = String.valueOf(System.currentTimeMillis());
            final StorageReference postReference = storage.getReference().child("photos").child(mUID+"/"+mTimeStamp+".jpeg");

            postReference.putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getActivity(), "Made a new Post",
                            Toast.LENGTH_SHORT).show();
                    postReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d(TAG, "onSuccess: get uri "+uri);
                            addToUserPosts(uri);
                            Intent intent = new Intent(getActivity(), DiscussionActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "OnFailure: ", e.getCause());
                }
            });
        }else{
            Toast.makeText(getActivity(), "Please upload an image!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static Bitmap cropToSquare(Bitmap bitmap){
        Bitmap cropImg;
        if (bitmap.getWidth() >= bitmap.getHeight()){
            cropImg = Bitmap.createBitmap(bitmap,bitmap.getWidth()/2 - bitmap.getHeight()/2,0,
                    bitmap.getHeight(),bitmap.getHeight());
        }else{
            cropImg = Bitmap.createBitmap(bitmap,0,bitmap.getHeight()/2 - bitmap.getWidth()/2,
                    bitmap.getWidth(),bitmap.getWidth());
        }
        return cropImg;
    }


    private void addToUserPosts(Uri uri){
        //get user info, including fullName, profilePic
        DocumentReference documentReference = db.collection("users").document(mUID);
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getActivity(), "Error while loading the profile", Toast.LENGTH_SHORT);
                    Log.d(TAG, "-->" + e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    mFullName = documentSnapshot.getString("fullName");
                    mProfilePic = documentSnapshot.getString("displayPicPath");
                    Map<String, Object> post = new HashMap<>();

                    //I don't use custom object post here because all field names are converted to lowercase automatically, like uid and timestamp
                    post.put("uID", mUID);
                    post.put("fullName", mFullName);
                    post.put("profilePic", mProfilePic);
                    post.put("postPic", String.valueOf(uri));
                    post.put("timeStamp", mTimeStamp);
                    post.put("topic", mTopic);
                    post.put("content", mContent);

                    //create a new post and store it in firestore
                    db.collection("posts")
                            .add(post)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "On Success: addToUserPosts" + documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error addToUserPosts", e);
                                }
                            });
                }
            }
        });
    }

}
