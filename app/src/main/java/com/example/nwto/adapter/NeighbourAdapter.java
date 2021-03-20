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
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.model.Neighbour;
import com.example.nwto.NeighboursActivity;
import com.example.nwto.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class NeighbourAdapter extends RecyclerView.Adapter<NeighbourAdapter.ViewHolder> {

    List<Neighbour> neighbourList;
    LayoutInflater inflater;
    Context ctx;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public NeighbourAdapter(Context ctx, List<Neighbour> neighbourList){
        this.ctx = ctx;
        this.neighbourList = neighbourList;
        this.inflater = LayoutInflater.from(ctx);
        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_layout_neighbour,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textFullName.setText(neighbourList.get(position).getFullName());
        holder.textEmail.setText(neighbourList.get(position).getEmail());
        holder.textPhoneNumber.setText(neighbourList.get(position).getPhoneNumber());
        holder.imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete the contact
                db.collection("contacts")
                        .whereEqualTo("email", neighbourList.get(position).getEmail())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                db.collection("contacts").document(document.getId()).delete();
                                Toast.makeText(ctx, "Contact deleted.", Toast.LENGTH_SHORT).show();
                                Log.d("Delete Contact", "Successfully deleting contact document: "+document.getId());
                                neighbourList.remove(position);
                                ctx.startActivity(new Intent(ctx, NeighboursActivity.class));
                            }
                        } else {
                            Log.d("Delete Contact", "Error getting comment documents when deleting: ", task.getException());
                        }
                    }
                });
            }
        });

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
        return neighbourList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textFullName;
        TextView textEmail;
        TextView textPhoneNumber;
        ImageView imageEdit;
        ImageView imageDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textFullName = itemView.findViewById(R.id.text_full_name);
            textEmail = itemView.findViewById(R.id.text_email);
            textPhoneNumber = itemView.findViewById(R.id.text_phone_number);
            imageEdit = itemView.findViewById(R.id.image_edit);
            imageDelete = itemView.findViewById(R.id.image_delete);
        }
    }


}
