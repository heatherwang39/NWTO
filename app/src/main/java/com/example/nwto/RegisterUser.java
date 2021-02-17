package com.example.nwto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class RegisterUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final String TAG = "EmailPassword";
    private TextView textViewBanner,textViewTakePicture;
    private ImageView profileImage;
    private Button signUp;
    private EditText editTextEmail, editTextPassword, editTextPassword2, editTextName, editTextAddress;
    private ProgressBar progressBar;
    private String uID;
    private String currentPhotoPath;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Boolean noProfilePic = true;
    private Bitmap imageBitmap;
    private String email, name, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Cloud Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Storage
        storage = FirebaseStorage.getInstance();

        textViewTakePicture = (TextView) findViewById(R.id.textViewTakePicture);
        textViewTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        signUp = (Button) findViewById(R.id.buttonSignUp);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        profileImage = (ImageView) findViewById(R.id.profileImage);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPassword2 = (EditText) findViewById(R.id.editTextPassword2);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    // [START on_start_check_user]
    // Cite: https://github.com/firebase/quickstart-android/blob/256c7e1e6e1dd2be7025bb3f858bf906fd158fa0/auth/app/src/main/java/com/google/firebase/quickstart/auth/java/EmailPasswordActivity.java#L46-L46
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }
    // [END on_start_check_user]


    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            profileImage.setImageBitmap(imageBitmap);
            noProfilePic = false;
        } else {
            Log.i(TAG, "takePictureIntent onActivityResult: RESULT CANCELLED");
            Toast.makeText(RegisterUser.this, "Take Picture Cancelled.",Toast.LENGTH_SHORT).show();
        }
    }


    public void signUp(){

        email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        String password2 = editTextPassword2.getText().toString();
        name = editTextName.getText().toString().trim();
        address = editTextAddress.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Valid Email Address is required!");
            editTextEmail.requestFocus();
            return;
        }

        if(password.length()<6){
            editTextPassword.setError("Password should be at least 6 characters.");
            editTextPassword.requestFocus();
            return;
        }

        if(!password.equals(password2)){
            editTextPassword2.setError("The password you entered doesn't match!");
            editTextPassword2.requestFocus();
            return;
        }

        if(noProfilePic){
            textViewTakePicture.setError("Profile picture is required!");
            textViewTakePicture.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // [START create_user_with_email]
        // Cite: https://github.com/firebase/quickstart-android/blob/256c7e1e6e1dd2be7025bb3f858bf906fd158fa0/auth/app/src/main/java/com/google/firebase/quickstart/auth/java/EmailPasswordActivity.java#L229
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:firebase success");
                            Toast.makeText(RegisterUser.this, "User created.",Toast.LENGTH_LONG).show();
                            //FirebaseUser user = mAuth.getCurrentUser();
                            uID = mAuth.getCurrentUser().getUid();

                            DocumentReference documentReference = db.collection("users").document(uID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("email",email);
                            user.put("fullName",name);
                            user.put("address",address);
                            upload(imageBitmap);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
//                                    Intent intent = new Intent(RegisterUser.this, Profile.class);
//                                    intent.putExtra("imageBitmap",imageBitmap);
//                                    startActivity(intent);
                                    Log.d(TAG, "createUserWithEmail: firestore success, user profile is created for"+uID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "failure: "+e.toString());
                                }
                            });
                            progressBar.setVisibility(View.GONE);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterUser.this, "Authentication failed.",Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
        // [END create_user_with_email]

    }

    private void upload(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        StorageReference reference = storage.getReference().child("profileImages").child(uID+".jpeg");

        reference.putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess: upload profile pic to storage"+uri);
                        updateProfilePic(uri);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "OnFailure: upload profile pic to storage", e.getCause());
            }
        });
    }

    private void updateProfilePic(Uri uri){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
        currentUser.updateProfile(request).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RegisterUser.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                String profilePic = currentUser.getPhotoUrl().toString();
                String userId = mAuth.getCurrentUser().getUid();
                DocumentReference documentReference = db.collection("users").document(userId);
                Map<String, Object> userUpdate = new HashMap<>();
                userUpdate.put("email", email);
                userUpdate.put("username", name);
                userUpdate.put("address", address);
                userUpdate.put("displayPicPath", profilePic);
                documentReference.update(userUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Profile pic has been updated for user " + userId);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterUser.this, "Failed in uploading profile pic.", Toast.LENGTH_SHORT).show();

            }
        });
    }



}