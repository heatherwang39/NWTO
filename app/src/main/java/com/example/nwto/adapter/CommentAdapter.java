package com.example.nwto.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.DiscussionActivity;
import com.example.nwto.DiscussionDetailActivity;
import com.example.nwto.NeighboursActivity;
import com.example.nwto.R;
import com.example.nwto.model.Comment;
import com.example.nwto.model.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Comment> commentList;
    private LayoutInflater inflater;
    private Context ctx;
    private FirebaseFirestore db;
    private String mCurrentUID;
    private boolean mIsAdmin;

    public CommentAdapter(Context ctx, List<Comment> commentList) {
        this.ctx = ctx;
        this.commentList = commentList;
        this.inflater = LayoutInflater.from(ctx);
        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();
        mCurrentUID = DiscussionActivity.uID;
        mIsAdmin = DiscussionActivity.isAdmin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_layout_comment, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(commentList.get(position).getProfilePic() != null){
            Picasso.get().load(commentList.get(position).getProfilePic()).into(holder.imageAvatar);
        }
        holder.name.setText(commentList.get(position).getFullName());
        holder.content.setText(commentList.get(position).getContent());
        String elapsedTime = getElapsedTime(commentList.get(position).getTimeStamp());
        holder.time.setText("Posted " + elapsedTime + " ago");

        if(mCurrentUID.equals(commentList.get(position).getCommenterUID()) || mIsAdmin){
            holder.buttonDeleteComment.setVisibility(View.VISIBLE);
        }
        holder.buttonDeleteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete the neighbour
                db.collection("comments") //TODO: this check is not enough
                        .whereEqualTo("timeStamp", commentList.get(position).getTimeStamp())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                db.collection("comments").document(document.getId()).delete();
                                Toast.makeText(ctx, "Comment deleted.", Toast.LENGTH_SHORT).show();
                                Log.d("Delete comment", "Successfully deleting comment document: "+document.getId());
                                commentList.remove(position);
                                notifyDataSetChanged();
                            }
                        } else {
                            Log.d("Delete comment", "Error getting comment documents when deleting: ", task.getException());
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAvatar, buttonDeleteComment;
        TextView name, time, content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.image_avatar);
            name = itemView.findViewById(R.id.text_name);
            time = itemView.findViewById(R.id.text_time);
            content = itemView.findViewById(R.id.text_content);
            buttonDeleteComment = itemView.findViewById(R.id.button_delete_comment);
        }
    }

    private String getElapsedTime(String timeStamp) {
        String elapsedTime = "";
        long milliseconds = System.currentTimeMillis() - Long.parseLong(timeStamp);
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        int days = (int) ((milliseconds / (1000 * 60 * 60)) / 24);

        if (seconds >= 0 && minutes == 0 && hours == 0 && days == 0) {
            elapsedTime = String.valueOf(seconds) + " s";
        } else if (minutes > 0 && hours == 0 && days == 0) {
            String value = String.valueOf(minutes);
            elapsedTime = (minutes == 1) ? value + " minute" : value + " minutes";
        } else if (hours > 0 && days == 0) {
            String value = String.valueOf(hours);
            elapsedTime = (hours == 1) ? value + " hour" : value + " hours" ;
        } else if (days > 0) {
            String value = String.valueOf(days);
            elapsedTime = (days == 1) ? value + " day" : value + " days" ;
        }

        return elapsedTime;
    }
}
