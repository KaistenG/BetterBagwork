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
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Button btnLogout; // Button-Variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Firebase initialisieren
        FirebaseApp.initializeApp(this);

        // Logout-Button initialisieren
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> FirebaseHelper.logout(MainActivity.this));

        // Prüfen, ob ein Nutzer eingeloggt ist
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Nutzer existiert lokal -> prüfen, ob er noch auf Firebase existiert
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Alles gut, Nutzer existiert -> Firestore & Auth Instanzen abrufen
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Toast.makeText(MainActivity.this, "Firebase erfolgreich verbunden!", Toast.LENGTH_SHORT).show();
                } else {
                    // Nutzer existiert nicht mehr -> zurück zum Login
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            });
        } else {
            // Kein Nutzer eingeloggt -> LoginActivity starten
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
