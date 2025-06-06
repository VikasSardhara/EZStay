package com.example.homepage.Profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.homepage.BOOKING.BookingsFetcher;
import com.example.homepage.R;
import com.example.homepage.ROOM.Room;
import com.example.homepage.ROOM.RoomSearch;
import com.example.homepage.USER.User;
import com.example.homepage.utils.ReservationManager;

import java.util.ArrayList;

public class ReservationsFragment extends Fragment {

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
        Log.d("userid", "hi" + user.getUserID());

        // Clear previous reservations and fetch new ones
        ReservationManager.getCurrentReservations().clear();
        BookingsFetcher.getBookings(user.getUserID(), false, new BookingsFetcher.BookingsListener() {
            @Override
            public void onBookingsReceived(ArrayList<ReservationManager.Reservation> bookings) {
                if (bookings.isEmpty()) {
                    Log.d("ReservationsFragment", "No current reservations to display.");
                    TextView noData = new TextView(getContext());
                    noData.setText("No future reservations.");
                    previousReservationsContainer.addView(noData);
                } else {
                    Log.d("ReservationsFragment", "Displaying " + bookings.size() + " current reservations.");
                    for (ReservationManager.Reservation res : bookings) {
                        Log.d("ReservationsFragment", "Fetching room info for room ID: " + res.roomID);
                        RoomSearch.getRoomById(res.roomID, new RoomSearch.RoomSearchCallback() {
                            @Override
                            public void onRoomFound(Room room) {
                                if (room == null) {
                                    Log.e("ROOMFOUND", "Room not found for room ID: " + res.roomID);
                                    return;
                                }
                                Log.d("ROOMFOUND", "Room found: " + room.getType() + ", Size: " + room.getSize());
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        TextView bookingText = new TextView(getContext());
                                        LinearLayout reservationLayout = new LinearLayout(getContext());
                                        reservationLayout.setOrientation(LinearLayout.HORIZONTAL);
                                        ImageView roomImage = new ImageView(getContext());

                                        // Set room image based on type
                                        if (room.getSize().equals("King")) {
                                            roomImage.setImageResource(R.drawable.king_room);
                                        } else {
                                            roomImage.setImageResource(R.drawable.queen_room);
                                        }

                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(350, 250);
                                        roomImage.setLayoutParams(params);

                                        String displayText = "Booking ID: " + res.bookingID + "\n" +
                                                "Room ID: " + res.roomID + "\n" +
                                                "Room Type: " + room.getType() + "\n" +
                                                "Room Size: " + room.getSize() + "\n" +
                                                "Check-in: " + res.checkIN + "\n" +
                                                "Check-out: " + res.checkOUT + "\n" +
                                                "Guests: " + res.guestCount;

                                        bookingText.setText(displayText);
                                        bookingText.setPadding(0, 0, 0, 100);

                                        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                                        bookingText.setLayoutParams(textParams);

                                        reservationLayout.addView(bookingText);
                                        reservationLayout.addView(roomImage);

                                        previousReservationsContainer.addView(reservationLayout);
                                    }
                                });
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(String message) {
                Log.e("ReservationsFragment", "Booking fetch failed: " + message);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to load reservations.", Toast.LENGTH_SHORT).show();
                });
            }

        });

        return view;
    }
}
