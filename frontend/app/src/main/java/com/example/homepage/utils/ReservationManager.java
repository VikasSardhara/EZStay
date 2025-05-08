package com.example.homepage.utils;

import java.util.ArrayList;
import java.util.List;

public class ReservationManager {

    public static class Reservation {
        public String date;
        public String roomType;
        public String smokingPreference;
        public int guestCount;
        public int bookingID;
        public String checkIN;
        public String checkOUT;
        public int roomID;


        public Reservation(String date, String roomType, String smokingPreference, int guestCount, int bookingId, String checkIN, String checkOUT) {
            this.date = date;
            this.roomType = roomType;
            this.smokingPreference = smokingPreference;
            this.guestCount = guestCount;
            this.bookingID = bookingId;
            this.checkIN = checkIN;
            this.checkOUT = checkOUT;

        }

        //constructor for fetched bookings
        public Reservation(int bookingID, int roomID, String checkIN, String checkOUT, int guestCount) {
            this.bookingID = bookingID;
            this.roomID = roomID;
            this.checkIN = checkIN;
            this.checkOUT = checkOUT;
            this.guestCount = guestCount;
        }

        public String toString() {
            return "Reservation{" + "bookingID=" + bookingID + ", roomID=" + roomID + ", checkIN='" + checkIN + '\'' + ", checkOUT='" + checkOUT + '\'' + ", guestCount=" + guestCount + '}';
        }

    }


    private static final List<Reservation> cart = new ArrayList<>();
    private static final List<Reservation> currentReservations = new ArrayList<>();
    private static final List<Reservation> pastReservations = new ArrayList<>();

    public static void addToCart(Reservation reservation) {
        cart.add(reservation);
    }

    public static List<Reservation> getCart() {
        return cart;
    }

    public static void confirmCartItems() {
        currentReservations.addAll(cart);
        cart.clear(); // Empty cart after confirmation
    }

    public static List<Reservation> getCurrentReservations() {
        return currentReservations;
    }

    public static List<Reservation> getPastReservations() {
        return pastReservations;
    }

    public static void removeFromCart(Reservation reservation) {
        cart.remove(reservation);
    }
}