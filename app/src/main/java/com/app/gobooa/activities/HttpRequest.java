package com.app.gobooa.activities;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// This is an server connection class. This java file is used to fetch data from server or send/update data to/on server..
public class HttpRequest {

    //This function is used to fetch data from server according to provided url and user credentials as arguments//
    public static String executeGet(String targetURL, String consumerKey, String consumerSecret) {
        URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Add authentication headers
            String authStr = consumerKey + ":" + consumerSecret;
            String authEncoded = Base64.encodeToString(authStr.getBytes(), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", "Basic " + authEncoded);

            InputStream is;
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK)
                is = connection.getErrorStream();
            else
                is = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("fetchError","Error: " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    //This function is used to post/send data to server. In this project this function is used to update order status on server..
    public static String executePost(String domain, String consumerKey, String consumerSecret, int orderId, String orderStatus) throws IOException {
        HttpURLConnection connection = null;
        try {
            // WooCommerce API URL for updating an order by its ID
            String apiUrl = domain + "/wp-json/wc/v3/orders/" + orderId;

            // Construct JSON request body with the new status
            JSONObject requestBody = new JSONObject();
            requestBody.put("status", orderStatus);

            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Add authentication headers
            String authStr = consumerKey + ":" + consumerSecret;
            String authEncoded = Base64.encodeToString(authStr.getBytes(), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", "Basic " + authEncoded);

            // Enable input and output streams
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Set the content type to JSON
            connection.setRequestProperty("Content-Type", "application/json");

            // Write the JSON request body to the output stream
            OutputStream os = connection.getOutputStream();
            os.write(requestBody.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            // Get the response code
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Order status updated successfully
                return "true";
            } else {
                // Handle the error (e.g., failed to update order status)
                Log.e("UpdateOrderStatus", "Failed to update order status. Response code: " + responseCode);

                // Print the response body for debugging
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();

                Log.e("UpdateOrderStatus2", "Error response: " + errorResponse.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log.e("UpdateOrderStatus", "Error updating order status: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return "false";
    }

}
