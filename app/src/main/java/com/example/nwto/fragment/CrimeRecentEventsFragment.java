package com.example.nwto.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nwto.CrimeStatsActivity;
import com.example.nwto.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CrimeRecentEventsFragment extends Fragment {
    private static final String TAG = "TAG: " + CrimeRecentEventsFragment.class.getSimpleName();

    private FloatingActionButton mFilterButton;
    private RecyclerView mCrimeRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_crime_recent_events, container, false);
        CrimeStatsActivity crimeStatsActivity = (CrimeStatsActivity) getActivity();

        // Initializes Layout Variables
        mFilterButton = (FloatingActionButton) view.findViewById(R.id.crimestats_filter_button);
        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crimestats_crimes_recyclerView);

        // Assigns Crime Adapter and Filter button listener
        mCrimeRecyclerView.setAdapter(crimeStatsActivity.getCrimeAdapter());
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(crimeStatsActivity));
        // mFilterButton.setOnClickListener(view1 -> crimeStatsActivity.openFilterDialog()); // TODO: remove
        mFilterButton.setOnClickListener(view1 -> CrimeFilterDialog.display(getFragmentManager()));
        return view;
    }
}
