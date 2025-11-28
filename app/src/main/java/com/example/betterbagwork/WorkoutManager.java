package com.example.betterbagwork;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutManager {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private static final String COLLECTION_USERS = "users";
    private static final String SUBCOLLECTION_WORKOUTS = "workouts";

    public WorkoutManager() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // Workout speichern
    public void saveWorkout(Context context, Workout workout, OnWorkoutSavedListener listener) {
        String userId = getUserId();
        if (userId == null) {
            Toast.makeText(context, "Nicht eingeloggt", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onError("Nicht eingeloggt");
            return;
        }

        if (workout.getName() == null || workout.getName().trim().isEmpty()) {
            Toast.makeText(context, "Bitte einen Namen eingeben", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onError("Name fehlt");
            return;
        }

        if (workout.getCombinationIds() == null || workout.getCombinationIds().isEmpty()) {
            Toast.makeText(context, "Bitte mindestens eine Kombination auswählen", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onError("Keine Kombinationen");
            return;
        }

        // Dokument erstellen
        Map<String, Object> workoutData = new HashMap<>();
        workoutData.put("name", workout.getName());
        workoutData.put("combinationIds", workout.getCombinationIds());
        workoutData.put("roundTimeSeconds", workout.getRoundTimeSeconds());
        workoutData.put("numberOfRounds", workout.getNumberOfRounds());
        workoutData.put("announcementInterval", workout.getAnnouncementInterval());
        workoutData.put("restTimeSeconds", workout.getRestTimeSeconds());
        workoutData.put("startDelaySeconds", workout.getStartDelaySeconds());
        workoutData.put("timestamp", System.currentTimeMillis());

        // In Firebase speichern unter: users/{userId}/workouts/
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_WORKOUTS)
                .add(workoutData)
                .addOnSuccessListener(documentReference -> {
                    workout.setId(documentReference.getId());
                    Toast.makeText(context, "Workout gespeichert", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onSuccess(workout);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Fehler beim Speichern: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    // Alle Workouts des Users laden
    public void loadUserWorkouts(Context context, OnWorkoutsLoadedListener listener) {
        String userId = getUserId();
        if (userId == null) {
            if (listener != null) listener.onError("Nicht eingeloggt");
            return;
        }

        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_WORKOUTS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Workout> workouts = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Workout workout = new Workout();
                        workout.setId(doc.getId());
                        workout.setName(doc.getString("name"));
                        workout.setCombinationIds((List<String>) doc.get("combinationIds"));
                        workout.setRoundTimeSeconds(doc.getLong("roundTimeSeconds") != null ?
                                doc.getLong("roundTimeSeconds").intValue() : 180);
                        workout.setNumberOfRounds(doc.getLong("numberOfRounds") != null ?
                                doc.getLong("numberOfRounds").intValue() : 5);
                        workout.setAnnouncementInterval(doc.getLong("announcementInterval") != null ?
                                doc.getLong("announcementInterval").intValue() : 15);
                        workout.setRestTimeSeconds(doc.getLong("restTimeSeconds") != null ?
                                doc.getLong("restTimeSeconds").intValue() : 60);
                        workout.setStartDelaySeconds(doc.getLong("startDelaySeconds") != null ?
                                doc.getLong("startDelaySeconds").intValue() : 0);
                        workout.setTimestamp(doc.getLong("timestamp") != null ?
                                doc.getLong("timestamp") : 0);
                        workouts.add(workout);
                    }
                    if (listener != null) listener.onSuccess(workouts);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Fehler beim Laden: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    // Workout löschen
    public void deleteWorkout(Context context, String workoutId, OnWorkoutDeletedListener listener) {
        String userId = getUserId();
        if (userId == null || workoutId == null) {
            if (listener != null) listener.onError("Keine ID");
            return;
        }

        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_WORKOUTS)
                .document(workoutId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Workout gelöscht", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Fehler beim Löschen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    // Hilfsmethode: User ID holen
    private String getUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    // Callback Interfaces
    public interface OnWorkoutSavedListener {
        void onSuccess(Workout workout);
        void onError(String error);
    }

    public interface OnWorkoutsLoadedListener {
        void onSuccess(List<Workout> workouts);
        void onError(String error);
    }

    public interface OnWorkoutDeletedListener {
        void onSuccess();
        void onError(String error);
    }
}