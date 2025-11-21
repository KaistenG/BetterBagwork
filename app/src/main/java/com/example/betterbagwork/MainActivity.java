package com.example.betterbagwork;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button btnCombinations, btnWorkouts, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Firebase initialisieren
        FirebaseApp.initializeApp(this);

        // Views initialisieren
        btnCombinations = findViewById(R.id.btnCombinations);
        btnWorkouts = findViewById(R.id.btnWorkouts);
        btnLogout = findViewById(R.id.btnLogout);

        // Button: Zu Kombinationen
        btnCombinations.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CombinationsActivity.class);
            startActivity(intent);
        });

        // Button: Zu Workouts
        btnWorkouts.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WorkoutsActivity.class);
            startActivity(intent);
        });

        // Logout-Button
        btnLogout.setOnClickListener(v -> FirebaseHelper.logout(MainActivity.this));

        // Prüfen, ob ein Nutzer eingeloggt ist
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

        // Layout-Handling EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}