package com.example.betterbagwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WorkoutsFragment extends Fragment {

    private RecyclerView recyclerViewWorkouts;
    private Button btnCreateWorkout;
    private WorkoutAdapter workoutAdapter;
    private WorkoutManager workoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workouts, container, false);

        // Manager initialisieren
        workoutManager = new WorkoutManager();

        // Views initialisieren
        recyclerViewWorkouts = view.findViewById(R.id.recyclerViewWorkouts);
        btnCreateWorkout = view.findViewById(R.id.btnCreateWorkout);

        // RecyclerView Setup
        setupRecyclerView();

        // Button: Neues Workout erstellen
        btnCreateWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateWorkoutActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWorkouts();
    }

    private void setupRecyclerView() {
        workoutAdapter = new WorkoutAdapter();
        recyclerViewWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewWorkouts.setAdapter(workoutAdapter);

        workoutAdapter.setOnWorkoutClickListener(new WorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onWorkoutClick(Workout workout) {
                startWorkoutTimer(workout);
            }

            @Override
            public void onDeleteClick(Workout workout) {
                showDeleteConfirmDialog(workout);
            }
        });
    }

    private void loadWorkouts() {
        workoutManager.loadUserWorkouts(getContext(), new WorkoutManager.OnWorkoutsLoadedListener() {
            @Override
            public void onSuccess(List<Workout> workouts) {
                workoutAdapter.setWorkouts(workouts);
                if (workouts.isEmpty()) {
                    Toast.makeText(getContext(),
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
        new AlertDialog.Builder(getContext())
                .setTitle("Workout löschen?")
                .setMessage("Möchtest du '" + workout.getName() + "' wirklich löschen?")
                .setPositiveButton("Löschen", (dialog, which) -> deleteWorkout(workout))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void deleteWorkout(Workout workout) {
        workoutManager.deleteWorkout(getContext(), workout.getId(),
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

    private void startWorkoutTimer(Workout workout) {
        Intent intent = new Intent(getActivity(), WorkoutTimerActivity.class);
        intent.putExtra("workoutId", workout.getId());
        intent.putExtra("workoutName", workout.getName());
        intent.putStringArrayListExtra("combinationIds", new ArrayList<>(workout.getCombinationIds()));
        intent.putExtra("roundTimeSeconds", workout.getRoundTimeSeconds());
        intent.putExtra("numberOfRounds", workout.getNumberOfRounds());
        intent.putExtra("announcementInterval", workout.getAnnouncementInterval());
        intent.putExtra("restTimeSeconds", workout.getRestTimeSeconds());
        intent.putExtra("startDelaySeconds", workout.getStartDelaySeconds());
        startActivity(intent);
    }
}