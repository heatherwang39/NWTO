package com.example.nwto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    List<Contact> contactList;
    LayoutInflater inflater;
    Context ctx;

    public ContactAdapter(Context ctx, List<Contact> contactList){
        this.ctx = ctx;
        this.contactList = contactList;
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_layout_contact,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textFullName.setText(contactList.get(position).getFullName());
        holder.textEmail.setText(contactList.get(position).getEmail());
        holder.textPhoneNumber.setText(contactList.get(position).getPhoneNumber());
//        holder.postView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ctx, CommentActivity.class);
//                intent.putExtra("postURL", postList.get(position).getStorageRef());
//                intent.putExtra("uID", postList.get(position).getUID());
//                intent.putExtra("caption", postList.get(position).getCaption());
//                ctx.startActivity(intent);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textFullName;
        TextView textEmail;
        TextView textPhoneNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textFullName = itemView.findViewById(R.id.text_full_name);
            textEmail = itemView.findViewById(R.id.text_email);
            textPhoneNumber = itemView.findViewById(R.id.text_phone_number);
        }
    }


}
