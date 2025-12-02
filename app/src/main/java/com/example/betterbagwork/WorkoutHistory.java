package com.example.betterbagwork;

public class WorkoutHistory {
    private String id;
    private String workoutId;
    private String workoutName;
    private long timestamp;
    private String status; // "completed" oder "aborted"
    private int totalDurationSeconds;
    private int roundsCompleted;
    private int totalRounds;

    // Leerer Konstruktor für Firebase
    public WorkoutHistory() {
    }

    // Konstruktor
    public WorkoutHistory(String id, String workoutId, String workoutName,
                          long timestamp, String status,
                          int totalDurationSeconds, int roundsCompleted, int totalRounds) {
        this.id = id;
        this.workoutId = workoutId;
        this.workoutName = workoutName;
        this.timestamp = timestamp;
        this.status = status;
        this.totalDurationSeconds = totalDurationSeconds;
        this.roundsCompleted = roundsCompleted;
        this.totalRounds = totalRounds;
    }

    // Getter und Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(String workoutId) {
        this.workoutId = workoutId;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalDurationSeconds() {
        return totalDurationSeconds;
    }

    public void setTotalDurationSeconds(int totalDurationSeconds) {
        this.totalDurationSeconds = totalDurationSeconds;
    }

    public int getRoundsCompleted() {
        return roundsCompleted;
    }

    public void setRoundsCompleted(int roundsCompleted) {
        this.roundsCompleted = roundsCompleted;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    // Hilfsmethoden
    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.GERMANY);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Europe/Berlin"));
        return sdf.format(new java.util.Date(timestamp));
    }

    public String getFormattedDuration() {
        int minutes = totalDurationSeconds / 60;
        int seconds = totalDurationSeconds % 60;
        return String.format("%d:%02d Min", minutes, seconds);
    }

    public String getStatusText() {
        return status.equals("completed") ? "Abgeschlossen" : "Abgebrochen";
    }

    public boolean isCompleted() {
        return status.equals("completed");
    }
}