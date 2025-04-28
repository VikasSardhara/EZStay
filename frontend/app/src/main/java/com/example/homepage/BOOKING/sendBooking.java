package com.example.homepage.BOOKING;

import android.util.Log;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class sendBooking {
    public static void makeBooking(int userId, int roomId, String checkInDate, String checkOutDate, int numGuests) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL("http://10.0.2.2:5000/book");  // change IP as needed
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                urlConnection.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user_id", userId);
                jsonParam.put("room_id", roomId);
                jsonParam.put("check_in_date", checkInDate);
                jsonParam.put("check_out_date", checkOutDate);
                jsonParam.put("num_guests", numGuests);

                byte[] postData = jsonParam.toString().getBytes(StandardCharsets.UTF_8);
                urlConnection.getOutputStream().write(postData);

                int responseCode = urlConnection.getResponseCode();
                Log.d("Booking", "Booking sent. Response Code: " + responseCode);

                if (responseCode == 200 || responseCode == 201) {
                    Log.d("Booking", "Booking successfully created.");
                } else {
                    Log.e("Booking", "Failed to send booking. Server responded with: " + responseCode);
                }

            } catch (Exception e) {
                Log.e("Booking", "Error sending booking: " + e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }
}
