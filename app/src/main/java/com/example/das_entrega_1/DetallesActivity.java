package com.example.das_entrega_1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DetallesActivity extends AppCompatActivity {
    private static final double BILBAO_LAT = 43.2630;
    private static final double BILBAO_LON = -2.9350;

    private MapaFragment gMap;
    private long actividadId = -1;
    private double latOriginal;
    private double lonOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeHelper.getThemeStyle(this)); // Aplicar colores
        ThemeHelper.applySettings(this); // Aplicar modo
        super.onCreate(savedInstanceState);
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
                Manifest.permission.ACCESS_FINE_LOCATION
        });

        gMap = (MapaFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentoMapa);
        Button bButton = findViewById(R.id.BorrarButton);
        Button aButton = findViewById(R.id.Aceptarbutton);

        // Comprobar si estamos editando
        Intent intent = getIntent();
        actividadId = intent.getLongExtra("actividad_id", -1);
        if (actividadId != -1) {
            bButton.setOnClickListener(view -> Borrar());
            aButton.setOnClickListener(view -> Aceptar());

            Data input = new Data.Builder()
                    .putString("accion", miDBRemota.ACCION_GET_ID)
                    .putLong("id", actividadId)
                    .build();

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(miDBRemota.class)
                    .setInputData(input).build();

            WorkManager.getInstance(this).enqueue(request);

            LiveData<WorkInfo> liveData = WorkManager.getInstance(this)
                    .getWorkInfoByIdLiveData(request.getId());

            Observer<WorkInfo>[] observerRef = new Observer[1];
            observerRef[0] = workInfo -> {
                Log.d("workinfo", String.valueOf(workInfo));
                if (workInfo != null && workInfo.getState().isFinished()) {
                    liveData.removeObserver(observerRef[0]);
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        try {
                            String actividadJson = workInfo.getOutputData().getString("actividad");
                            JSONObject obj = new JSONObject(actividadJson);

                            String nombre = obj.getString("nombre");
                            double distancia = obj.getDouble("distancia");
                            double duracion = obj.getDouble("duracion");
                            String descripcion = obj.optString("descripcion", "");
                            latOriginal = obj.getDouble("latitud");
                            lonOriginal = obj.getDouble("longitud");

                            ((EditText) findViewById(R.id.nombreInput)).setText(nombre);
                            ((EditText) findViewById(R.id.kmInput)).setText(String.valueOf(distancia));
                            ((EditText) findViewById(R.id.horasInput)).setText(String.valueOf((int)(duracion / 3600)));
                            ((EditText) findViewById(R.id.minutosInput)).setText(String.valueOf((int)((duracion % 3600) / 60)));
                            ((EditText) findViewById(R.id.descripcionInput)).setText(descripcion);
                            gMap.centrar(latOriginal, lonOriginal);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            liveData.observeForever(observerRef[0]);
        }
        else{ // Logica para añadir una actividad nueva
            bButton.setText(R.string.cancelar);
            aButton.setText(R.string.crear);

            // Se usan las coordenadas de Bilbao como placeholder, para evitar errores por null
            aButton.setOnClickListener(view -> Aceptar());
            bButton.setOnClickListener(view -> Volver());
            
            latOriginal = BILBAO_LAT;
            lonOriginal = BILBAO_LON;
            gMap.centrar(latOriginal, lonOriginal);
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
                if (gMap != null) {
                    gMap.activarUbicacionYCentrar();
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

        Double duracionSeg = (double) (horas * 3600L + minutos * 60L);

        Intent intent = new Intent();
        intent.putExtra("nombre", valorNombre);
        intent.putExtra("distancia", km);
        intent.putExtra("duracion", duracionSeg);
        intent.putExtra("descripcion", valorDescripcion);

        // Obtener la ubicación del mapa
        double currentLat = latOriginal;
        double currentLon = lonOriginal;
        
        LatLng pos = gMap.getPosicionActual();
        if (pos != null) {
            currentLat = pos.latitude;
            currentLon = pos.longitude;
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
