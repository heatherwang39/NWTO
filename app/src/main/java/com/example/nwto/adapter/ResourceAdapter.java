package com.example.nwto.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.R;
import com.example.nwto.model.Resource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.MyViewHolder> {
    private Context context;
    private List<Resource> resources;

    public ResourceAdapter(Context context, List<Resource> resources) {
        this.context = context;
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
        // sets behaviour for each resource card
        Resource resource = resources.get(position);
        holder.mTitle.setText(resource.getTitle());
        holder.mName.setText(resource.getName());
        holder.mEmail.setText(resource.getEmail());
        holder.mPhoneNumb.setText(resource.getPhoneNumb());
        if (position % 2 == 0) holder.mCardView.setCardBackgroundColor(Color.parseColor("#2196F3"));
        else holder.mCardView.setCardBackgroundColor(Color.parseColor("#38B7F1"));

        // opens Android system's default email or phone features
        holder.mEmailButton.setOnClickListener(new OpenEmail(resource.getEmail()));
        holder.mPhoneButton.setOnClickListener(new OpenPhone(resource.getPhoneNumb()));
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    // Class for one list (single resource item)
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle, mName, mEmail, mPhoneNumb;
        public CardView mCardView;
        public FloatingActionButton mEmailButton, mPhoneButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.resources_textView_title);
            mName = (TextView) itemView.findViewById(R.id.resources_textView_name);
            mEmail = (TextView) itemView.findViewById(R.id.resources_textView_email);
            mPhoneNumb = (TextView) itemView.findViewById(R.id.resources_textView_phoneNumb);
            mCardView = (CardView) itemView.findViewById(R.id.resources_cardView);
            mEmailButton = (FloatingActionButton) itemView.findViewById(R.id.floatingActionButton_email);
            mPhoneButton = (FloatingActionButton) itemView.findViewById(R.id.floatingActionButton_phoneNumb);
        }
    }

    private class OpenEmail implements View.OnClickListener {
        private String emailAddress;

        public OpenEmail(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + emailAddress)); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
            ((Activity) context).startActivity(intent);
        }
    }

    private class OpenPhone implements View.OnClickListener {
        private String phoneNumb;

        public OpenPhone(String phoneNumb) {
            this.phoneNumb = phoneNumb;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumb));
            ((Activity) context).startActivity(intent);
        }

    }
}
