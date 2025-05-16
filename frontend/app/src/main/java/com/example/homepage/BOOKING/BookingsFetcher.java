package com.example.homepage.BOOKING;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.homepage.ApiConfig;
import com.example.homepage.utils.ReservationManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class BookingsFetcher {

    public static void getBookings(int uID, boolean ifExpired, BookingsListener listener) {
        new Thread(() -> {
            ArrayList<ReservationManager.Reservation> expiredBookings = new ArrayList<>();
            ArrayList<ReservationManager.Reservation> futureBookings = new ArrayList<>();

            HttpURLConnection urlConnection = null;
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            try {
                //URL url = new URL("http://192.168.0.35:5000/bookings" + uID);
                URL url = new URL(ApiConfig.BOOKINGS_URL + uID);
                Log.d("BookingsFetcher", "Requesting URL: " + url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);

                Log.d("BookingsFetcher", "Fetching bookings for user ID: " + uID);

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == 200) {
                    InputStream is = urlConnection.getInputStream();
                    byte[] bytes = is.readAllBytes();
                    String jsonResponse = new String(bytes, StandardCharsets.UTF_8);
                    JSONObject jsonObj = new JSONObject(jsonResponse);
                    JSONArray bookingsArray = jsonObj.getJSONArray("user_bookings");

                    for (int i = 0; i < bookingsArray.length(); i++) {
                        JSONObject JSONbooking = bookingsArray.getJSONObject(i);
                        int bookingID = JSONbooking.getInt("booking_id");
                        int roomID = JSONbooking.getInt("room_id");
                        String checkIN = JSONbooking.getString("check_in_date");
                        String checkOUT = JSONbooking.getString("check_out_date");
                        int num_guests = JSONbooking.getInt("num_guests");

                        ReservationManager.Reservation booking = new ReservationManager.Reservation(bookingID, roomID, checkIN, checkOUT, num_guests);

                        LocalDate checkOutTime = LocalDate.parse(checkOUT, dateFormatter);

                        if (currentDate.isAfter(checkOutTime)) {
                            expiredBookings.add(booking);
                        } else {
                            futureBookings.add(booking);
                        }
                    }

                    // Update ReservationManager
                    ReservationManager.getCurrentReservations().clear();
                    ReservationManager.getPastReservations().clear();
                    ReservationManager.getCurrentReservations().addAll(futureBookings);
                    ReservationManager.getPastReservations().addAll(expiredBookings);

                    // Return result on main thread
                    ArrayList<ReservationManager.Reservation> result = ifExpired ? expiredBookings : futureBookings;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onBookingsReceived(result);
                    });

                } else {
                    postError(listener, "Failed to fetch bookings. Response code: " + responseCode);
                }

            } catch (Exception e) {
                postError(listener, "Error fetching bookings: " + e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    public static void getBookingsAll(BookingsListener listener) {
        new Thread(() -> {
            ArrayList<ReservationManager.Reservation> allBookings = new ArrayList<>();

            HttpURLConnection urlConnection = null;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            try {
                //URL url = new URL("http://192.168.0.35:5000/bookings");
                URL url = new URL(ApiConfig.BOOKINGS_URL);
                Log.d("BookingsFetcher", "Requesting URL: " + url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);


                Log.d("BookingsFetcher", "Requesting URL: " + url.toString());

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == 200) {
                    InputStream is = urlConnection.getInputStream();
                    byte[] bytes = is.readAllBytes();
                    String jsonResponse = new String(bytes, StandardCharsets.UTF_8);
                    JSONObject jsonObj = new JSONObject(jsonResponse);
                    JSONArray bookingsArray = jsonObj.getJSONArray("bookings");

                    for (int i = 0; i < bookingsArray.length(); i++) {
                        JSONObject JSONbooking = bookingsArray.getJSONObject(i);
                        int bookingID = JSONbooking.getInt("booking_id");
                        int roomID = JSONbooking.getInt("room_id");
                        String checkIN = JSONbooking.getString("check_in_date");
                        String checkOUT = JSONbooking.getString("check_out_date");
                        int num_guests = JSONbooking.getInt("num_guests");

                        ReservationManager.Reservation booking = new ReservationManager.Reservation(bookingID, roomID, checkIN, checkOUT, num_guests);

                        allBookings.add(booking);
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onBookingsReceived(allBookings);
                    });

                } else {
                    postError(listener, "Failed to fetch bookings. Response code: " + responseCode);
                }

            } catch (Exception e) {
                postError(listener, "Error fetching bookings: " + e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    private static void postError(BookingsListener listener, String message) {
        if (listener != null) {
            new Handler(Looper.getMainLooper()).post(() -> listener.onError(message));
        }
    }


    public interface BookingsListener {
        void onBookingsReceived(ArrayList<ReservationManager.Reservation> bookings);

        void onError(String message);
    }
}
