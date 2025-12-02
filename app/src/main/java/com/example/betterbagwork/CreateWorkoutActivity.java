package com.example.betterbagwork;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class CreateWorkoutActivity extends AppCompatActivity {

    private EditText inputWorkoutName;
    private RecyclerView recyclerViewCombinations;
    private NumberPicker pickerRoundMinutes, pickerRoundSeconds;
    private NumberPicker pickerNumberOfRounds;
    private NumberPicker pickerAnnouncementInterval;
    private NumberPicker pickerRestMinutes, pickerRestSeconds;
    private NumberPicker pickerStartDelay;
    private Button btnSaveWorkout;

    private CombinationSelectionAdapter selectionAdapter;
    private CombinationManager combinationManager;
    private WorkoutManager workoutManager;
    private List<Combination> allCombinations;

    // Edit Mode
    private boolean isEditMode = false;
    private String editWorkoutId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        // Prüfen ob Edit-Mode
        checkEditMode();

        // Return Button Handler
        ImageView btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> showExitConfirmDialog());

        // Toolbar Titel anpassen
        updateToolbarTitle();

        // Manager initialisieren
        combinationManager = new CombinationManager();
        workoutManager = new WorkoutManager();
        allCombinations = new ArrayList<>();

        // Views initialisieren
        inputWorkoutName = findViewById(R.id.inputWorkoutName);
        recyclerViewCombinations = findViewById(R.id.recyclerViewCombinations);
        pickerRoundMinutes = findViewById(R.id.pickerRoundMinutes);
        pickerRoundSeconds = findViewById(R.id.pickerRoundSeconds);
        pickerNumberOfRounds = findViewById(R.id.pickerNumberOfRounds);
        pickerAnnouncementInterval = findViewById(R.id.pickerAnnouncementInterval);
        pickerRestMinutes = findViewById(R.id.pickerRestMinutes);
        pickerRestSeconds = findViewById(R.id.pickerRestSeconds);
        pickerStartDelay = findViewById(R.id.pickerStartDelay);
        btnSaveWorkout = findViewById(R.id.btnSaveWorkout);

        // NumberPicker konfigurieren
        setupNumberPickers();

        // RecyclerView Setup
        setupRecyclerView();

        // Kombinationen laden
        loadCombinations();

        // Speichern Button
        btnSaveWorkout.setOnClickListener(v -> saveWorkout());

        // Back-Button Handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmDialog();
            }
        });
    }

    private void checkEditMode() {
        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        if (isEditMode) {
            editWorkoutId = getIntent().getStringExtra("WORKOUT_ID");
        }
    }

    private void updateToolbarTitle() {
        android.widget.TextView txtTitle = findViewById(R.id.txtTitle);
        if (isEditMode) {
            txtTitle.setText("Workout bearbeiten");
        } else {
            txtTitle.setText("Neues Workout");
        }
    }

    private void loadEditData() {
        if (!isEditMode) return;

        // Daten aus Intent laden
        String workoutName = getIntent().getStringExtra("WORKOUT_NAME");
        ArrayList<String> combinationIds = getIntent().getStringArrayListExtra("COMBINATION_IDS");
        int roundTimeSeconds = getIntent().getIntExtra("ROUND_TIME_SECONDS", 180);
        int numberOfRounds = getIntent().getIntExtra("NUMBER_OF_ROUNDS", 5);
        int announcementInterval = getIntent().getIntExtra("ANNOUNCEMENT_INTERVAL", 5);
        int restTimeSeconds = getIntent().getIntExtra("REST_TIME_SECONDS", 60);
        int startDelaySeconds = getIntent().getIntExtra("START_DELAY_SECONDS", 10);

        // Daten vorausfüllen
        inputWorkoutName.setText(workoutName);

        // NumberPicker setzen
        pickerRoundMinutes.setValue(roundTimeSeconds / 60);
        pickerRoundSeconds.setValue((roundTimeSeconds % 60) / 5); // Index für 5er-Schritte
        pickerNumberOfRounds.setValue(numberOfRounds);
        pickerAnnouncementInterval.setValue(announcementInterval);
        pickerRestMinutes.setValue(restTimeSeconds / 60);
        pickerRestSeconds.setValue((restTimeSeconds % 60) / 5); // Index für 5er-Schritte
        pickerStartDelay.setValue(startDelaySeconds / 10); // 0, 10, 20, 30 → Index

        // Kombinationen vorauswählen
        if (combinationIds != null) {
            selectionAdapter.setSelectedCombinations(combinationIds);
        }
    }

    private void showExitConfirmDialog() {
        if (inputWorkoutName.getText().toString().trim().isEmpty() &&
                selectionAdapter.getSelectedCombinationIds().isEmpty()) {
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

    private void setupNumberPickers() {
        // Rundenzeit - Minuten (0-10)
        pickerRoundMinutes.setMinValue(0);
        pickerRoundMinutes.setMaxValue(10);
        pickerRoundMinutes.setValue(3); // Default: 3 Minuten

        // Rundenzeit - Sekunden (0, 5, 10, 15, ..., 55) - 5er Schritte
        String[] roundSecondsValues = new String[12]; // 0, 5, 10, ..., 55
        for (int i = 0; i < 12; i++) {
            roundSecondsValues[i] = String.valueOf(i * 5);
        }
        pickerRoundSeconds.setMinValue(0);
        pickerRoundSeconds.setMaxValue(11);
        pickerRoundSeconds.setDisplayedValues(roundSecondsValues);
        pickerRoundSeconds.setValue(0); // Default: 0 Sekunden

        // Anzahl Runden (1-12) - NEU: Maximum auf 12 gesetzt
        pickerNumberOfRounds.setMinValue(1);
        pickerNumberOfRounds.setMaxValue(12);
        pickerNumberOfRounds.setValue(5); // Default: 5 Runden

        // Ansage-Intervall (3-15 Sekunden) - BLEIBT bei 1-Sekunden-Schritten
        pickerAnnouncementInterval.setMinValue(3);
        pickerAnnouncementInterval.setMaxValue(15);
        pickerAnnouncementInterval.setValue(5); // Default: 5 Sekunden

        // Pausenzeit - Minuten (0-5)
        pickerRestMinutes.setMinValue(0);
        pickerRestMinutes.setMaxValue(5);
        pickerRestMinutes.setValue(1); // Default: 1 Minute

        // Pausenzeit - Sekunden (0, 5, 10, 15, ..., 55) - 5er Schritte
        String[] restSecondsValues = new String[12];
        for (int i = 0; i < 12; i++) {
            restSecondsValues[i] = String.valueOf(i * 5);
        }
        pickerRestSeconds.setMinValue(0);
        pickerRestSeconds.setMaxValue(11);
        pickerRestSeconds.setDisplayedValues(restSecondsValues);
        pickerRestSeconds.setValue(0); // Default: 0 Sekunden

        // Startverzögerung (0, 10, 20, 30 Sekunden)
        pickerStartDelay.setMinValue(0);
        pickerStartDelay.setMaxValue(3);
        pickerStartDelay.setDisplayedValues(new String[]{"0s", "10s", "20s", "30s"});
        pickerStartDelay.setValue(1); // Default: 10s Verzögerung
    }

    private void setupRecyclerView() {
        selectionAdapter = new CombinationSelectionAdapter();
        recyclerViewCombinations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCombinations.setAdapter(selectionAdapter);
    }

    private void loadCombinations() {
        combinationManager.loadUserCombinations(this, new CombinationManager.OnCombinationsLoadedListener() {
            @Override
            public void onSuccess(List<Combination> combinations) {
                allCombinations = combinations;
                selectionAdapter.setCombinations(combinations);

                // Edit-Daten laden (nach Kombinationen geladen)
                loadEditData();

                if (combinations.isEmpty()) {
                    Toast.makeText(CreateWorkoutActivity.this,
                            "Erstelle zuerst Kombinationen!",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CreateWorkoutActivity.this,
                        "Fehler beim Laden der Kombinationen",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveWorkout() {
        String name = inputWorkoutName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Bitte einen Namen eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ausgewählte Kombinationen holen
        List<String> selectedIds = selectionAdapter.getSelectedCombinationIds();

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Bitte mindestens eine Kombination auswählen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Werte aus NumberPicker holen
        int roundTimeSeconds = (pickerRoundMinutes.getValue() * 60) + (pickerRoundSeconds.getValue() * 5);
        int numberOfRounds = pickerNumberOfRounds.getValue();
        int announcementInterval = pickerAnnouncementInterval.getValue();
        int restTimeSeconds = (pickerRestMinutes.getValue() * 60) + (pickerRestSeconds.getValue() * 5);
        int startDelaySeconds = pickerStartDelay.getValue() * 10;

        // Validierung
        if (roundTimeSeconds < 30) {
            Toast.makeText(this, "Rundenzeit muss mindestens 30 Sekunden sein", Toast.LENGTH_SHORT).show();
            return;
        }

        // Workout erstellen/aktualisieren
        Workout workout = new Workout(
                isEditMode ? editWorkoutId : null,
                name,
                selectedIds,
                roundTimeSeconds,
                numberOfRounds,
                announcementInterval,
                restTimeSeconds,
                startDelaySeconds
        );

        // Speichern oder Aktualisieren
        if (isEditMode) {
            workoutManager.updateWorkout(this, workout, new WorkoutManager.OnWorkoutSavedListener() {
                @Override
                public void onSuccess(Workout savedWorkout) {
                    finish();
                }

                @Override
                public void onError(String error) {
                    // Fehler wird bereits im Manager angezeigt
                }
            });
        } else {
            workoutManager.saveWorkout(this, workout, new WorkoutManager.OnWorkoutSavedListener() {
                @Override
                public void onSuccess(Workout savedWorkout) {
                    finish();
                }

                @Override
                public void onError(String error) {
                    // Fehler wird bereits im Manager angezeigt
                }
            });
        }
    }
}