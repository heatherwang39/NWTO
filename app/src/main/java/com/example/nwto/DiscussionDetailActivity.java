package com.example.nwto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nwto.adapter.CommentAdapter;
import com.example.nwto.model.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DiscussionDetailActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private static final String TAG = "Discussion Detail";

    private TextView mTextNameAndTime, mTextTopic, mTextContent, mTextNeighbourhood, mTextCrimeType;
    private ImageView mImagePostPic;
    private EditText mEditComment;
    private ImageView mButtonSendComment;
    private Button mButtonDeletePost;

    private RecyclerView mRecycleCommentList;
    private CommentAdapter mCommentAdapter;
    private GridLayoutManager mGridLayoutManager;
    private ArrayList<Comment> mCommentList;

    private String mPostOwnerUID, mCommenterUID, mFullName, mContent, mProfilePic, mTimeStamp, mPostTimeStamp;
    private boolean mIsAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion_detail);

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        mCommenterUID = DiscussionActivity.uID;
        mFullName = DiscussionActivity.fullName;
        mProfilePic = DiscussionActivity.profilePic;
        mIsAdmin = DiscussionActivity.isAdmin;

        mTextNameAndTime = (TextView) findViewById(R.id.text_name_and_time);
        mTextTopic = (TextView) findViewById(R.id.text_topic);
        mTextContent = (TextView) findViewById(R.id.text_content);
        mTextNeighbourhood = (TextView) findViewById(R.id.text_neighbourhood);
        mTextCrimeType = (TextView) findViewById(R.id.text_crime_type);
        mImagePostPic = (ImageView) findViewById(R.id.image_post_pic);
        mEditComment = (EditText) findViewById(R.id.edit_comment);
        mButtonSendComment = (ImageView) findViewById(R.id.button_send_comment);
        mButtonDeletePost = (Button) findViewById(R.id.button_delete_post);

        mCommentList = new ArrayList<Comment>();
        mRecycleCommentList = (RecyclerView) findViewById(R.id.recycler_comment_list);
        mGridLayoutManager = new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false);
        mRecycleCommentList.setLayoutManager(mGridLayoutManager);

        mButtonSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

        mButtonDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePost();
            }
        });

        showDiscussionDetail();
        showComments();

    }


    private void showDiscussionDetail() {
        //show discussion details
        //ownerUID AND postTimeStamp will be used to filter comment
        //I don't use the postPic as identifier cuz postPic is optional
        if (getIntent().hasExtra("ownerUID")) {
            mPostOwnerUID = getIntent().getStringExtra("ownerUID");
            //Allow the admin user or the post owner to delete the post
            if(mCommenterUID.equals(mPostOwnerUID)||mIsAdmin){
                mButtonDeletePost.setVisibility(View.VISIBLE);
            }
            Log.d(TAG,mPostOwnerUID);
        }
        if (getIntent().hasExtra("postTimeStamp")) {
            mPostTimeStamp = getIntent().getStringExtra("postTimeStamp");
        }
        if (getIntent().hasExtra("nameAndTime")) {
            mTextNameAndTime.setText("Posted by: " + getIntent().getStringExtra("nameAndTime") + " ago");
            Log.d(TAG,getIntent().getStringExtra("nameAndTime"));
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

    private void showComments() {
        mCommentList.clear();
        CollectionReference collectionReference = db.collection("comments");
        collectionReference.orderBy("timeStamp", Query.Direction.DESCENDING)
                .whereEqualTo("postOwnerUID",mPostOwnerUID)
                .whereEqualTo("postTimeStamp",mPostTimeStamp)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Comment comment = document.toObject(Comment.class);
                                mCommentList.add(comment);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        mCommentAdapter = new CommentAdapter(DiscussionDetailActivity.this, mCommentList);
                        mRecycleCommentList.setAdapter(mCommentAdapter);
                        mRecycleCommentList.setHasFixedSize(true);
                    }
                });
    }


    private void sendComment() {
        String mContent = mEditComment.getText().toString();

        if(mContent.length()<1){
            mEditComment.setError("Content can't be empty.");
            mEditComment.requestFocus();
            return;
        }

        mTimeStamp = String.valueOf(System.currentTimeMillis());

        Comment comment = new Comment(mCommenterUID, mPostOwnerUID, mProfilePic, mFullName, mTimeStamp, mContent, mPostTimeStamp);
        db.collection("comments")
                .add(comment)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        mEditComment.setText("");
                        Toast.makeText(DiscussionDetailActivity.this,"Comment added!",Toast.LENGTH_SHORT).show();
                        //hide the keyboard
                        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);

                        //reload comments
                        showComments();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding a new comment", e);
                    }
                });
    }

    private void deletePost() {
        //delete the post
        db.collection("posts")
                .whereEqualTo("timeStamp",mPostTimeStamp)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        db.collection("posts").document(document.getId()).delete();
                        Log.d(TAG, "Successfully deleting post document: "+document.getId());

                        startActivity(new Intent(DiscussionDetailActivity.this, DiscussionActivity.class));
                        Toast.makeText(DiscussionDetailActivity.this, "Post deleted!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Error getting post documents when deleting: ", task.getException());
                }
            }
        });

        //delete the comments
        db.collection("comments")
                .whereEqualTo("postTimeStamp",mPostTimeStamp)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        db.collection("comments").document(document.getId()).delete();
                        Log.d(TAG, "Successfully deleting comment document: "+document.getId());
                    }
                } else {
                    Log.d(TAG, "Error getting comment documents when deleting: ", task.getException());
                }
            }
        });
    }

}