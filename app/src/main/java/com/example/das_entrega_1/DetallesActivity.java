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
    private static final double BILBAO_LAT = 43.2630;
    private static final double BILBAO_LON = -2.9350;

    private MapView map;
    private MyLocationNewOverlay mLocationOverlay;
    private miDB gestorDB;
    private long actividadId = -1;
    private double latOriginal;
    private double lonOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeHelper.getThemeStyle(this)); // Aplicar colores
        ThemeHelper.applySettings(this); // Aplicar modo
        super.onCreate(savedInstanceState);
        MapaHelper.init(this); // Inicializar mapa
        LocaleHelper.onAttach(this); // Aplicar idioma
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
        if (actividadId != -1){ // -1 en caso de no existir el campo extra para id, por lo tanto actividad nueva

            bButton.setOnClickListener(view -> Borrar());
            aButton.setOnClickListener(view -> Aceptar());

            gestorDB = new miDB(this, "Actividades", null, 1);
            Actividad actividad = gestorDB.getActividadPorId(actividadId);
            // Rellenar los campos para poder actualizarlos
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

            // Se usan las coordenadas de Bilbao como placeholder, para evitar errores por null
            aButton.setOnClickListener(view -> Aceptar());
            bButton.setOnClickListener(view -> Volver());
            latOriginal = BILBAO_LAT;
            lonOriginal = BILBAO_LON;

            MapaHelper.basicConfig(map, latOriginal, lonOriginal, 15.0, true);

            mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLocationOverlay.enableMyLocation();
            }

            // Cuando se reciben las coordenadas se actualiza el mapa
            mLocationOverlay.runOnFirstFix(() -> {
                runOnUiThread(() -> {
                    // Hay veces que se runea esta parte cuando la pantalla se ha cerrado, este if evita el error
                    if (!isFinishing() && map != null) {
                        GeoPoint myLocation = mLocationOverlay.getMyLocation();
                        if (myLocation != null) {
                            latOriginal = myLocation.getLatitude();
                            lonOriginal = myLocation.getLongitude();
                            MapaHelper.basicConfig(map, latOriginal, lonOriginal, 18.0, true);
                        }
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
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mLocationOverlay != null) {
                    mLocationOverlay.enableMyLocation();
                }
            } else {
                // Si deniegan los permisos y es una actividad nueva se usan las coordenadas de bilbao
                if (actividadId == -1) {
                    MapaHelper.basicConfig(map, BILBAO_LAT, BILBAO_LON, 15.0, true);
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

        if (valorNombre.isEmpty()) {
            nombreInput.setError(getString(R.string.errorNombre));
            return;
        }

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

    public void confirmarBorrado() { // Metodo al que llama BorrarFragment
        Intent intent = new Intent();
        intent.putExtra("borrar", true);
        intent.putExtra("actividad_id", actividadId);
        setResult(RESULT_OK, intent);
        finish();
    }
}