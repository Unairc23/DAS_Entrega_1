package com.example.das_entrega_1;

import static android.content.Context.MODE_PRIVATE;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Locale;

public class WidgetHelper extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("miWidget", "entra al broadcast");

        long userId = intent.getLongExtra("userId", -1);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName tipowidget = new ComponentName(context, ActividadWidget.class);
        int[] appWidgetIds = manager.getAppWidgetIds(tipowidget);

        if (appWidgetIds.length == 0) {
            return; // Si no hay ids es que no hay widgets que actualizar
        }

        ejecutarActualizacion(context, userId);
    }

    public static void ejecutarActualizacion(Context context, long userId) {
        Log.d("miWidget", "ejecutarActualizacion llamado, userId=" + userId);

        // Datos guardados en prefs para evitar problemas con Workers y trabajos en segundo plano
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        float distancia = prefs.getFloat("distancia", 0f);
        float duracion = prefs.getFloat("duracion", 0f);

        long horas = (long) (duracion / 3600);
        long minutos = (long) ((duracion % 3600) / 60);

        RemoteViews remoteViews= new RemoteViews(context.getPackageName(),R.layout.actividad_widget);

        remoteViews.setTextViewText(R.id.distanciaText, String.format(Locale.getDefault(), "%.2f km", distancia));
        remoteViews.setTextViewText(R.id.tiempoText, String.format(Locale.getDefault(), "%02d:%02d", horas, minutos));

        ComponentName tipowidget = new ComponentName(context, ActividadWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        // Llamar al widget para que actualice
        manager.updateAppWidget(tipowidget, remoteViews);
    }
}