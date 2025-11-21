package com.example.betterbagwork;

import java.util.ArrayList;
import java.util.List;

public class Combination {
    private String id;
    private String name;
    private List<String> moves;
    private long timestamp;

    // Leerer Konstruktor für Firebase
    public Combination() {
        this.moves = new ArrayList<>();
    }

    // Konstruktor
    public Combination(String id, String name, List<String> moves) {
        this.id = id;
        this.name = name;
        this.moves = moves != null ? moves : new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }

    // Getter und Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMoves() {
        return moves;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Hilfsmethode: Kombination als String (für Anzeige)
    public String getMovesAsString() {
        if (moves == null || moves.isEmpty()) {
            return "Keine Schläge";
        }
        return String.join(" → ", moves);
    }

    // Hilfsmethode: Anzahl der Schläge
    public int getMoveCount() {
        return moves != null ? moves.size() : 0;
    }
}