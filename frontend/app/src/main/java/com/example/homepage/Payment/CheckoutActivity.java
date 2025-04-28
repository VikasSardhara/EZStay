package com.example.homepage.Payment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homepage.MainActivity;
import com.example.homepage.R;
import com.example.homepage.USER.User;
import com.example.homepage.USER.UserInfoFetcher;
import com.example.homepage.utils.ReservationManager;
import com.example.homepage.BOOKING.sendBooking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CheckoutActivity extends AppCompatActivity {

    private static final String TAG = "CheckoutActivity";

    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    private PaymentSheet.CustomerConfiguration customerConfig;
    private FirebaseAuth auth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        paymentSheet = new PaymentSheet(this, this::handlePaymentSheetResult);
        auth = FirebaseAuth.getInstance();
        mUser = auth.getCurrentUser();

        if (mUser == null) {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (User.getInstance().getEmail() == null) {
            // Fetch user info if missing
            UserInfoFetcher.getUserInfo(mUser.getEmail());
        }

        Intent intent = getIntent();
        int amount = intent.getIntExtra("amount", 0);

        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://10.0.2.2:4242/payment-sheet");
                Log.d(TAG, "Accessing: " + url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                urlConnection.setRequestProperty("Accept", "application/json");

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("amount", amount);

                if (mUser != null && User.getInstance().getEmail() != null) {
                    User currentUser = User.getInstance();
                    jsonParam.put("first_name", currentUser.getFirstName());
                    jsonParam.put("last_name", currentUser.getLastName());
                    jsonParam.put("email", currentUser.getEmail());

                    JSONArray reservationArray = new JSONArray();
                    for (ReservationManager.Reservation res : ReservationManager.getCurrentReservations()) {
                        JSONObject resJson = new JSONObject();
                        resJson.put("size", res.roomType);
                        resJson.put("type", res.smokingPreference);
                        resJson.put("check_in", res.checkIN);
                        resJson.put("check_out", res.checkOUT);
                        reservationArray.put(resJson);
                    }
                    jsonParam.put("reservations", reservationArray);
                } else {
                    jsonParam.put("first_name", intent.getStringExtra("first_name"));
                    jsonParam.put("last_name", intent.getStringExtra("last_name"));
                    jsonParam.put("email", intent.getStringExtra("email"));
                }

                OutputStream os = urlConnection.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                writer.write(jsonParam.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == 200) {
                    InputStream is = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    String jsonResponse = sb.toString();
                    is.close();

                    Log.d(TAG, "PaymentResponse: " + jsonResponse);
                    JSONObject result = new JSONObject(jsonResponse);

                    runOnUiThread(() -> {
                        try {
                            customerConfig = new PaymentSheet.CustomerConfiguration(
                                    result.getString("customer"),
                                    result.getString("ephemeralKey")
                            );
                            paymentIntentClientSecret = result.getString("paymentIntent");
                            PaymentConfiguration.init(getApplicationContext(), result.getString("publishableKey"));
                            presentPaymentSheet();
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing Stripe response", e);
                            Toast.makeText(this, "Error initializing payment", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "Server error code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Payment init failed", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    private void presentPaymentSheet() {
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("EZSTAY CORP")
                .customer(customerConfig)
                .allowsDelayedPaymentMethods(true)
                .build();
        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
    }

    private void handlePaymentSheetResult(@NonNull PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, "Payment canceled");
            Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Failed) {
            Log.e(TAG, "Payment failed", ((PaymentSheetResult.Failed) result).getError());
            Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Completed) {
            Log.d(TAG, "Payment successful");

            User currentUser = User.getInstance();
            int userId = currentUser.getUserID();

            int roomId = getIntent().getIntExtra("roomId", -1);
            String checkInDate = getIntent().getStringExtra("checkInDate");
            String checkOutDate = getIntent().getStringExtra("checkOutDate");
            int numGuests = getIntent().getIntExtra("numGuests", 1);

            sendBooking.makeBooking(userId, roomId, checkInDate, checkOutDate, numGuests);

            Toast.makeText(this, "Booking confirmed!", Toast.LENGTH_LONG).show();

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        }
    }
}
