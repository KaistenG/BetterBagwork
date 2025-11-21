package com.example.betterbagwork;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseHelper {

    private FirebaseAuth auth;

    public FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
    }

    // --- LOGIN ---
    public void loginUser(Context context, String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Bitte E-Mail und Passwort eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Login erfolgreich", Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(context, MainActivity.class));
                        if (context instanceof LoginActivity) {
                            ((LoginActivity) context).finish();
                        }
                    } else {
                        Toast.makeText(context, "Login fehlgeschlagen: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- REGISTRIERUNG ---
    public void registerUser(Context context, String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Bitte E-Mail und Passwort eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Registrierung erfolgreich", Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(context, MainActivity.class));
                        if (context instanceof RegisterActivity) {
                            ((RegisterActivity) context).finish();
                        }
                    } else {
                        Toast.makeText(context, "Registrierung fehlgeschlagen: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- LOGOUT ---
    public static void logout(Context context) {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(context, "Erfolgreich ausgeloggt", Toast.LENGTH_SHORT).show();
        context.startActivity(new Intent(context, LoginActivity.class));
        // Falls die aufrufende Activity nicht beendet wird, kann man casten:
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).finish();
        }
    }

    // --- Status prüfen ---
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
}

