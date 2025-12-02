package com.example.betterbagwork;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
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
                        // Spezifische Fehlermeldungen basierend auf Firebase Error
                        String errorMessage = getLoginErrorMessage(task.getException());
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
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
                        // Spezifische Fehlermeldungen für Registrierung
                        String errorMessage = getRegisterErrorMessage(task.getException());
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
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

    // --- Hilfsmethoden für benutzerfreundliche Fehlermeldungen ---

    private String getLoginErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            return "Kein Account mit dieser E-Mail gefunden";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Falsches Passwort";
        } else if (exception != null && exception.getMessage() != null) {
            if (exception.getMessage().contains("network")) {
                return "Keine Internetverbindung";
            } else if (exception.getMessage().contains("badly formatted")) {
                return "Ungültiges E-Mail-Format";
            }
        }
        return "Login fehlgeschlagen. Bitte versuche es erneut.";
    }

    private String getRegisterErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            return "Passwort zu schwach. Mindestens 6 Zeichen erforderlich.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Ungültiges E-Mail-Format";
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            return "E-Mail bereits registriert";
        } else if (exception != null && exception.getMessage() != null) {
            if (exception.getMessage().contains("network")) {
                return "Keine Internetverbindung";
            }
        }
        return "Registrierung fehlgeschlagen. Bitte versuche es erneut.";
    }
}