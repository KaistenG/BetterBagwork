package com.example.betterbagwork;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnLogout, btnCreateCombination;
    private RecyclerView recyclerViewCombinations;
    private CombinationAdapter combinationAdapter;
    private CombinationManager combinationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Firebase initialisieren
        FirebaseApp.initializeApp(this);

        // Manager initialisieren
        combinationManager = new CombinationManager();

        // Views initialisieren
        btnLogout = findViewById(R.id.btnLogout);
        btnCreateCombination = findViewById(R.id.btnCreateCombination);
        recyclerViewCombinations = findViewById(R.id.recyclerViewCombinations);

        // RecyclerView Setup
        setupRecyclerView();

        // Logout-Button
        btnLogout.setOnClickListener(v -> FirebaseHelper.logout(MainActivity.this));

        // Button: Neue Kombination erstellen
        btnCreateCombination.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateCombinationActivity.class);
            startActivity(intent);
        });

        // Prüfen, ob ein Nutzer eingeloggt ist
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Toast.makeText(MainActivity.this, "Firebase erfolgreich verbunden!", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        // Kombinationen laden, wenn Activity wieder sichtbar wird
        loadCombinations();
    }

    // RecyclerView einrichten
    private void setupRecyclerView() {
        combinationAdapter = new CombinationAdapter();
        recyclerViewCombinations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCombinations.setAdapter(combinationAdapter);

        // Click Listener
        combinationAdapter.setOnCombinationClickListener(new CombinationAdapter.OnCombinationClickListener() {
            @Override
            public void onCombinationClick(Combination combination) {
                // Später: Details anzeigen oder Workout starten
                Toast.makeText(MainActivity.this,
                        "Kombination: " + combination.getName(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Combination combination) {
                showDeleteConfirmDialog(combination);
            }
        });
    }

    // Kombinationen laden
    private void loadCombinations() {
        combinationManager.loadUserCombinations(this, new CombinationManager.OnCombinationsLoadedListener() {
            @Override
            public void onSuccess(List<Combination> combinations) {
                combinationAdapter.setCombinations(combinations);
                if (combinations.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "Noch keine Kombinationen vorhanden",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                // Fehler wird bereits im Manager angezeigt
            }
        });
    }

    // Lösch-Bestätigung anzeigen
    private void showDeleteConfirmDialog(Combination combination) {
        new AlertDialog.Builder(this)
                .setTitle("Kombination löschen?")
                .setMessage("Möchtest du '" + combination.getName() + "' wirklich löschen?")
                .setPositiveButton("Löschen", (dialog, which) -> deleteCombination(combination))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    // Kombination löschen
    private void deleteCombination(Combination combination) {
        combinationManager.deleteCombination(this, combination.getId(),
                new CombinationManager.OnCombinationDeletedListener() {
                    @Override
                    public void onSuccess() {
                        loadCombinations(); // Liste neu laden
                    }

                    @Override
                    public void onError(String error) {
                        // Fehler wird bereits im Manager angezeigt
                    }
                });
    }
}