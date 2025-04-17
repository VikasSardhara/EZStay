package com.example.homepage.USER;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String firstName;
    private String lastName;
    private String dob;
    private String email;
    private int userID;

    public User() {
        this.firstName = "";
        this.lastName = "";
        this.dob = "";
        this.email = "";
        this.userID = 0;
    }

    public User(String firstName, String lastName, String dob, String email, int userID) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.email = email;
        this.userID = userID;
    }

    protected User(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
        dob = in.readString();
        email = in.readString();
        userID = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(dob);
        dest.writeString(email);
        dest.writeInt(userID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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
