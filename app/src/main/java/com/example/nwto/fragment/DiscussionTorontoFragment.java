package com.example.nwto.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.DiscussionActivity;
import com.example.nwto.R;
import com.example.nwto.adapter.PostAdapter;
import com.example.nwto.model.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class DiscussionTorontoFragment<GlobalPostAdapter> extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser mCurrentUser;
    private FirebaseStorage storage;

    private static final String TAG = "All Toronto Discussion";

    private String mUID, mFullName, mCurrentPhotoPath, mTimeStamp, mTopic, mContent;

    private RecyclerView mRecycleTorontoPostList;
    private PostAdapter mPostAdapter;
    private GridLayoutManager mGridLayoutManager;
    private ArrayList<Post> mTorontoPostList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discussion_toronto, container, false);

        // Set the title of Action Bar
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle("Toronto");

        mUID = DiscussionActivity.uID;
        mFullName = DiscussionActivity.fullName;

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        mTorontoPostList = new ArrayList<Post>();
        mRecycleTorontoPostList = (RecyclerView) rootView.findViewById(R.id.recycler_discussion_toronto);
        mGridLayoutManager = new GridLayoutManager(getActivity(), 1, GridLayoutManager.VERTICAL, false);
        mRecycleTorontoPostList.setLayoutManager(mGridLayoutManager);

        loadTorontoPosts();

        return rootView;
    }

    private void loadTorontoPosts() {
        mTorontoPostList.clear();
        CollectionReference collectionReference = db.collection("posts");
        collectionReference.orderBy("timeStamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post post = document.toObject(Post.class);
                                mTorontoPostList.add(post);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        Log.d(TAG, "all global posts:" + mTorontoPostList.toString());
                        mPostAdapter = new PostAdapter(getActivity(), mTorontoPostList);
                        DiscussionActivity.postAdapter = mPostAdapter;
                        mRecycleTorontoPostList.setAdapter(mPostAdapter);
                        mRecycleTorontoPostList.setHasFixedSize(true);
                    }
                });
    }
}
