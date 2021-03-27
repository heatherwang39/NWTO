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
import com.example.nwto.model.TableBox;
import com.google.android.material.button.MaterialButton;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

public class CrimeStatsFragment extends Fragment {
    private MaterialButton mMode1Button, mMode2Button, mTableButton, mGraphButton;
    private RecyclerView mTableRecyclerView_mode1, mTableRecyclerView_mode2;
    private GraphView mGraphView_mode1, mGraphView_mode2;

    private CrimeStatsActivity mCrimeStatsActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_crime_stats, container, false);
        mCrimeStatsActivity = (CrimeStatsActivity) getActivity();

        // Initializes Layout variables
        mMode1Button = (MaterialButton) view.findViewById(R.id.crimestats_mode1);
        mMode2Button = (MaterialButton) view.findViewById(R.id.crimestats_mode2);
        mTableButton = (MaterialButton) view.findViewById(R.id.crimestats_byTable);
        mGraphButton = (MaterialButton) view.findViewById(R.id.crimestats_byGraph);
        mTableRecyclerView_mode1 = (RecyclerView) view.findViewById(R.id.crimestats_tableMode1_recylcerView);
        mTableRecyclerView_mode2 = (RecyclerView) view.findViewById(R.id.crimestats_tableMode2_recylcerView);
        mGraphView_mode1 = (GraphView) view.findViewById(R.id.crimestats_graphMode1_graphView);
        mGraphView_mode2 = (GraphView) view.findViewById(R.id.crimestats_graphMode2_graphView);

        // Assigns adapters to recycler views for Display By Table
        mTableRecyclerView_mode1.setAdapter(mCrimeStatsActivity.getTableAdapter_mode1());
        mTableRecyclerView_mode1.setLayoutManager(new GridLayoutManager(mCrimeStatsActivity, CrimeStatsActivity.HEADER_MODE1.length));
        mTableRecyclerView_mode2.setAdapter(mCrimeStatsActivity.getTableAdapter_mode2());
        mTableRecyclerView_mode2.setLayoutManager(new GridLayoutManager(mCrimeStatsActivity, CrimeStatsActivity.HEADER_MODE2.length));

        // Assigns on click listeners to buttons
        mMode1Button.setOnClickListener(view1 -> clickMode1());
        mMode2Button.setOnClickListener(view1 -> clickMode2());
        mTableButton.setOnClickListener(view1 -> clickDisplayByTable());
        mGraphButton.setOnClickListener(view1 -> clickDisplayByGraph());

        int[] colors = new int[] {
                view.getResources().getColor(R.color.blue),
                view.getResources().getColor(R.color.purple_200),
                view.getResources().getColor(R.color.green),
                view.getResources().getColor(R.color.grey),
                view.getResources().getColor(R.color.yellow),
                view.getResources().getColor(R.color.red),
                view.getResources().getColor(R.color.black),
                view.getResources().getColor(R.color.crimeCard1)
        };

        displayGraphMode1(colors);
        displayGraphMode2(colors);
        return view;
    }

    private void displayGraphMode1(int[] colors) {//
        List<TableBox> table = mCrimeStatsActivity.getTable_mode1();
        int rowNumb = table.size() / CrimeStatsActivity.HEADER_MODE1.length - 1; // excluding the header row
        DataPoint[] monthlyAvgDataPoints = new DataPoint[rowNumb];
        DataPoint[] lastMonthDataPoints = new DataPoint[rowNumb];
        String[] xLabel = new String[rowNumb + 1];
        xLabel[0] = "";

        // initializes renderers
        GridLabelRenderer gridRenderer = mGraphView_mode1.getGridLabelRenderer();
        LegendRenderer legendRenderer = mGraphView_mode1.getLegendRenderer();
        Viewport viewport = mGraphView_mode1.getViewport();
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(mGraphView_mode1);
        staticLabelsFormatter.setHorizontalLabels(xLabel);

        // builds plots
        for (int i = 0; i < rowNumb; i++) {
            int rowIndex = CrimeStatsActivity.HEADER_MODE1.length * (i + 1);
            xLabel[i+1] = table.get(rowIndex).getText(); // finds x label (Crime Types)

            TableBox monthlyAvg = table.get(rowIndex + 1); // plot 1
            String monthlyAvgVal = monthlyAvg.getText();
            if (monthlyAvgVal.equals(CrimeStatsActivity.EMPTY_VALUE)) monthlyAvgVal = "0.0";
            monthlyAvgDataPoints[i] = new DataPoint(i, Double.parseDouble(monthlyAvgVal));

            TableBox lastMonth = table.get(rowIndex + 3); // plot 2
            String lastMonthVal = lastMonth.getText();
            if (lastMonthVal.equals(CrimeStatsActivity.EMPTY_VALUE)) lastMonthVal = "0.0";
            lastMonthDataPoints[i] = new DataPoint(i, Double.parseDouble(lastMonthVal));
        }

        // plot 1
        BarGraphSeries<DataPoint> dataEntry_monthlyAvg = new BarGraphSeries<>(monthlyAvgDataPoints);
        dataEntry_monthlyAvg.setColor(colors[0]);
        dataEntry_monthlyAvg.setTitle(CrimeStatsActivity.HEADER_MODE1[1]);
        dataEntry_monthlyAvg.setDataWidth(0.5);
        mGraphView_mode1.addSeries(dataEntry_monthlyAvg);

        // plot 2
        BarGraphSeries<DataPoint> dataEntry_lastMonth = new BarGraphSeries<>(lastMonthDataPoints);
        dataEntry_monthlyAvg.setColor(colors[1]);
        dataEntry_lastMonth.setTitle(CrimeStatsActivity.HEADER_MODE1[3]);
        dataEntry_lastMonth.setDataWidth(0.5);
        mGraphView_mode1.addSeries(dataEntry_lastMonth);

        // sets renderers settings
        gridRenderer.setLabelFormatter(staticLabelsFormatter);
//        gridRenderer.setLabelsSpace(20);
        gridRenderer.setHorizontalLabelsAngle(110);
        gridRenderer.setHighlightZeroLines(false);
        viewport.setMinX(-1);
        viewport.setXAxisBoundsManual(true);
        legendRenderer.setVisible(true);
        legendRenderer.setBackgroundColor(colors[colors.length - 1]);
        legendRenderer.setAlign(LegendRenderer.LegendAlign.TOP);
    }

    private void displayGraphMode2(int[] colors) {
        List<TableBox> table = mCrimeStatsActivity.getTable_mode2();
        String[] crimeTypes = CrimeStatsActivity.STUB; // Y- Axis
        int[] years = CrimeStatsActivity.YEARS; // X - Axis
        int rowLength = crimeTypes.length;
        int colLength = CrimeStatsActivity.HEADER_MODE2.length - 1; // excluding "Types" col

        // finds x labels
        String[] xLabel = new String[colLength];
        for (int i = 0; i < colLength; i++) {
            if (i == colLength - 1)
                xLabel[i] = CrimeStatsActivity.HEADER_MODE2[4];
            else
                xLabel[i] = Integer.toString(years[i]); // 2018, 2019, 2020
        }

        // initializes renderers
        GridLabelRenderer gridRenderer = mGraphView_mode2.getGridLabelRenderer();
        LegendRenderer legendRenderer = mGraphView_mode2.getLegendRenderer();
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(mGraphView_mode2);
        staticLabelsFormatter.setHorizontalLabels(xLabel);

        // builds plots
        for (int i = 0; i < rowLength; i++) {
            int row = i + 1;
            DataPoint[] plot = new DataPoint[colLength];
            String plotName = crimeTypes[i];
            for (int j = 0; j < colLength; j++) {
                int col = j + 1;
                TableBox entry = table.get(row * (colLength + 1) + col);
                String entryVal = entry.getText();
                if (entryVal.equals(CrimeStatsActivity.EMPTY_VALUE)) entryVal = "0.0";
                plot[j] = new DataPoint(j, Double.parseDouble(entryVal));
            }
            LineGraphSeries<DataPoint> dataSeries = new LineGraphSeries<>(plot);
            dataSeries.setColor(colors[i % colors.length]);
            dataSeries.setTitle(plotName);
            dataSeries.setDrawDataPoints(true);
            mGraphView_mode2.addSeries(dataSeries);
        }

        // sets renderers settings
        gridRenderer.setLabelFormatter(staticLabelsFormatter);
        gridRenderer.setLabelsSpace(15);
        gridRenderer.setHorizontalLabelsAngle(110);
        gridRenderer.setHighlightZeroLines(false);
        legendRenderer.setVisible(true);
        legendRenderer.setBackgroundColor(colors[colors.length - 1]);
        legendRenderer.setAlign(LegendRenderer.LegendAlign.TOP);
    }

    private void clickMode1() {
        mMode2Button.setBackgroundColor(getResources().getColor(R.color.windowBackground));
        mMode2Button.setTextColor(getResources().getColor(R.color.colorAccent));
        mMode1Button.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mMode1Button.setTextColor(getResources().getColor(R.color.white));
        if (mTableButton.getCurrentTextColor() == getResources().getColor(R.color.windowBackground)) {
            // Mode 1 Table
            mTableRecyclerView_mode1.setVisibility(View.VISIBLE);
            mGraphView_mode1.setVisibility(View.GONE);
        }
        else {
            // Mode 1 Graph
            mTableRecyclerView_mode1.setVisibility(View.GONE);
            mGraphView_mode1.setVisibility(View.VISIBLE);
        }
        mTableRecyclerView_mode2.setVisibility(View.GONE);
        mGraphView_mode2.setVisibility(View.GONE);
    }

    private void clickMode2() {
        mMode1Button.setBackgroundColor(getResources().getColor(R.color.windowBackground));
        mMode1Button.setTextColor(getResources().getColor(R.color.colorAccent));
        mMode2Button.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mMode2Button.setTextColor(getResources().getColor(R.color.white));
        if (mTableButton.getCurrentTextColor() == getResources().getColor(R.color.windowBackground)) {
            // Mode 2 Table
            mTableRecyclerView_mode2.setVisibility(View.VISIBLE);
            mGraphView_mode2.setVisibility(View.GONE);
        }
        else {
            // Mode 2 Graph
            mTableRecyclerView_mode2.setVisibility(View.GONE);
            mGraphView_mode2.setVisibility(View.VISIBLE);
        }
        mTableRecyclerView_mode1.setVisibility(View.GONE);
        mGraphView_mode1.setVisibility(View.GONE);
    }

    private void clickDisplayByTable() {
        mGraphButton.setBackgroundColor(getResources().getColor(R.color.windowBackground));
        mGraphButton.setTextColor(getResources().getColor(R.color.colorAccent));
        mTableButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mTableButton.setTextColor(getResources().getColor(R.color.white));
        if (mMode1Button.getCurrentTextColor() == getResources().getColor(R.color.windowBackground)) {
            // Mode 1 Table
            mTableRecyclerView_mode1.setVisibility(View.VISIBLE);
            mTableRecyclerView_mode2.setVisibility(View.GONE);
        }
        else {
            // Mode 2 Table
            mTableRecyclerView_mode1.setVisibility(View.GONE);
            mTableRecyclerView_mode2.setVisibility(View.VISIBLE);
        }
        mGraphView_mode1.setVisibility(View.GONE);
        mGraphView_mode2.setVisibility(View.GONE);
    }

    private void clickDisplayByGraph() {
        mTableButton.setBackgroundColor(getResources().getColor(R.color.windowBackground));
        mTableButton.setTextColor(getResources().getColor(R.color.colorAccent));
        mGraphButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mGraphButton.setTextColor(getResources().getColor(R.color.white));
        if (mMode1Button.getCurrentTextColor() == getResources().getColor(R.color.windowBackground)) {
            // Mode 1 Graph
            mGraphView_mode1.setVisibility(View.VISIBLE);
            mGraphView_mode2.setVisibility(View.GONE);
        }
        else {
            // Mode 2 Graph
            mGraphView_mode1.setVisibility(View.GONE);
            mGraphView_mode2.setVisibility(View.VISIBLE);
        }
        mTableRecyclerView_mode1.setVisibility(View.GONE);
        mTableRecyclerView_mode2.setVisibility(View.GONE);
    }
}
