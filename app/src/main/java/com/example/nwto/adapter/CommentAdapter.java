package com.example.nwto.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.R;
import com.example.nwto.model.Comment;
import com.example.nwto.model.Post;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    List<Comment> commentList;
    LayoutInflater inflater;
    Context ctx;

    public CommentAdapter(Context ctx, List<Comment> commentList) {
        this.ctx = ctx;
        this.commentList = commentList;
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_layout_comment, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get().load(commentList.get(position).getProfilePic()).into(holder.imageAvatar);
        holder.name.setText(commentList.get(position).getFullName());
        holder.content.setText(commentList.get(position).getContent());
        String elapsedTime = getElapsedTime(commentList.get(position).getTimeStamp());
        holder.time.setText("Posted " + elapsedTime + " ago");
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAvatar;
        TextView name, time, content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.image_avatar);
            name = itemView.findViewById(R.id.text_name);
            time = itemView.findViewById(R.id.text_time);
            content = itemView.findViewById(R.id.text_content);
        }
    }

    private String getElapsedTime(String timeStamp) {
        String elapsedTime = "";
        long milliseconds = System.currentTimeMillis() - Long.parseLong(timeStamp);
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        int days = (int) ((milliseconds / (1000 * 60 * 60)) / 24);

        if (seconds > 0 && minutes == 0 && hours == 0 && days == 0) {
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
