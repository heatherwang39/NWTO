package com.example.nwto.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.model.Crime;
import com.example.nwto.R;

import java.util.List;

public class CrimeAdapter extends RecyclerView.Adapter<CrimeAdapter.MyViewHolder> {
    private List<Crime> crimes;

    public CrimeAdapter(List<Crime> crimes) {
        this.crimes = crimes;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout_crime, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // set behaviour for each resource card
        Crime crime = crimes.get(position);
        holder.mUniqueID.setText(crime.getUniqueID());
        holder.mDate.setText(crime.getDate());
        holder.mDivision.setText(crime.getDivision());
        holder.mCrimeType.setText(crime.getCategory());
        holder.mPremiseType.setText(crime.getPremise());
        holder.mGeometry.setText("(" + crime.getLatitude() + ", " + crime.getLongitude() + ")");

        if (position % 2 == 0) holder.mCardView.setCardBackgroundColor(Color.parseColor("#DAE9F4"));
        else holder.mCardView.setCardBackgroundColor(Color.parseColor("#8CC5EF"));
    }

    @Override
    public int getItemCount() {
        return crimes.size();
    }

    // Class for one list (single comment item)
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mUniqueID, mDate, mDivision, mCrimeType, mPremiseType, mGeometry;
        public CardView mCardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mUniqueID = (TextView) itemView.findViewById(R.id.crime_uniqueID);
            mDate = (TextView) itemView.findViewById(R.id.crime_date);
            mDivision = (TextView) itemView.findViewById(R.id.crime_division);
            mCrimeType = (TextView) itemView.findViewById(R.id.crime_category);
            mPremiseType = (TextView) itemView.findViewById(R.id.crime_premise);
            mGeometry = (TextView) itemView.findViewById(R.id.crime_geometry);
            mCardView = (CardView) itemView.findViewById(R.id.crime_cardView);
        }
    }
}
