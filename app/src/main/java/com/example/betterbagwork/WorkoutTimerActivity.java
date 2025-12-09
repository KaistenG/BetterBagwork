package com.example.betterbagwork;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorkoutTimerActivity extends AppCompatActivity {

    private static final String TAG = "WorkoutTimer";

    private TextView txtTimer, txtCurrentRound, txtCurrentCombination, txtStatus;
    private MaterialButton btnPauseResume, btnStop;

    private Workout workout;
    private List<Combination> combinations;
    private CombinationManager combinationManager;
    private HistoryManager historyManager;

    private AudioAnnouncerHelper audioAnnouncer;
    private Handler handler;
    private Runnable timerRunnable;

    private int currentRound = 1;
    private int remainingSeconds;
    private int nextAnnouncementIn;
    private boolean isPaused = false;
    private boolean isResting = false;
    private boolean isPreStart = false;
    private Random random;

    // Für History-Tracking
    private long workoutStartTime;
    private int totalSecondsElapsed = 0;

    private MediaPlayer openingBellPlayer;
    private MediaPlayer lastTenBellPlayer;
    private MediaPlayer roundOverBellPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_timer);

        // Display bleibt an während Workout
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d(TAG, "=== WorkoutTimer Start ===");

        // Views
        txtTimer = findViewById(R.id.txtTimer);
        txtCurrentRound = findViewById(R.id.txtCurrentRound);
        txtCurrentCombination = findViewById(R.id.txtCurrentCombination);
        txtStatus = findViewById(R.id.txtStatus);
        btnPauseResume = findViewById(R.id.btnPauseResume);
        btnStop = findViewById(R.id.btnStop);

        String workoutId = getIntent().getStringExtra("workoutId");
        if (workoutId == null) {
            Toast.makeText(this, "Fehler: Kein Workout", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Init
        combinationManager = new CombinationManager();
        historyManager = new HistoryManager();
        random = new Random();
        handler = new Handler();
        combinations = new ArrayList<>();

        // AUDIO ANNOUNCER
        audioAnnouncer = new AudioAnnouncerHelper(this);
        Toast.makeText(this, "Audio-System bereit", Toast.LENGTH_SHORT).show();

        // Bells
        initBells();

        // Workout laden
        loadWorkoutAndCombinations(workoutId);

        // Buttons
        btnPauseResume.setOnClickListener(v -> togglePauseResume());
        btnStop.setOnClickListener(v -> showStopConfirmDialog());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showStopConfirmDialog();
            }
        });
    }

    private void initBells() {
        try {
            openingBellPlayer = MediaPlayer.create(this, R.raw.opening_bell);
            if (openingBellPlayer != null) openingBellPlayer.setOnCompletionListener(mp -> mp.seekTo(0));

            lastTenBellPlayer = MediaPlayer.create(this, R.raw.last_ten_bell);
            if (lastTenBellPlayer != null) lastTenBellPlayer.setOnCompletionListener(mp -> mp.seekTo(0));

            roundOverBellPlayer = MediaPlayer.create(this, R.raw.round_over_bell);
            if (roundOverBellPlayer != null) roundOverBellPlayer.setOnCompletionListener(mp -> mp.seekTo(0));
        } catch (Exception e) {
            Log.e(TAG, "Bell init error", e);
        }
    }

    private void loadWorkoutAndCombinations(String workoutId) {
        combinationManager.loadUserCombinations(this, new CombinationManager.OnCombinationsLoadedListener() {
            @Override
            public void onSuccess(List<Combination> allCombinations) {
                workout = createWorkoutFromIntent();
                combinations = filterSelectedCombinations(allCombinations, workout.getCombinationIds());

                if (combinations.isEmpty()) {
                    Toast.makeText(WorkoutTimerActivity.this, "Keine Kombinationen", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Log.d(TAG, "Kombinationen: " + combinations.size());
                startWorkout();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WorkoutTimerActivity.this, "Fehler", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private Workout createWorkoutFromIntent() {
        return new Workout(
                getIntent().getStringExtra("workoutId"),
                getIntent().getStringExtra("workoutName"),
                getIntent().getStringArrayListExtra("combinationIds"),
                getIntent().getIntExtra("roundTimeSeconds", 180),
                getIntent().getIntExtra("numberOfRounds", 5),
                getIntent().getIntExtra("announcementInterval", 15),
                getIntent().getIntExtra("restTimeSeconds", 60),
                getIntent().getIntExtra("startDelaySeconds", 0)
        );
    }

    private List<Combination> filterSelectedCombinations(List<Combination> all, List<String> selectedIds) {
        List<Combination> filtered = new ArrayList<>();
        for (Combination c : all) {
            if (selectedIds.contains(c.getId())) filtered.add(c);
        }
        return filtered;
    }

    private void startWorkout() {
        currentRound = 1;
        isResting = false;
        workoutStartTime = System.currentTimeMillis();
        totalSecondsElapsed = 0;
        Log.d(TAG, "Workout Start");

        if (workout.getStartDelaySeconds() > 0) {
            startPreStart();
        } else {
            startRound();
        }
    }

    private void startPreStart() {
        remainingSeconds = workout.getStartDelaySeconds();
        isPreStart = true;
        updateUI();

        audioAnnouncer.announceText("Get ready!", () -> {});
        startTimer();
    }

    private void startRound() {
        Log.d(TAG, "=== Runde " + currentRound + " ===");

        remainingSeconds = workout.getRoundTimeSeconds();
        nextAnnouncementIn = -1;
        isResting = false;
        isPreStart = false;
        updateUI();

        // Audio: "Round X"
        audioAnnouncer.announceRound(currentRound, () -> {
            // Nach Ansage → Gong
            handler.postDelayed(() -> {
                if (openingBellPlayer != null) openingBellPlayer.start();

                // Nach Gong → Kombination + Timer
                handler.postDelayed(() -> {
                    announceRandomCombination();
                    startTimer();
                }, 1000);
            }, 500);
        });
    }

    private void startRest() {
        remainingSeconds = workout.getRestTimeSeconds();
        isResting = true;
        updateUI();

        if (roundOverBellPlayer != null) roundOverBellPlayer.start();

        handler.postDelayed(() -> {
            audioAnnouncer.announceText("Rest!", () -> {});
        }, 1000);

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
                    totalSecondsElapsed++;

                    if (isPreStart) {
                        // Pre-Start Phase
                        if (remainingSeconds <= 0) {
                            onTimerFinished();
                            return;
                        }
                    } else if (isResting) {
                        // Rest-Phase: "Get Ready!" 10 Sekunden vor Rundenstart
                        if (remainingSeconds == 10) {
                            audioAnnouncer.announceText("Get ready!", () -> {});
                            Log.d(TAG, "Get Ready für Runde " + (currentRound + 1));
                        }
                    } else {
                        // Training-Phase: Last-10-Bell und Kombinationen
                        if (remainingSeconds == 10 && lastTenBellPlayer != null) {
                            lastTenBellPlayer.start();
                        }

                        if (!audioAnnouncer.isAnnouncing()) {
                            if (nextAnnouncementIn > 0) nextAnnouncementIn--;

                            if (nextAnnouncementIn == 0 && remainingSeconds > 1) {
                                announceRandomCombination();
                                nextAnnouncementIn = -1;
                            }
                        }
                    }

                    updateUI();

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
            startRound();
        } else if (isResting) {
            currentRound++;
            if (currentRound <= workout.getNumberOfRounds()) {
                startRound();
            } else {
                finishWorkout();
            }
        } else {
            if (currentRound < workout.getNumberOfRounds()) {
                startRest();
            } else {
                if (roundOverBellPlayer != null) roundOverBellPlayer.start();
                handler.postDelayed(() -> finishWorkout(), 1000);
            }
        }
    }

    private void announceRandomCombination() {
        if (combinations.isEmpty()) return;

        Combination combo = combinations.get(random.nextInt(combinations.size()));
        txtCurrentCombination.setText(combo.getName() + "\n" + combo.getMovesAsString());

        Log.d(TAG, "Sage Kombination: " + combo.getName());

        audioAnnouncer.announceCombination(combo.getMoves(), () -> {
            nextAnnouncementIn = workout.getAnnouncementInterval();
            Log.d(TAG, "Kombination fertig, nächste in " + nextAnnouncementIn + "s");
        });
    }

    private void updateUI() {
        int min = remainingSeconds / 60;
        int sec = remainingSeconds % 60;
        txtTimer.setText(String.format("%02d:%02d", min, sec));
        txtCurrentRound.setText("Round " + currentRound + " / " + workout.getNumberOfRounds());

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

        if (isPaused) {
            // Pausiert → Play-Icon anzeigen
            btnPauseResume.setIcon(getDrawable(R.drawable.ic_play));
            btnPauseResume.setContentDescription("Resume");
        } else {
            // Läuft → Pause-Icon anzeigen
            btnPauseResume.setIcon(getDrawable(R.drawable.ic_pause));
            btnPauseResume.setContentDescription("Pause");
        }
    }

    private void showStopConfirmDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Workout abbrechen?")
                .setMessage("Wirklich beenden?")
                .setPositiveButton("Ja", (d, w) -> {
                    // Speichere als "aborted" - current round minus 1 wenn in Runde, minus 0 wenn in Rest
                    int completedRounds = isResting ? currentRound - 1 : currentRound - 1;
                    saveHistory("aborted", Math.max(0, completedRounds));
                    finish();
                })
                .setNegativeButton("Nein", null)
                .show();
    }

    private void finishWorkout() {
        audioAnnouncer.announceText("Workout complete!", () -> {});

        // Speichere als "completed"
        saveHistory("completed", workout.getNumberOfRounds());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Workout fertig!")
                .setMessage("Du hast " + workout.getNumberOfRounds() + " Runden absolviert!")
                .setPositiveButton("OK", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private void saveHistory(String status, int roundsCompleted) {
        WorkoutHistory history = new WorkoutHistory(
                null,
                workout.getId(),
                workout.getName(),
                System.currentTimeMillis(),
                status,
                totalSecondsElapsed,
                roundsCompleted,
                workout.getNumberOfRounds()
        );

        historyManager.saveHistory(this, history, new HistoryManager.OnHistorySavedListener() {
            @Override
            public void onSuccess(WorkoutHistory savedHistory) {
                Log.d(TAG, "History gespeichert: " + status);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "History Fehler: " + error);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && timerRunnable != null) handler.removeCallbacks(timerRunnable);
        if (audioAnnouncer != null) audioAnnouncer.shutdown();
        if (openingBellPlayer != null) openingBellPlayer.release();
        if (lastTenBellPlayer != null) lastTenBellPlayer.release();
        if (roundOverBellPlayer != null) roundOverBellPlayer.release();
        Log.d(TAG, "WorkoutTimer beendet");
    }
}