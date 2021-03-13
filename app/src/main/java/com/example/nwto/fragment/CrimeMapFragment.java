package com.example.nwto.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nwto.CrimeStatsActivity;
import com.example.nwto.R;
import com.example.nwto.model.Crime;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class CrimeMapFragment extends Fragment {
    private MapView mapView;
    private IMapController mapController;
    private CrimeStatsActivity crimeStatsActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_crime_map, container, false);
        crimeStatsActivity = (CrimeStatsActivity) getActivity();

        // ensures that the map has a writable location for the map cache
        Context ctx = crimeStatsActivity.getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView = (MapView) view.findViewById(R.id.crimestats_map);
        mapController = mapView.getController();
        initializeMap();
        displayCrimeMarkers();

        return view;
    }

    private void initializeMap() {
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setMultiTouchControls(true);
        mapView.setHorizontalMapRepetitionEnabled(false);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.setScrollableAreaLimitLatitude(43.833104, 43.596233, 20);
        mapView.setScrollableAreaLimitLongitude(-79.646647, -79.059209, 20);
        mapView.setMinZoomLevel(10.0);
        mapController.setCenter(crimeStatsActivity.getStartingPoint());
        mapController.setZoom(15.0);
    }


    private void displayPoliceBoundaries() {

    }

    private void displayCrimeMarkers() {
        final List<Crime> crimes = crimeStatsActivity.getCrimes();
        for (final Crime crime : crimes) {
            GeoPoint location = new GeoPoint(crime.getLatitude(), crime.getLongitude());
            Marker crimeMarker = new Marker(mapView);
            crimeMarker.setPosition(location);
//            MarkerInfoWindow infoWindow = new MarkerInfoWindow()
//            crimeMarker.setInfoWindow();
            crimeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(crimeMarker);
        }
    }
}
