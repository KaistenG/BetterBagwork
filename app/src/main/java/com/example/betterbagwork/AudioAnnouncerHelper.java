package com.example.betterbagwork;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AudioAnnouncerHelper {

    private static final String TAG = "AudioAnnouncer";
    private static final int OVERLAP_DELAY_MS = 570; // Zeit zwischen Schlag-Starts (anpassbar!)
    private static final int MAX_STREAMS = 10; // Maximale gleichzeitige Sounds

    private Context context;
    private SoundPool soundPool;
    private HashMap<String, Integer> soundIds; // Maps move name -> sound ID
    private HashMap<String, Integer> audioResources; // Maps move name -> R.raw.xxx
    private Handler handler;
    private OnAnnouncementCompleteListener listener;
    private boolean isAnnouncing = false;
    private AtomicInteger runningPlayers;
    private boolean isLoaded = false;
    private int loadedSoundsCount = 0;
    private int totalSoundsToLoad = 0;

    public AudioAnnouncerHelper(Context context) {
        this.context = context;
        this.handler = new Handler();
        this.soundIds = new HashMap<>();
        this.audioResources = new HashMap<>();
        this.runningPlayers = new AtomicInteger(0);

        initSoundPool();
        initAudioFiles();

        Log.d(TAG, "AudioAnnouncer erstellt (SoundPool Mode)");
    }

    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setFlags(AudioAttributes.FLAG_LOW_LATENCY) // Reduziert Audio-Latenz und Crunch
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(audioAttributes)
                .build();

        // Listener für Lade-Status
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                loadedSoundsCount++;
                Log.d(TAG, "Sound geladen: " + loadedSoundsCount + "/" + totalSoundsToLoad);

                if (loadedSoundsCount >= totalSoundsToLoad) {
                    isLoaded = true;
                    Log.d(TAG, "✅ Alle Sounds geladen!");
                }
            } else {
                Log.e(TAG, "❌ Fehler beim Laden von Sound ID: " + sampleId);
            }
        });
    }

    private void initAudioFiles() {
        audioResources = new HashMap<>();

        // === Schläge (13 Stück) ===
        audioResources.put("Jab", R.raw.jab);
        audioResources.put("Cross", R.raw.cross);
        audioResources.put("Left Hook", R.raw.left_hook);
        audioResources.put("Right Hook", R.raw.right_hook);
        audioResources.put("Left Uppercut", R.raw.left_uppercut);
        audioResources.put("Right Uppercut", R.raw.right_uppercut);
        audioResources.put("Teep", R.raw.teep);
        audioResources.put("Rear Kick", R.raw.rear_kick);
        audioResources.put("Switch Kick", R.raw.switch_kick);
        audioResources.put("Rear Knee", R.raw.rear_knee);
        audioResources.put("Switch Knee", R.raw.switch_knee);
        audioResources.put("Left Elbow", R.raw.left_elbow);
        audioResources.put("Right Elbow", R.raw.right_elbow);

        // === Runden (1-12) ===
        audioResources.put("Round 1", R.raw.round_one);
        audioResources.put("Round 2", R.raw.round_two);
        audioResources.put("Round 3", R.raw.round_three);
        audioResources.put("Round 4", R.raw.round_four);
        audioResources.put("Round 5", R.raw.round_five);
        audioResources.put("Round 6", R.raw.round_six);
        audioResources.put("Round 7", R.raw.round_seven);
        audioResources.put("Round 8", R.raw.round_eight);
        audioResources.put("Round 9", R.raw.round_nine);
        audioResources.put("Round 10", R.raw.round_ten);
        audioResources.put("Round 11", R.raw.round_eleven);
        audioResources.put("Round 12", R.raw.round_twelve);

        // === Status-Ansagen ===
        audioResources.put("Get ready!", R.raw.get_ready);
        audioResources.put("Rest!", R.raw.rest);
        audioResources.put("Workout complete!", R.raw.workout_complete);

        totalSoundsToLoad = audioResources.size();
        Log.d(TAG, "Lade " + totalSoundsToLoad + " Sounds...");

        // Alle Sounds in den SoundPool laden
        for (String key : audioResources.keySet()) {
            Integer resId = audioResources.get(key);
            if (resId != null) {
                int soundId = soundPool.load(context, resId, 1);
                soundIds.put(key, soundId);
            }
        }
    }

    public void announceCombination(List<String> moves, OnAnnouncementCompleteListener completeListener) {
        if (moves == null || moves.isEmpty()) {
            if (completeListener != null) completeListener.onComplete();
            return;
        }

        if (!isLoaded) {
            Log.w(TAG, "Sounds noch nicht geladen! Warte...");
            handler.postDelayed(() -> announceCombination(moves, completeListener), 100);
            return;
        }

        this.listener = completeListener;
        this.isAnnouncing = true;
        this.runningPlayers.set(moves.size());

        Log.d(TAG, "Starte Kombination (overlapping): " + moves.size() + " Schläge");

        // Starte alle Schläge nacheinander mit kurzem Delay
        for (int i = 0; i < moves.size(); i++) {
            final String move = moves.get(i);
            final int index = i;

            handler.postDelayed(() -> {
                playMoveOverlapping(move, index + 1, moves.size());
            }, i * OVERLAP_DELAY_MS);
        }

        // Berechne Gesamtdauer und setze Timeout
        int estimatedDuration = (moves.size() * OVERLAP_DELAY_MS) + 2000; // +2s Buffer
        handler.postDelayed(() -> {
            if (runningPlayers.get() > 0) {
                Log.w(TAG, "Timeout - erzwinge Completion");
                runningPlayers.set(0);
                isAnnouncing = false;
                if (listener != null) listener.onComplete();
            }
        }, estimatedDuration);
    }

    public void announceRound(int roundNumber, OnAnnouncementCompleteListener completeListener) {
        if (!isLoaded) {
            Log.w(TAG, "Sounds noch nicht geladen! Warte...");
            handler.postDelayed(() -> announceRound(roundNumber, completeListener), 100);
            return;
        }

        this.listener = completeListener;
        this.isAnnouncing = true;

        String roundKey = "Round " + roundNumber;
        Integer soundId = soundIds.get(roundKey);

        Log.d(TAG, "Runde " + roundNumber + " - Audio: " + (soundId != null ? "JA" : "NEIN"));

        if (soundId != null) {
            playAudioSingle(soundId, () -> {
                isAnnouncing = false;
                if (listener != null) listener.onComplete();
            });
        } else {
            // Kein Audio vorhanden - Überspringe
            Log.w(TAG, "Kein Audio für Runde " + roundNumber);
            isAnnouncing = false;
            if (listener != null) listener.onComplete();
        }
    }

    public void announceText(String text, OnAnnouncementCompleteListener completeListener) {
        if (!isLoaded) {
            Log.w(TAG, "Sounds noch nicht geladen! Warte...");
            handler.postDelayed(() -> announceText(text, completeListener), 100);
            return;
        }

        this.listener = completeListener;
        this.isAnnouncing = true;

        Integer soundId = soundIds.get(text);

        if (soundId != null) {
            playAudioSingle(soundId, () -> {
                isAnnouncing = false;
                if (listener != null) listener.onComplete();
            });
        } else {
            // Kein Audio - Überspringe
            Log.w(TAG, "Kein Audio für: " + text);
            isAnnouncing = false;
            if (listener != null) listener.onComplete();
        }
    }

    private void playMoveOverlapping(String move, int index, int total) {
        Integer soundId = soundIds.get(move);

        Log.d(TAG, "Schlag " + index + "/" + total + ": " + move);

        if (soundId != null) {
            try {
                // SoundPool.play: soundID, leftVolume, rightVolume, priority, loop, rate
                // Volume auf 0.7 reduziert um Clipping bei Overlapping zu vermeiden
                soundPool.play(soundId, 0.7f, 0.7f, 1, 0, 1.0f);

                // Schätze Sound-Dauer (ca. 700ms max) und plane Completion
                handler.postDelayed(() -> {
                    int remaining = runningPlayers.decrementAndGet();
                    Log.d(TAG, "Schlag fertig. Noch laufend: " + remaining);

                    if (remaining <= 0) {
                        isAnnouncing = false;
                        Log.d(TAG, "Kombination komplett");
                        if (listener != null) {
                            listener.onComplete();
                        }
                    }
                }, 800); // Max. erwartete Sound-Länge

            } catch (Exception e) {
                Log.e(TAG, "Audio-Fehler bei: " + move, e);
                runningPlayers.decrementAndGet();
            }
        } else {
            Log.w(TAG, "Audio fehlt für: " + move);
            runningPlayers.decrementAndGet();
        }
    }

    private void playAudioSingle(int soundId, Runnable onComplete) {
        try {
            // Volume auf 0.7 reduziert für Konsistenz
            soundPool.play(soundId, 0.7f, 0.7f, 1, 0, 1.0f);

            // Schätze Dauer für Single-Sounds (meist kürzer)
            handler.postDelayed(() -> {
                if (onComplete != null) {
                    onComplete.run();
                }
            }, 1000);

        } catch (Exception e) {
            Log.e(TAG, "Audio-Fehler", e);
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    public boolean isAnnouncing() {
        return isAnnouncing;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void shutdown() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        soundIds.clear();
        audioResources.clear();

        Log.d(TAG, "AudioAnnouncer beendet");
    }

    public interface OnAnnouncementCompleteListener {
        void onComplete();
    }
}