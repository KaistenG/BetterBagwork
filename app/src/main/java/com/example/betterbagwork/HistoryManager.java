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

public class HistoryManager {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private static final String COLLECTION_USERS = "users";
    private static final String SUBCOLLECTION_HISTORY = "workout_history";

    public HistoryManager() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // History-Eintrag speichern
    public void saveHistory(Context context, WorkoutHistory history, OnHistorySavedListener listener) {
        String userId = getUserId();
        if (userId == null) {
            if (listener != null) listener.onError("Nicht eingeloggt");
            return;
        }

        Map<String, Object> historyData = new HashMap<>();
        historyData.put("workoutId", history.getWorkoutId());
        historyData.put("workoutName", history.getWorkoutName());
        historyData.put("timestamp", history.getTimestamp());
        historyData.put("status", history.getStatus());
        historyData.put("totalDurationSeconds", history.getTotalDurationSeconds());
        historyData.put("roundsCompleted", history.getRoundsCompleted());
        historyData.put("totalRounds", history.getTotalRounds());

        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_HISTORY)
                .add(historyData)
                .addOnSuccessListener(documentReference -> {
                    history.setId(documentReference.getId());
                    if (listener != null) listener.onSuccess(history);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Fehler beim Speichern der History", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    // Alle History-Einträge laden (neueste zuerst)
    public void loadUserHistory(Context context, OnHistoryLoadedListener listener) {
        String userId = getUserId();
        if (userId == null) {
            if (listener != null) listener.onError("Nicht eingeloggt");
            return;
        }

        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_HISTORY)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<WorkoutHistory> historyList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        WorkoutHistory history = new WorkoutHistory();
                        history.setId(doc.getId());
                        history.setWorkoutId(doc.getString("workoutId"));
                        history.setWorkoutName(doc.getString("workoutName"));
                        history.setTimestamp(doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0);
                        history.setStatus(doc.getString("status"));
                        history.setTotalDurationSeconds(doc.getLong("totalDurationSeconds") != null ?
                                doc.getLong("totalDurationSeconds").intValue() : 0);
                        history.setRoundsCompleted(doc.getLong("roundsCompleted") != null ?
                                doc.getLong("roundsCompleted").intValue() : 0);
                        history.setTotalRounds(doc.getLong("totalRounds") != null ?
                                doc.getLong("totalRounds").intValue() : 0);
                        historyList.add(history);
                    }
                    if (listener != null) listener.onSuccess(historyList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Fehler beim Laden der History", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    // History-Eintrag löschen
    public void deleteHistory(Context context, String historyId, OnHistoryDeletedListener listener) {
        String userId = getUserId();
        if (userId == null || historyId == null) {
            if (listener != null) listener.onError("Keine ID");
            return;
        }

        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_HISTORY)
                .document(historyId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Eintrag gelöscht", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Fehler beim Löschen", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    private String getUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    // Callback Interfaces
    public interface OnHistorySavedListener {
        void onSuccess(WorkoutHistory history);
        void onError(String error);
    }

    public interface OnHistoryLoadedListener {
        void onSuccess(List<WorkoutHistory> historyList);
        void onError(String error);
    }

    public interface OnHistoryDeletedListener {
        void onSuccess();
        void onError(String error);
    }
}