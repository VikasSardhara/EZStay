package com.example.homepage.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConfirmedBookingManager {

    public static class ConfirmedReservation {
        private final BookingCart.Reservation reservation;

        public ConfirmedReservation(BookingCart.Reservation reservation) {
            this.reservation = reservation;
        }

        public BookingCart.Reservation getReservation() {
            return reservation;
        }

        // These methods will delegate to the Reservation object (from BookingCart)
        public Date getCheckInDate() {
            return reservation.getCheckInDate();
        }

        public Date getCheckOutDate() {
            return reservation.getCheckOutDate();
        }

        public String getRoomType() {
            return reservation.getRoomType();
        }

        public String getSmokingPreference() {
            return reservation.getSmokingPreference();
        }

        public int getRoomId() {
            return reservation.getRoomId();
        }

        public double getPrice() {
            return reservation.getPrice();
        }

        public int getGuestCount() {
            return reservation.getGuestCount();
        }
    }


    private static final List<ConfirmedReservation> confirmedBookings = new ArrayList<>();

    // Called when confirming a booking from Cart
    public static void confirmBooking(BookingCart.Reservation reservation) {
        confirmedBookings.add(new ConfirmedReservation(reservation));
    }

    // Used by DashboardFragment to show confirmed reservations
    public static List<ConfirmedReservation> getConfirmedBookings() {
        return new ArrayList<>(confirmedBookings);
    }

    // Used by "Remove" button in Dashboard to cancel a confirmed booking
    public static void removeConfirmedReservation(BookingCart.Reservation reservation) {
        confirmedBookings.removeIf(item -> item.getReservation().equals(reservation));
    }

    // Optional: For testing/debug
    public static void clearConfirmedBookings() {
        confirmedBookings.clear();
    }

    public static void removeReservation(BookingCart.Reservation reservation) {
        confirmedBookings.removeIf(confirmed -> confirmed.getReservation().equals(reservation));
    }
}