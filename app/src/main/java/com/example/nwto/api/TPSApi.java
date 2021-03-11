package com.example.nwto.api;

import android.os.AsyncTask;
import android.util.Log;

import com.example.nwto.model.Crime;
import com.example.nwto.adapter.CrimeAdapter;
import com.example.nwto.util.ServerConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TPSApi {
    private static final String TAG = "TAG: " + TPSApi.class.getSimpleName();
    private static final String endPoint = "https://services.arcgis.com/S9th0jAJ7bqgIRjw/arcgis/rest/services/";
    private static final String YTD = "YTD_Crime/FeatureServer/0/query";
    private static final String YE = "MCI_2014_to_2019/FeatureServer/0/query";

    private final List<Crime> crimes;
    private final CrimeAdapter crimeAdapter;

    public TPSApi(List<Crime> crimes, CrimeAdapter crimeAdapter) {
        this.crimes = crimes;
        this.crimeAdapter = crimeAdapter;
    }

    /**
     * Queries TPS Year-to-Date (2021) API to retrieve necessary information
     * @param radius search filter: radius in kilometers (-1 to include all Toronto City)
     * @param latitude the User's latitude
     * @param longitude the User's longitude
     * @param divisionNumb search filter: integer number of division (-1 to include all)
     * @param startY search filter: starting year
     * @param startM search filter: starting month
     * @param startD search filter: starting day
     * @param endY search filter: end year
     * @param endM search filter: end month
     * @param endD search filter: end day (ex. to query for 1 day of 20210215, start date should be 20210215 & end date should be 20210216)
     * @param premiseType search filter: Apartment, Commercial, Educational, House, Transit, Outside, Other, (null to include all)
     * @param category search filter: Assault, Auto Theft, Break and Enter, Homicide, Robbery, Sexual Violation, Shooting, Theft Over, (null to include all)
     */
    public void queryYTD(int radius, double latitude, double longitude,
                         int divisionNumb, int startY, int startM, int startD, int endY, int endM, int endD, String premiseType, String category) {
        String query = buildQuery_YTD(divisionNumb, startY, startM, startD, endY, endM, endD, premiseType, category);
        String defaultOutFields = "*";
        Map<String, String> params = buildParams(query, radius, latitude, longitude, defaultOutFields);
        new GetResponse(endPoint + YTD, params, YTD).execute();
    }

    /**
     * Queries TPS Year-End (2014 to 2019) API to retrieve necessary information
     * @param radius search filter: radius in kilometers (-1 to include all Toronto City)
     * @param latitude the User's latitude
     * @param longitude the User's longitude
     * @param divisionNumb search filter: integer number of division (-1 to include all)
     * @param neighbourhoodNumb search filter: integer number of neighbourhood (-1 to include all)
     * @param startY search filter: starting year
     * @param startM search filter: starting month
     * @param startD search filter: starting day
     * @param endY search filter: end year
     * @param endM search filter: end month
     * @param endD search filter: end day (ex. to query for 1 day of 20180215, start date should be 20180215 & end date should be 20180216)
     * @param premiseType search filter: Apartment, Commercial, House, Outside, Other, (null to include all)
     * @param category search filter: Assault, Auto Theft, Break and Enter, Robbery, Theft Over, (null to include all)
     */
    public void queryYE(int radius, double latitude, double longitude,
                        int divisionNumb, int neighbourhoodNumb, int startY, int startM, int startD, int endY, int endM, int endD, String premiseType, String category) {
        String query = buildQuery_YE(divisionNumb, neighbourhoodNumb, startY, startM, startD, endY, endM, endD, premiseType, category);
        String defaultOutFields = "*";
        Map<String, String> params = buildParams(query, radius, latitude, longitude, defaultOutFields);
        new GetResponse(endPoint + YE, params, YE).execute();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Builds the full query parameters for TPS server
     * @param query YTD query or YE query
     * @param radius radius in kilometers (-1 to include all Toronto City)
     * @param latitude the User's latitude
     * @param longitude the User's longitude
     * @param outFields parameter for output fields
     * @return full query parameters
     */
    private Map<String, String> buildParams(String query, int radius, double latitude, double longitude, String outFields) {
        Map<String, String> params = new HashMap<String, String>(){{
            put("f", "json"); // output response format
            put("returnGeometry", "true"); // return lat & long
            put("inSR", "4326"); // input geometry format
            put("outSR", "4326"); // output geometry format
            put("where", query);
            put("outFields", outFields);
        }};

        if (radius >= 0) {
            params.put("geometryType", "esriGeometryPoint"); // input geometry type
            params.put("spatialRel", "esriSpatialRelContains"); // geometry filter type
            params.put("units", "esriSRUnit_Kilometer");
            params.put("distance", Integer.toString(radius));
            params.put("geometry", Double.toString(longitude) + ", " + Double.toString(latitude));
        }

        return params;
    }

    /**
     * Builds the query for Year-to-Date endpoint
     * @param divisionNumb integer number of division (-1 to include all)
     * @param startY integer number of start year
     * @param startM integer number of start month
     * @param startD integer number of start day
     * @param endY integer number of end year
     * @param endM integer number of end month
     * @param endD integer number of end day
     * @param premiseType type of premise: Apartment, Commercial, Educational, House, Transit, Outside, Other, (null to include all)
     * @param category type of crime: Assault, Auto Theft, Break and Enter, Homicide, Robbery, Sexual Violation, Shooting, Theft Over, (null to include all)
     * @return
     */
    private String buildQuery_YTD(int divisionNumb, int startY, int startM, int startD, int endY, int endM, int endD, String premiseType, String category) {
        // converts integer values to strings
        String division = "D" + Integer.toString(divisionNumb);
        String startDate = convertToDate(startY, startM, startD);
        String endDate = convertToDate(endY, endM, endD);

        // builds the query
        StringBuilder sb = new StringBuilder();
        List<String> queries = new ArrayList<>();
        if (divisionNumb >= 0) queries.add("(division='" + division + "')");
        queries.add("(occurrencedate BETWEEN '" + startDate + "' AND '" + endDate + "')");
        if (premiseType != null) queries.add("(premisetype='" + premiseType + "')");
        if (category != null) queries.add("(mci_category='" + category + "')");

        for (String query : queries) {
            sb.append(query);
            sb.append(" AND ");
        }

        return sb.substring(0, sb.length() - " AND ".length());
    }

    /**
     * Builds the query for Year-End endpoint
     * @param divisionNumb integer number of division (-1 to include all)
     * @param neighbourhoodNumb integer number of neighbourhood (-1 to include all)
     * @param startY integer number of start year
     * @param startM integer number of start month
     * @param startD integer number of start day
     * @param endY integer number of end year
     * @param endM integer number of end month
     * @param endD integer number of end day
     * @param premiseType type of premise: Apartment, Commercial, House, Outside, Other, (null to include all)
     * @param category type of crime: Assault, Auto Theft, Break and Enter, Robbery, Theft Over, (null to include all)
     * @return
     */
    private String buildQuery_YE(int divisionNumb, int neighbourhoodNumb, int startY, int startM, int startD, int endY, int endM, int endD, String premiseType, String category) {
        // converts integer values to strings
        String division = "D" + Integer.toString(divisionNumb);
        String hood = Integer.toString(neighbourhoodNumb);
        String startDate = convertToDate(startY, startM, startD);
        String endDate = convertToDate(endY, endM, endD);

        // builds the query
        StringBuilder sb = new StringBuilder();
        List<String> queries = new ArrayList<>();
        if (divisionNumb >= 0) queries.add("(Division='" + division + "')");
        if (neighbourhoodNumb >= 0) queries.add("(Hood_ID='" + hood + "')");
        queries.add("(occurrencedate BETWEEN '" + startDate + "' AND '" + endDate + "')");
        if (premiseType != null) queries.add("(premisetype='" + premiseType + "')");
        if (category != null) queries.add("(MCI='" + category + "')");

        for (String query : queries) {
            sb.append(query);
            sb.append(" AND ");
        }

        return sb.substring(0, sb.length() - " AND ".length());
    }

    private String convertToDate(int yyyy, int mm, int dd) {
        String year = Integer.toString(yyyy);
        String month = mm < 10 ? "0" + Integer.toString(mm) : Integer.toString(mm);
        String day = dd < 10 ? "0" + Integer.toString(dd) : Integer.toString(dd);
        String date = year + "-" + month + "-" + day;
        return date;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class GetResponse extends AsyncTask<Void, Void, JsonObject> {
        private final String endPoint;
        private final Map<String, String> params;
        private final String type;

        public GetResponse(String endPoint, Map<String, String> params, String type) {
            this.endPoint = endPoint;
            this.params = params;
            this.type = type;
        }

        @Override
        protected JsonObject doInBackground(Void... voids) {
            String query = ServerConnection.createQuery(endPoint, params);
            Log.d(TAG, "GetResponse: doInBackground: Query -> " + query);
            return ServerConnection.requestGET(query);
        }

        @Override
        protected void onPostExecute(JsonObject response) {
            super.onPostExecute(response);
            if (type.equals(YTD)) parseResponse_YTD(response);
            else parseResponse_YE(response);
        }
    }

    private void parseResponse_YTD(JsonObject response) {
        crimes.clear();
        JsonArray features = response.getAsJsonArray("features");

        if (features.size() == 0)
            crimes.add(new Crime(0, "No Results Found", "", "", "", "", 0, 0));
        else {
            for (int i = 0; i < features.size(); i++) {
                JsonObject feature = (JsonObject) features.get(i);
                JsonObject attributes = feature.getAsJsonObject("attributes");
                JsonObject geometry = feature.getAsJsonObject("geometry");

                String uniqueID = attributes.getAsJsonPrimitive("event_unique_id").getAsString();
                String division = attributes.getAsJsonPrimitive("division").getAsString();
                long occurrencedate = attributes.getAsJsonPrimitive("occurrencedate").getAsLong();
                String premisetype = attributes.getAsJsonPrimitive("premisetype").getAsString();
                String category = attributes.getAsJsonPrimitive("mci_category").getAsString();
                double latitude = geometry.getAsJsonPrimitive("y").getAsDouble();
                double longitude = geometry.getAsJsonPrimitive("x").getAsDouble();

                Date date = new Date(occurrencedate);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
                String convertedDate = dateFormat.format(date);

                crimes.add(new Crime(i, uniqueID, division, premisetype, category, convertedDate, latitude, longitude));
            }
        }
        crimeAdapter.notifyDataSetChanged();
    }

    private void parseResponse_YE(JsonObject resposne) {

    }
}
