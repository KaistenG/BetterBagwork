package com.example.betterbagwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase initialisieren
        FirebaseApp.initializeApp(this);

        // Prüfen, ob Nutzer eingeloggt
        checkUserLogin();

        // Views initialisieren
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Drawer Toggle (Hamburger-Icon)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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
        new AlertDialog.Builder(this)
                .setTitle("Ausloggen?")
                .setMessage("Möchtest du dich wirklich ausloggen?")
                .setPositiveButton("Ja", (dialog, which) -> {
                    FirebaseHelper.logout(MainActivity.this);
                })
                .setNegativeButton("Nein", null)
                .show();
    }

    private void showImpressum() {
        new AlertDialog.Builder(this)
                .setTitle("Impressum")
                .setMessage("Better Bagwork\n\nEntwickelt von: [Dein Name]\n\nVersion: 1.0")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
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