package com.example.das_entrega_1;

import static android.content.Context.MODE_PRIVATE;

import static com.example.das_entrega_1.WidgetHelper.ejecutarActualizacion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;

import java.util.concurrent.atomic.AtomicBoolean;

public class ActividadWidget extends AppWidgetProvider {
    static final long INTERVAL_MS = 5000;


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        long savedUserId = prefs.getLong("userId", -1);
        Log.d("miWidget", "updateAppWidget llamado, userId=" + savedUserId);

        ejecutarActualizacion(context, savedUserId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("miWidget", "Esta en onUpdate, widgets=" + appWidgetIds.length);
        if (appWidgetIds.length > 0) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[0]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.d("miWidget", "onEnabled llamado");

        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        long savedUserId = prefs.getLong("userId", -1);
        ejecutarActualizacion(context, savedUserId);
        programarAlarma(context, savedUserId);
    }

    @Override
    public void onDisabled(Context context) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, WidgetHelper.class);
        intent.setAction("com.example.das_entrega_1.UPDATE_WIDGET");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    static void programarAlarma(Context context, long userId) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, WidgetHelper.class);
        intent.setAction("com.example.das_entrega_1.UPDATE_WIDGET");
        intent.putExtra("userId", userId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis() + INTERVAL_MS,
                INTERVAL_MS,
                pendingIntent
        );
    }
}