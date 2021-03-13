package com.example.nwto.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nwto.R;

public class CrimeStatsFragment extends Fragment {
//    private static final String[] YTD_CRIME = new String[] {Assault, Auto Theft, Break and Enter, Homicide, Robbery, Sexual Violation, Shooting, Theft Over};
//    private static final String[] YE_CRIME = new String[] {Assault, Auto Theft, Break and Enter, Robbery, Theft Over};
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_crime_stats, container, false);

        return view;
    }
}
