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

import com.example.homepage.BOOKING.BookingsFetcher;
import com.example.homepage.R;
//import com.example.homepage.BOOKING.Booking;
import com.example.homepage.ROOM.Room;
import com.example.homepage.ROOM.RoomSearch;
import com.example.homepage.USER.User;
import com.example.homepage.utils.BookingCart;
import com.example.homepage.utils.ReservationManager;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {


    Button backBtn;
    User user;

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reservations, container, false);
        LinearLayout previousReservationsContainer = view.findViewById(R.id.previousReservationsContainer);

        backBtn = view.findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        user = User.getInstance();
        int userId = user.getUserID(); // Replace this with actual userId logic

        BookingsFetcher.getBookings(1010, false, new BookingsFetcher.BookingsListener() {
            @Override
            public void onBookingsReceived(final ArrayList<ReservationManager.Reservation> bookings) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("HistoryFragment", "Fetched bookings: " + bookings.size() + " bookings received.");

                        if (ReservationManager.getCurrentReservations().isEmpty()) {
                            Log.d("ReservationsFragment", "No current reservations to display.");
                            TextView noData = new TextView(getContext());
                            noData.setText("No future reservations.");
                            previousReservationsContainer.addView(noData);
                        } else {
                            for (ReservationManager.Reservation res : ReservationManager.getPastReservations()) {
                                Log.d("ReservationsFragment", "Fetching room info for room ID: " + res.roomID);

                                RoomSearch.getRoomById(res.roomID, new RoomSearch.RoomSearchCallback() {
                                    @Override
                                    public void onRoomFound(Room room) {
                                        if (room == null) {
                                            Log.e("ROOMFOUND", "Room not found for room ID: " + res.roomID);
                                            return;
                                        }

                                        Log.d("ROOMFOUND", "Room found: " + room.getType() + ", Size: " + room.getSize());

                                        // Ensure UI updates happen on the main thread
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Create and configure TextView to display room details
                                                TextView bookingText = new TextView(getContext());
                                                String displayText = "Booking ID: " + res.bookingID + "\n" +
                                                        "Room ID: " + res.roomID + "\n" +
                                                        "Room Type: " + room.getType() + "\n" +
                                                        "Room Size: " + room.getSize() + "\n" +
                                                        "Check-in: " + res.checkIN + "\n" +
                                                        "Check-out: " + res.checkOUT + "\n" +
                                                        "Guests: " + res.guestCount;

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
            }
        });

        return view;
    }
}