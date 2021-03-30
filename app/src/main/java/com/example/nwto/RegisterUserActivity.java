package com.example.nwto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nwto.adapter.PlaceAutoSuggestAdapter;
import com.example.nwto.api.ResourceApi;
import com.example.nwto.util.Notification;
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
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterUserActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final String TAG = "EmailPassword";
    private TextView textViewTakePicture;
    private ImageView profileImage;
    private Button signUp;
    private EditText editTextEmail, editTextPassword, editTextPassword2, editTextName, mEditPhoneNumber;
    private AutoCompleteTextView autoCompleteTextView;
    private ProgressBar progressBar;
    private String uID, mNeighbourhoodName;
    private String currentPhotoPath;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Boolean noProfilePic = true;
    private Bitmap imageBitmap;
    private String email, name, mPhoneNumber;
    private String autoCompleteAddress, postalCode;
    private double mLatitude, mLongitude;

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

        textViewTakePicture = (TextView) findViewById(R.id.text_take_picture);
        textViewTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        signUp = (Button) findViewById(R.id.button_sign_up);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        profileImage = (ImageView) findViewById(R.id.image_profile);
        editTextEmail = (EditText) findViewById(R.id.edit_email);
        editTextPassword = (EditText) findViewById(R.id.edit_password);
        editTextPassword2 = (EditText) findViewById(R.id.edit_password2);
        editTextName = (EditText) findViewById(R.id.edit_name);
        mEditPhoneNumber = (EditText) findViewById(R.id.edit_phone_number);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        autoCompleteTextView = findViewById(R.id.auto_complete_address);
        autoCompleteTextView.setAdapter(new PlaceAutoSuggestAdapter(RegisterUserActivity.this, android.R.layout.simple_list_item_1));

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                convertAddress();
            }
        });
    }

    private void convertAddress() {
        autoCompleteAddress = autoCompleteTextView.getText().toString();
        Log.d("Address : ", autoCompleteAddress);
        LatLng latLng = getLatLngFromAddress(autoCompleteAddress);
        if (latLng != null) {
            Log.d("Lat Lng : ", " " + latLng.latitude + " " + latLng.longitude);
            mLatitude = latLng.latitude;
            mLongitude = latLng.longitude;
            Address address = getAddressFromLatLng(latLng);
            if (address != null) {
                postalCode = address.getPostalCode();
                Log.d("Pin Code : ", "" + postalCode);
            } else {
                Log.d("Address", "Address Not Found");
            }
        } else {
            Log.d("Lat Lng", "Lat Lng Not Found");
        }
    }

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
            Toast.makeText(RegisterUserActivity.this, "Take Picture Cancelled.", Toast.LENGTH_SHORT).show();
        }
    }


    private void signUp() {

        email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        String password2 = editTextPassword2.getText().toString();
        name = editTextName.getText().toString().trim();
        mPhoneNumber = mEditPhoneNumber.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Valid Email Address is required!");
            editTextEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password should be at least 6 characters.");
            editTextPassword.requestFocus();
            return;
        }

        if (!password.equals(password2)) {
            editTextPassword2.setError("The password you entered doesn't match!");
            editTextPassword2.requestFocus();
            return;
        }

        if (noProfilePic) {
            textViewTakePicture.setError("Profile picture is required!");
            textViewTakePicture.requestFocus();
            return;
        }

        if (mPhoneNumber.length()<1) {
            mPhoneNumber = "N/A";
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
                            Toast.makeText(RegisterUserActivity.this, "User created.", Toast.LENGTH_LONG).show();
                            //FirebaseUser user = mAuth.getCurrentUser();
                            uID = mAuth.getCurrentUser().getUid();

                            new ResourceApi() {
                                @Override
                                public void processNeighbourhoodName(String neighbourhoodName) {
                                    Log.d("NeighbourhoodName:", neighbourhoodName);
                                    mNeighbourhoodName = neighbourhoodName.split("\\(")[0].trim();
                                    Log.d("NeighbourhoodName:", mNeighbourhoodName);

                                    DocumentReference documentReference = db.collection("users").document(uID);
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("email", email);
                                    user.put("fullName", name);
                                    user.put("address", autoCompleteAddress);
                                    user.put("phoneNumber", mPhoneNumber);
                                    user.put("postalCode", postalCode);
                                    user.put("coordinates", Arrays.asList(mLatitude, mLongitude));
                                    user.put("radius", "5");
                                    user.put("frequency", "1");
                                    user.put("neighbourhood", mNeighbourhoodName);
                                    user.put("isAdmin", false);
                                    upload(imageBitmap);
                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent intent = new Intent(RegisterUserActivity.this, NavigationActivity.class);
                                            startActivity(intent);
                                            new Notification().schedule(RegisterUserActivity.this, mLatitude, mLongitude, 5, 1);
                                            Log.d(TAG, "createUserWithEmail: firestore success, user profile is created for" + uID);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "failure: " + e.toString());
                                        }
                                    });
                                    progressBar.setVisibility(View.GONE);
                                }
                            }.getMappingResource(mLatitude, mLongitude, 3);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterUserActivity.this, "Authentication failed.", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
        // [END create_user_with_email]

    }

    private void upload(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        StorageReference reference = storage.getReference().child("profileImages").child(uID + ".jpeg");

        reference.putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess: upload profile pic to storage" + uri);
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

    private void updateProfilePic(Uri uri) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
        currentUser.updateProfile(request).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RegisterUserActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                String profilePic = currentUser.getPhotoUrl().toString();
                String userId = mAuth.getCurrentUser().getUid();
                DocumentReference documentReference = db.collection("users").document(userId);
                Map<String, Object> userUpdate = new HashMap<>();
                userUpdate.put("displayPicPath", profilePic);
                documentReference.set(userUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Profile pic has been updated for user " + userId);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterUserActivity.this, "Failed in uploading profile pic.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private LatLng getLatLngFromAddress(String address) {

        Geocoder geocoder = new Geocoder(RegisterUserActivity.this);
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null) {
                Address singleAddress = addressList.get(0);
                LatLng latLng = new LatLng(singleAddress.getLatitude(), singleAddress.getLongitude());
                return latLng;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Address getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(RegisterUserActivity.this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
            if (addresses != null) {
                Address address = addresses.get(0);
                return address;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private void getNeighbourhood() {
        new ResourceApi() {
            @Override
            public void processNeighbourhoodName(String neighbourhoodName) {
                mNeighbourhoodName = neighbourhoodName.split("\\(")[0];
                Log.d("NeighbourhoodName:", mNeighbourhoodName);
            }
        }.getMappingResource(mLatitude, mLongitude, 3);
    }

}