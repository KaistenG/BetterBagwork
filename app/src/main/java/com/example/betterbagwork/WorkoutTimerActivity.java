package com.example.betterbagwork;

import android.media.MediaPlayer;
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
    private boolean isSpeaking = false;
    private boolean isPreStart = false;
    private Random random;
    private static final String UTTERANCE_ID = "combo_announcement";

    private MediaPlayer openingBellPlayer;
    private MediaPlayer lastTenBellPlayer;
    private MediaPlayer roundOverBellPlayer;

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

        // Sound-Player laden
        initOpeningBellSound();
        initLastTenBellSound();
        initRoundOverBellSound();

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
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "English language not available", Toast.LENGTH_SHORT).show();
                }

                // TTS-Qualität und Geschwindigkeit optimieren
                tts.setSpeechRate(1.0f);  // 20% schneller (1.0 = normal, 1.5 = 50% schneller) - bleibe hier erst einmal bei 1.0f, da es ohne "," schon schnell genug ist.
                tts.setPitch(1.0f);       // Normale Tonhöhe (0.5-2.0)

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

    private void initOpeningBellSound() {
        try {
            openingBellPlayer = MediaPlayer.create(this, R.raw.opening_bell);
            openingBellPlayer.setOnCompletionListener(mp -> mp.seekTo(0));
        } catch (Exception e) {
            Toast.makeText(this, "Opening bell sound nicht gefunden", Toast.LENGTH_SHORT).show();
        }
    }

    private void initLastTenBellSound() {
        try {
            lastTenBellPlayer = MediaPlayer.create(this, R.raw.last_ten_bell);
            lastTenBellPlayer.setOnCompletionListener(mp -> mp.seekTo(0));
        } catch (Exception e) {
            Toast.makeText(this, "Last ten bell sound nicht gefunden", Toast.LENGTH_SHORT).show();
        }
    }

    private void initRoundOverBellSound() {
        try {
            roundOverBellPlayer = MediaPlayer.create(this, R.raw.round_over_bell);
            roundOverBellPlayer.setOnCompletionListener(mp -> mp.seekTo(0));
        } catch (Exception e) {
            Toast.makeText(this, "Round over bell sound nicht gefunden", Toast.LENGTH_SHORT).show();
        }
    }

    private void playOpeningBell() {
        if (openingBellPlayer != null) {
            openingBellPlayer.start();
        }
    }

    private void playLastTenBell() {
        if (lastTenBellPlayer != null) {
            lastTenBellPlayer.start();
        }
    }

    private void playBeepSequence() {
        // Nicht mehr benötigt - kann gelöscht werden
    }

    private void playRoundOverBell() {
        if (roundOverBellPlayer != null) {
            roundOverBellPlayer.start();
        }
    }

    private void loadWorkoutAndCombinations(String workoutId) {
        // Erst alle Kombinationen laden
        combinationManager.loadUserCombinations(this, new CombinationManager.OnCombinationsLoadedListener() {
            @Override
            public void onSuccess(List<Combination> allCombinations) {
                // Workout-Daten aus Intent
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
        String name = getIntent().getStringExtra("workoutName");
        ArrayList<String> comboIds = getIntent().getStringArrayListExtra("combinationIds");
        int roundTime = getIntent().getIntExtra("roundTimeSeconds", 180);
        int rounds = getIntent().getIntExtra("numberOfRounds", 5);
        int interval = getIntent().getIntExtra("announcementInterval", 15);
        int rest = getIntent().getIntExtra("restTimeSeconds", 60);
        int startDelay = getIntent().getIntExtra("startDelaySeconds", 0);

        return new Workout(null, name, comboIds, roundTime, rounds, interval, rest, startDelay);
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

        // Wenn Startverzögerung gesetzt ist, Pre-Start starten
        if (workout.getStartDelaySeconds() > 0) {
            startPreStart();
        } else {
            // Direkt Runde starten
            startRound();
        }
    }

    private void startPreStart() {
        remainingSeconds = workout.getStartDelaySeconds();
        isPreStart = true;

        updateUI();
        speak("Get ready! Starting in " + remainingSeconds + " seconds", null);

        startTimer();
    }

    private void startRound() {
        remainingSeconds = workout.getRoundTimeSeconds();
        nextAnnouncementIn = -1;
        isResting = false;
        isPreStart = false;

        updateUI();

        // Erst Ansage "Round X"
        speak("Round " + currentRound, null);

        // Nach 1 Sekunde: Gong
        handler.postDelayed(() -> {
            playOpeningBell();
        }, 1000);

        // Nach 2 Sekunden: Timer starten + erste Kombination
        handler.postDelayed(() -> {
            announceRandomCombination();
            startTimer();
        }, 2000);
    }

    private void startRest() {
        remainingSeconds = workout.getRestTimeSeconds();
        isResting = true;

        updateUI();

        // Erst Round Over Bell spielen
        playRoundOverBell();

        // Nach 1 Sekunde: "Rest!" ansagen
        handler.postDelayed(() -> {
            speak("Rest!", null);
        }, 1000);

        // Nach 2 Sekunden: Timer starten
        handler.postDelayed(() -> {
            startTimer();
        }, 2000);
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    remainingSeconds--;

                    if (isPreStart) {
                        // Pre-Start Phase: Nur Countdown
                        if (remainingSeconds <= 0) {
                            onTimerFinished();
                            return;
                        }
                    } else if (!isResting) {
                        // Letzte 10 Sekunden Bell (auch während TTS!)
                        if (remainingSeconds == 10) {
                            playLastTenBell();
                        }

                        // Kombinationen nur wenn NICHT gesprochen wird
                        if (!isSpeaking) {
                            if (nextAnnouncementIn > 0) {
                                nextAnnouncementIn--;
                            }

                            // Neue Kombination ansagen? NUR wenn mehr als 3 Sekunden übrig!
                            if (nextAnnouncementIn == 0 && remainingSeconds > 3) {
                                announceRandomCombination();
                                nextAnnouncementIn = -1;
                            }
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
        if (isPreStart) {
            // Pre-Start beendet -> Runde starten
            startRound();
        } else if (isResting) {
            // Pause vorbei -> nächste Runde
            currentRound++;
            if (currentRound <= workout.getNumberOfRounds()) {
                startRound();
            } else {
                finishWorkout();
            }
        } else {
            // Runde vorbei -> Pause oder Ende
            if (currentRound < workout.getNumberOfRounds()) {
                // Pause starten (spielt selbst den Round Over Bell)
                startRest();
            } else {
                // Letzte Runde -> Round Over Bell + Workout beendet
                playRoundOverBell();
                handler.postDelayed(() -> {
                    finishWorkout();
                }, 1000);
            }
        }
    }

    private void announceRandomCombination() {
        if (combinations.isEmpty()) return;

        Combination combo = combinations.get(random.nextInt(combinations.size()));
        String announcement = buildAnnouncementText(combo);

        txtCurrentCombination.setText(combo.getName() + "\n" + combo.getMovesAsString());
        speak(announcement, UTTERANCE_ID);
    }

    private String buildAnnouncementText(Combination combo) {
        // Schläge ohne Kommas - klingt schneller und cleaner
        StringBuilder sb = new StringBuilder();
        for (String move : combo.getMoves()) {
            sb.append(move).append(" ");
        }
        return sb.toString().trim();
    }

    private void speak(String text, String utteranceId) {
        if (tts != null && !text.isEmpty()) {
            android.os.Bundle params = new android.os.Bundle();
            tts.speak(text, TextToSpeech.QUEUE_ADD, params, utteranceId);
        }
    }

    private void updateUI() {
        // Timer formatieren
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        txtTimer.setText(String.format("%02d:%02d", minutes, seconds));

        // Runde
        txtCurrentRound.setText("Round " + currentRound + " / " + workout.getNumberOfRounds());

        // Status
        if (isPreStart) {
            txtStatus.setText("GET READY");
            txtStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
        } else if (isResting) {
            txtStatus.setText("REST");
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
            speak("Paused", null);
        } else {
            speak("Continue", null);
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
        speak("Workout complete! Great job!", null);

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
        if (openingBellPlayer != null) {
            openingBellPlayer.release();
        }
        if (lastTenBellPlayer != null) {
            lastTenBellPlayer.release();
        }
        if (roundOverBellPlayer != null) {
            roundOverBellPlayer.release();
        }
    }
}