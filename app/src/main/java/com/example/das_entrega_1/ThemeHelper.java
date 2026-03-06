package com.example.das_entrega_1;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {

    private static final String SELECTED_COLOR = "Theme.Helper.Selected.Color";
    private static final String SELECTED_MODE = "Theme.Helper.Selected.Mode";

    public static int getThemeStyle(Context context) {
        String color = getColor(context);
        if (color.equals("azul")) {
            return R.style.Theme_Azul;
        } else if (color.equals("rojo")) {
            return R.style.Theme_Rojo;
        } else {
            return R.style.Theme_DAS_Entrega_1;
        }
    }

    public static String getColor(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_COLOR, "base");
    }

    public static String getMode(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_MODE, "claro");
    }

    public static void applySettings(Context context) {
        String mode = getMode(context);
        if (mode.equals("oscuro")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void setSettings(Context context, String color, String mode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_COLOR, color);
        editor.putString(SELECTED_MODE, mode);
        editor.apply();

        if (mode.equals("oscuro")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
