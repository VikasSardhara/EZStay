package com.example.test;

import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class userSendData implements Runnable {
    private String firstName;
    private String lastName;
    private String dob;
    private String email;

    userSendData(String firstName, String lastName, String dob, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.email = email;
    }

    @Override
    public void run() {
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(endpoint);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setDoOutput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("first_name", firstName);
            jsonParam.put("last_name", lastName);
            jsonParam.put("email", email);
            jsonParam.put("dob", dob);

            byte[] postData = jsonParam.toString().getBytes("UTF-8");
            OutputStream os = urlConnection.getOutputStream();

            os.write(postData);
            os.flush();
            os.close();


        } catch (Exception e) {
            Log.e("Error", "Failed to send data: " + e.getMessage());
        }
        }
    }

