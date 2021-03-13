package com.example.nwto.fragment;

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
import org.osmdroid.views.overlay.Polygon;

import java.util.List;

public class CrimeMapFragment extends Fragment {
    private MapView mapView;
    private IMapController mapController;
    private CrimeStatsActivity crimeStatsActivity;

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_crime_map, container, false);
        crimeStatsActivity = (CrimeStatsActivity) getActivity();

        // ensures that the map has a writable location for the map cache
        Configuration.getInstance().load(crimeStatsActivity, PreferenceManager.getDefaultSharedPreferences(crimeStatsActivity));
        mapView = (MapView) view.findViewById(R.id.crimestats_map);
        mapController = mapView.getController();
        initializeMap();
        displayPoliceBoundaries();
        displayCrimeMarkers();
        displayUserLocationMarker();
        return view;
    }

    private void initializeMap() {
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setMultiTouchControls(true);
        mapView.setHorizontalMapRepetitionEnabled(false);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.setScrollableAreaLimitLatitude(43.955045, 43.374475, 20);
        mapView.setScrollableAreaLimitLongitude(-79.881222, -78.940272, 20);
        mapView.setMinZoomLevel(12.0);
        mapController.setCenter(crimeStatsActivity.getStartingPoint());
        mapController.setZoom(15.3);
    }

    private void displayPoliceBoundaries() {
        List<List<GeoPoint>> policeBoundaries = crimeStatsActivity.getPoliceBoundaries();
        List<String> policeDivisionNames = crimeStatsActivity.getPoliceDivisionNames();

        for (int i = 0; i < policeBoundaries.size(); i++) {
            Polygon polygon = new Polygon(mapView);
            polygon.setPoints(policeBoundaries.get(i));
            polygon.setTitle("Division " + policeDivisionNames.get(i));
            mapView.getOverlayManager().add(polygon);
        }
    }

    private void displayCrimeMarkers() {
        final List<Crime> crimes = crimeStatsActivity.getCrimes();
        for (final Crime crime : crimes) {
            GeoPoint location = new GeoPoint(crime.getLatitude(), crime.getLongitude());
            Marker crimeMarker = new Marker(mapView);
            crimeMarker.setPosition(location);
            crimeMarker.setTitle(crime.getCategory());
            crimeMarker.setSnippet(crime.getPremise());
            crimeMarker.setSubDescription(crime.getDate());
            crimeMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_location_on_24));
            crimeMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    mapController.animateTo(location);
                    if (crimeMarker.isInfoWindowShown()) crimeMarker.closeInfoWindow();
                    else crimeMarker.showInfoWindow();
                    return true;
                }
            });

            crimeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(crimeMarker);
        }
    }

    private void displayUserLocationMarker() {
        Marker locationMarker = new Marker(mapView);
        locationMarker.setPosition(crimeStatsActivity.getUserLocation());
        locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        locationMarker.setTitle("My Home");
        locationMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_home_24));
        mapView.getOverlays().add(locationMarker);
    }
}
