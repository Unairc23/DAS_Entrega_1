package com.example.das_entrega_1;

import android.content.Context;
import android.preference.PreferenceManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapaHelper {

    public static void init(Context context) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
    }

    // Configurar el mapa para poder interactuar con el, centrarse en la ubi del movil etc.
    public static void basicConfig(MapView map, double lat, double lon, double zoom, boolean interactive) {
        map.setMultiTouchControls(interactive);
        IMapController mapController = map.getController();
        mapController.setZoom(zoom);
        GeoPoint point = new GeoPoint(lat, lon);
        mapController.setCenter(point);

        // Añadir un marcador en el punto
        Marker startMarker = new Marker(map);
        startMarker.setPosition(point);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
    }
}