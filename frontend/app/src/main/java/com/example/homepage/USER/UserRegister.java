package com.example.homepage.USER;

import android.util.Log;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserRegister {

    public static void registerUser(String firstName, String lastName, String dob, String email,int userID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://10.0.2.2:5000/register");
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
        }).start();
    }
}
