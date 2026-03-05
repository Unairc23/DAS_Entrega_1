package com.example.das_entrega_1;

public class Actividad {
    private long id;
    private String nombre;
    private double lat;
    private double lon;


    public Actividad(long id, String nombre, double lat, double lon) {
        this.id = id;
        this.nombre = nombre;
        this.lat = lat;
        this.lon = lon;
    }

    public long getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public void update(String nombre, double lat, double lon){
        this.nombre = nombre;
        this.lat = lat;
        this.lon = lon;
    }
}
