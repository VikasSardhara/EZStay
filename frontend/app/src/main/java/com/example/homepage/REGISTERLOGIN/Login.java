package com.example.homepage.REGISTERLOGIN;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homepage.BOOKING.BookingsFetcher;
import com.example.homepage.MainActivity;
import com.example.homepage.Payment.GuestFormActivity;
import com.example.homepage.R;
import com.example.homepage.USER.User;
import com.example.homepage.USER.UserInfoFetcher;
import com.example.homepage.utils.ReservationManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.ArrayList;

public class Login extends AppCompatActivity {

    TextInputEditText editTextPassword;
    TextInputEditText editTextEmail;
    Button loginButton;
    Button registerButton;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    TextView continueGuest;
    FirebaseUser mUser;
    FirebaseAuth auth;
    TextView loginDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);


        loginDisplay = findViewById(R.id.login_display);
        loginButton = findViewById(R.id.login_button);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progress_bar);
        continueGuest = findViewById(R.id.continue_guest);
        registerButton = findViewById(R.id.register_button);

        auth = FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = auth.getCurrentUser();

        Intent intent = getIntent();
        boolean fromCheckout = intent.getBooleanExtra("from_checkout", false);
        Log.d("Fromcheckout", "val" + fromCheckout);

        if (fromCheckout) {
            loginDisplay.setText("You're almost There!");
        }



        if (mUser != null) {
            UserInfoFetcher.getUserInfo(mUser.getEmail(), new UserInfoFetcher.UserIDCallback() {
                @Override
                public void onUserIdReceived(int userID) {
                    BookingsFetcher.getBookings(userID);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = String.valueOf(editTextEmail.getText());
                String password = String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Login.this, "Successfully signed in!.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Register.class));
                finish();
            }
        });

        continueGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromCheckout) {
                    startActivity(new Intent(getApplicationContext(), GuestFormActivity.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
                finish();
            }
        });
    }
}
