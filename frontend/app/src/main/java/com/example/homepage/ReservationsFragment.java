package com.example.homepage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.homepage.R;
import com.example.homepage.utils.ConfirmedBookingManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReservationsFragment extends Fragment {

    public ReservationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reservations, container, false);
        LinearLayout previousReservationsContainer = view.findViewById(R.id.previousReservationsContainer);







        List<ConfirmedBookingManager.ConfirmedReservation> confirmed = ConfirmedBookingManager.getConfirmedBookings();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        if (confirmed.isEmpty()) {
            TextView noData = new TextView(getContext());
            noData.setText("No previous reservations.");
            previousReservationsContainer.addView(noData);
        } else {
            for (ConfirmedBookingManager.ConfirmedReservation res : confirmed) {
                TextView reservation = new TextView(getContext());
                reservation.setText(
                        "Room: " + res.getReservation().getRoomType() + "\n"
                                + "Smoking: " + res.getReservation().getSmokingPreference() + "\n"
                                + "Guests: " + res.getReservation().getGuestCount() + "\n"
                                + "Check-in: " + sdf.format(res.getReservation().getCheckInDate()) + "\n"
                                + "Check-out: " + sdf.format(res.getReservation().getCheckOutDate())
                );
                reservation.setPadding(0, 0, 0, 24);
                previousReservationsContainer.addView(reservation);
            }
        }

        return view;
    }
}
