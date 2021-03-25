package com.example.nwto.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.R;
import com.example.nwto.model.TableBox;

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TableBox tableBox = table.get(position);
        holder.mTextBox.setText(tableBox.getText());
//        if (tableBox.getBackgroundColor() != context.getResources().getColor(R.color.white))
        holder.mTextBox.getBackground().setTint(tableBox.getBackgroundColor());
//        else {
//            holder.mTextBox.setBackground(context.getDrawable(R.drawable.tablebox_border));
//        }

    }

    @Override
    public int getItemCount() {
        return table.size();
    }

    // Class for one grid (single table box)
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextBox;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextBox = (TextView) itemView.findViewById(R.id.crimestats_textBox);
        }
    }
}

