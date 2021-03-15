package com.example.nwto.api;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.example.nwto.util.ServerConnection;
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

public class ResourceApi {
    private final static String WARD_METADATA_ID = "5e7a8234-f805-43ac-820f-03d7c360b588";
    private final static String POLICE_DIVISION_METADATA_ID = "1f736bc6-a4be-4b78-95a4-19c74c8663dc";
    private final static String NEIGHBOURHOOD_METADATA_ID = "4def3f65-2a65-4a4f-83c4-b2a4aed72d46";

    /**
     * Finds mapping resources of City of Toronto
     * @param latitude latitude of the User
     * @param longitude longitude of the User
     * @param mode 1: Ward Info, 2: Police Division Info, 3: Neighbourhood Info, 4: Police Division Boundary Info
     */
    public final void getMappingResource(double latitude, double longitude, int mode) {
        GetCityResource cityResource = new GetCityResource(latitude, longitude, mode);
        if (mode == 1) new GetCityMetadata(WARD_METADATA_ID, cityResource).execute();
        else if (mode == 2 || mode == 4) new GetCityMetadata(POLICE_DIVISION_METADATA_ID, cityResource).execute();
        else if (mode == 3) new GetCityMetadata(NEIGHBOURHOOD_METADATA_ID, cityResource).execute();
    }

    public final void getOfficialResource(String postalCode) {
        new GetOpenNorthResource(postalCode).execute();
    }

    public void updateWardInfoCard(String wardNumb, String wardName) {}
    public void updatePoliceDivisionContactCard(String divisionNumb) {}
    public void updateOfficialContactCard(String title, String name, String email, String phoneNumb) {}
    public void processNeighbourhoodName(String neighbourhoodName) {}
    public void processPoliceDivisionBoundaries(List<Record> records) {}

    protected class Record {
        private String areaShortCode, areaName;
        private List<List<Double>> polygonCoordinates;

        public Record(String areaShortCode, String areaName, List<List<Double>> polygonCoordinates) {
            this.areaShortCode = areaShortCode;
            this.areaName = areaName;
            this.polygonCoordinates = polygonCoordinates;
        }

        public List<List<Double>> getCoordinates() {
            return polygonCoordinates;
        }

