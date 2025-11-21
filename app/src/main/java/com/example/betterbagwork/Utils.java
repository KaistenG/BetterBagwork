package com.example.betterbagwork;

import android.content.Context;
import android.widget.Toast;

public class Utils {

    // Einfaches Toast anzeigen
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // E-Mail prüfen
    public static boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Passwort prüfen (mindestens 6 Zeichen z.B.)
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
