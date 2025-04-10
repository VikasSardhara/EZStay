package com.example.homepage.home;

import android.app.DatePickerDialog;
import android.os.Bundle;
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

import com.example.homepage.R;
import com.example.homepage.cart.CartFragment;
import com.example.homepage.utils.BookingCart;

import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment {

    private Calendar checkInCalendar = null;
    private Calendar checkOutCalendar = null;

    private TextView tvCheckInDate, tvCheckOutDate, tvGuestCount;
    private Button btnDecreaseGuests, btnIncreaseGuests, btnBookNow;
    private RadioButton rbSmoking, rbKing;
    private RadioGroup rgRoomType;

    private int guestCount = 1;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        Button btnCheckInDate = view.findViewById(R.id.btnCheckInDate);
        Button btnCheckOutDate = view.findViewById(R.id.btnCheckOutDate);
        tvCheckInDate = view.findViewById(R.id.tvCheckInDate);
        tvCheckOutDate = view.findViewById(R.id.tvCheckOutDate);
        tvGuestCount = view.findViewById(R.id.tvGuestCount);
        btnDecreaseGuests = view.findViewById(R.id.btnDecreaseGuests);
        btnIncreaseGuests = view.findViewById(R.id.btnIncreaseGuests);
        btnBookNow = view.findViewById(R.id.btnBookNow);
        rbSmoking = view.findViewById(R.id.rbSmoking);
        rbKing = view.findViewById(R.id.rbKing);
        rgRoomType = view.findViewById(R.id.rgRoomType);

        tvGuestCount.setText(String.valueOf(guestCount));

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

        btnBookNow.setOnClickListener(v -> {
            // Validations
            if (checkInCalendar == null || checkOutCalendar == null) {
                Toast.makeText(getContext(), "Please select check-in and check-out dates.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (checkOutCalendar.before(checkInCalendar)) {
                Toast.makeText(getContext(), "Check-out must be after check-in.", Toast.LENGTH_SHORT).show();
                return;
            }

            long diff = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
            long days = diff / (1000 * 60 * 60 * 24);
            if (days > 30) {
                Toast.makeText(getContext(), "Booking cannot exceed 30 days.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (rgRoomType.getCheckedRadioButtonId() == -1) {
                Toast.makeText(getContext(), "Please select a room type.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (guestCount > 2 && rbKing.isChecked()) {
                Toast.makeText(getContext(), "King room allows max 2 guests.", Toast.LENGTH_SHORT).show();
                return;
            }

            String roomType = rbKing.isChecked() ? "King" : "2 Queens";
            String smokingPref = rbSmoking.isChecked() ? "Smoking" : "Non-Smoking";

            BookingCart.Reservation booking = new BookingCart.Reservation(
                    checkInCalendar.getTime(),
                    checkOutCalendar.getTime(),
                    roomType,
                    smokingPref,
                    guestCount
            );

            BookingCart.addItem(booking);

            Toast.makeText(getContext(), "Booking added to cart.", Toast.LENGTH_SHORT).show();

            // Navigate to Cart
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new CartFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    private void showDatePickerDialog(boolean isCheckIn) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                (view, year, month, day) -> {
                    calendar.set(year, month, day);

                    if (isCheckIn) {
                        checkInCalendar = calendar;
                    } else {
                        checkOutCalendar = calendar;
                    }

                    if (checkInCalendar != null && checkOutCalendar != null) {
                        if (checkOutCalendar.before(checkInCalendar)) {
                            Toast.makeText(getContext(), "Check-out must be after check-in.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        long diff = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
                        long days = diff / (1000 * 60 * 60 * 24);
                        if (days > 30) {
                            Toast.makeText(getContext(), "Max booking is 30 days.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    updateDateText();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        if (!isCheckIn && checkInCalendar != null) {
            dialog.getDatePicker().setMinDate(checkInCalendar.getTimeInMillis() + 24 * 60 * 60 * 1000);
        }

        dialog.show();
    }

    private void updateDateText() {
        if (checkInCalendar != null) {
            Date checkIn = checkInCalendar.getTime();
            tvCheckInDate.setText("Check-in: " + android.text.format.DateFormat.format("MM/dd/yyyy", checkIn));
        }
        if (checkOutCalendar != null) {
            Date checkOut = checkOutCalendar.getTime();
            tvCheckOutDate.setText("Check-out: " + android.text.format.DateFormat.format("MM/dd/yyyy", checkOut));
        }
    }
}
