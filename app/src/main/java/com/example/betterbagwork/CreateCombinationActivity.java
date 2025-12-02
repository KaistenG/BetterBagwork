package com.example.betterbagwork;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

        // Return Button Handler
        ImageView btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> showExitConfirmDialog());

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

    private void showExitConfirmDialog() {
        if (selectedMoves.isEmpty() && inputCombinationName.getText().toString().trim().isEmpty()) {
            // Nichts eingegeben → direkt zurück
            finish();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Abbrechen?")
                .setMessage("Möchtest du die Erstellung wirklich abbrechen? Alle Eingaben gehen verloren.")
                .setPositiveButton("Ja, abbrechen", (dialog, which) -> finish())
                .setNegativeButton("Weiter bearbeiten", null)
                .show();
    }

    // Schlag-Buttons dynamisch erstellen
    private void createMoveButtons() {
        // Spezifische Anordnung: 2 pro Zeile
        String[][] movesPairs = {
                {"Jab", "Cross"},
                {"Left Hook", "Right Hook"},
                {"Left Uppercut", "Right Uppercut"},
                {"Switch Kick", "Rear Kick"},
                {"Switch Knee", "Rear Knee"},
                {"Left Elbow", "Right Elbow"},
                {"Teep", null}  // Teep alleine
        };

        for (String[] pair : movesPairs) {
            // Horizontales Layout für 2 Buttons
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Erster Button
            com.google.android.material.button.MaterialButton btn1 = createMoveButton(pair[0]);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );
            params1.setMargins(0, 0, 8, 8);
            btn1.setLayoutParams(params1);
            rowLayout.addView(btn1);

            // Zweiter Button (falls vorhanden)
            if (pair[1] != null) {
                com.google.android.material.button.MaterialButton btn2 = createMoveButton(pair[1]);
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
                params2.setMargins(8, 0, 0, 8);
                btn2.setLayoutParams(params2);
                rowLayout.addView(btn2);
            }

            moveButtonsLayout.addView(rowLayout);
        }
    }

    // Helper-Methode zum Erstellen eines Buttons
    private com.google.android.material.button.MaterialButton createMoveButton(String move) {
        com.google.android.material.button.MaterialButton btn = new com.google.android.material.button.MaterialButton(this);
        btn.setText(move);
        btn.setTextColor(getResources().getColor(R.color.text_primary, null));  // Weiß
        btn.setBackgroundColor(getResources().getColor(R.color.surface_dark, null));  // Grau (#2E2E2E)
        btn.setCornerRadius(8);
        btn.setOnClickListener(v -> addMove(move));
        return btn;
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