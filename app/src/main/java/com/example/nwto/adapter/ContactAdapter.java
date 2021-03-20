package com.example.nwto.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.R;
import com.example.nwto.model.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {
    private Context context;
    private List<Contact> contacts;

    public ContactAdapter(Context context, List<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout_contact, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // sets behaviour for each resource card
        Contact contact = contacts.get(position);
        holder.mTitle.setText(contact.getTitle());
        holder.mName.setText(contact.getName());
        holder.mEmail.setText(contact.getEmail());
        holder.mPhoneNumb.setText(contact.getPhoneNumb());
        if (position % 2 == 0) holder.mCardView.setCardBackgroundColor(context.getResources().getColor(R.color.resourceCard2));
        else holder.mCardView.setCardBackgroundColor(context.getResources().getColor(R.color.resourceCard1));

        // opens Android system's default email or phone features
        holder.mEmailButton.setOnClickListener(new OpenEmail(contact.getEmail()));
        holder.mPhoneButton.setOnClickListener(new OpenPhone(contact.getPhoneNumb()));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
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
