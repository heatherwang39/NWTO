package com.example.nwto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private static final String TAG = "LoginByEmailPassword";

    private Button mButtonLogin;
    private TextView mButtonSignUp, mNWTO, mTextForgetPassword;
    private EditText mEditEmail, mEditPassword;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mNWTO = (TextView) findViewById(R.id.text_nwto);
        mNWTO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, AboutActivity.class));
            }
        });

        mTextForgetPassword = (TextView) findViewById(R.id.text_forget_password);
        mTextForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Forget password is clicked" , Toast.LENGTH_SHORT).show();
                final EditText resetMail = new EditText(v.getContext());
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset your password?");
                passwordResetDialog.setMessage("Please enter your email address to receive the reset link.");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetMail.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Reset link has been sent to your email.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Error! Reset link is not sent!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close the dialog
                    }
                });

                passwordResetDialog.create().show();
            }
        });

        // Check whether the user has been logged in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, NavigationActivity.class));
        } else {
            mButtonSignUp = (TextView) findViewById(R.id.button_sign_up);
            mButtonSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
                }
            });

            mButtonLogin = (Button) findViewById(R.id.button_login);
            mButtonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login();
                }
            });

            mEditEmail = (EditText) findViewById(R.id.edit_email);
            mEditPassword = (EditText) findViewById(R.id.edit_password);
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        }
    }

    public void login() {
        Log.i(TAG, "Login Button pressed!");

        String email = mEditEmail.getText().toString().trim();
        String password = mEditPassword.getText().toString();
        mProgressBar.setVisibility(View.VISIBLE);
        Log.i(TAG, email + password);

        if (email.isEmpty()) {
            mEditEmail.setError("Email is required!");
            mEditEmail.requestFocus();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if (password.isEmpty()) {
            mEditPassword.setError("Password is required!");
            mEditPassword.requestFocus();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }

        // [START sign_in_with_email]
        // Cite: https://github.com/firebase/quickstart-android/blob/256c7e1e6e1dd2be7025bb3f858bf906fd158fa0/auth/app/src/main/java/com/google/firebase/quickstart/auth/java/EmailPasswordActivity.java#L229
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
                            startActivity(intent);
                            Log.d(TAG, "successfully logged in");
                            mProgressBar.setVisibility(View.INVISIBLE);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
        // [END sign_in_with_email]
    }
}