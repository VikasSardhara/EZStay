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

import com.example.homepage.R;
import com.example.homepage.dashboard.DashboardFragment;
import com.example.homepage.utils.BookingCart;
import com.example.homepage.utils.ConfirmedBookingManager;

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

        // Handle back button manually to go to DashboardFragment
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

        for (BookingCart.Reservation res : items) {
            View card = inflater.inflate(R.layout.item_cart_reservation, cartContainer, false);

            TextView tvDetails = card.findViewById(R.id.tvReservationDetails);
            Button btnRemove = card.findViewById(R.id.btnRemove);
            Button btnConfirm = card.findViewById(R.id.btnConfirmBooking);
            Button btnPay = card.findViewById(R.id.btnPayment);

            String info = "Room: " + res.getRoomType() + "\n"
                    + "Smoking: " + res.getSmokingPreference() + "\n"
                    + "Guests: " + res.getGuestCount() + "\n"
                    + "Check-in: " + sdf.format(res.getCheckInDate()) + "\n"
                    + "Check-out: " + sdf.format(res.getCheckOutDate());

            tvDetails.setText(info);

            btnRemove.setOnClickListener(v -> {
                BookingCart.removeItem(res);
                displayCartItems(); // Refresh
            });

            btnConfirm.setOnClickListener(v -> {
                BookingCart.removeItem(res); // ✅ Remove FIRST
                ConfirmedBookingManager.confirmBooking(res); // ✅ Then confirm
                Toast.makeText(getContext(), "Booking confirmed!", Toast.LENGTH_SHORT).show();
                displayCartItems(); // Refresh UI
            });

            btnPay.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Redirecting to payment... (Not implemented)", Toast.LENGTH_SHORT).show();
            });

            cartContainer.addView(card);
        }
    }

    private void navigateToDashboard() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new DashboardFragment());
        transaction.commit();
    }
}
