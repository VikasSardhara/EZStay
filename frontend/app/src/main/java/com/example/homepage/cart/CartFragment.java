package com.example.homepage.cart;

import android.content.Intent;
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
import com.example.homepage.REGISTERLOGIN.Login;
import com.example.homepage.dashboard.DashboardFragment;
import com.example.homepage.utils.BookingCart;
import com.example.homepage.utils.ConfirmedBookingManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.google.firebase.auth.FirebaseUser;

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

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
                    + "Check-out: " + sdf.format(res.getCheckOutDate()) + "\n"
                    + "Price: " + currencyFormat.format(res.getPrice());

            tvDetails.setText(info);
            tvPrice.setText("Price: " + currencyFormat.format(res.getPrice()));

            btnRemove.setOnClickListener(v -> {
                BookingCart.removeItem(res);
                Toast.makeText(getContext(), "Removed from cart", Toast.LENGTH_SHORT).show();
                displayCartItems();

                String checkIn = String.valueOf(res.getCheckInDate());
                String checkOut = String.valueOf(res.getCheckOutDate());
                String roomId = res.getRoomType().equalsIgnoreCase("King") ? "101" : "102";

                // Use StringRequest instead of JsonObjectRequest
                String unlockUrl = "http://10.0.2.2:5000/lock";
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
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                if (mUser != null) {
                    Intent i = new Intent(requireContext(), com.example.homepage.Payment.CheckoutActivity.class);
                    i.putExtra("amount", (int)(res.getPrice()));
                    i.putExtra("email", mUser.getEmail());
                    i.putExtra("roomId", res.getRoomId());
                    i.putExtra("checkInDate", sdf.format(res.getCheckInDate()));  // be sure format is yyyy-MM-dd if backend expects it
                    i.putExtra("checkOutDate", sdf.format(res.getCheckOutDate()));
                    i.putExtra("numGuests", res.getGuestCount());
                    startActivity(i);
                } else {
                    Intent intent = new Intent(requireContext(), Login.class);
                    intent.putExtra("from_checkout", true);
                    intent.putExtra("amount", (int)(res.getPrice()));
                    startActivity(intent);
                }
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