package com.example.homepage.REGISTERLOGIN;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.homepage.ApiConfig;
import com.example.homepage.R;
import com.example.homepage.USER.DOBPicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity {

    TextInputEditText editTextfirstName;
    TextInputEditText editTextlastName;
    TextInputEditText editTextEmail;
    Button nextButton;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    TextView loginButton;
    TextInputEditText editTextDOB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        editTextEmail = findViewById(R.id.register_email);
        editTextfirstName = findViewById(R.id.first_name);
        editTextlastName = findViewById(R.id.last_name);
        editTextDOB = findViewById(R.id.dob_input);
        nextButton = findViewById(R.id.next_button);
        mAuth = FirebaseAuth.getInstance();
        loginButton = findViewById(R.id.continue_login);

        editTextDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DOBPicker.showDatePicker(Register.this, new DOBPicker.DOBSelectedCallback() {
                    @Override
                    public void onValidDOBSelected(String formattedDate) {
                        editTextDOB.setText(formattedDate);
                    }
                });
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), Register2.class);
                String firstNameText = String.valueOf(editTextfirstName.getText());
                String lastNameText = String.valueOf(editTextlastName.getText());
                String email = String.valueOf(editTextEmail.getText());
                String dob = String.valueOf(editTextDOB.getText());

                if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(Register.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(firstNameText) || TextUtils.isEmpty(lastNameText)) {
                    Toast.makeText(Register.this, "Please enter full name", Toast.LENGTH_SHORT).show();
                    return;
                }

                sendUserToBackend(firstNameText + " " + lastNameText, email, dob);

                intent.putExtra("first_name", firstNameText);
                intent.putExtra("last_name", lastNameText);
                intent.putExtra("email", email);
                intent.putExtra("dob", dob);
                startActivity(intent);
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void sendUserToBackend(String fullName, String email, String dob) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", fullName);
            jsonBody.put("email", email);
            jsonBody.put("dob", dob);

            //String registerUrl = "http://192.168.1.117:5000/register";
            //String registerUrl = ApiConfig.BASE_URL + "register";
            String registerUrl = ApiConfig.REGISTER_URL;//

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    registerUrl,
                    jsonBody,
                    response -> Toast.makeText(Register.this, "User registered successfully in backend", Toast.LENGTH_SHORT).show(),
                    error -> Toast.makeText(Register.this, "Failed to save user in backend", Toast.LENGTH_SHORT).show()
            );

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
