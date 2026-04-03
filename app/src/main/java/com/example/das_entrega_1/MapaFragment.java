package com.example.das_entrega_1;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap elmapa;
    private Double lon;
    private Double lat;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indica que queremos cargar el mapa
        getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        elmapa = googleMap;
        elmapa.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        elmapa.setBuildingsEnabled(true);

        if (lat != null && lon != null) {
            centrar(lat, lon);
            lat = null;
            lon = null;
        }
    }

    public void centrar(double lat, double lon) {
        LatLng nuevaPosicion = new LatLng(lat, lon);
        CameraUpdate actualizar = CameraUpdateFactory.newLatLngZoom(nuevaPosicion,15);
        if (elmapa != null){
            elmapa.moveCamera(actualizar);
            elmapa.addMarker(new MarkerOptions().position(nuevaPosicion));
        }
        else{
            this.lat = lat;
            this.lon = lon;
        }
    }
}