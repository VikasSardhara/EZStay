package com.example.homepage;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.Serializable;


import com.example.homepage.REGISTERLOGIN.Login;
import com.example.homepage.REGISTERLOGIN.Register;
import com.google.firebase.auth.FirebaseUser;


import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class User implements Serializable{

    private String firstName;
    private String lastName;
    private String dob;
    private String email;
    private String token;

    public User(String firstName, String lastName, String dob, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.email = email;
    }

    public void registerUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://127.0.0.1:5000/register"); // Change to 10.0.2.2 if using an emulator
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);

                    // Create JSON object with data
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("first name", firstName + " " + lastName);
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

    public void getUserInfo(final String email, final UserInfoCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://127.0.0.1:5000/users/" + URLEncoder.encode(email, StandardCharsets.UTF_8));
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

                        String fullName = jsonObject.getString("name");
                        String[] name_split = fullName.split(" ");
                        String firstname = name_split[0];
                        String lastname = name_split[1];

                        String dob = jsonObject.getString("dob");
                        String email = jsonObject.getString("email");

                        User user = new User(firstname, lastname, dob, email);
                        callback.onUserInfoReceived(user);

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

    public void getBooking(final String uID, final UserInfoCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://127.0.0.1:5000/bookings/" + URLEncoder.encode(uID, StandardCharsets.UTF_8));
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Authorization", "Bearer " + token); // Use Firebase token here
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);

                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == 200) {
                        InputStream is = urlConnection.getInputStream();
                        byte[] bytes = is.readAllBytes();
                        String jsonResponse = new String(bytes, StandardCharsets.UTF_8);

                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        int bookingID = jsonObject.getInt("booking_id");
                        int roomID = jsonObject.getInt("room_id");

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

    public static void checkIfLoggedIn(final FirebaseUser user, final Context context) {
        if (user != null) {
            // User is logged in, navigate to MainActivity
            if (context instanceof Login) {
                Intent i = new Intent(context, MainActivity.class);
                i.putExtra("navigateTo", "main");
                context.startActivity(i);
            }
        } else {
            // User is not logged in, navigate to Register
            Log.e("Error", "No account found");
            Intent i = new Intent(context, Register.class);
            context.startActivity(i);
        }
    }

    // Get the full name of the user
    public String getName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDob() {
        return dob;
    }

    public String getEmail() {
        return email;
    }

    // Callback interface for passing user info
    public interface UserInfoCallback {
        void onUserInfoReceived(User user);
    }
}
