package com.example.das_entrega_1;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {

    private static final String SELECTED_THEME = "Theme.Helper.Selected.Theme";

    public static String getTheme(Context context) {
        return getPersistedTheme(context, "claro");
    }

    public static void setTheme(Context context, String theme) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_THEME, theme);
        editor.apply();
        if (theme.equals("oscuro")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private static String getPersistedTheme(Context context, String defaultTheme) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_THEME, defaultTheme);
    }
}
