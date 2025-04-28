package com.example.homepage.Payment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homepage.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class GuestFormActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etBirthDate;
    private Button btnSubmit;
    private Calendar selectedDOB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_form);

        etFirstName = findViewById(R.id.firstName);
        etLastName = findViewById(R.id.lastName);
        etEmail = findViewById(R.id.email);
        etBirthDate = findViewById(R.id.birthDate);
        btnSubmit = findViewById(R.id.submitGuestForm);

        selectedDOB = Calendar.getInstance();

        etBirthDate.setOnClickListener(v -> showDatePickerDialog());

        btnSubmit.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String birthDate = etBirthDate.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || birthDate.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!is18OrOlder(selectedDOB)) {
                Toast.makeText(this, "You must be at least 18 years old to book.", Toast.LENGTH_LONG).show();
                return;
            }
            Intent i = new Intent(getApplicationContext(), CheckoutActivity.class);
            i.putExtra("first_name", firstName);
            i.putExtra("last_name", lastName);
            i.putExtra("email", email);
            startActivity(i);
            finish();

            Toast.makeText(this, "Guest form submitted. Welcome!", Toast.LENGTH_SHORT).show();
            // You can now navigate to the Home screen or store this guest data for further use.
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDOB.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    etBirthDate.setText(sdf.format(selectedDOB.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private boolean is18OrOlder(Calendar dob) {
        Calendar today = Calendar.getInstance();
        Calendar legalAge = (Calendar) dob.clone();
        legalAge.add(Calendar.YEAR, 18);
        return !today.before(legalAge); // returns true if today is after or equal to 18th birthday
    }


}
