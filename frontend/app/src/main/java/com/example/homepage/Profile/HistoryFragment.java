package com.example.homepage.Profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.homepage.R;
import com.example.homepage.BOOKING.Booking;
import com.example.homepage.ROOM.Room;
import com.example.homepage.ROOM.RoomSearch;
import com.example.homepage.USER.User;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {


    Button backBtn;
    User user;

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);
        LinearLayout previousReservationsContainer = view.findViewById(R.id.previousReservationsContainer);

        backBtn = view.findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        Bundle bundle = getArguments();
        user = bundle.getParcelable("user_data");

        //remind to use REAL USER ID

        Booking.getBookings(1001, false, new Booking.BookingsListener() {
            @Override
            public void onBookingsReceived(ArrayList<Booking> expiredBookingsList) {
                if (expiredBookingsList.isEmpty()) {
                    TextView noData = new TextView(getContext());
                    noData.setText("No previous reservations.");
                    previousReservationsContainer.addView(noData);
                }

                if (expiredBookingsList.isEmpty()) {
                    TextView noData = new TextView(getContext());
                    noData.setText("No previous reservations.");
                    previousReservationsContainer.addView(noData);
                } else {
                    for (Booking expiredBooking : expiredBookingsList) {
                        Log.d("tetstt", "hi" + expiredBookingsList);
                        Log.d("tetstt", "hi" + expiredBooking.getRoomId());

                        // Get room details for each booking
                        RoomSearch.RoomSearch(getContext(), "rooms copy.csv", expiredBooking.getRoomId(), new RoomSearch.RoomSearchCallback() {
                            @Override
                            public void onRoomFound(Room room) {
                                // Ensure UI updates happen on the main thread
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        TextView bookingText = new TextView(getContext());
                                        Log.d("ROOMFOUND", "onRoomFound: " + room.getType());
                                        String displayText = "Booking ID: " + expiredBooking.getBookingId() + "\n" +
                                                "Room ID: " + expiredBooking.getRoomId() + "\n" +
                                                "Room Type: " + room.getType() + "\n" +
                                                "Room Size: " + room.getSize() + "\n" +
                                                "Check-in: " + expiredBooking.getCheckInDate() + "\n" +
                                                "Check-out: " + expiredBooking.getCheckOutDate() + "\n" +
                                                "Guests: " + expiredBooking.getNumGuests();
                                        bookingText.setText(displayText);
                                        bookingText.setPadding(0, 0, 0, 24);
                                        previousReservationsContainer.addView(bookingText);
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });

        return view;
    }
}


