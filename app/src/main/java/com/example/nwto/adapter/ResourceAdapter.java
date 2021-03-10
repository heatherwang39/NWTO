package com.example.nwto.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.R;
import com.example.nwto.model.Resource;

import java.util.List;

public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.MyViewHolder> {
    private List<Resource> resources;

    public ResourceAdapter(List<Resource> resources) {
        this.resources = resources;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout_resource, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // set behaviour for each resource card
        Resource resource = resources.get(position);
        holder.mTitle.setText(resource.getTitle());
        holder.mName.setText(resource.getName());
        holder.mEmail.setText(resource.getEmail());
        holder.mPhoneNumb.setText(resource.getPhoneNumb());
        if (position % 2 == 0) holder.mCardView.setCardBackgroundColor(Color.parseColor("#2196F3"));
        else holder.mCardView.setCardBackgroundColor(Color.parseColor("#38B7F1"));
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    // Class for one list (single comment item)
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle, mName, mEmail, mPhoneNumb;
        public CardView mCardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.resources_textView_title);
            mName = (TextView) itemView.findViewById(R.id.resources_textView_name);
            mEmail = (TextView) itemView.findViewById(R.id.resources_textView_email);
            mPhoneNumb = (TextView) itemView.findViewById(R.id.resources_textView_phoneNumb);
            mCardView = (CardView) itemView.findViewById(R.id.resources_cardView);
        }
    }
}
