<<<<<<< HEAD
package com.example.homepage.USER;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;

public class DOBPicker {

    public interface DOBSelectedCallback {
        void onValidDOBSelected(String formattedDate);
    }

    public static void showDatePicker(Activity activity, final DOBSelectedCallback callback) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {

                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(selectedYear, selectedMonth, selectedDay);

                Calendar current_time = Calendar.getInstance();
                if (selectedDate.after(current_time)) {
                    Toast.makeText(activity, "The date cannot be in the future", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (is18OrOlder(selectedDate)) {
                    Toast.makeText(activity, "You must be at least 18 years old", Toast.LENGTH_SHORT).show();
                    return;
                }

                String formattedDate = String.format("%02d/%02d/%4d", selectedMonth + 1, selectedDay, selectedYear);
                callback.onValidDOBSelected(formattedDate);
            }
        }, year, month, day);

        // Set the maximum date to today to prevent selecting a future date
        datePicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePicker.show();
    }

    private static boolean is18OrOlder(Calendar dob) {
        Calendar today = Calendar.getInstance();
        Calendar legalAge = (Calendar) dob.clone();
        legalAge.add(Calendar.YEAR, 18);
        return today.before(legalAge);
    }
}
=======
package com.example.homepage.USER;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;

public class DOBPicker {

    public interface DOBSelectedCallback {
        void onValidDOBSelected(String formattedDate);
    }

    public static void showDatePicker(Activity activity, final DOBSelectedCallback callback) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {

                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(selectedYear, selectedMonth, selectedDay);

                Calendar current_time = Calendar.getInstance();
                if (selectedDate.after(current_time)) {
                    Toast.makeText(activity, "The date cannot be in the future", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (is18OrOlder(selectedDate)) {
                    Toast.makeText(activity, "You must be at least 18 years old", Toast.LENGTH_SHORT).show();
                    return;
                }

                String formattedDate = String.format("%02d/%02d/%4d", selectedMonth + 1, selectedDay, selectedYear);
                callback.onValidDOBSelected(formattedDate);
            }
        }, year, month, day);

        // Set the maximum date to today to prevent selecting a future date
        datePicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePicker.show();
    }

    private static boolean is18OrOlder(Calendar dob) {
        Calendar today = Calendar.getInstance();
        Calendar legalAge = (Calendar) dob.clone();
        legalAge.add(Calendar.YEAR, 18);
        return today.before(legalAge);
    }
}
>>>>>>> 54e63762880cba51b179e7b9d6c14d38264b3d60
