package com.example.betterbagwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase initialisieren
        FirebaseApp.initializeApp(this);

        // Prüfen, ob Nutzer eingeloggt
        checkUserLogin();

        // Views initialisieren
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        btnMenu = findViewById(R.id.btnMenu);

        // Hamburger Menu Click (öffnet Drawer rechts)
        btnMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Drawer Menu Listener
        navigationView.setNavigationItemSelectedListener(this);

        // Bottom Navigation Listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_combinations) {
                selectedFragment = new CombinationsFragment();
            } else if (itemId == R.id.nav_workouts) {
                selectedFragment = new WorkoutsFragment();
            } else if (itemId == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Standard-Fragment laden (Workouts als Haupt-Tab)
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_workouts);
        }
    }

    private void checkUserLogin() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Willkommen zurück!", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            });
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.drawer_logout) {
            showLogoutDialog();
        } else if (itemId == R.id.drawer_impressum) {
            showImpressum();
        } else if (itemId == R.id.drawer_about) {
            showAbout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Ausloggen?")
                .setMessage("Möchtest du dich wirklich ausloggen?")
                .setPositiveButton("Ja", (dialog, which) -> {
                    FirebaseHelper.logout(MainActivity.this);
                })
                .setNegativeButton("Nein", null)
                .show();
    }

    private void showImpressum() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Impressum")
                .setMessage("Better Bagwork\n\nEntwickelt von: [Dein Name]\n\nVersion: 1.0")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAbout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Über Better Bagwork")
                .setMessage("Better Bagwork ist deine App für effektives Bagwork-Training.\n\n" +
                        "Erstelle Kombinationen, plane Workouts und trainiere strukturiert!")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}