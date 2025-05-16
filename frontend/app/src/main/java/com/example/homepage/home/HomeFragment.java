// Updated HomeFragment.java with fixes to prevent crash after "Book Now"

package com.example.homepage.home;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.homepage.BOOKING.BookingsFetcher;
import com.example.homepage.R;
import com.example.homepage.cart.CartFragment;
import com.example.homepage.utils.BookingCart;
import com.example.homepage.utils.ReservationManager;
import com.example.homepage.ApiConfig;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private Calendar checkInCalendar;
    private Calendar checkOutCalendar;
    private int guestCount = 1;

    private TextView tvCheckInDate, tvCheckOutDate, tvGuestCount;
    private RadioButton rbSmoking, rbKing;
    private RadioGroup rgRoomType;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        initDateDefaults();
        setListeners();

        return view;
    }

    private void initViews(View view) {
        tvCheckInDate = view.findViewById(R.id.tvCheckInDate);
        tvCheckOutDate = view.findViewById(R.id.tvCheckOutDate);
        tvGuestCount = view.findViewById(R.id.tvGuestCount);
        rbSmoking = view.findViewById(R.id.rbSmoking);
        rbKing = view.findViewById(R.id.rbKing);
        rgRoomType = view.findViewById(R.id.rgRoomType);

        Button btnCheckInDate = view.findViewById(R.id.btnCheckInDate);
        Button btnCheckOutDate = view.findViewById(R.id.btnCheckOutDate);
        Button btnDecreaseGuests = view.findViewById(R.id.btnDecreaseGuests);
        Button btnIncreaseGuests = view.findViewById(R.id.btnIncreaseGuests);
        Button btnBookNow = view.findViewById(R.id.btnBookNow);

        btnCheckInDate.setOnClickListener(v -> showDatePickerDialog(true));
        btnCheckOutDate.setOnClickListener(v -> showDatePickerDialog(false));

        btnDecreaseGuests.setOnClickListener(v -> {
            if (guestCount > 1) {
                guestCount--;
                tvGuestCount.setText(String.valueOf(guestCount));
            }
        });

        btnIncreaseGuests.setOnClickListener(v -> {
            int maxGuests = rbKing.isChecked() ? 2 : 4;
            if (guestCount < maxGuests) {
                guestCount++;
                tvGuestCount.setText(String.valueOf(guestCount));
            }
        });

        btnBookNow.setOnClickListener(v -> handleBookNow());
    }

    private void initDateDefaults() {
        checkInCalendar = Calendar.getInstance();
        checkOutCalendar = Calendar.getInstance();
        checkOutCalendar.add(Calendar.DAY_OF_MONTH, 1);
        updateDateText();
    }

    private void setListeners() {
        tvGuestCount.setText(String.valueOf(guestCount));
    }

    private void handleBookNow() {
        if (checkInCalendar == null || checkOutCalendar == null) {
            showToast("Please select check-in and check-out dates.");
            return;
        }

        if (checkOutCalendar.before(checkInCalendar)) {
            showToast("Check-out must be after check-in.");
            return;
        }

        long days = TimeUnit.MILLISECONDS.toDays(checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis());
        if (days > 30) {
            showToast("Booking cannot exceed 30 days.");
            return;
        }

        if (rgRoomType.getCheckedRadioButtonId() == -1) {
            showToast("Please select a room type.");
            return;
        }

        if (guestCount > 2 && rbKing.isChecked()) {
            showToast("King room allows max 2 guests.");
            return;
        }

        String roomType = rbKing.isChecked() ? "King" : "Queen";
        String smokingPref = rbSmoking.isChecked() ? "Smoking" : "Non-Smoking";

        BookingsFetcher.getBookingsAll(new BookingsFetcher.BookingsListener() {
            @Override
            public void onBookingsReceived(ArrayList<ReservationManager.Reservation> bookings) {
                fetchAndLockAvailableRoom(bookings, roomType, smokingPref);
            }

            @Override
            public void onError(String message) {
                showToast("Failed to load bookings: " + message);
            }
        });
    }

    private void fetchAndLockAvailableRoom(ArrayList<ReservationManager.Reservation> bookings, String roomType, String smokingPref) {
        //String url = "http:///192.168.0.35:5000/rooms?size=" + roomType + "&type=" + smokingPref + "&guests=" + guestCount + "&check_in=" + formatDate(checkInCalendar) + "&check_out=" + formatDate(checkOutCalendar);
        String url = ApiConfig.ROOMSIZE_URL + roomType + "&type=" + smokingPref + "&guests=" + guestCount + "&check_in=" + formatDate(checkInCalendar) + "&check_out=" + formatDate(checkOutCalendar);



        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                JSONArray rooms = response.getJSONArray("rooms");
                int availableRoomId = getAvailableRoomId(rooms, bookings);

                if (availableRoomId != -1) {
                    lockRoom(availableRoomId);
                    BookingCart.addItem(new BookingCart.Reservation(checkInCalendar.getTime(), checkOutCalendar.getTime(), roomType, smokingPref, guestCount, availableRoomId));

                    showToast("Room locked and added to cart.");
                    new Handler(Looper.getMainLooper()).postDelayed(this::navigateToCart, 500);
                } else {
                    showToast("No available rooms for selected dates.");
                }
            } catch (JSONException e) {
                showToast("Error parsing response.");
            }
        }, error -> showToast("Error: " + error.getMessage()));

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private int getAvailableRoomId(JSONArray rooms, ArrayList<ReservationManager.Reservation> bookings) throws JSONException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate selectedCheckIn = checkInCalendar.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate selectedCheckOut = checkOutCalendar.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        for (int i = 0; i < rooms.length(); i++) {
            JSONObject room = rooms.getJSONObject(i);
            int roomId = room.getInt("room_id");

            boolean isAvailable = bookings.stream().noneMatch(res -> res.roomID == roomId && !(selectedCheckOut.isBefore(LocalDate.parse(res.checkIN, formatter)) || selectedCheckIn.isAfter(LocalDate.parse(res.checkOUT, formatter))));

            if (isAvailable) return roomId;
        }
        return -1;
    }

    private void lockRoom(int roomId) {
        //String url = "http://10.40.146.171:5000/lock";
        String url = ApiConfig.LOCK_URL;

        JSONObject body = new JSONObject();
        try {
            body.put("room_id", roomId);
            body.put("check_in", formatDate(checkInCalendar));
            body.put("check_out", formatDate(checkOutCalendar));
        } catch (JSONException e) {
            Log.e("LockRoom", "Invalid JSON", e);
            return;
        }

        JsonObjectRequest lockRequest = new JsonObjectRequest(Request.Method.POST, url, body, response -> Log.d("Lock", "Room locked successfully"), error -> Log.e("Lock", "Failed to lock room: " + error.getMessage()));

        Volley.newRequestQueue(requireContext()).add(lockRequest);
    }

    private void showDatePickerDialog(boolean isCheckIn) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            calendar.set(year, month, day);
            if (isCheckIn) checkInCalendar = calendar;
            else checkOutCalendar = calendar;
            updateDateText();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void updateDateText() {
        tvCheckInDate.setText("Check-in: " + formatDateDisplay(checkInCalendar));
        tvCheckOutDate.setText("Check-out: " + formatDateDisplay(checkOutCalendar));
    }

    private String formatDate(Calendar cal) {
        return android.text.format.DateFormat.format("yyyy-MM-dd", cal).toString();
    }

    private String formatDateDisplay(Calendar cal) {
        return android.text.format.DateFormat.format("MM/dd/yyyy", cal).toString();
    }

    private void showToast(String msg) {
        if (isAdded()) {
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToCart() {
        if (getActivity() != null && isAdded()) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new CartFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}
