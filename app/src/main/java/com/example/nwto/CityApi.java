package com.example.nwto;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
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

    private final CardView cardView;
    private final TextView textWardNumb, textAreaName;
    private final ProgressBar progressBar;
    private final List<Resource> resources;
    private final ResourceAdapter resourceAdapter;

    /**
     *
     * @param cardView
     * @param textWardNumb TextView object where ward number will be updated
     * @param textAreaName TextView object where area name will be updated
     * @param progressBar spinning progress bar (when all resources are loaded, the bar will be gone)
     * @param resources a list of Resource objects
     * @param resourceAdapter adapter for resources
     */
    public CityApi(CardView cardView, TextView textWardNumb, TextView textAreaName, ProgressBar progressBar, List<Resource> resources, ResourceAdapter resourceAdapter) {
        this.cardView = cardView;
        this.textWardNumb = textWardNumb;
        this.textAreaName = textAreaName;
        this.progressBar = progressBar;
        this.resources = resources;
        this.resourceAdapter = resourceAdapter;
    }

    /**
     * Locates the User's ward number based on latitude and longitude
     * @param latitude ex. 43.7681507
     * @param longitude ex. -79.4143751
     */
    public void getWard(double latitude, double longitude) {
        new GetWardMetadata(latitude, longitude).execute();
    }

    /**
     * Finds the contact information for the User's Councillor, MPP and MP
     * @param postalCode non-spaced postal code
     */
    public void getResources(String postalCode) {
        new GetResources(postalCode).execute();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Used in getWard() method.
     * Queries City of Toronto's Package API to receive ward metadata resources.
     */
    private class GetWardMetadata extends AsyncTask<Void, Void, JsonObject> {
        private final String cityOfTorontoEndPoint = "https://ckan0.cf.opendata.inter.prod-toronto.ca/api/3/action/package_show";
        private final String cityWard_metaData_ID = "5e7a8234-f805-43ac-820f-03d7c360b588";
        private final double latitude, longitude;

        public GetWardMetadata(double latitude, double longitude) {
            super();
            this.latitude = latitude;
            this.longitude = longitude;
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
                    new GetWardResource(latitude, longitude).execute(resourceID);
                }
            }
        }
    }

    /**
     * Used in getWard() method.
     * Queries City of Toronto's DataStore API to find ward geometries.
     * Finds the User's ward number + area name and updates the main title card from activity_resources.xml.
     */
    private class GetWardResource extends AsyncTask<String, Void, JsonObject> {
        private final String dataStoreEndPoint = "https://ckan0.cf.opendata.inter.prod-toronto.ca/api/3/action/datastore_search";
        private final double latitude, longitude;

        public GetWardResource(double latitude, double longitude) {
            super();
            this.latitude = latitude;
            this.longitude = longitude;
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
                String areaName = record.getAsJsonPrimitive("AREA_NAME").getAsString();
                String geometry = record.getAsJsonPrimitive("geometry").getAsString();
                JsonObject wardGeometry = ServerConnection.convertStringToJson(geometry);
                JsonElement wardCoordinates = wardGeometry.getAsJsonArray("coordinates").get(0);
                Type listType = new TypeToken<List<List<Double>>>() {}.getType();
                List<List<Double>> polygonCoordinates = new Gson().fromJson(wardCoordinates, listType);

                // confirms if the User is located in the selected ward polygon coordinates
                if (containsUserLocation(polygonCoordinates)) {
                    Log.d(TAG, "AsyncGetWardResource: onPostExecute -> ward number=" + wardNumber + " area name=" + areaName);
                    textWardNumb.setText("Ward" + wardNumber); // updates the TextViews
                    textAreaName.setText(areaName);
                    cardView.setVisibility(View.VISIBLE);
                    return;
                }
            }
            Log.e(TAG, "AsyncGetWardResource: onPostExecute -> ward number & area name not found");
        }

        /**
         * Confirms if the User is located in the given ward coordinates.
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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Used in getResources() method.
     * Queries OpenNorth API to find contact information of Councillor, MPP and MP based on the User's postal code.
     * Updates the resources cards from activity_resources.xml.
     */
    private class GetResources extends AsyncTask<Void, Void, JsonObject> {
        private final String openNorthEndpoint = "https://represent.opennorth.ca/postcodes";
        private final String postalCode;

        public GetResources(String postalCode) {
            this.postalCode = postalCode;
        }

        @Override
        protected JsonObject doInBackground(Void... voids) {
            String query = ServerConnection.createQuery(openNorthEndpoint, postalCode);
            return ServerConnection.requestGET(query);
        }

        @Override
        protected void onPostExecute(JsonObject response) {
            super.onPostExecute(response);
            JsonArray representatives_centroid = response.getAsJsonArray("representatives_centroid");
            for (int i = 0; i < representatives_centroid.size(); i++) {
                JsonObject representative = (JsonObject) representatives_centroid.get(i);
                String elected_office = representative.getAsJsonPrimitive("elected_office").getAsString();

                // reads information about Councillor, MPP and MP
                if (elected_office.equals("Councillor") || elected_office.equals("MPP") || elected_office.equals("MP")) {
                    String name = representative.getAsJsonPrimitive("name").getAsString();
                    String email = representative.getAsJsonPrimitive("email").getAsString();
                    // String partyName = representative.getAsJsonPrimitive("party_name").getAsString();
                    JsonArray offices = representative.getAsJsonArray("offices");
                    String constituencyOfficePhoneNumb = "-";

                    // reads phone number and updates the resources cards
                    switch (elected_office) {
                        case "Councillor":
                            for (int j = 0; j < offices.size(); j++) {
                                JsonObject office = (JsonObject) offices.get(j);
                                String type = office.getAsJsonPrimitive("type").getAsString();
                                if (type.equals("legislature")) constituencyOfficePhoneNumb = office.getAsJsonPrimitive("tel").getAsString();
                            }
                            Log.d(TAG, "AsyncGetResources: onPostExecute: Councillor -> name=" + name + " email=" + email + " phoneNumb=" + constituencyOfficePhoneNumb);
                            addResource(1, "Councillor", name, email, constituencyOfficePhoneNumb);
                            break;
                        case "MPP":
                            for (int j = 0; j < offices.size(); j++) {
                                JsonObject office = (JsonObject) offices.get(j);
                                String type = office.getAsJsonPrimitive("type").getAsString();
                                if (type.equals("constituency")) constituencyOfficePhoneNumb = office.getAsJsonPrimitive("tel").getAsString();
                            }
                            Log.d(TAG, "AsyncGetResources: onPostExecute: MPP -> name=" + name + " email=" + email + " phoneNumb=" + constituencyOfficePhoneNumb);
                            addResource(2, "MPP", name, email, constituencyOfficePhoneNumb);
                            break;
                        case "MP":
                            for (int j = 0; j < offices.size(); j++) {
                                JsonObject office = (JsonObject) offices.get(j);
                                String type = office.getAsJsonPrimitive("type").getAsString();
                                if (type.equals("constituency")) constituencyOfficePhoneNumb = office.getAsJsonPrimitive("tel").getAsString();
                            }
                            Log.d(TAG, "AsyncGetResources: onPostExecute: MP -> name=" + name + " email=" + email + " phoneNumb=" + constituencyOfficePhoneNumb);
                            addResource(3, "MP", name, email, constituencyOfficePhoneNumb);
                            break;
                    }
                }
            }
            // updates the resources cards and stops the progress bar
            Collections.sort(resources);
            progressBar.setVisibility(View.GONE);
            resourceAdapter.notifyDataSetChanged();
        }

        private void addResource(int order, String title, String name, String email, String phoneNumb) {
            resources.add(new Resource(order, title, name, email, phoneNumb));
        }
    }
}
