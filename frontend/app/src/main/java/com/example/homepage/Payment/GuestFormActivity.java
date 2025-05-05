package com.example.homepage.Payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.homepage.USER.DOBPicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homepage.R;

public class GuestFormActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etBirthDate;
    private Button btnSubmit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_form);

        etFirstName = findViewById(R.id.first_name);
        etLastName = findViewById(R.id.last_name);
        etEmail = findViewById(R.id.register_email);
        etBirthDate = findViewById(R.id.dob_input);
        btnSubmit = findViewById(R.id.next_button);

        etBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DOBPicker.showDatePicker(GuestFormActivity.this, new DOBPicker.DOBSelectedCallback() {
                    @Override
                    public void onValidDOBSelected(String formattedDate) {
                        etBirthDate.setText(formattedDate);
                    }
                });
            }
        });

        // Handle form submission
        btnSubmit.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String birthDate = etBirthDate.getText().toString().trim();

            // Check if all fields are filled
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || birthDate.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Passing the guest form data to the next activity (CheckoutActivity)
            Intent i = new Intent(getApplicationContext(), CheckoutActivity.class);
            i.putExtra("first_name", firstName);
            i.putExtra("last_name", lastName);
            i.putExtra("email", email);
            startActivity(i);
            finish();

            Toast.makeText(this, "Guest form submitted. Welcome!", Toast.LENGTH_SHORT).show();
        });
    }
}