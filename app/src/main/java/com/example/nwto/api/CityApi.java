package com.example.nwto.api;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.example.nwto.R;
import com.example.nwto.ResourcesActivity;
import com.example.nwto.model.Resource;
import com.example.nwto.adapter.ResourceAdapter;
import com.example.nwto.util.ServerConnection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

    private CardView cardView;
    private TextView textWardNumb, textAreaName;
    private ProgressBar progressBar;
    private List<Resource> resources;
    private ResourceAdapter resourceAdapter;

    public CityApi() {
        this.cardView = null;
        this.textWardNumb = null;
        this.textAreaName = null;
        this.progressBar = null;
        this.resources = null;
        this.resourceAdapter = null;
    }

    /**
     * @param textWardNumb TextView object where ward number will be updated
     * @param textAreaName TextView object where area name will be updated
     * @param progressBar spinning progress bar (when all resources are loaded, the bar will be gone)
     * @param resources a list of Resource objects
     * @param resourceAdapter adapter for resources
     */
    public void updateResourcesPage(double latitude, double longitude, String postalCode, CardView cardView, TextView textWardNumb, TextView textAreaName, ProgressBar progressBar, List<Resource> resources, ResourceAdapter resourceAdapter) {
        this.cardView = cardView;
        this.textWardNumb = textWardNumb;
        this.textAreaName = textAreaName;
        this.progressBar = progressBar;
        this.resources = resources;
        this.resourceAdapter = resourceAdapter;

        new GetCityMetadata(latitude, longitude, 0).execute(); // locates the User's ward number based on latitude and longitude
        new GetCityMetadata(latitude, longitude, 1).execute();  // locates the User's police division number based on latitude and longitude
        new GetGovernmentResources(postalCode).execute(); // finds the contact information for the User's Councillor, MPP and MP
    }

    public void getNeighbourhood(double latitude, double longitude) {
        new GetCityMetadata(latitude, longitude, 2).execute(); // locates the User's neighbourhood name based on latitude and longitude
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Used in getWard() method.
     * Queries City of Toronto's Package API to receive ward metadata resources.
     */
    private class GetCityMetadata extends AsyncTask<Void, Void, JsonObject> {
        private final String cityOfTorontoEndPoint = "https://ckan0.cf.opendata.inter.prod-toronto.ca/api/3/action/package_show";
        private final String cityWard_metaData_ID = "5e7a8234-f805-43ac-820f-03d7c360b588";
        private final String cityPoliceDivision_metaData_ID = "1f736bc6-a4be-4b78-95a4-19c74c8663dc";
        private final String cityNeighbourhood_metaData_ID = "4def3f65-2a65-4a4f-83c4-b2a4aed72d46";

        private final double latitude, longitude;
        private final String metaDataID;
        private final int dataSetNumb;

        public GetCityMetadata(double latitude, double longitude, int id) {
            super();
            this.latitude = latitude;
            this.longitude = longitude;
            this.dataSetNumb = id;
            switch (id) {
                case 0: // ward
                    metaDataID = cityWard_metaData_ID;
                    break;
                case 1: // police division
                    metaDataID = cityPoliceDivision_metaData_ID;
                    break;
                case 2: // neighbourhood
                    metaDataID = cityNeighbourhood_metaData_ID;
                    break;
                default:
                    metaDataID = null;
                    break;
            }
        }

        @Override
        protected JsonObject doInBackground(Void... voids) {
            // makes a GET request to City of Toronto's Package API
            Map<String, String> metaDataParams = new HashMap<String, String>() {{
                put("id", metaDataID);
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
                    new GetCityResource(latitude, longitude, dataSetNumb).execute(resourceID);
                }
            }
        }
    }

    /**
     * Used in getWard() method.
     * Queries City of Toronto's DataStore API to find ward geometries.
     * Finds the User's ward number + area name and updates the main title card from activity_resources.xml.
     */
    private class GetCityResource extends AsyncTask<String, Void, JsonObject> {
        private final String dataStoreEndPoint = "https://ckan0.cf.opendata.inter.prod-toronto.ca/api/3/action/datastore_search";
        private final double latitude, longitude;
        private final int dataSetNumb;

        public GetCityResource(double latitude, double longitude, int dataSetNumb) {
            super();
            this.latitude = latitude;
            this.longitude = longitude;
            this.dataSetNumb = dataSetNumb;
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
            switch (dataSetNumb) {
                case 0: // ward
                    readWardRecords(records);
                    break;
                case 1: // police division
                    readPoliceDivisionRecords(records);
                    break;
                case 2: // neighbourhood
                    readNeighbourhoodRecords(records);
                    break;
            }
        }

        /**
         * Confirms if the User is located in the given ward coordinates.
         * @param polygonCoordinates List of [latitude, longitude] coordinates of a ward
         * @return true if the User is located in the given ward coordinates false if not
         */
        private boolean containsUserLocation(List<List<Double>> polygonCoordinates) {
            List<LatLng> latlngCoordinates = new ArrayList<>();
            for (List<Double> coordinate : polygonCoordinates)
                latlngCoordinates.add(new LatLng(coordinate.get(1), coordinate.get(0)));
            return PolyUtil.containsLocation(latitude, longitude, latlngCoordinates, false);
        }

        private void readWardRecords(JsonArray records) {
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
                    Log.d(TAG, "AsyncGetWardResource: onPostExecute: readWardRecords -> ward number=" + wardNumber + " area name=" + areaName);
                    textWardNumb.setText("Ward" + wardNumber); // updates the TextViews
                    textAreaName.setText(areaName);
                    cardView.setVisibility(View.VISIBLE);
                    return;
                }
            }
            Log.e(TAG, "AsyncGetWardResource: onPostExecute: readWardRecords -> ward number & area name not found");
        }

        private void readPoliceDivisionRecords(JsonArray records) {
            // iterates each division records to get division number and geometry polygon coordinates
            for (int i = 0; i < records.size(); i++) {
                JsonObject record = (JsonObject) records.get(i);
                String divisionNumb = record.getAsJsonPrimitive("AREA_NAME").getAsString();
                String geometry = record.getAsJsonPrimitive("geometry").getAsString();
                JsonObject wardGeometry = ServerConnection.convertStringToJson(geometry);
                JsonElement wardCoordinates = wardGeometry.getAsJsonArray("coordinates").get(0);
                Type listType = new TypeToken<List<List<Double>>>() {}.getType();
                List<List<Double>> polygonCoordinates = new Gson().fromJson(wardCoordinates, listType);

                // confirms if the User is located in the selected police division polygon coordinates
                if (containsUserLocation(polygonCoordinates)) {
                    Log.d(TAG, "AsyncGetWardResource: onPostExecute: readPoliceDivisionRecords -> police division=" + divisionNumb);
                    getPoliceDivisionInfo(divisionNumb);
                    return;
                }
            }
            Log.e(TAG, "AsyncGetWardResource: onPostExecute: readPoliceDivisionRecords -> police division number not found");
        }

        private void getPoliceDivisionInfo(String divisionNumb) {
            FirebaseFirestore fireStore = FirebaseFirestore.getInstance();

            String collectionName = "police_contact_info";
            String documentID = divisionNumb;
            String documentField_divisionAddress = "divisionAddress";
            String documentField_divisionEmail = "divisionEmail";
            String documentField_divisionPhone = "divisionPhone";
            String documentField_officerName = "officerName";
            String documentField_officerEmail = "officerEmail";
            String documentField_officerPhone = "officerPhone";

            // reads the police division and crime prevention officer info
            fireStore.collection(collectionName).document(documentID).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String divisionAddress = (String) document.get(documentField_divisionAddress);
                                    String divisionEmail = (String) document.get(documentField_divisionEmail);
                                    String divisionPhone = (String) document.get(documentField_divisionPhone);
                                    String officerName = (String) document.get(documentField_officerName);
                                    String officerEmail = (String) document.get(documentField_officerEmail);
                                    String officerPhone = (String) document.get(documentField_officerPhone);
                                    resources.add(new Resource(0, "Division " + divisionNumb, divisionAddress, divisionEmail, divisionPhone));
                                    resources.add(new Resource(1, "Crime Prevention Officer", officerName, officerEmail, officerPhone));
                                    Collections.sort(resources);
                                    resourceAdapter.notifyDataSetChanged();
                                    Log.d(TAG, "AsyncGetWardResource: onPostExecute: readPoliceDivisionRecords: getPoliceDivisionInfo -> Read Info Success");
                                }
                            }
                            else Log.e(TAG, "AsyncGetWardResource: onPostExecute: readPoliceDivisionRecords: getPoliceDivisionInfo -> Read Info Fail", task.getException());
                            progressBar.setVisibility(View.GONE); // stops the progress bar
                        }
                    });
        }

        private void readNeighbourhoodRecords(JsonArray records) {
            // iterates each neighbourhood records to get neighbourhood name and geometry polygon coordinates
            for (int i = 0; i < records.size(); i++) {
                JsonObject record = (JsonObject) records.get(i);
                String neighbourhoodName = record.getAsJsonPrimitive("AREA_NAME").getAsString();
                String geometry = record.getAsJsonPrimitive("geometry").getAsString();
                JsonObject wardGeometry = ServerConnection.convertStringToJson(geometry);
                JsonElement wardCoordinates = wardGeometry.getAsJsonArray("coordinates").get(0);
                Type listType = new TypeToken<List<List<Double>>>() {}.getType();
                List<List<Double>> polygonCoordinates = new Gson().fromJson(wardCoordinates, listType);

                // confirms if the User is located in the selected neighbourhood polygon coordinates
                if (containsUserLocation(polygonCoordinates)) {
                    Log.d(TAG, "AsyncGetWardResource: onPostExecute: readNeighbourhoodRecords -> neighbourhood name=" + neighbourhoodName);
                    processNeighbourhoodName(neighbourhoodName);
                    return;
                }
            }
            Log.e(TAG, "AsyncGetWardResource: onPostExecute: readNeighbourhoodRecords -> neighbourhood name not found");
        }

        private void processNeighbourhoodName(String neighbourhoodName) {
            // to pass parameters, pass them through getNeighbourhood() method and assign them to global variables and access them here...
            // TODO: some of the neighbourhoods are not provided from the city API ...
            // TODO: Heather
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Used in getResources() method.
     * Queries OpenNorth API to find contact information of Councillor, MPP and MP based on the User's postal code.
     * Updates the resources cards from activity_resources.xml.
     */
    private class GetGovernmentResources extends AsyncTask<Void, Void, JsonObject> {
        private final String openNorthEndpoint = "https://represent.opennorth.ca/postcodes";
        private final String postalCode;

        public GetGovernmentResources(String postalCode) {
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
                            addResource(2, "Councillor", name, email, constituencyOfficePhoneNumb);
                            break;
                        case "MPP":
                            for (int j = 0; j < offices.size(); j++) {
                                JsonObject office = (JsonObject) offices.get(j);
                                String type = office.getAsJsonPrimitive("type").getAsString();
                                if (type.equals("constituency")) constituencyOfficePhoneNumb = office.getAsJsonPrimitive("tel").getAsString();
                            }
                            Log.d(TAG, "AsyncGetResources: onPostExecute: MPP -> name=" + name + " email=" + email + " phoneNumb=" + constituencyOfficePhoneNumb);
                            addResource(3, "MPP", name, email, constituencyOfficePhoneNumb);
                            break;
                        case "MP":
                            for (int j = 0; j < offices.size(); j++) {
                                JsonObject office = (JsonObject) offices.get(j);
                                String type = office.getAsJsonPrimitive("type").getAsString();
                                if (type.equals("constituency")) constituencyOfficePhoneNumb = office.getAsJsonPrimitive("tel").getAsString();
                            }
                            Log.d(TAG, "AsyncGetResources: onPostExecute: MP -> name=" + name + " email=" + email + " phoneNumb=" + constituencyOfficePhoneNumb);
                            addResource(4, "MP", name, email, constituencyOfficePhoneNumb);
                            break;
                    }
                }
            }
            // updates the resources cards
            Collections.sort(resources);
            resourceAdapter.notifyDataSetChanged();
        }

        private void addResource(int order, String title, String name, String email, String phoneNumb) {
            resources.add(new Resource(order, title, name, email, phoneNumb));
        }
    }
}
