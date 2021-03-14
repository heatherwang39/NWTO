package com.example.nwto.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.CrimeStatsActivity;
import com.example.nwto.R;

public class CrimeStatsFragment extends Fragment {
    private RecyclerView mTableRecyclerView_mode1;
    private int numCol_mode1 = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_crime_stats, container, false);
        CrimeStatsActivity crimeStatsActivity = (CrimeStatsActivity) getActivity();

        mTableRecyclerView_mode1 = (RecyclerView) view.findViewById(R.id.crimestats_tableMode1_recylcerView);
        mTableRecyclerView_mode1.setAdapter(crimeStatsActivity.getTableAdapter_mode1());
        mTableRecyclerView_mode1.setLayoutManager(new GridLayoutManager(crimeStatsActivity, numCol_mode1));

        return view;
    }
}
