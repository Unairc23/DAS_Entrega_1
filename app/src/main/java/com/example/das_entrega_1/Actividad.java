package com.example.das_entrega_1;

import java.sql.Time;
import java.util.Date;

public class Actividad {
    private long id;
    private String nombre;
    private String descripcion;
    private double lat;
    private double lon;
    private double distancia;
    private double duracion;
    private Date fecha;

    public Actividad(long id, String nombre, double lat, double lon, double distancia, double duracion, String descripcion, Date fecha) {
        this.id = id;
        this.nombre = nombre;
        this.lat = lat;
        this.lon = lon;
        this.distancia = distancia;
        this.duracion = duracion;
        this.descripcion = descripcion;
        if (fecha != null) {
            this.fecha = fecha;
        } else {
            this.fecha = new Date();
            fecha.setTime(System.currentTimeMillis());
        }
    }

    public long getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getDescripcion() { return descripcion; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public double getDistancia() { return distancia; }
    public double getDuracion() { return duracion; }
    public Date getFecha() { return fecha; }

    public void update(String nombre, double lat, double lon, double distancia, double duracion, String descripcion){
        this.nombre = nombre;
        this.lat = lat;
        this.lon = lon;
        this.distancia = distancia;
        this.duracion = duracion;
        this.descripcion = descripcion;
    }
}
