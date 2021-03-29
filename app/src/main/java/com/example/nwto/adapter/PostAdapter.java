package com.example.nwto.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.DiscussionDetailActivity;
import com.example.nwto.R;
import com.example.nwto.model.Post;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> implements Filterable {
    List<Post> postListAll;
    List<Post> postList;
    LayoutInflater inflater;
    Context ctx;

    public PostAdapter(Context ctx, List<Post> postList) {
        this.ctx = ctx;
        this.postList = postList;
        this.postListAll = new ArrayList<>(postList);
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
        holder.time.setText("Posted " + elapsedTime + " ago");
        holder.crimeType.setText("Crime Type: " + postList.get(position).getCrimeType());
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
                intent.putExtra("ownerUID", postList.get(position).getOwnerUID());
                intent.putExtra("postTimeStamp", postList.get(position).getTimeStamp());
                intent.putExtra("topic", postList.get(position).getTopic());
                intent.putExtra("content", postList.get(position).getContent());
                intent.putExtra("crimeType", postList.get(position).getCrimeType());
                intent.putExtra("neighbourhood", postList.get(position).getNeighbourhood());
                intent.putExtra("nameAndTime", postList.get(position).getFullName() + " " + elapsedTime);
                intent.putExtra("postPic", postList.get(position).getPostPic());
                ctx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        //run on the background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Post> filteredList = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                //if constraint is empty, return all posts
                filteredList.addAll(postListAll);
            } else {
                for (Post post : postListAll) {
                    //if the post's topic, content, or poster's full name, neighbourhood contains the contraint, then return
                    if (post.getTopic().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            post.getContent().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            post.getFullName().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            post.getNeighbourhood().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            post.getCrimeType().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredList.add(post);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        //runs on a ui thread
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            postList.clear();
            postList.addAll((Collection<? extends Post>) results.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardPost;
        ImageView imageAvatar;
        TextView topic, neighbourhood, name, time, content, crimeType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPost = itemView.findViewById(R.id.card_discussion_post);
            imageAvatar = itemView.findViewById(R.id.image_avatar);
            topic = itemView.findViewById(R.id.text_topic);
            neighbourhood = itemView.findViewById(R.id.text_area);
            name = itemView.findViewById(R.id.text_name);
            time = itemView.findViewById(R.id.text_time);
            content = itemView.findViewById(R.id.text_content);
            crimeType = itemView.findViewById(R.id.text_crime_type);
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
            elapsedTime = (hours == 1) ? value + " hour" : value + " hours";
        } else if (days > 0) {
            String value = String.valueOf(days);
            elapsedTime = (days == 1) ? value + " day" : value + " days";
        }

        return elapsedTime;
    }
}
