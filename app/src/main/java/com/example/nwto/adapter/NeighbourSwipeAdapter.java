package com.example.nwto.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.example.nwto.NeighboursActivity;
import com.example.nwto.R;
import com.example.nwto.model.Neighbour;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NeighbourSwipeAdapter extends RecyclerView.Adapter<NeighbourSwipeAdapter.SwipeViewHolder> {

    // 1. Making swipe adaper constructor and implement the methods
    private List<Neighbour> neighbourList;
    private LayoutInflater inflater;
    private Context ctx;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public NeighbourSwipeAdapter(Context ctx, List<Neighbour> neighbourList){
        this.ctx = ctx;
        this.neighbourList = neighbourList;
        this.inflater = LayoutInflater.from(ctx);
        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();
    }

    public void setNeighbours(ArrayList<Neighbour> neighbourList){
        this.neighbourList = new ArrayList<>();
        this.neighbourList = neighbourList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SwipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_neighbour_swipe,parent,false);
        return new SwipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SwipeViewHolder holder, int position) {
        viewBinderHelper.setOpenOnlyOne(true);
        viewBinderHelper.bind(holder.swipeRevealLayout,String.valueOf(neighbourList.get(position).getFullName()));
        viewBinderHelper.closeLayout(String.valueOf(neighbourList.get(position).getFullName()));
        holder.bindData(neighbourList.get(position));

        // Handling the click events

        holder.textEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ctx,"Edit is clicked",Toast.LENGTH_SHORT).show();
            }
        });
        holder.textDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ctx,"delete is clicked",Toast.LENGTH_SHORT).show();
                //delete the contact
                db.collection("neighbours")
                        .whereEqualTo("email", neighbourList.get(position).getEmail())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                db.collection("neighbours").document(document.getId()).delete();
                                Toast.makeText(ctx, "Neighbour deleted.", Toast.LENGTH_SHORT).show();
                                Log.d("Delete neighbour", "Successfully deleting neighbour document: "+document.getId());
                                neighbourList.remove(position);
                                ctx.startActivity(new Intent(ctx, NeighboursActivity.class));
                            }
                        } else {
                            Log.d("Delete neighbour", "Error getting neighbour documents when deleting: ", task.getException());
                        }
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return neighbourList.size();
    }


    // 2. ViewHolder: SwipeViewHolder
    class SwipeViewHolder extends RecyclerView.ViewHolder{
        SwipeRevealLayout swipeRevealLayout;

        //in swipe layout
        TextView textEdit;
        TextView textDelete;

        //in main layout
        TextView textFullName;
        TextView textEmail;
        TextView textPhoneNumber;
        ImageView imageEdit;
        ImageView imageDelete;

        public SwipeViewHolder(@NonNull View itemView) {
            super(itemView);
            swipeRevealLayout = itemView.findViewById(R.id.swipe_layout);
            textEdit = itemView.findViewById(R.id.text_edit);
            textDelete = itemView.findViewById(R.id.text_delete);
            textFullName = itemView.findViewById(R.id.text_full_name);
            textEmail = itemView.findViewById(R.id.text_email);
            textPhoneNumber = itemView.findViewById(R.id.text_phone_number);
            imageEdit = itemView.findViewById(R.id.image_edit);
            imageDelete = itemView.findViewById(R.id.image_delete);


        }

        void bindData(Neighbour neighbour){
            textFullName.setText(neighbour.getFullName());
            textEmail.setText(neighbour.getEmail());
            textPhoneNumber.setText(neighbour.getPhoneNumber());
        }


    }


}
