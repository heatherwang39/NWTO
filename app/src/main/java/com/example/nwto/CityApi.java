package com.example.nwto;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    Gson Structure:
        Parent:    JsonElement
        Children:  JsonObject -> {}
                   JsonArray -> []
                   JsonPrimitive -> values in primitive
 */

public class CityApi {
    private static final String TAG = "TAG: " + CityApi.class.getSimpleName();

    /**
     * Locates the User's ward number based on latitude and longitude
     * @param latitude ex. 43.7681507
     * @param longitude ex. -79.4143751
     * @param TextWardNumb TextView object where ward number will be updated
     */
    public void getWard(double latitude, double longitude, TextView TextWardNumb) {
        new AsyncGetWard(latitude, longitude, TextWardNumb).execute();
    }

    /**
     * Queries City of Toronto's Package API to receive ward metadata resources
     */
    private class AsyncGetWard extends AsyncTask<Void, Void, JsonObject> {
        private final String cityOfTorontoEndPoint = "https://ckan0.cf.opendata.inter.prod-toronto.ca/api/3/action/package_show";
        private final String cityWard_metaData_ID = "5e7a8234-f805-43ac-820f-03d7c360b588";
        private final double latitude, longitude;
        private final TextView TextWardNumb;

        public AsyncGetWard(double latitude, double longitude, TextView TextWardNumb) {
            super();
            this.latitude = latitude;
            this.longitude = longitude;
            this.TextWardNumb = TextWardNumb;
        }

        @Override
        protected JsonObject doInBackground(Void... voids) {
            // makes a GET request to City of Toronto's Package API
            Map<String, String> metaDataParams = new HashMap<String, String>() {{
                put("id", cityWard_metaData_ID);
            }};
            String query = ServerConnection.createQuery(cityOfTorontoEndPoint, metaDataParams);
            return ServerConnection.requestGET(query);
        }

        @Override
        protected void onPostExecute(JsonObject response) {
            // finds the resource ID for all ward data in Toronto
            super.onPostExecute(response);
            JsonObject result = response.getAsJsonObject("result");
            JsonArray resources = result.getAsJsonArray("resources");
            for (int i = 0; i < resources.size(); i++) {
                JsonObject resource = (JsonObject) resources.get(i);
                Boolean datastore_active = resource.getAsJsonPrimitive("datastore_active").getAsBoolean();
                if (datastore_active) {
                    String resourceID = resource.getAsJsonPrimitive("id").getAsString();
                    new AsyncGetWardResource(latitude, longitude, TextWardNumb).execute(resourceID);
                }
            }
        }
    }

    /**
     * Query City of Toronto's DataStore API to find ward geometries and finds the User's ward number
     */
    private class AsyncGetWardResource extends AsyncTask<String, Void, JsonObject> {
        private final String dataStoreEndPoint = "https://ckan0.cf.opendata.inter.prod-toronto.ca/api/3/action/datastore_search";
        private final double latitude, longitude;
        private final TextView TextWardNumb;

        public AsyncGetWardResource(double latitude, double longitude, TextView TextWardNumb) {
            super();
            this.latitude = latitude;
            this.longitude = longitude;
            this.TextWardNumb = TextWardNumb;
        }

        @Override
        protected JsonObject doInBackground(String... strings) {
            // makes a GET request to City of Toronto's DataStore API
            String resourceID = strings[0];
            Map<String, String> dataParams = new HashMap<String, String>() {{
                put("id", resourceID);
            }};
            String query = ServerConnection.createQuery(dataStoreEndPoint, dataParams);
            return ServerConnection.requestGET(query);
        }

        @Override
        protected void onPostExecute(JsonObject response) {
            super.onPostExecute(response);
            JsonObject result = response.getAsJsonObject("result");
            JsonArray records = result.getAsJsonArray("records");

            // iterates each ward records to get ward number and geometry polygon coordinates
            for (int i = 0; i < records.size(); i++) {
                JsonObject record = (JsonObject) records.get(i);
                String wardNumber = record.getAsJsonPrimitive("AREA_SHORT_CODE").getAsString();
                String geometry = record.getAsJsonPrimitive("geometry").getAsString();
                JsonObject wardGeometry = ServerConnection.convertStringToJson(geometry);
                JsonElement wardCoordinates = wardGeometry.getAsJsonArray("coordinates").get(0);
                Type listType = new TypeToken<List<List<Double>>>() {}.getType();
                List<List<Double>> polygonCoordinates = new Gson().fromJson(wardCoordinates, listType);

                // confirms if the User is located in the selected ward polygon coordinates
                if (containsUserLocation(polygonCoordinates)) {
                    Log.d(TAG, "AsyncGetWardResource: onPostExecute -> ward number=" + wardNumber);
                    TextWardNumb.setText(wardNumber); // updates the TextView
                    return;
                }
            }
            Log.e(TAG, "AsyncGetWardResource: onPostExecute -> ward number not found");
        }

        /**
         * Confirms if the User is located in the given ward coordinates
         * @param wardCoordinates List of [latitude, longitude] coordinates of a ward
         * @return true if the User is located in the given ward coordinates false if not
         */
        private boolean containsUserLocation(List<List<Double>> wardCoordinates) {
            List<LatLng> latlngCoordinates = new ArrayList<>();
            for (List<Double> coordinate : wardCoordinates)
                latlngCoordinates.add(new LatLng(coordinate.get(1), coordinate.get(0)));
            return PolyUtil.containsLocation(latitude, longitude, latlngCoordinates, false);
        }
    }

    public void getResources(String postalCode) {

    }
}
