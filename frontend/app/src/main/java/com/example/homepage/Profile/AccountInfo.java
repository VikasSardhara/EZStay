package com.example.homepage.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.homepage.R;
import com.example.homepage.REGISTERLOGIN.Login;
import com.example.homepage.USER.User;
import com.example.homepage.USER.UserInfoFetcher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountInfo extends AppCompatActivity {

    private User user;
    private UserInfoFetcher userFetcher;
    private FirebaseAuth auth;
    private FirebaseUser mUser;
    private Button logoutBtn, loginBtn;
    ImageButton backBtn;
    private LinearLayout reservations, history;
    private TextView notLoggedInMessage, fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_info);

        fullName = findViewById(R.id.full_name);
        logoutBtn= findViewById(R.id.logout_button);
        reservations = findViewById(R.id.lin_layout_reservations);
        history = findViewById(R.id.lin_layout_history);
        loginBtn = findViewById(R.id.login_button);
        notLoggedInMessage = findViewById(R.id.not_logged_in_message);

        Group accountInfoGroup = findViewById(R.id.account_info_group);
        Group guestInfoGroup = findViewById(R.id.guest_info_group);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        mUser = auth.getCurrentUser();


        if (mUser != null) {
            accountInfoGroup.setVisibility(View.VISIBLE);
            guestInfoGroup.setVisibility(View.GONE);
            user = User.getInstance();
            fullName.setText(user.getName());
        } else {
            accountInfoGroup.setVisibility(View.GONE);
            guestInfoGroup.setVisibility(View.VISIBLE);
        }



        getSupportFragmentManager().addOnBackStackChangedListener(new androidx.fragment.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Group accountInfoGroup = findViewById(R.id.account_info_group);
                View fragmentContainer = findViewById(R.id.fragment_container);


                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    // No fragments on backstack, show account info
                    accountInfoGroup.setVisibility(View.VISIBLE);
                    fragmentContainer.setVisibility(View.GONE);
                    fullName.setVisibility(View.VISIBLE);
                } else {
                    accountInfoGroup.setVisibility(View.GONE);
                    fragmentContainer.setVisibility(View.VISIBLE);
                }
            }
        });


        reservations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullName.setVisibility(View.GONE);

                ReservationsFragment reservationsFragment = new ReservationsFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                // Replace the current content (which is the AccountInfoActivity layout) with ReservationsFragment
                transaction.replace(R.id.fragment_container, reservationsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullName.setVisibility(View.GONE);

                HistoryFragment historyFragment = new HistoryFragment();

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                // Replace the current content (which is the AccountInfoActivity layout) with ReservationsFragment
                transaction.replace(R.id.fragment_container, historyFragment);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.clear();
                FirebaseAuth.getInstance().signOut();
                accountInfoGroup.setVisibility(View.GONE);
                fullName.setVisibility(View.GONE);
                notLoggedInMessage.setVisibility(View.VISIBLE);
                loginBtn.setVisibility(View.VISIBLE);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AccountInfo.this, Login.class);
                startActivity(i);
                finish();
            }
        });

        backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}

