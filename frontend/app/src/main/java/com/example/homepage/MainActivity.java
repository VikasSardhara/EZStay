package com.example.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.homepage.Profile.AccountInfo;
import com.example.homepage.dashboard.DashboardFragment;
import com.example.homepage.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("go_to_dashboard", false)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Load HomeFragment by default when app starts
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                // Use if-else instead of switch
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_dashboard) {
                    selectedFragment = new DashboardFragment();
                } else if (itemId == R.id.nav_account) {
                    Intent intent = new Intent(MainActivity.this, AccountInfo.class);
                    startActivity(intent);
                    return false;
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }

                return true;
            }
        });


    }
}
