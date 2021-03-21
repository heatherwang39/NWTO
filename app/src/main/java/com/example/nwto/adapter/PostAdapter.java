package com.example.nwto.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.DiscussionDetailActivity;
import com.example.nwto.R;
import com.example.nwto.model.Post;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    List<Post> postList;
    LayoutInflater inflater;
    Context ctx;

    public PostAdapter(Context ctx, List<Post> postList) {
        this.ctx = ctx;
        this.postList = postList;
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_layout_discussion, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String elapsedTime = getElapsedTime(postList.get(position).getTimeStamp());
        Picasso.get().load(postList.get(position).getProfilePic()).into(holder.imageAvatar);
        holder.topic.setText(postList.get(position).getTopic());
        holder.neighbourhood.setText("Neighbourhood: " + postList.get(position).getNeighbourhood());
        holder.name.setText(postList.get(position).getFullName());
        holder.time.setText("Posted " + elapsedTime);
        String content = postList.get(position).getContent();
        if (content.length() > 82) {
            holder.content.setText(content.substring(0, 82) + "...");
        } else {
            holder.content.setText(content);
        }

        holder.cardPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, DiscussionDetailActivity.class);
                intent.putExtra("topic", postList.get(position).getTopic());
                intent.putExtra("content", postList.get(position).getContent());
                intent.putExtra("crimeType", postList.get(position).getCrimeType());
                intent.putExtra("neighbourhood", postList.get(position).getNeighbourhood());
                intent.putExtra("nameAndTime", postList.get(position).getFullName()+" " + elapsedTime);
                intent.putExtra("postPic",postList.get(position).getPostPic());
                ctx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardPost;
        ImageView imageAvatar;
        TextView topic, neighbourhood, name, time, content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPost = itemView.findViewById(R.id.card_discussion_post);
            imageAvatar = itemView.findViewById(R.id.image_avatar);
            topic = itemView.findViewById(R.id.text_topic);
            neighbourhood = itemView.findViewById(R.id.text_area);
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
            elapsedTime = String.valueOf(seconds) + " s ago";
        } else if (minutes > 0 && hours == 0 && days == 0) {
            elapsedTime = String.valueOf(minutes) + " m ago";
        } else if (hours > 0 && days == 0) {
            elapsedTime = String.valueOf(hours) + " h ago";
        } else if (days > 0) {
            elapsedTime = String.valueOf(days) + " d ago";
        }

        return elapsedTime;
    }
}
