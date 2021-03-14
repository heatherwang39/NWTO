package com.example.nwto.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.R;
import com.example.nwto.model.TableBox;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.MyViewHolder> {
    private Context context;
    private List<TableBox> table;

    public TableAdapter(Context context, List<TableBox> table) {
        this.context = context;
        this.table = table;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout_table, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TableBox tableBox = table.get(position);
        holder.mTextBox.setText(tableBox.getText());
        holder.mTextBox.setBackgroundColor(tableBox.getBackgroundColor());
    }

    @Override
    public int getItemCount() {
        return table.size();
    }

    // Class for one grid (single table box)
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public MaterialButton mTextBox;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextBox = (MaterialButton) itemView.findViewById(R.id.crimestats_textBox);
        }
    }
}

