package com.example.betterbagwork;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CombinationsActivity extends AppCompatActivity {

    private Button btnCreateCombination, btnBack;
    private RecyclerView recyclerViewCombinations;
    private CombinationAdapter combinationAdapter;
    private CombinationManager combinationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combinations);

        // Manager initialisieren
        combinationManager = new CombinationManager();

        // Views initialisieren
        btnCreateCombination = findViewById(R.id.btnCreateCombination);
        btnBack = findViewById(R.id.btnBack);
        recyclerViewCombinations = findViewById(R.id.recyclerViewCombinations);

        // RecyclerView Setup
        setupRecyclerView();

        // Button: Neue Kombination erstellen
        btnCreateCombination.setOnClickListener(v -> {
            Intent intent = new Intent(CombinationsActivity.this, CreateCombinationActivity.class);
            startActivity(intent);
        });

        // Button: Zurück zur MainActivity
        btnBack.setOnClickListener(v -> finish());
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
                // Später: Details anzeigen oder direkt für Workout verwenden
                Toast.makeText(CombinationsActivity.this,
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
                    Toast.makeText(CombinationsActivity.this,
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