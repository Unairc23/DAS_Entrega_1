package com.example.das_entrega_1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap elmapa;
    private Double lon;
    private Double lat;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        elmapa = googleMap;
        elmapa.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        elmapa.setBuildingsEnabled(true);

        activarUbicacionYCentrar();

        if (lat != null && lon != null) {
            centrar(lat, lon);
            lat = null;
            lon = null;
        }
    }

    public void activarUbicacionYCentrar() {
        if (elmapa != null) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                elmapa.setMyLocationEnabled(true);
                elmapa.getUiSettings().setMyLocationButtonEnabled(true);

                // Obtener la ubicación y centrar
                fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            centrar(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
            }
        }
    }

    public void centrar(double lat, double lon) {
        LatLng nuevaPosicion = new LatLng(lat, lon);
        CameraUpdate actualizar = CameraUpdateFactory.newLatLngZoom(nuevaPosicion, 15);
        if (elmapa != null) {
            elmapa.animateCamera(actualizar);
            elmapa.clear();
            elmapa.addMarker(new MarkerOptions().position(nuevaPosicion));
        } else {
            this.lat = lat;
            this.lon = lon;
        }
    }

    public LatLng getPosicionActual() {
        if (elmapa != null) {
            return elmapa.getCameraPosition().target;
        }
        return null;
    }
}