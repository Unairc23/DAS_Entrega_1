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

public class AddActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map;
    private MyLocationNewOverlay mLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapaHelper.init(this);
        LocaleHelper.onAttach(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);

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

        // Cargar mapa y esperar a recibir ubicacion para actualizarlo
        map = findViewById(R.id.map);
        map.getOverlays().add(mLocationOverlay);
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        mLocationOverlay.enableMyLocation();

        mLocationOverlay.runOnFirstFix(() -> {
            runOnUiThread(() -> {
                GeoPoint myLocation = mLocationOverlay.getMyLocation();
                if (myLocation != null) {
                    MapaHelper.basicConfig(map, myLocation.getLatitude(), myLocation.getLongitude(), 18.0, true);
                }
            });
        });

        Button aButton = findViewById(R.id.Aceptarbutton);
        aButton.setOnClickListener(view -> Aceptar());
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
        String valor = nombreInput.getText().toString();
        Intent intent = new Intent();
        intent.putExtra("nombre", valor);

        if (mLocationOverlay != null) {
            GeoPoint myLocation = mLocationOverlay.getMyLocation();
            if (myLocation != null) {
                intent.putExtra("latitud", myLocation.getLatitude());
                intent.putExtra("longitud", myLocation.getLongitude());
            }
        }
        setResult(RESULT_OK, intent);
        finish();
    }
}
