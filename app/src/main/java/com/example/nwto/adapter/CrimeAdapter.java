package com.example.nwto.adapter;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.model.Crime;
import com.example.nwto.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CrimeAdapter extends RecyclerView.Adapter<CrimeAdapter.MyViewHolder> {
    private Context context;
    private List<Crime> crimes;

    public CrimeAdapter(Context context, List<Crime> crimes) {
        this.context = context;
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
        // sets behaviour for each crime info card
        Crime crime = crimes.get(position);
        holder.mUniqueID.setText(crime.getUniqueID());
        holder.mDate.setText(crime.getDate());
        holder.mDivision.setText(crime.getDivision());
        holder.mCrimeType.setText(crime.getCategory());
        holder.mPremiseType.setText(crime.getPremise());
        holder.mGeometry.setText(convertGeometryToAddress(crime.getLatitude(), crime.getLongitude()));

        if (position % 2 == 0) holder.mCardView.setCardBackgroundColor(context.getResources().getColor(R.color.crimeCard1));
        else holder.mCardView.setCardBackgroundColor(context.getResources().getColor(R.color.crimeCard2));
    }

    @Override
    public int getItemCount() {
        return crimes.size();
    }

    // Class for one list (single crime item)
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

    private String convertGeometryToAddress(double latitude, double longitude) {
        String location = "(" + latitude + ", " + longitude + ")";
        Geocoder geocoder = new Geocoder(context, Locale.CANADA);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            location = addresses.get(0).getAddressLine(0);
            location = location.replace(" at ", " & ");
            location = location.split(",")[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }
}
