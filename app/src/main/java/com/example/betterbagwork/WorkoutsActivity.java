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

public class WorkoutsActivity extends AppCompatActivity {

    private Button btnCreateWorkout, btnBack;
    private RecyclerView recyclerViewWorkouts;
    private WorkoutAdapter workoutAdapter;
    private WorkoutManager workoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts);

        // Manager initialisieren
        workoutManager = new WorkoutManager();

        // Views initialisieren
        btnCreateWorkout = findViewById(R.id.btnCreateWorkout);
        btnBack = findViewById(R.id.btnBack);
        recyclerViewWorkouts = findViewById(R.id.recyclerViewWorkouts);

        // RecyclerView Setup
        setupRecyclerView();

        // Button: Neues Workout erstellen
        btnCreateWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutsActivity.this, CreateWorkoutActivity.class);
            startActivity(intent);
        });

        // Button: Zurück
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkouts();
    }

    private void setupRecyclerView() {
        workoutAdapter = new WorkoutAdapter();
        recyclerViewWorkouts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewWorkouts.setAdapter(workoutAdapter);

        // Click Listener
        workoutAdapter.setOnWorkoutClickListener(new WorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onWorkoutClick(Workout workout) {
                // TODO: Später - Workout starten (Timer-Activity)
                Toast.makeText(WorkoutsActivity.this,
                        "Workout-Start kommt bald!",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Workout workout) {
                showDeleteConfirmDialog(workout);
            }
        });
    }

    private void loadWorkouts() {
        workoutManager.loadUserWorkouts(this, new WorkoutManager.OnWorkoutsLoadedListener() {
            @Override
            public void onSuccess(List<Workout> workouts) {
                workoutAdapter.setWorkouts(workouts);
                if (workouts.isEmpty()) {
                    Toast.makeText(WorkoutsActivity.this,
                            "Noch keine Workouts vorhanden",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                // Fehler wird bereits im Manager angezeigt
            }
        });
    }

    private void showDeleteConfirmDialog(Workout workout) {
        new AlertDialog.Builder(this)
                .setTitle("Workout löschen?")
                .setMessage("Möchtest du '" + workout.getName() + "' wirklich löschen?")
                .setPositiveButton("Löschen", (dialog, which) -> deleteWorkout(workout))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void deleteWorkout(Workout workout) {
        workoutManager.deleteWorkout(this, workout.getId(),
                new WorkoutManager.OnWorkoutDeletedListener() {
                    @Override
                    public void onSuccess() {
                        loadWorkouts();
                    }

                    @Override
                    public void onError(String error) {
                        // Fehler wird bereits im Manager angezeigt
                    }
                });
    }
}