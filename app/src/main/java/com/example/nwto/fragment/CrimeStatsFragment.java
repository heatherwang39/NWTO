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
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
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
        mTableRecyclerView_mode1.setLayoutManager(new GridLayoutManager(mCrimeStatsActivity, CrimeStatsActivity.NUMB_COL_MODE1));
        mTableRecyclerView_mode2.setAdapter(mCrimeStatsActivity.getTableAdapter_mode2());
        mTableRecyclerView_mode2.setLayoutManager(new GridLayoutManager(mCrimeStatsActivity, CrimeStatsActivity.NUMB_COL_MODE2));

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
                view.getResources().getColor(R.color.crimeCard1)
        };

        displayGraphMode1(colors);
        displayGraphMode2(colors);
        return view;
    }

    private void displayGraphMode1(int[] colors) {//
        List<TableBox> table = mCrimeStatsActivity.getTable_mode1();
        int rowNumb = table.size() / CrimeStatsActivity.NUMB_COL_MODE1 - 1; // excluding the header row
        DataPoint[] monthlyAvgDataPoints = new DataPoint[rowNumb];
        DataPoint[] lastMonthDataPoints = new DataPoint[rowNumb];
        String[] xLabel = new String[rowNumb];

        // initializes renderers
        GridLabelRenderer gridRenderer = mGraphView_mode1.getGridLabelRenderer();
        LegendRenderer legendRenderer = mGraphView_mode1.getLegendRenderer();
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(mGraphView_mode1);
        staticLabelsFormatter.setHorizontalLabels(xLabel);

        // builds plots
        for (int i = 0; i < rowNumb; i++) {
            int rowIndex = CrimeStatsActivity.NUMB_COL_MODE1 * (i + 1);
            xLabel[i] = table.get(rowIndex).getText(); // finds x label (Crime Types)

            TableBox monthlyAvg = table.get(rowIndex + 1); // plot 1
            monthlyAvgDataPoints[i] = new DataPoint(i, Double.parseDouble(monthlyAvg.getText()));

            TableBox lastMonth = table.get(rowIndex + 3); // plot 2
            lastMonthDataPoints[i] = new DataPoint(i, Double.parseDouble(lastMonth.getText()));
        }

        // plot 1
        LineGraphSeries<DataPoint> dataEntry_monthlyAvg = new LineGraphSeries<>(monthlyAvgDataPoints);
        dataEntry_monthlyAvg.setColor(colors[0]);
        dataEntry_monthlyAvg.setTitle("2018-19 Monthly Avg");
        dataEntry_monthlyAvg.setDrawDataPoints(true);
        mGraphView_mode1.addSeries(dataEntry_monthlyAvg);

        // plot 2
        LineGraphSeries<DataPoint> dataEntry_lastMonth = new LineGraphSeries<>(lastMonthDataPoints);
        dataEntry_monthlyAvg.setColor(colors[1]);
        dataEntry_lastMonth.setTitle("Last Month");
        dataEntry_lastMonth.setDrawDataPoints(true);
        mGraphView_mode1.addSeries(dataEntry_lastMonth);

        // sets renderers settings
        gridRenderer.setLabelFormatter(staticLabelsFormatter);
        gridRenderer.setLabelsSpace(15);
        gridRenderer.setHorizontalLabelsAngle(120);
        legendRenderer.setVisible(true);
        legendRenderer.setBackgroundColor(colors[colors.length - 1]);
        legendRenderer.setAlign(LegendRenderer.LegendAlign.TOP);
    }

    private void displayGraphMode2(int[] colors) {
        List<TableBox> table = mCrimeStatsActivity.getTable_mode2();
        String[] crimeTypes = CrimeStatsActivity.STUB; // Y- Axis
        int[] years = CrimeStatsActivity.COLUMN_MODE2; // X - Axis
        int rowLength = crimeTypes.length;
        int colLength = years.length;

        // finds x labels
        String[] xLabel = new String[colLength];
        for (int i = 0; i < colLength; i++) xLabel[i] = Integer.toString(years[i]);

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
                plot[j] = new DataPoint(j, Double.parseDouble(entry.getText()));
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
        gridRenderer.setHorizontalLabelsAngle(120);
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
