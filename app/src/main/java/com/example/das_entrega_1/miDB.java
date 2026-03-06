package com.example.das_entrega_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

public class miDB extends SQLiteOpenHelper {
    public miDB(@Nullable Context context, @Nullable String name,
                @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE Actividades (" +
                "'Codigo' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "'Nombre' VARCHAR(255), " +
                "'Latitud' REAL, " +
                "'Longitud' REAL, " +
                "'Descripcion' VARCHAR(255), " +
                "'Fecha' DATE, " +
                "'Distancia' REAL, " +
                "'Duracion' REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Actividades");
        onCreate(db);
    }

    public long addActividad(String nombre, double latitud, double longitud, String descripcion, double distancia, double duracion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Nombre", nombre);
        values.put("Latitud", latitud);
        values.put("Longitud", longitud);
        values.put("Fecha", System.currentTimeMillis());
        values.put("Descripcion", descripcion);
        values.put("Distancia", distancia);
        values.put("Duracion", duracion);
        long id = db.insert("Actividades", null, values);
        db.close();
        return id;
    }

    public ArrayList<Actividad> getActividades() {
        ArrayList<Actividad> listaActividades = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Codigo, Nombre, Latitud, Longitud, Descripcion, Fecha, Distancia, Duracion FROM Actividades", null);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String nombre = cursor.getString(1);
                double latitud = cursor.getDouble(2);
                double longitud = cursor.getDouble(3);
                String descripcion = cursor.getString(4);
                Date fecha = new Date(cursor.getLong(5));
                double distancia = cursor.getDouble(6);
                double duracion = cursor.getDouble(7);
                listaActividades.add(new Actividad(id, nombre, latitud, longitud, distancia, duracion, descripcion, fecha));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listaActividades;
    }

    public Actividad getActividadPorId(long id) {
        Actividad actividad = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Codigo, Nombre, Latitud, Longitud, Descripcion, Fecha, Distancia, Duracion FROM Actividades WHERE Codigo = " + id, null);
        if (cursor.moveToFirst()) {
            String nombre = cursor.getString(1);
            double latitud = cursor.getDouble(2);
            double longitud = cursor.getDouble(3);
            String descripcion = cursor.getString(4);
            Date fecha = new Date(cursor.getLong(5));
            double distancia = cursor.getDouble(6);
            double duracion = cursor.getDouble(7);
            actividad = new Actividad(id, nombre, latitud, longitud, distancia, duracion, descripcion, fecha);
        }
        cursor.close();
        db.close();
        return actividad;
    }

    public void deleteActividadPorId(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Actividades", "Codigo=?", new String[]{String.valueOf(id)});
        db.close();
    }
}
