package com.example.homepage.USER;


public class User {

    private static User instance;

    private String firstName;
    private String lastName;
    private String dob;
    private String email;
    private int userID;

    public static User getInstance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }

    public static void setUserData(String firstName, String lastName, String dob, String email, int userID) {
        User user = getInstance();
        user.firstName = capitalize(firstName);
        user.lastName = capitalize(lastName);
        user.dob = dob;
        user.email = email;
        user.userID = userID;
    }

    public static String capitalize(String name){
        return name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();
    }

    public static void clear() {
        instance = null;
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDob() {
        return dob;
    }

    public String getEmail() {
        return email;
    }

    public int getUserID() {
        return userID;
    }

}


