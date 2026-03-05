package com.example.das_entrega_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

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
                "'Longitud' REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Actividades");
        onCreate(db);
    }

    public long addActividad(String nombre, double latitud, double longitud) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Nombre", nombre);
        values.put("Latitud", latitud);
        values.put("Longitud", longitud);
        long id = db.insert("Actividades", null, values);
        db.close();
        return id;
    }

    public ArrayList<Actividad> getActividades() {
        ArrayList<Actividad> listaActividades = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Codigo, Nombre, Latitud, Longitud FROM Actividades", null);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String nombre = cursor.getString(1);
                double latitud = cursor.getDouble(2);
                double longitud = cursor.getDouble(3);
                listaActividades.add(new Actividad(id, nombre, latitud, longitud));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listaActividades;
    }

    public Actividad getActividadPorId(long id) {
        Actividad actividad = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Codigo, Nombre, Latitud, Longitud FROM Actividades WHERE Codigo = " + id, null);
        if (cursor.moveToFirst()) {
            String nombre = cursor.getString(1);
            double latitud = cursor.getDouble(2);
            double longitud = cursor.getDouble(3);
            actividad = new Actividad(id, nombre, latitud, longitud);
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
