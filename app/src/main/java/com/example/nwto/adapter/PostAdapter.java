package com.example.nwto.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.R;
import com.example.nwto.model.Post;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{
    List<Post> postList;
    LayoutInflater inflater;
    Context ctx;

    public PostAdapter(Context ctx, List<Post> postList){
        this.ctx = ctx;
        this.postList = postList;
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_layout_discussion,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Picasso.get().load(postList.get(position).getStorageRef()).into(holder.postView);
//        Glide.with(this).load(postList.get(position).getStorageRef()).into(holder.postView);
//        holder.postView.setOnClickListener(new View.OnClickListener() {
        holder.topicAndArea.setText(postList.get(position).getTopic());
//        holder.nameAndTime.setText(postList.get(position).getFullName());
        holder.content.setText(postList.get(position).getContent());
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ctx, CommentActivity.class);
//                intent.putExtra("postURL", postList.get(position).getStorageRef());
//                intent.putExtra("uID", postList.get(position).getUID());
//                intent.putExtra("caption", postList.get(position).getCaption());
//                ctx.startActivity(intent);
//            }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageAvatar;
        TextView topicAndArea, nameAndTime, content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.image_avatar);
            topicAndArea = itemView.findViewById(R.id.text_topic_and_area);
            nameAndTime = itemView.findViewById(R.id.text_name_and_time);
            content = itemView.findViewById(R.id.text_content);
        }
    }
}
