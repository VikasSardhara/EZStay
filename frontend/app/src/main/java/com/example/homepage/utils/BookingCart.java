package com.example.homepage.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BookingCart {

    public static class Reservation {
        private final Date checkInDate;
        private final Date checkOutDate;
        private final String roomType;
        private final String smokingPreference;
        private final int guestCount;
        private final double price;

        private final int roomId;

        public Reservation(Date checkInDate, Date checkOutDate, String roomType, String smokingPreference, int guestCount, int roomId) {
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.roomType = roomType;
            this.smokingPreference = smokingPreference;
            this.guestCount = guestCount;
            this.price = calculatePrice();
            this.roomId = roomId;
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

        public double getPrice() {
            return price;
        }

        public int getRoomId(){return roomId;}

        private double calculatePrice() {
            int nights = getNumberOfNights();
            int baseRate = roomType.equalsIgnoreCase("King") ? 100 : 140;
            return baseRate * nights;
        }

        private int getNumberOfNights() {
            long diffInMillis = checkOutDate.getTime() - checkInDate.getTime();
            return (int) TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        }


    }

    private static final List<Reservation> cartItems = new ArrayList<>();

    public static void addItem(Reservation reservation) {
        cartItems.add(reservation);
    }

    public static void removeItem(Reservation reservation) {
        cartItems.remove(reservation);
    }

    public static List<Reservation> getItems() {
        return new ArrayList<>(cartItems);
    }

    public static void clearCart() {
        cartItems.clear();
    }
}