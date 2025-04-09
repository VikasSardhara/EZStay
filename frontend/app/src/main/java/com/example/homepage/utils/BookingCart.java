package com.example.homepage.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingCart {

    public static class Reservation {
        private final Date checkInDate;
        private final Date checkOutDate;
        private final String roomType;
        private final String smokingPreference;
        private final int guestCount;

        public Reservation(Date checkInDate, Date checkOutDate, String roomType, String smokingPreference, int guestCount) {
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.roomType = roomType;
            this.smokingPreference = smokingPreference;
            this.guestCount = guestCount;
        }

        public Date getCheckInDate() {
            return checkInDate;
        }

        public Date getCheckOutDate() {
            return checkOutDate;
        }

        public String getRoomType() {
            return roomType;
        }

        public String getSmokingPreference() {
            return smokingPreference;
        }

        public int getGuestCount() {
            return guestCount;
        }
    }

    private static final List<Reservation> cartItems = new ArrayList<>();

    public static void addItem(Reservation reservation) {
        cartItems.add(reservation);
    }

    public static void removeItem(Reservation reservation) {
        cartItems.remove(reservation); // uses object equality
    }

    public static List<Reservation> getItems() {
        return new ArrayList<>(cartItems);
    }

    public static void clearCart() {
        cartItems.clear();
    }
}
