package com.example.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.homepage.R;
import com.example.homepage.notifications.ReservationsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountInfo extends AppCompatActivity {

    private User user; // Declare the user object
    private FirebaseAuth auth;
    private FirebaseUser mUser;
    private Button logoutBtn, loginBtn;
    private LinearLayout reservations, history, editProfile;
    private TextView notLoggedInMessage, fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_info);

        fullName = findViewById(R.id.full_name);
        logoutBtn= findViewById(R.id.logout_button);
        reservations = findViewById(R.id.lin_layout_reservations);

        loginBtn = findViewById(R.id.login_button);
        notLoggedInMessage = findViewById(R.id.not_logged_in_message);
        Group accountInfoGroup = findViewById(R.id.account_info_group);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        mUser = auth.getCurrentUser();
        Log.d("AccountInfo", "mUser: " + mUser); // Add this log
        Log.d("AccountInfo", "User email: " + mUser.getEmail());  // Log email for debugging


        if (mUser != null) {
            // User is signed in
            accountInfoGroup.setVisibility(View.VISIBLE);
            notLoggedInMessage.setVisibility(View.GONE);
            loginBtn.setVisibility(View.GONE);

            user = new User("", "", "", mUser.getEmail());
            user.getUserInfo(mUser.getEmail(), new User.UserInfoCallback() {
                @Override
                public void onUserInfoReceived(User newUser) {
                    user = newUser;
                }
            });
        } else {
            // User is not signed in
            accountInfoGroup.setVisibility(View.GONE);
            notLoggedInMessage.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.VISIBLE);
            // Hide all other sections like reservations, history, and edit profile
            logoutBtn.setVisibility(View.GONE);            // Hide logout button
            reservations.setVisibility(View.GONE);         // Hide reservations button
            history.setVisibility(View.GONE);              // Hide history button
            editProfile.setVisibility(View.GONE);          // Hide edit profile button
        }

        fullName.setText(user.getName());

        reservations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new instance of ReservationsFragment
                ReservationsFragment reservationsFragment = new ReservationsFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", user);
                reservationsFragment.setArguments(bundle);

                // Begin a FragmentTransaction to replace the current fragment with ReservationsFragment
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, reservationsFragment); // Replace with ReservationsFragment
                transaction.addToBackStack(null); // Optionally add this transaction to the back stack so the user can press back
                transaction.commit(); // Commit the transaction
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                accountInfoGroup.setVisibility(View.GONE);
                notLoggedInMessage.setVisibility(View.VISIBLE);
                loginBtn.setVisibility(View.VISIBLE);
            }
        });
    }
}

