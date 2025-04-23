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

import com.example.homepage.R;
import com.example.homepage.REGISTERLOGIN.Login;
import com.example.homepage.dashboard.DashboardFragment;
import com.example.homepage.utils.BookingCart;
import com.example.homepage.utils.ConfirmedBookingManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

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
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

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
                    + "Check-out: " + sdf.format(res.getCheckOutDate()) + "\n"
                    + "Price: " + currencyFormat.format(res.getPrice());

            tvDetails.setText(info);

            btnRemove.setOnClickListener(v -> {
                BookingCart.removeItem(res);
                highlightDashboardTab();
                navigateToDashboard();
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
                    i.putExtra("amount", (int)(res.getPrice() * 100));
                    startActivity(i);
                } else {
                    Intent intent = new Intent(requireContext(), Login.class);
                    intent.putExtra("from_checkout", true);
                    intent.putExtra("amount", (int)(res.getPrice() * 100));
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