        public String getAreaName() {
            return areaName;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetCityMetadata extends AsyncTask<Void, Void, JsonObject> {
        private final static String CITY_ENDPOINT = "https://ckan0.cf.opendata.inter.prod-toronto.ca/api/3/action/package_show";
        private final String metaDataID;
        private GetCityResource cityResource;

        public GetCityMetadata(String metaDataID, GetCityResource cityResource) {
            this.metaDataID = metaDataID;
            this.cityResource = cityResource;
        }

        @Override
        protected JsonObject doInBackground(Void... voids) {
            // makes a GET request to City of Toronto's Package API
            Map<String, String> metaDataParams = new HashMap<String, String>() {{
                put("id", metaDataID);
            }};
            String query = ServerConnection.createQuery(CITY_ENDPOINT, metaDataParams);
            return ServerConnection.requestGET(query);
        }

        @Override
        protected void onPostExecute(JsonObject response) {
            // finds the resource ID for all ward data in Toronto
            super.onPostExecute(response);
            JsonObject result = response.getAsJsonObject("result");
            JsonArray resources = result.getAsJsonArray("resources");
            String resourceID = null;
            for (int i = 0; i < resources.size(); i++) {
                JsonObject resource = (JsonObject) resources.get(i);
                Boolean datastore_active = resource.getAsJsonPrimitive("datastore_active").getAsBoolean();
                if (datastore_active) {
                    resourceID = resource.getAsJsonPrimitive("id").getAsString();
                    break;
                }
            }
            cityResource.execute(resourceID);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetCityResource extends AsyncTask<String, Void, JsonObject> {
        private final static String DATA_ENDPOINT = "https://ckan0.cf.opendata.inter.prod-toronto.ca/api/3/action/datastore_search";
        private final double latitude, longitude;
        private final int mode;

        public GetCityResource(double latitude, double longitude, int mode) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.mode = mode;
        }

        @Override
        protected JsonObject doInBackground(String... strings) {
            // makes a GET request to City of Toronto's DataStore API
            String resourceID = strings[0];
            Map<String, String> dataParams = new HashMap<String, String>() {{
                put("id", resourceID);
                put("limit", "200");
            }};
            String query = ServerConnection.createQuery(DATA_ENDPOINT, dataParams);
            return ServerConnection.requestGET(query);
        }

        @Override
        protected void onPostExecute(JsonObject response) {
            super.onPostExecute(response);
            JsonObject result = response.getAsJsonObject("result");
            JsonArray records = result.getAsJsonArray("records");
            readRecords(records);
        }

        private void readRecords(JsonArray records) {
            List<Record> recordList = new ArrayList<>();
            for (int i = 0; i < records.size(); i++) {
                JsonObject record = (JsonObject) records.get(i);
                String areaShortCode = record.getAsJsonPrimitive("AREA_SHORT_CODE").getAsString();
                String areaName = record.getAsJsonPrimitive("AREA_NAME").getAsString();
                if ((mode == 2 || mode == 4) && areaName.equals("00")) continue; // division 00 DNE & has a type of multipolygon
                String geometry = record.getAsJsonPrimitive("geometry").getAsString();
                JsonObject geometryObject = ServerConnection.convertStringToJson(geometry);
                JsonElement geometryCoordinates = geometryObject.getAsJsonArray("coordinates").get(0);
                Type listType = new TypeToken<List<List<Double>>>() {}.getType();
                List<List<Double>> polygonCoordinates = new Gson().fromJson(geometryCoordinates, listType);
                recordList.add(new Record(areaShortCode, areaName, polygonCoordinates));
            }

            int userRecordIndex = -1;
            switch (mode) {
                case 1: // ward number & name
                    userRecordIndex = findUserRecord(recordList);
                    String wardNumb = userRecordIndex != -1 ? recordList.get(userRecordIndex).areaShortCode : null;
                    String wardName = userRecordIndex != -1 ? recordList.get(userRecordIndex).areaName : null;
                    updateWardInfoCard(wardNumb, wardName);
                    break;
                case 2: // police division number
                    userRecordIndex = findUserRecord(recordList);
                    String divisionNumb = userRecordIndex != -1 ? recordList.get(userRecordIndex).areaName : null;
                    updatePoliceDivisionContactCard(divisionNumb);
                    break;
                case 3: // neighbourhood name
                    userRecordIndex = findUserRecord(recordList);
                    String neighbourhoodName = userRecordIndex != -1 ? recordList.get(userRecordIndex).areaName : null;
                    processNeighbourhoodName(neighbourhoodName);
                    break;
                case 4: // police division boundaries
                    processPoliceDivisionBoundaries(recordList);
                    break;
            }
        }

        private int findUserRecord(List<Record> records) {
            for (int i = 0; i < records.size(); i++) {
                if (containsUserLocation(records.get(i).polygonCoordinates)) return i;
            }
            return -1;
        }

        private boolean containsUserLocation(List<List<Double>> polygonCoordinates) {
            // confirms if the User is located in the selected polygon coordinates
            List<LatLng> latlngCoordinates = new ArrayList<>();
            for (List<Double> coordinate : polygonCoordinates)
                latlngCoordinates.add(new LatLng(coordinate.get(1), coordinate.get(0)));
            return PolyUtil.containsLocation(latitude, longitude, latlngCoordinates, false);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetOpenNorthResource extends AsyncTask<Void, Void, JsonObject> {
        private final static String OPEN_NORTH_ENDPOINT = "https://represent.opennorth.ca/postcodes";
        private final String postalCode;

        public GetOpenNorthResource(String postalCode) {
            this.postalCode = postalCode;
        }

        @Override
        protected JsonObject doInBackground(Void... voids) {
            String query = ServerConnection.createQuery(OPEN_NORTH_ENDPOINT, postalCode);
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
                            updateOfficialContactCard("Councillor", name, email, constituencyOfficePhoneNumb);
                            break;
                        case "MPP":
                            for (int j = 0; j < offices.size(); j++) {
                                JsonObject office = (JsonObject) offices.get(j);
                                String type = office.getAsJsonPrimitive("type").getAsString();
                                if (type.equals("constituency")) constituencyOfficePhoneNumb = office.getAsJsonPrimitive("tel").getAsString();
                            }
                            updateOfficialContactCard("MPP", name, email, constituencyOfficePhoneNumb);
                            break;
                        case "MP":
                            for (int j = 0; j < offices.size(); j++) {
                                JsonObject office = (JsonObject) offices.get(j);
                                String type = office.getAsJsonPrimitive("type").getAsString();
                                if (type.equals("constituency")) constituencyOfficePhoneNumb = office.getAsJsonPrimitive("tel").getAsString();
                            }
                            updateOfficialContactCard("MP", name, email, constituencyOfficePhoneNumb);
                            break;
                    }
                }
            }
        }
    }

    /* NOTE
    Gson Structure:
        Parent:    JsonElement
        Children:  JsonObject -> {}
                   JsonArray -> []
                   JsonPrimitive -> values in primitive
    */
}
