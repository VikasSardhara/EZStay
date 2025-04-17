/* package com.example.homepage.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.homepage.R;
import com.example.homepage.dashboard.DashboardFragment;
import com.example.homepage.utils.BookingCart;
import com.example.homepage.utils.ConfirmedBookingManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private LinearLayout cartContainer;

    public CartFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        cartContainer = view.findViewById(R.id.cartContainer);

        displayCartItems();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToDashboard();
            }
        });

        return view;
    }

    private void displayCartItems() {
        cartContainer.removeAllViews();
        List<BookingCart.Reservation> items = BookingCart.getItems();

        if (items.isEmpty()) {
            TextView emptyMsg = new TextView(getContext());
            emptyMsg.setText("Your cart is empty.");
            cartContainer.addView(emptyMsg);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        SimpleDateFormat sdfBackend = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        for (BookingCart.Reservation res : items) {
            View card = inflater.inflate(R.layout.item_cart_reservation, cartContainer, false);

            TextView tvDetails = card.findViewById(R.id.tvReservationDetails);
            TextView tvPrice = card.findViewById(R.id.tvReservationPrice);
            Button btnRemove = card.findViewById(R.id.btnRemove);
            Button btnConfirm = card.findViewById(R.id.btnConfirmBooking);
            Button btnPay = card.findViewById(R.id.btnPayment);

            String info = "Room: " + res.getRoomType() + "\n"
                    + "Smoking: " + res.getSmokingPreference() + "\n"
                    + "Guests: " + res.getGuestCount() + "\n"
                    + "Check-in: " + sdf.format(res.getCheckInDate()) + "\n"
                    + "Check-out: " + sdf.format(res.getCheckOutDate());

            tvDetails.setText(info);
            tvPrice.setText("Price: " + currencyFormat.format(res.getPrice()));

            btnRemove.setOnClickListener(v -> {
                BookingCart.removeItem(res);
                Toast.makeText(getContext(), "Removed from cart", Toast.LENGTH_SHORT).show();
                displayCartItems();

                String checkIn = sdfBackend.format(res.getCheckInDate());
                String checkOut = sdfBackend.format(res.getCheckOutDate());
                String roomId = res.getRoomType().equalsIgnoreCase("King") ? "101" : "102";


                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("room_id", roomId);
                    jsonBody.put("check_in", checkIn);
                    jsonBody.put("check_out", checkOut);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String unlockUrl = "http://192.168.1.117:5000/lock";

                JsonObjectRequest deleteRequest = new JsonObjectRequest(
                        Request.Method.DELETE,
                        unlockUrl,
                        jsonBody,
                        response -> Toast.makeText(getContext(), "Backend lock removed", Toast.LENGTH_SHORT).show(),
                        error -> Toast.makeText(getContext(), "Failed to remove lock", Toast.LENGTH_SHORT).show()
                ) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(requireContext());
                queue.add(deleteRequest);
            });



            btnConfirm.setOnClickListener(v -> {
                BookingCart.removeItem(res);
                ConfirmedBookingManager.confirmBooking(res);
                Toast.makeText(getContext(), "Booking confirmed!", Toast.LENGTH_SHORT).show();
                highlightDashboardTab();
                navigateToDashboard();
            });

            btnPay.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Redirecting to payment... (Not implemented)", Toast.LENGTH_SHORT).show();
                highlightDashboardTab();
                navigateToDashboard();
            });

            cartContainer.addView(card);
        }
    }

    private void highlightDashboardTab() {
        BottomNavigationView navView = requireActivity().findViewById(R.id.bottomNavigationView);
        navView.setSelectedItemId(R.id.nav_dashboard);
    }

    private void navigateToDashboard() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new DashboardFragment());
        transaction.commit();
    }
}

*/

package com.example.homepage.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.homepage.R;
import com.example.homepage.dashboard.DashboardFragment;
import com.example.homepage.utils.BookingCart;
import com.example.homepage.utils.ConfirmedBookingManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private LinearLayout cartContainer;

    public CartFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        cartContainer = view.findViewById(R.id.cartContainer);

        displayCartItems();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToDashboard();
            }
        });

        return view;
    }

    private void displayCartItems() {
        cartContainer.removeAllViews();
        List<BookingCart.Reservation> items = BookingCart.getItems();

        if (items.isEmpty()) {
            TextView emptyMsg = new TextView(getContext());
            emptyMsg.setText("Your cart is empty.");
            cartContainer.addView(emptyMsg);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        SimpleDateFormat sdfBackend = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        for (BookingCart.Reservation res : items) {
            View card = inflater.inflate(R.layout.item_cart_reservation, cartContainer, false);

            TextView tvDetails = card.findViewById(R.id.tvReservationDetails);
            TextView tvPrice = card.findViewById(R.id.tvReservationPrice);
            Button btnRemove = card.findViewById(R.id.btnRemove);
            Button btnConfirm = card.findViewById(R.id.btnConfirmBooking);
            Button btnPay = card.findViewById(R.id.btnPayment);

            String info = "Room: " + res.getRoomType() + "\n"
                    + "Smoking: " + res.getSmokingPreference() + "\n"
                    + "Guests: " + res.getGuestCount() + "\n"
                    + "Check-in: " + sdf.format(res.getCheckInDate()) + "\n"
                    + "Check-out: " + sdf.format(res.getCheckOutDate());

            tvDetails.setText(info);
            tvPrice.setText("Price: " + currencyFormat.format(res.getPrice()));

            btnRemove.setOnClickListener(v -> {
                BookingCart.removeItem(res);
                Toast.makeText(getContext(), "Removed from cart", Toast.LENGTH_SHORT).show();
                displayCartItems();

                String checkIn = sdfBackend.format(res.getCheckInDate());
                String checkOut = sdfBackend.format(res.getCheckOutDate());
                String roomId = res.getRoomType().equalsIgnoreCase("King") ? "101" : "102";

                // Use StringRequest instead of JsonObjectRequest
                String unlockUrl = "http://192.168.1.117:5000/lock";
                // Add the parameters to the URL as query parameters
                unlockUrl += "?room_id=" + roomId + "&check_in=" + checkIn + "&check_out=" + checkOut;

                RequestQueue queue = Volley.newRequestQueue(requireContext());

                StringRequest deleteRequest = new StringRequest(
                        Request.Method.DELETE,
                        unlockUrl,
                        response -> Toast.makeText(getContext(), "Backend lock removed", Toast.LENGTH_SHORT).show(),
                        error -> Toast.makeText(getContext(), "Failed to remove lock: " + error.toString(), Toast.LENGTH_SHORT).show()
                );

                queue.add(deleteRequest);
            });

            btnConfirm.setOnClickListener(v -> {
                BookingCart.removeItem(res);
                ConfirmedBookingManager.confirmBooking(res);
                Toast.makeText(getContext(), "Booking confirmed!", Toast.LENGTH_SHORT).show();
                highlightDashboardTab();
                navigateToDashboard();
            });

            btnPay.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Redirecting to payment... (Not implemented)", Toast.LENGTH_SHORT).show();
                highlightDashboardTab();
                navigateToDashboard();
            });

            cartContainer.addView(card);
        }
    }
    private void highlightDashboardTab() {
        BottomNavigationView navView = requireActivity().findViewById(R.id.bottomNavigationView);
        navView.setSelectedItemId(R.id.nav_dashboard);
    }

    private void navigateToDashboard() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new DashboardFragment());
        transaction.commit();
    }
}
