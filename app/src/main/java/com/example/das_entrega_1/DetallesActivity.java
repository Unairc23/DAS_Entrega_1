package com.example.das_entrega_1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class DetallesActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map;
    private MyLocationNewOverlay mLocationOverlay;
    private miDB gestorDB;
    private long actividadId = -1;
    private double latOriginal = 0.0;
    private double lonOriginal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeHelper.getThemeStyle(this));
        ThemeHelper.applySettings(this);
        super.onCreate(savedInstanceState);
        MapaHelper.init(this);
        LocaleHelper.onAttach(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalles);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Permisos
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        map = findViewById(R.id.map);
        Button bButton = findViewById(R.id.BorrarButton);
        Button aButton = findViewById(R.id.Aceptarbutton);
        
        // Comprobar si estamos editando
        Intent intent = getIntent();
        actividadId = intent.getLongExtra("actividad_id", -1);
        if (actividadId != -1){ // Logica para actualizar actividad

            bButton.setOnClickListener(view -> Borrar());
            aButton.setOnClickListener(view -> Aceptar());

            gestorDB = new miDB(this, "Actividades", null, 1);
            Actividad actividad = gestorDB.getActividadPorId(actividadId);
            if (actividad != null){
                EditText nombreInput = findViewById(R.id.nombreInput);
                nombreInput.setText(actividad.getNombre());

                EditText kmInput = findViewById(R.id.kmInput);
                kmInput.setText(String.valueOf(actividad.getDistancia()));

                EditText horasInput = findViewById(R.id.horasInput);
                int horas = (int) (actividad.getDuracion() / 3600);

                EditText minutosInput = findViewById(R.id.minutosInput);
                int minutos = (int) ((actividad.getDuracion() % 3600) / 60);

                horasInput.setText(String.valueOf(horas));
                minutosInput.setText(String.valueOf(minutos));

                EditText descripcionInput = findViewById(R.id.descripcionInput);
                descripcionInput.setText(actividad.getDescripcion());

                latOriginal = actividad.getLat();
                lonOriginal = actividad.getLon();

                MapaHelper.basicConfig(map, latOriginal, lonOriginal, 18.0, true);
            }
        }
        else{ // Logica para añadir una actividad nueva

            bButton.setText(R.string.cancelar);
            aButton.setText(R.string.crear);

            // Cargar mapa y esperar a recibir ubicacion para actualizarlo
            mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
            mLocationOverlay.enableMyLocation();

            mLocationOverlay.runOnFirstFix(() -> {
                runOnUiThread(() -> {
                    GeoPoint myLocation = mLocationOverlay.getMyLocation();
                    if (myLocation != null) {
                        MapaHelper.basicConfig(map, myLocation.getLatitude(), myLocation.getLongitude(), 18.0, true);
                        aButton.setOnClickListener(view -> Aceptar()); // Esperar a que exista una ubicacion para no devolver null
                        bButton.setOnClickListener(view -> Volver());
                    }
                });
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (map != null) {
            map.onDetach();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mLocationOverlay != null) {
                    mLocationOverlay.enableMyLocation();
                }
            }
        }
    }

    public void Aceptar(){
        EditText nombreInput = findViewById(R.id.nombreInput);
        EditText kmInput = findViewById(R.id.kmInput);
        EditText horasInput = findViewById(R.id.horasInput);
        EditText minutosInput = findViewById(R.id.minutosInput);
        EditText descripcionInput = findViewById(R.id.descripcionInput);

        String valorNombre = nombreInput.getText().toString();
        String valorKm = kmInput.getText().toString();
        String valorHoras = horasInput.getText().toString();
        String valorMinutos = minutosInput.getText().toString();
        String valorDescripcion = descripcionInput.getText().toString();

        double km = 0.0;
        try {
            km = Double.parseDouble(valorKm);
        } catch (NumberFormatException e) {
            km = 0.0;
        }

        int horas = 0;
        try {
            horas = Integer.parseInt(valorHoras);
        } catch (NumberFormatException e) {
            horas = 0;
        }

        int minutos = 0;
        try {
            minutos = Integer.parseInt(valorMinutos);
        } catch (NumberFormatException e) {
            minutos = 0;
        }

        long duracionSeg = (horas * 3600L + minutos * 60L);

        Intent intent = new Intent();
        intent.putExtra("nombre", valorNombre);
        intent.putExtra("distancia", km);
        intent.putExtra("duracion", duracionSeg);
        intent.putExtra("descripcion", valorDescripcion);

        double currentLat = latOriginal;
        double currentLon = lonOriginal;

        if (mLocationOverlay != null) {
            GeoPoint myLocation = mLocationOverlay.getMyLocation();
            if (myLocation != null) {
                currentLat = myLocation.getLatitude();
                currentLon = myLocation.getLongitude();
            }
        }
        
        intent.putExtra("latitud", currentLat);
        intent.putExtra("longitud", currentLon);

        if (actividadId != -1) {
            intent.putExtra("actividad_id", actividadId);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    public void Volver(){
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    public void Borrar(){
        BorrarFragment dialog = new BorrarFragment();
        dialog.show(getSupportFragmentManager(), "BorrarDialog");
    }

    public void confirmarBorrado() {
        Intent intent = new Intent();
        intent.putExtra("borrar", true);
        intent.putExtra("actividad_id", actividadId);
        setResult(RESULT_OK, intent);
        finish();
    }
}
