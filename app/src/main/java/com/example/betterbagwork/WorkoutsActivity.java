package com.example.betterbagwork;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WorkoutsActivity extends AppCompatActivity {

    private TextView txtPlaceholder;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts);

        // Views initialisieren
        txtPlaceholder = findViewById(R.id.txtPlaceholder);
        btnBack = findViewById(R.id.btnBack);

        // Zurück-Button
        btnBack.setOnClickListener(v -> finish());

        // Platzhalter-Text
        txtPlaceholder.setText("Workout-Funktion kommt hier rein!\n\nBald kannst du hier:\n• Workouts erstellen\n• Timer starten\n• Kombinationen ansagen lassen");
    }
}