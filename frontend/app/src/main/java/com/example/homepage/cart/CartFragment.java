package com.example.homepage.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.homepage.R;
import com.example.homepage.dashboard.DashboardFragment;
import com.example.homepage.utils.BookingCart;
import com.example.homepage.utils.ConfirmedBookingManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.homepage.ApiConfig;

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
        if (getContext() == null || cartContainer == null) return;

        try {
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

                    String checkIn = sdf.format(res.getCheckInDate());
                    String checkOut = sdf.format(res.getCheckOutDate());
                    int roomId = res.getRoomId();

                    //String unlockUrl = "http:///192.168.0.35:5000/lock?room_id=" + roomId + "&check_in=" + checkIn + "&check_out=" + checkOut;
                    String unlockUrl = ApiConfig.LOCKROOMID_URL + roomId + "&check_in=" + checkIn + "&check_out=" + checkOut;

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

                cartContainer.addView(card);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error displaying cart: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void highlightDashboardTab() {
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.bottomNavigationView);
            if (navView != null) {
                navView.setSelectedItemId(R.id.nav_dashboard);
            }
        }
    }

    private void navigateToDashboard() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new DashboardFragment());
        transaction.commit();
    }
}
