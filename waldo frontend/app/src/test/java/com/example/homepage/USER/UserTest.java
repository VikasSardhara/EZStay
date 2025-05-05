package com.example.homepage.USER;

import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void testNameCapitalization_validInput() {
        User.setUserData("Dylan", "Hang", "01/01/9999", "dylan@example.com", 444);
        User user = User.getInstance();
        assertEquals("Dylan", user.getFirstName());
        assertEquals("Hang", user.getLastName());
    }

    @Test
    public void testNameCapitalization_uppercaseInput() {
        User.setUserData("DYLAN", "HANG", "01/01/9999", "dylan@example.com", 444);
        User user = User.getInstance();
        assertEquals("Dylan", user.getFirstName());
        assertEquals("Hang", user.getLastName());
    }

    @Test
    public void testNameCapitalization_lowercaseInput() {
        User.setUserData("waldo", "aldo", "01/01/9999", "waldo@example.com", 444);
        User user = User.getInstance();
        assertEquals("Waldo", user.getFirstName());
        assertEquals("Aldo", user.getLastName());
    }

    @Test
    public void testNameCapitalization_mixedCaseInput() {
        User.setUserData("vEd", "StARks", "01/01/9999", "ved@example.com", 444);
        User user = User.getInstance();
        assertEquals("Ved", user.getFirstName());
        assertEquals("Starks", user.getLastName());
    }

    @Test
    public void testNameCapitalization_emptyName() {
        User.setUserData("", "", "01/01/9999", "null@example.com", 444);
        User user = User.getInstance();
        assertEquals("", user.getFirstName());
        assertEquals("", user.getLastName());
    }
}
