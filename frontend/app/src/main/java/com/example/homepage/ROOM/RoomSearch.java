package com.example.homepage.ROOM;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RoomSearch {

    public static void getRoomById(final int roomId, final RoomSearchCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    String roomIdEncoded = URLEncoder.encode(String.valueOf(roomId), StandardCharsets.UTF_8);
                    URL url = new URL("http://10.0.2.2:5000/rooms?room_id=" + roomIdEncoded);
                    Log.d("RoomSearch", "Accessing: " + url.toString());

                    // Open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);

                    // Get the response code
                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == 200) {
                        // Read the response
                        InputStream is = urlConnection.getInputStream();
                        byte[] bytes = is.readAllBytes();
                        String jsonResponse = new String(bytes, StandardCharsets.UTF_8);

                        Log.d("RoomSearch", "Response: " + jsonResponse);

                        // Parse the response as a JSONObject
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        JSONArray roomsArray = jsonObject.getJSONArray("rooms");

                        int target_id;


                        for (int i = 0; i < roomsArray.length(); i++) {
                            JSONObject room = roomsArray.getJSONObject(i);

                            target_id= room.getInt("room_id");

                            if (target_id == roomId) {
                                String size = room.getString("size");
                                String type = room.getString("type");
                                Room roomObj = new Room(target_id, size, type);
                                callback.onRoomFound(roomObj);
                                break;
                            }
                        }


                    } else {
                        Log.e("RoomSearch", "Unable to retrieve room info. Response code: " + responseCode);
                        callback.onRoomFound(null);
                    }

                } catch (Exception e) {
                    Log.e("RoomSearch", "Failed to get data: " + e.getMessage());
                    callback.onRoomFound(null);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    public interface RoomSearchCallback {
        void onRoomFound(Room room);
    }
}
