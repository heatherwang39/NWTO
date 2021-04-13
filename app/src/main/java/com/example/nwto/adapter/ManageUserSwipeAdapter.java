package com.example.nwto.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.example.nwto.R;
import com.example.nwto.model.RegisteredUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUserSwipeAdapter extends RecyclerView.Adapter<ManageUserSwipeAdapter.SwipeViewHolder> {
    private static final String TAG = "Manage User";

    private List<RegisteredUser> registeredUserList;
    private LayoutInflater inflater;
    private Context ctx;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ManageUserSwipeAdapter(Context ctx, List<RegisteredUser> registeredUserList) {
        this.ctx = ctx;
        this.registeredUserList = registeredUserList;
        this.inflater = LayoutInflater.from(ctx);
        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();
    }

    public void setRegisteredUsers(ArrayList<RegisteredUser> registeredUserList) {
        this.registeredUserList = new ArrayList<>();
        this.registeredUserList = registeredUserList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SwipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.custom_layout_manage_user, parent, false);
        return new SwipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SwipeViewHolder holder, int position) {
        viewBinderHelper.setOpenOnlyOne(true);
        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(registeredUserList.get(position).getFullName()));
        viewBinderHelper.closeLayout(String.valueOf(registeredUserList.get(position).getFullName()));
        holder.bindData(registeredUserList.get(position));


        holder.textDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete the registered User
                db.collection("users") //TODO: this check is not enough
                        .whereEqualTo("email", registeredUserList.get(position).getEmail())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String selectedUserId = document.getId();
                                //delete the user
                                //notes: it will only delete the user object in the firestore, not in firebase auth
                                //Firebase only allow to delete already signed-in user
                                db.collection("users").document(document.getId()).delete();
                                Toast.makeText(ctx, "Registered User deleted.", Toast.LENGTH_SHORT).show();
                                Log.d("Delete User", "Successfully deleting User document: " + document.getId());
                                registeredUserList.remove(position);
                                notifyDataSetChanged();

                                //delete the user's posts
                                db.collection("posts")
                                        .whereEqualTo("ownerUID", selectedUserId)
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : task.getResult()) {
                                                db.collection("posts").document(document.getId()).delete();
                                                Log.d(TAG, "Successfully deleting posts document: " + document.getId());
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting post documents when deleting: ", task.getException());
                                        }
                                    }
                                });
                                //delete the user's comments in their own posts
                                db.collection("comments")
                                        .whereEqualTo("postOwnerUID", selectedUserId)
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : task.getResult()) {
                                                db.collection("comments").document(document.getId()).delete();
                                                Log.d(TAG, "Successfully deleting comment document: " + document.getId());
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting comment documents when deleting: ", task.getException());
                                        }
                                    }
                                });
                                //delete the user's comments in other users' posts
                                db.collection("comments")
                                        .whereEqualTo("commenterUID", selectedUserId)
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : task.getResult()) {
                                                db.collection("comments").document(document.getId()).delete();
                                                Log.d(TAG, "Successfully deleting comment document: " + document.getId());
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting comment documents when deleting: ", task.getException());
                                        }
                                    }
                                });
                            }
                        } else {
                            Log.d("Delete User", "Error getting User documents when deleting: ", task.getException());
                        }
                    }
                });
            }
        });

        holder.textMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ctx, "Mute User is clicked.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return registeredUserList.size();
    }


    class SwipeViewHolder extends RecyclerView.ViewHolder {
        SwipeRevealLayout swipeRevealLayout;

        //in swipe layout
        TextView textMute;
        TextView textDelete;

        //in main layout
        TextView textNeighbourhood, textFullName, textEmail, textPhoneNumber, textIsMuted;


        public SwipeViewHolder(@NonNull View itemView) {
            super(itemView);
            swipeRevealLayout = itemView.findViewById(R.id.swipe_layout);
            textMute = itemView.findViewById(R.id.text_mute);
            textDelete = itemView.findViewById(R.id.text_delete);
            textNeighbourhood = itemView.findViewById(R.id.text_neighbourhood);
            textFullName = itemView.findViewById(R.id.text_full_name);
            textEmail = itemView.findViewById(R.id.text_email);
            textPhoneNumber = itemView.findViewById(R.id.text_phone_number);
            textIsMuted = itemView.findViewById(R.id.text_is_muted);
        }

        void bindData(RegisteredUser registeredUser) {
            textNeighbourhood.setText("Neighbourhood: " + registeredUser.getNeighbourhood());
            textFullName.setText(registeredUser.getFullName());
            textEmail.setText(registeredUser.getEmail());
            textPhoneNumber.setText(registeredUser.getPhoneNumber());
            textIsMuted.setText("Muted: " + String.valueOf(registeredUser.isMuted()));
        }


    }


}
