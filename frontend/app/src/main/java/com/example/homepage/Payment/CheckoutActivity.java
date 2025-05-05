package com.example.homepage.Payment;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static com.example.homepage.USER.User.getInstance;

import com.example.homepage.MainActivity;
import com.example.homepage.USER.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homepage.R;
import com.example.homepage.USER.User;
import com.example.homepage.utils.ConfirmedBookingManager;
import com.example.homepage.utils.ReservationManager;
import com.example.homepage.BOOKING.sendBooking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stripe.android.paymentsheet.*;
import com.stripe.android.PaymentConfiguration;

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

    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration customerConfig;
    private static final String TAG = "CheckoutActivity";

    FirebaseUser mUser;
    FirebaseAuth auth;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        paymentSheet = new PaymentSheet(this, new PaymentSheetResultCallback() {
            @Override
            public void onPaymentSheetResult(@NonNull PaymentSheetResult paymentSheetResult) {
                handlePaymentSheetResult(paymentSheetResult);
            }
        });

        Intent i = getIntent();
        int amount = i.getIntExtra("amount", 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://10.0.2.2:4242/payment-sheet");
                    Log.d("URL", "Accessing: " + url);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");

                    urlConnection.setRequestProperty("Accept", "application/json");

                    Log.d("PaymentRequest", "Amount to be sent: " + amount);

                    auth = FirebaseAuth.getInstance();
                    mUser = auth.getCurrentUser();

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("amount", amount);

                    Intent i = getIntent();

                    Log.d("getcurrent", "hi" + ReservationManager.getCurrentReservations());

                    if(mUser != null) {
                        jsonParam.put("first_name", getInstance().getFirstName());
                        jsonParam.put("last_name", getInstance().getLastName());
                        jsonParam.put("email", getInstance().getEmail());
                        JSONArray reservationArray = new JSONArray();
                        for (ReservationManager.Reservation res : ReservationManager.getCurrentReservations()) {
                            JSONObject resJson = new JSONObject();
                            resJson.put("roomId", res.roomID);
                            resJson.put("check_in", res.checkIN);
                            resJson.put("check_out", res.checkOUT);
                            reservationArray.put(resJson);
                        }
                        jsonParam.put("reservations", reservationArray);
                        Log.d("array", "hi" + reservationArray);
                    }
                    else {
                        jsonParam.put("first_name", i.getStringExtra("first_name"));
                        jsonParam.put("last_name", i.getStringExtra("last_name"));
                        jsonParam.put("email", i.getStringExtra("email"));
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
                        byte[] bytes = is.readAllBytes();
                        String jsonResponse = new String(bytes, StandardCharsets.UTF_8);

                        Log.d("PaymentResponse", "Response: " + jsonResponse);

                        final JSONObject result = new JSONObject(jsonResponse);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    customerConfig = new PaymentSheet.CustomerConfiguration(
                                            result.getString("customer"),
                                            result.getString("ephemeralKey")
                                    );
                                    paymentIntentClientSecret = result.getString("paymentIntent");
                                    PaymentConfiguration.init(getApplicationContext(), result.getString("publishableKey"));
                                    presentPaymentSheet();
                                } catch (JSONException e) {
                                    Log.e(TAG, "Error parsing the response", e);
                                }
                            }
                        });
                    } else {
                        Log.e(TAG, "Server returned non-OK code: " + responseCode);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Failed to post data: " + e.getMessage(), e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
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

    private void handlePaymentSheetResult(@NonNull PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, "Payment canceled");
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Log.e(TAG, "Payment failed", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Log.d(TAG, "Payment completed successfully!");

            int userId = getInstance().getUserID();
            int roomId = getIntent().getIntExtra("roomId", -1);
            String checkInDate = getIntent().getStringExtra("checkInDate");
            String checkOutDate = getIntent().getStringExtra("checkOutDate");
            int numGuests = getIntent().getIntExtra("numGuests", 1);

            sendBooking.makeBooking(userId, roomId, checkInDate, checkOutDate, numGuests);

            Toast.makeText(this, "Booking confirmed!", Toast.LENGTH_LONG).show();
            ConfirmedBookingManager.getConfirmedBookings().clear();

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
    }
}
