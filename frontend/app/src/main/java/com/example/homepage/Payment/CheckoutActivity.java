package com.example.homepage.Payment;


import static com.example.homepage.USER.User.getInstance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homepage.BOOKING.sendBooking;
import com.example.homepage.MainActivity;
import com.example.homepage.utils.BookingCart;
import com.example.homepage.utils.ConfirmedBookingManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.stripe.android.paymentsheet.PaymentSheetResultCallback;
import com.example.homepage.ApiConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;


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
        String guestFirstName = i.getStringExtra("first_name");
        String guestLastName = i.getStringExtra("last_name");
        String guestEmail = i.getStringExtra("email");

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    //URL url = new URL("http://192.168.0.35:4242/payment-sheet");
                    URL url = new URL(ApiConfig.PAYMENTSHEET_URL);

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

                    if (mUser != null) {
                        jsonParam.put("first_name", getInstance().getFirstName());
                        jsonParam.put("last_name", getInstance().getLastName());
                        jsonParam.put("email", getInstance().getEmail());
                    } else {
                        jsonParam.put("first_name", i.getStringExtra("first_name"));
                        jsonParam.put("last_name", i.getStringExtra("last_name"));
                        jsonParam.put("email", i.getStringExtra("email"));
                    }
                    JSONArray reservationArray = new JSONArray();

                    for (ConfirmedBookingManager.ConfirmedReservation res : ConfirmedBookingManager.getConfirmedBookings()) {
                        JSONObject resJson = new JSONObject();

                        resJson.put("roomId", res.getReservation().getRoomId());
                        resJson.put("room_type", res.getReservation().getRoomType());
                        resJson.put("check_in", res.getReservation().getCheckInDate());
                        resJson.put("check_out", res.getReservation().getCheckOutDate());
                        ;
                        resJson.put("guest_num", res.getReservation().getGuestCount());

                        Log.d("array", "hi" + res.getReservation().getRoomType());
                        reservationArray.put(resJson);
                    }
                    jsonParam.put("reservations", reservationArray);
                    Log.d("array", "hi" + reservationArray);
                    Log.d("array", "hi" + ConfirmedBookingManager.getConfirmedBookings());


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
                                    customerConfig = new PaymentSheet.CustomerConfiguration(result.getString("customer"), result.getString("ephemeralKey"));
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
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("EZSTAY CORP").customer(customerConfig).allowsDelayedPaymentMethods(true).build();
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

            for (ConfirmedBookingManager.ConfirmedReservation res : ConfirmedBookingManager.getConfirmedBookings()) {
                BookingCart.Reservation reservation = res.getReservation();
                String checkInDate = new SimpleDateFormat("yyyy-MM-dd").format(reservation.getCheckInDate());
                String checkOutDate = new SimpleDateFormat("yyyy-MM-dd").format(reservation.getCheckOutDate());
                int roomId = reservation.getRoomId();
                int numGuests = reservation.getGuestCount();

                sendBooking.makeBooking(userId, roomId, checkInDate, checkOutDate, numGuests);
                Log.d(TAG, "Sent booking for Room ID: " + roomId);
            }
            ConfirmedBookingManager.clearConfirmedBookings();


            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
    }
}
