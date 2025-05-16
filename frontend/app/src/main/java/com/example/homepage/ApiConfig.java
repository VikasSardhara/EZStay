package com.example.homepage;

public class ApiConfig {

    // Replace with your actual IP address or use 10.0.2.2 for emulator
    public static final String BASE_URL = "http://192.168.1.91:5000";

    // Stripe payment server (if different port or server)
    public static final String BASE_PAYMENT_URL = "http://192.168.1.91:4242";


    public static final String USERS_URL = BASE_URL + "/users";

    public static final String LOCK_URL = BASE_URL + "/lock";
    public static final String LOCKROOMID_URL = BASE_URL + "/lock?room_id=?";
    public static final String REGISTER_URL = BASE_URL + "/register";
    public static final String BOOK_URL = BASE_URL + "/book";

    public static final String ROOMSIZE_URL = BASE_URL + "/rooms?size=";

    public static final String ROOMID_URL = BASE_URL + "/rooms?room_id=?";

    public static final String BOOKINGS_URL = BASE_URL + "/bookings";

    public static final String PAYMENTSHEET_URL = BASE_PAYMENT_URL + "/payment-sheet";

}
