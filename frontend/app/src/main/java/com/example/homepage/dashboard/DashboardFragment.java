package com.example.homepage.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.homepage.Payment.CheckoutActivity;
import com.example.homepage.R;
import com.example.homepage.REGISTERLOGIN.Login;
import com.example.homepage.USER.User;
import com.example.homepage.cart.CartFragment;
import com.example.homepage.utils.ConfirmedBookingManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private LinearLayout currentReservationsContainer;
    private Button btnPayFromCurrent;
    private TextView tvTotalPrice;

    public DashboardFragment() {}

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // ✅ Enables the cart menu
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        currentReservationsContainer = view.findViewById(R.id.currentReservationsContainer);
        btnPayFromCurrent = view.findViewById(R.id.btnPayFromCurrent);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);

        displayCurrentReservations();

        btnPayFromCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double total = 0;
                for (ConfirmedBookingManager.ConfirmedReservation res : ConfirmedBookingManager.getConfirmedBookings()) {
                    total += res.getReservation().getPrice();
                }
                String totalPrice = NumberFormat.getCurrencyInstance(Locale.US).format(total);
                Intent i = new Intent(getActivity(), CheckoutActivity.class);
                i.putExtra("amount", total);
                startActivity(i);
            }
        });


        return view;
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.dashboard_menu, menu); // ✅ Inflate your menu file
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_cart) {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new CartFragment());
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayCurrentReservations() {
        currentReservationsContainer.removeAllViews();

        List<ConfirmedBookingManager.ConfirmedReservation> confirmed = ConfirmedBookingManager.getConfirmedBookings();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        if (confirmed.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No current reservations.");
            currentReservationsContainer.addView(empty);
            tvTotalPrice.setText("Total: $0");
            return;
        }

        double total = 0;

        for (ConfirmedBookingManager.ConfirmedReservation res : confirmed) {
            View card = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_dashboard_reservation, currentReservationsContainer, false);

            TextView tvDetails = card.findViewById(R.id.tvReservationDetails);
            TextView tvPrice = card.findViewById(R.id.tvReservationPrice);
            Button btnPay = card.findViewById(R.id.btnPaymentConfirmed);
            Button btnRemove = card.findViewById(R.id.btnRemoveConfirmed);

            String info = "Room: " + res.getReservation().getRoomType() + "\n"
                    + "Smoking: " + res.getReservation().getSmokingPreference() + "\n"
                    + "Guests: " + res.getReservation().getGuestCount() + "\n"
                    + "Check-in: " + sdf.format(res.getReservation().getCheckInDate()) + "\n"
                    + "Check-out: " + sdf.format(res.getReservation().getCheckOutDate());

            tvDetails.setText(info);
            tvPrice.setText("Price: " + currencyFormat.format(res.getReservation().getPrice()));

            total += res.getReservation().getPrice();

            btnPay.setOnClickListener(v -> {
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

                if (mUser != null) {
                    Intent i = new Intent(requireContext(), com.example.homepage.Payment.CheckoutActivity.class);
                   //i.putExtra("amount", (double)(res.getPrice() * 100));
                    startActivity(i);
                } else {
                    Intent intent = new Intent(requireContext(), Login.class);
                    intent.putExtra("from_checkout", true);
                    startActivity(intent);
                }
            });

            btnRemove.setOnClickListener(v -> {
                ConfirmedBookingManager.removeReservation(res.getReservation());
                Toast.makeText(getContext(), "Reservation removed", Toast.LENGTH_SHORT).show();
                displayCurrentReservations();
            });

            currentReservationsContainer.addView(card);
        }

        // Final total display
        tvTotalPrice.setText("Total: " + currencyFormat.format(total));
    }
}
