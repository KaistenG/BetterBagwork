package com.example.betterbagwork;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
            "Right Uppercut"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_combination);

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

    // Anzeige aktualisieren
    private void updateSelectedMovesDisplay() {
        if (selectedMoves.isEmpty()) {
            txtSelectedMoves.setText("Keine Schläge ausgewählt");
        } else {
            String movesText = String.join(" → ", selectedMoves);
            txtSelectedMoves.setText(movesText);
        }
    }

    // Ausgewählte Schläge löschen
    private void clearSelectedMoves() {
        selectedMoves.clear();
        updateSelectedMovesDisplay();
        Toast.makeText(this, "Schläge gelöscht", Toast.LENGTH_SHORT).show();
    }

    // Kombination speichern
    private void saveCombination() {
        String name = inputCombinationName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Bitte Namen eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedMoves.isEmpty()) {
            Toast.makeText(this, "Bitte mindestens einen Schlag hinzufügen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kombination erstellen
        Combination combination = new Combination(null, name, new ArrayList<>(selectedMoves));

        // In Firebase speichern
        combinationManager.saveCombination(this, combination, new CombinationManager.OnCombinationSavedListener() {
            @Override
            public void onSuccess(Combination savedCombination) {
                // Zurück zur MainActivity
                finish();
            }

            @Override
            public void onError(String error) {
                // Fehler wird bereits im Manager als Toast angezeigt
            }
        });
    }
}