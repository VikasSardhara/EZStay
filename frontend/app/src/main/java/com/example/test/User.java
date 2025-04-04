package com.example.test;

import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class User implements Runnable {
    private String firstName;
    private String lastName;
    private String dob;
    private String email;

    User(String firstName, String lastName, String dob, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.email = email;
    }

    @Override
    public void run() {
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL("http://10.0.2.2:5000/register"); // http://127.0.0.1:5000/register Change to 10.0.2.2 if using an emulator,
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            // Create JSON object with data
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("name", firstName + " " + lastName);
            jsonParam.put("email", email);
            jsonParam.put("dob", dob);

            // Send data
            byte[] postData = jsonParam.toString().getBytes("UTF-8");
            OutputStream os = urlConnection.getOutputStream();
            os.write(postData);
            os.flush();
            os.close();

            int responseCode = urlConnection.getResponseCode();
            Log.d("Response", "Server Response Code: " + responseCode);

        } catch (Exception e) {
            Log.e("Error", "Failed to send data: " + e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
