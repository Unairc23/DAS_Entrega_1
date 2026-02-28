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
                "'Nombre' VARCHAR(255))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Actividades");
        onCreate(db);
    }

    public void addNombre(String nombre) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Nombre", nombre);
        db.insert("Actividades", null, values);
        db.close();
    }

    public ArrayList<String> getNombres() {
        ArrayList<String> listaNombres = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Nombre FROM Actividades", null);
        if (cursor.moveToFirst()) {
            do {
                listaNombres.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listaNombres;
    }

    public void deleteNombre(String nombre) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {nombre};
        db.delete("Actividades", "Nombre = ?", args);
        db.close();
    }
}
