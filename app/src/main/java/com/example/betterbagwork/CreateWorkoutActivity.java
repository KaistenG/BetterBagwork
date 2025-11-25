package com.example.betterbagwork;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

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
    }

    private void setupNumberPickers() {
        // Rundenzeit - Minuten (0-10)
        pickerRoundMinutes.setMinValue(0);
        pickerRoundMinutes.setMaxValue(10);
        pickerRoundMinutes.setValue(3); // Default: 3 Minuten

        // Rundenzeit - Sekunden (0-59)
        pickerRoundSeconds.setMinValue(0);
        pickerRoundSeconds.setMaxValue(59);
        pickerRoundSeconds.setValue(0);

        // Anzahl Runden (1-20)
        pickerNumberOfRounds.setMinValue(1);
        pickerNumberOfRounds.setMaxValue(20);
        pickerNumberOfRounds.setValue(5); // Default: 5 Runden

        // Ansage-Intervall (5-60 Sekunden)
        pickerAnnouncementInterval.setMinValue(3); //3 Sekunden, weil 5 zu niedrig ist
        pickerAnnouncementInterval.setMaxValue(15);
        pickerAnnouncementInterval.setValue(5); // Default: 5 Sekunden

        // Pausenzeit - Minuten (0-5)
        pickerRestMinutes.setMinValue(0);
        pickerRestMinutes.setMaxValue(5);
        pickerRestMinutes.setValue(1); // Default: 1 Minute

        // Pausenzeit - Sekunden (0-59)
        pickerRestSeconds.setMinValue(0);
        pickerRestSeconds.setMaxValue(59);
        pickerRestSeconds.setValue(0);

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
        int roundTimeSeconds = (pickerRoundMinutes.getValue() * 60) + pickerRoundSeconds.getValue();
        int numberOfRounds = pickerNumberOfRounds.getValue();
        int announcementInterval = pickerAnnouncementInterval.getValue();
        int restTimeSeconds = (pickerRestMinutes.getValue() * 60) + pickerRestSeconds.getValue();
        int startDelaySeconds = pickerStartDelay.getValue() * 10; // 0, 10, 20, 30

        // Validierung
        if (roundTimeSeconds < 30) {
            Toast.makeText(this, "Rundenzeit muss mindestens 30 Sekunden sein", Toast.LENGTH_SHORT).show();
            return;
        }

        // Workout erstellen
        Workout workout = new Workout(
                null,
                name,
                selectedIds,
                roundTimeSeconds,
                numberOfRounds,
                announcementInterval,
                restTimeSeconds,
                startDelaySeconds
        );

        // Speichern
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