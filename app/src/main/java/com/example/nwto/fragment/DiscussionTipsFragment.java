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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class DiscussionTipsFragment extends Fragment {
    private FirebaseFirestore db;

    private static final String TAG = "Tips Discussion";

    private String mUID, mFullName, mNeighbourhoodName;
    private RecyclerView mRecycleTipsList;
    private PostAdapter mPostAdapter;
    private GridLayoutManager mGridLayoutManager;
    private ArrayList<Post> mTipsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discussion_tips, container, false);

        // Set the title of Action Bar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Tips");

        mUID = DiscussionActivity.uID;
        mFullName = DiscussionActivity.fullName;

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        mTipsList = new ArrayList<Post>();
        mRecycleTipsList = (RecyclerView) rootView.findViewById(R.id.recycler_discussion_tips);
        mGridLayoutManager = new GridLayoutManager(getActivity(), 1, GridLayoutManager.VERTICAL, false);
        mRecycleTipsList.setLayoutManager(mGridLayoutManager);

        loadTips();

        return rootView;
    }

    private void loadTips() {
        mTipsList.clear();
        CollectionReference collectionReference = db.collection("posts");
        collectionReference.orderBy("timeStamp", Query.Direction.DESCENDING)
                .whereEqualTo("isTip", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post post = document.toObject(Post.class);
                                mTipsList.add(post);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        mPostAdapter = new PostAdapter(getActivity(), mTipsList);
                        mRecycleTipsList.setAdapter(mPostAdapter);
                        mRecycleTipsList.setHasFixedSize(true);
                    }
                });
    }


}
