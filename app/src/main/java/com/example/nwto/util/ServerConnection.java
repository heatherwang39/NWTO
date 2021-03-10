package com.example.nwto.util;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class ServerConnection {
    private static final String TAG = "TAG: " + ServerConnection.class.getSimpleName();
    private static final String CHARSET_ENCODING = "UTF-8";
    private static final String ERROR = "-1";

    public static String createQuery(String endPoint, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(endPoint);
        sb.append("?");
        for (Map.Entry<String, String> param : params.entrySet()) {
            sb.append(param.getKey());
            sb.append("=");
            sb.append(param.getValue());
            sb.append("&");
        }
        String query = sb.toString();
        return query.substring(0, query.length() - 1);
    }

    public static String createQuery(String endPoint, String... params) {
        String delimiter = "/";
        StringBuilder sb = new StringBuilder(endPoint);
        for (String param : params) {
            sb.append(delimiter);
            sb.append(param);
        }
        return sb.toString();
    }

    public static JsonObject requestGET(String requestURL) {
        HttpURLConnection connection = null;
        String stringResponse = ERROR;
        try {
            // creates a connection
            URL url = new URL(requestURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // configures timeouts
            connection.setConnectTimeout(3000); // 3s
            connection.setReadTimeout(3000);

            // reads the status code
            int responseCode = connection.getResponseCode();
            InputStreamReader inputStreamReader;
            if (responseCode < 200 || responseCode > 299) inputStreamReader = new InputStreamReader(connection.getErrorStream(), CHARSET_ENCODING);
            else inputStreamReader = new InputStreamReader(connection.getInputStream(), CHARSET_ENCODING);

            // reads the response
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = bufferedReader.readLine()) != null)
                response.append(responseLine.trim());
            stringResponse = response.toString();
            bufferedReader.close();
        }
        catch (Exception e) {
            Log.e(TAG, "requestGET: ", e);
        }
        finally {
            if (connection != null) connection.disconnect();
        }
        return convertStringToJson(stringResponse);
    }

    public static JsonObject convertStringToJson(String response) {
        JsonObject jsonResponse = null;
        if (response != ERROR) jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        return jsonResponse;
    }

//    Map<String, String> parameters
//            // adds request body
//            connection.setDoOutput(true);
//            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
//            outputStream.writeBytes(convertParameters(parameters));
//            outputStream.flush();
//            outputStream.close();
//    private static String convertParameters(Map<String, String> parameters) throws UnsupportedEncodingException {
//        if (parameters.isEmpty()) return "";
//
//        StringBuilder paramURL = new StringBuilder();
//
//        for (Map.Entry<String, String> entry : parameters.entrySet()) {
//            paramURL.append(URLEncoder.encode(entry.getKey(), CHARSET_ENCODING));
//            paramURL.append("=");
//            paramURL.append(URLEncoder.encode(entry.getValue(), CHARSET_ENCODING));
//            paramURL.append("&");
//        }
//
//        return paramURL.toString().substring(0, paramURL.length() - 1);
//    }
}
