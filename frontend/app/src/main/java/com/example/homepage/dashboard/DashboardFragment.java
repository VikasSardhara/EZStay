package com.example.homepage.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.homepage.R;
import com.example.homepage.cart.CartFragment;
import com.example.homepage.ui.dashboard.DashboardViewModel;
import com.example.homepage.utils.ConfirmedBookingManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private LinearLayout currentReservationsContainer;

    public DashboardFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        currentReservationsContainer = view.findViewById(R.id.currentReservationsContainer);
        Button btnViewCart = view.findViewById(R.id.btnViewCart);

        btnViewCart.setOnClickListener(v -> openCartFragment());
        displayCurrentReservations();

        return view;
    }

    private void displayCurrentReservations() {
        currentReservationsContainer.removeAllViews();
        List<ConfirmedBookingManager.ConfirmedReservation> confirmed = ConfirmedBookingManager.getConfirmedBookings();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        if (confirmed.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No current reservations.");
            currentReservationsContainer.addView(empty);
            return;
        }

        for (ConfirmedBookingManager.ConfirmedReservation res : confirmed) {
            View card = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_dashboard_reservation, currentReservationsContainer, false);

            TextView tvDetails = card.findViewById(R.id.tvReservationDetails);
            Button btnPay = card.findViewById(R.id.btnPayment);

            btnPay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(requireContext(), com.example.homepage.Payment.Checkout.class);
                    startActivity(i);
                }
            });


            Button btnRemove = card.findViewById(R.id.btnRemove);

            tvDetails.setText(
                    "Room: " + res.getReservation().getRoomType() + "\n"
                            + "Smoking: " + res.getReservation().getSmokingPreference() + "\n"
                            + "Guests: " + res.getReservation().getGuestCount() + "\n"
                            + "Check-in: " + sdf.format(res.getReservation().getCheckInDate()) + "\n"
                            + "Check-out: " + sdf.format(res.getReservation().getCheckOutDate())
            );

            btnPay.setOnClickListener(v -> {
                // Placeholder for payment integration
            });

            btnRemove.setOnClickListener(v -> {
                ConfirmedBookingManager.removeConfirmedReservation(res.getReservation());
                displayCurrentReservations();
            });

            currentReservationsContainer.addView(card);
        }
    }

    private void openCartFragment() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new CartFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }



}
