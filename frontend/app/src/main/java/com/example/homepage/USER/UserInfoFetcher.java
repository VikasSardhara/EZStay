package com.example.homepage.USER;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UserInfoFetcher {

    public static void getUserInfo(final String email) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://10.0.2.2:5000/users/" + URLEncoder.encode(email, StandardCharsets.UTF_8));
                    Log.d("URL", "Accessing: " + url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);

                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == 200) {
                        InputStream is = urlConnection.getInputStream();
                        byte[] bytes = is.readAllBytes();
                        String jsonResponse = new String(bytes, StandardCharsets.UTF_8);
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        Log.d("API Response", "Response: " + jsonResponse);

                        JSONArray usersArray = jsonObject.getJSONArray("users");
                        JSONObject identified_user = usersArray.getJSONObject(0);

                        String fullName = identified_user.getString("name");
                        String[] name_split = fullName.split(" ");
                        String firstname = name_split[0];
                        String lastname = name_split[1];

                        Log.d("infoo",  firstname);

                        String dob = identified_user.getString("dob");
                        String email = identified_user.getString("email");
                        int userID = identified_user.getInt("user_id");

                        User.setUserData(firstname, lastname, dob, email, userID);

                    } else {
                        Log.e("Error", "Unable to retrieve user info. Response code: " + responseCode);
                    }

                } catch (Exception e) {
                    Log.e("Error", "Failed to get data: " + e.getMessage());
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    public interface UserInfoCallback {
        void onUserInfoReceived(User user);
    }
}
