package com.example.test;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register2 extends AppCompatActivity {

    TextView first_name_display;
    TextInputEditText editTextPassword;
    Button registerButton;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register2);

        Intent i = getIntent();

        mAuth = FirebaseAuth.getInstance();
        first_name_display = findViewById(R.id.first_name_display);
        editTextPassword = findViewById(R.id.register_password);
        registerButton = findViewById(R.id.register_button);
        progressBar = findViewById(R.id.progressBar);

        String firstNameText = i.getStringExtra("first_name");
        String lastNameText = i.getStringExtra("last_name");
        String email = i.getStringExtra("email");
        String dob = i.getStringExtra("dob");
        first_name_display.setText("Almost finished, " + firstNameText);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String password = String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register2.this, "Please enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    progressBar.setVisibility(View.GONE);
                                    Log.d(TAG, "createUserWithEmail:success");
                                    Toast.makeText(Register2.this, "Successfully Created ", Toast.LENGTH_SHORT).show();
                                    userSendData sendData = new userSendData(firstNameText, lastNameText, dob, email);
                                    Thread thread = new Thread(sendData);
                                    thread.start();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    progressBar.setVisibility(View.GONE);
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(Register2.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }




        });
    }
}