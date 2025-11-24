package com.example.betterbagwork;

import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class WorkoutTimerActivity extends AppCompatActivity {

    private TextView txtTimer, txtCurrentRound, txtCurrentCombination, txtStatus;
    private Button btnPauseResume, btnStop;

    private Workout workout;
    private List<Combination> combinations;
    private CombinationManager combinationManager;

    private TextToSpeech tts;
    private Handler handler;
    private Runnable timerRunnable;

    private int currentRound = 1;
    private int remainingSeconds;
    private int nextAnnouncementIn;
    private boolean isPaused = false;
    private boolean isResting = false;
    private boolean isSpeaking = false; // Neu: Track ob TTS gerade spricht
    private Random random;
    private static final String UTTERANCE_ID = "combo_announcement";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_timer);

        // Views initialisieren
        txtTimer = findViewById(R.id.txtTimer);
        txtCurrentRound = findViewById(R.id.txtCurrentRound);
        txtCurrentCombination = findViewById(R.id.txtCurrentCombination);
        txtStatus = findViewById(R.id.txtStatus);
        btnPauseResume = findViewById(R.id.btnPauseResume);
        btnStop = findViewById(R.id.btnStop);

        // Workout aus Intent holen
        String workoutId = getIntent().getStringExtra("workoutId");
        if (workoutId == null) {
            Toast.makeText(this, "Fehler: Kein Workout übergeben", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Manager & Random
        combinationManager = new CombinationManager();
        random = new Random();
        handler = new Handler();
        combinations = new ArrayList<>();

        // Text-to-Speech initialisieren
        initTextToSpeech();

        // Workout laden
        loadWorkoutAndCombinations(workoutId);

        // Buttons
        btnPauseResume.setOnClickListener(v -> togglePauseResume());
        btnStop.setOnClickListener(v -> showStopConfirmDialog());

        // Back Button Handler
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showStopConfirmDialog();
            }
        });
    }

    private void initTextToSpeech() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.GERMAN);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Deutsche Sprache nicht verfügbar", Toast.LENGTH_SHORT).show();
                }

                // UtteranceProgressListener setzen
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        if (UTTERANCE_ID.equals(utteranceId)) {
                            isSpeaking = true;
                        }
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        if (UTTERANCE_ID.equals(utteranceId)) {
                            isSpeaking = false;
                            // Intervall-Timer neu starten NACH der Ansage
                            nextAnnouncementIn = workout.getAnnouncementInterval();
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {
                        if (UTTERANCE_ID.equals(utteranceId)) {
                            isSpeaking = false;
                        }
                    }
                });
            }
        });
    }

    private void loadWorkoutAndCombinations(String workoutId) {
        // Erst alle Kombinationen laden
        combinationManager.loadUserCombinations(this, new CombinationManager.OnCombinationsLoadedListener() {
            @Override
            public void onSuccess(List<Combination> allCombinations) {
                // Workout-Daten müssen wir uns vom Intent holen oder neu laden
                // Für jetzt: Workout-Daten aus Intent
                workout = createWorkoutFromIntent();

                // Nur die ausgewählten Kombinationen filtern
                combinations = filterSelectedCombinations(allCombinations, workout.getCombinationIds());

                if (combinations.isEmpty()) {
                    Toast.makeText(WorkoutTimerActivity.this,
                            "Keine Kombinationen gefunden", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Workout starten
                startWorkout();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WorkoutTimerActivity.this,
                        "Fehler beim Laden", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private Workout createWorkoutFromIntent() {
        // Workout-Daten aus Intent extrahieren
        String name = getIntent().getStringExtra("workoutName");
        ArrayList<String> comboIds = getIntent().getStringArrayListExtra("combinationIds");
        int roundTime = getIntent().getIntExtra("roundTimeSeconds", 180);
        int rounds = getIntent().getIntExtra("numberOfRounds", 5);
        int interval = getIntent().getIntExtra("announcementInterval", 15);
        int rest = getIntent().getIntExtra("restTimeSeconds", 60);

        return new Workout(null, name, comboIds, roundTime, rounds, interval, rest);
    }

    private List<Combination> filterSelectedCombinations(List<Combination> all, List<String> selectedIds) {
        List<Combination> filtered = new ArrayList<>();
        for (Combination c : all) {
            if (selectedIds.contains(c.getId())) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    private void startWorkout() {
        currentRound = 1;
        isResting = false;
        startRound();
    }

    private void startRound() {
        remainingSeconds = workout.getRoundTimeSeconds();
        nextAnnouncementIn = -1; // -1 = Warte auf TTS-Fertigstellung
        isResting = false;

        updateUI();
        speak("Runde " + currentRound + " Start!", null);
        announceRandomCombination();

        startTimer();
    }

    private void startRest() {
        remainingSeconds = workout.getRestTimeSeconds();
        isResting = true;

        updateUI();
        speak("Pause!", null);

        startTimer();
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    remainingSeconds--;

                    if (!isResting && !isSpeaking) {
                        // Nur runterzählen wenn NICHT gerade gesprochen wird
                        if (nextAnnouncementIn > 0) {
                            nextAnnouncementIn--;
                        }

                        // Neue Kombination ansagen?
                        if (nextAnnouncementIn == 0) {
                            announceRandomCombination();
                            nextAnnouncementIn = -1; // Warten auf TTS-Fertigstellung
                        }
                    }

                    updateUI();

                    // Zeit abgelaufen?
                    if (remainingSeconds <= 0) {
                        onTimerFinished();
                        return;
                    }
                }

                handler.postDelayed(this, 1000);
            }
        };

        handler.post(timerRunnable);
    }

    private void onTimerFinished() {
        if (isResting) {
            // Pause vorbei -> nächste Runde
            currentRound++;
            if (currentRound <= workout.getNumberOfRounds()) {
                startRound();
            } else {
                // Workout beendet
                finishWorkout();
            }
        } else {
            // Runde vorbei
            if (currentRound < workout.getNumberOfRounds()) {
                // Pause starten
                startRest();
            } else {
                // Letzte Runde -> Workout beendet
                finishWorkout();
            }
        }
    }

    private void announceRandomCombination() {
        if (combinations.isEmpty()) return;

        Combination combo = combinations.get(random.nextInt(combinations.size()));
        String announcement = buildAnnouncementText(combo);

        txtCurrentCombination.setText(combo.getName() + "\n" + combo.getMovesAsString());
        speak(announcement, UTTERANCE_ID); // Mit Utterance ID für Tracking
    }

    private String buildAnnouncementText(Combination combo) {
        // Kombinationsname und Schläge einzeln ansagen
        StringBuilder sb = new StringBuilder();
        for (String move : combo.getMoves()) {
            sb.append(move).append(", ");
        }
        return sb.toString();
    }

    private void speak(String text, String utteranceId) {
        if (tts != null && !text.isEmpty()) {
            Bundle params = new Bundle();
            tts.speak(text, TextToSpeech.QUEUE_ADD, params, utteranceId);
        }
    }

    private void updateUI() {
        // Timer formatieren
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        txtTimer.setText(String.format("%02d:%02d", minutes, seconds));

        // Runde
        txtCurrentRound.setText("Runde " + currentRound + " / " + workout.getNumberOfRounds());

        // Status
        if (isResting) {
            txtStatus.setText("PAUSE");
            txtStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            txtStatus.setText("TRAINING");
            txtStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void togglePauseResume() {
        isPaused = !isPaused;
        btnPauseResume.setText(isPaused ? "▶ Resume" : "⏸ Pause");

        if (isPaused) {
            speak("Pause", null);
        } else {
            speak("Weiter", null);
        }
    }

    private void showStopConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Workout abbrechen?")
                .setMessage("Möchtest du das Workout wirklich beenden?")
                .setPositiveButton("Abbrechen", (dialog, which) -> finish())
                .setNegativeButton("Weitermachen", null)
                .show();
    }

    private void finishWorkout() {
        speak("Workout beendet! Gut gemacht!", null);

        new AlertDialog.Builder(this)
                .setTitle("Workout abgeschlossen!")
                .setMessage("Du hast " + workout.getNumberOfRounds() + " Runden absolviert!")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}