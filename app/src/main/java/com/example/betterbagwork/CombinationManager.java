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

public class CombinationManager {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private static final String COLLECTION_COMBINATIONS = "combinations";

    public CombinationManager() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // Kombination speichern
    public void saveCombination(Context context, Combination combination, OnCombinationSavedListener listener) {
        String userId = getUserId();
        if (userId == null) {
            Toast.makeText(context, "Nicht eingeloggt", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onError("Nicht eingeloggt");
            return;
        }

        if (combination.getName() == null || combination.getName().trim().isEmpty()) {
            Toast.makeText(context, "Bitte einen Namen eingeben", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onError("Name fehlt");
            return;
        }

        if (combination.getMoves() == null || combination.getMoves().isEmpty()) {
            Toast.makeText(context, "Bitte mindestens einen Schlag hinzufügen", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onError("Keine Schläge");
            return;
        }

        // Dokument erstellen
        Map<String, Object> combinationData = new HashMap<>();
        combinationData.put("userId", userId);
        combinationData.put("name", combination.getName());
        combinationData.put("moves", combination.getMoves());
        combinationData.put("timestamp", System.currentTimeMillis());

        // In Firebase speichern
        db.collection(COLLECTION_COMBINATIONS)
                .add(combinationData)
                .addOnSuccessListener(documentReference -> {
                    combination.setId(documentReference.getId());
                    Toast.makeText(context, "Kombination gespeichert", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onSuccess(combination);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Fehler beim Speichern: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    // Alle Kombinationen des Users laden
    public void loadUserCombinations(Context context, OnCombinationsLoadedListener listener) {
        String userId = getUserId();
        if (userId == null) {
            if (listener != null) listener.onError("Nicht eingeloggt");
            return;
        }

        db.collection(COLLECTION_COMBINATIONS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Combination> combinations = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Combination combo = new Combination();
                        combo.setId(doc.getId());
                        combo.setName(doc.getString("name"));
                        combo.setMoves((List<String>) doc.get("moves"));
                        combo.setTimestamp(doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0);
                        combinations.add(combo);
                    }
                    if (listener != null) listener.onSuccess(combinations);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Fehler beim Laden: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    // Kombination löschen
    public void deleteCombination(Context context, String combinationId, OnCombinationDeletedListener listener) {
        if (combinationId == null) {
            if (listener != null) listener.onError("Keine ID");
            return;
        }

        db.collection(COLLECTION_COMBINATIONS)
                .document(combinationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Kombination gelöscht", Toast.LENGTH_SHORT).show();
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
    public interface OnCombinationSavedListener {
        void onSuccess(Combination combination);
        void onError(String error);
    }

    public interface OnCombinationsLoadedListener {
        void onSuccess(List<Combination> combinations);
        void onError(String error);
    }

    public interface OnCombinationDeletedListener {
        void onSuccess();
        void onError(String error);
    }
}