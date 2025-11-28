package com.example.betterbagwork;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class CreateCombinationActivity extends AppCompatActivity {

    private EditText inputCombinationName;
    private LinearLayout moveButtonsLayout;
    private TextView txtSelectedMoves;
    private Button btnSaveCombination, btnClearMoves;

    private List<String> selectedMoves;
    private CombinationManager combinationManager;

    // Verfügbare Schläge
    private final String[] availableMoves = {
            "Jab",
            "Cross",
            "Left Hook",
            "Right Hook",
            "Left Uppercut",
            "Right Uppercut",
            "Teep",
            "Rear Kick",
            "Switch Kick",
            "Rear Knee",
            "Switch Knee",
            "Left Elbow",
            "Right Elbow"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_combination);

        // Toolbar mit Zurück-Button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Neue Kombination");
        }

        // Initialisierung
        combinationManager = new CombinationManager();
        selectedMoves = new ArrayList<>();

        // Views finden
        inputCombinationName = findViewById(R.id.inputCombinationName);
        moveButtonsLayout = findViewById(R.id.moveButtonsLayout);
        txtSelectedMoves = findViewById(R.id.txtSelectedMoves);
        btnSaveCombination = findViewById(R.id.btnSaveCombination);
        btnClearMoves = findViewById(R.id.btnClearMoves);

        // Schlag-Buttons dynamisch erstellen
        createMoveButtons();

        // Clear Button
        btnClearMoves.setOnClickListener(v -> clearSelectedMoves());

        // Speichern Button
        btnSaveCombination.setOnClickListener(v -> saveCombination());

        // Initial-Anzeige
        updateSelectedMovesDisplay();

        // Back-Button Handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmDialog();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showExitConfirmDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExitConfirmDialog() {
        if (selectedMoves.isEmpty() && inputCombinationName.getText().toString().trim().isEmpty()) {
            // Nichts eingegeben → direkt zurück
            finish();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Abbrechen?")
                .setMessage("Möchtest du die Erstellung wirklich abbrechen? Alle Eingaben gehen verloren.")
                .setPositiveButton("Ja, abbrechen", (dialog, which) -> finish())
                .setNegativeButton("Weiter bearbeiten", null)
                .show();
    }

    // Schlag-Buttons dynamisch erstellen
    private void createMoveButtons() {
        for (String move : availableMoves) {
            Button btn = new Button(this);
            btn.setText(move);
            btn.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            btn.setOnClickListener(v -> addMove(move));
            moveButtonsLayout.addView(btn);
        }
    }

    // Schlag zur Kombination hinzufügen
    private void addMove(String move) {
        selectedMoves.add(move);
        updateSelectedMovesDisplay();
    }

    // Alle Schläge löschen
    private void clearSelectedMoves() {
        selectedMoves.clear();
        updateSelectedMovesDisplay();
    }

    // Anzeige aktualisieren
    private void updateSelectedMovesDisplay() {
        if (selectedMoves.isEmpty()) {
            txtSelectedMoves.setText("Noch keine Schläge ausgewählt");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < selectedMoves.size(); i++) {
                sb.append((i + 1)).append(". ").append(selectedMoves.get(i)).append("\n");
            }
            txtSelectedMoves.setText(sb.toString());
        }
    }

    // Kombination speichern
    private void saveCombination() {
        String name = inputCombinationName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Bitte einen Namen eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedMoves.isEmpty()) {
            Toast.makeText(this, "Bitte mindestens einen Schlag hinzufügen", Toast.LENGTH_SHORT).show();
            return;
        }

        Combination combination = new Combination(null, name, new ArrayList<>(selectedMoves));

        combinationManager.saveCombination(this, combination,
                new CombinationManager.OnCombinationSavedListener() {
                    @Override
                    public void onSuccess(Combination savedCombination) {
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        // Fehler wird bereits im Manager angezeigt
                    }
                });
    }
}