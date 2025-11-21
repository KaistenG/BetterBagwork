package com.example.betterbagwork;

import java.util.ArrayList;
import java.util.List;

public class Workout {
    private String id;
    private String name;
    private List<String> combinationIds; // IDs der ausgewählten Kombinationen
    private int roundTimeSeconds;        // Rundenzeit in Sekunden (z.B. 180 = 3 Min)
    private int numberOfRounds;          // Anzahl der Runden
    private int announcementInterval;    // Ansage-Intervall in Sekunden (z.B. 15)
    private int restTimeSeconds;         // Pause zwischen Runden in Sekunden
    private long timestamp;

    // Leerer Konstruktor für Firebase
    public Workout() {
        this.combinationIds = new ArrayList<>();
    }

    // Konstruktor
    public Workout(String id, String name, List<String> combinationIds,
                   int roundTimeSeconds, int numberOfRounds,
                   int announcementInterval, int restTimeSeconds) {
        this.id = id;
        this.name = name;
        this.combinationIds = combinationIds != null ? combinationIds : new ArrayList<>();
        this.roundTimeSeconds = roundTimeSeconds;
        this.numberOfRounds = numberOfRounds;
        this.announcementInterval = announcementInterval;
        this.restTimeSeconds = restTimeSeconds;
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

    public List<String> getCombinationIds() {
        return combinationIds;
    }

    public void setCombinationIds(List<String> combinationIds) {
        this.combinationIds = combinationIds;
    }

    public int getRoundTimeSeconds() {
        return roundTimeSeconds;
    }

    public void setRoundTimeSeconds(int roundTimeSeconds) {
        this.roundTimeSeconds = roundTimeSeconds;
    }

    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    public void setNumberOfRounds(int numberOfRounds) {
        this.numberOfRounds = numberOfRounds;
    }

    public int getAnnouncementInterval() {
        return announcementInterval;
    }

    public void setAnnouncementInterval(int announcementInterval) {
        this.announcementInterval = announcementInterval;
    }

    public int getRestTimeSeconds() {
        return restTimeSeconds;
    }

    public void setRestTimeSeconds(int restTimeSeconds) {
        this.restTimeSeconds = restTimeSeconds;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Hilfsmethoden
    public String getRoundTimeFormatted() {
        int minutes = roundTimeSeconds / 60;
        int seconds = roundTimeSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getRestTimeFormatted() {
        int minutes = restTimeSeconds / 60;
        int seconds = restTimeSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public int getTotalDurationSeconds() {
        return (roundTimeSeconds * numberOfRounds) + (restTimeSeconds * (numberOfRounds - 1));
    }

    public String getTotalDurationFormatted() {
        int totalSeconds = getTotalDurationSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}