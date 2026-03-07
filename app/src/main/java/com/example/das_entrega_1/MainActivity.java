package com.example.das_entrega_1;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MainRecyclerAdapter eladaptador;
    private ActivityResultLauncher<Intent> startActivityIntent;
    private miDB gestorDB;
    private ArrayList<Actividad> actividades;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeHelper.getThemeStyle(this));
        ThemeHelper.applySettings(this);
        super.onCreate(savedInstanceState);
        LocaleHelper.onAttach(this);
        MapaHelper.init(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(findViewById(R.id.toolbar));

        gestorDB = new miDB(this, "Actividades", null, 1);

        startActivityIntent =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK) {
                                if (result.getData() != null) {
                                    boolean borrar = result.getData().getBooleanExtra("borrar", false);
                                    if (!borrar){
                                        if (result.getData().hasExtra("actividad_id")) {
                                            long id = result.getData().getLongExtra("actividad_id", -1);
                                            actualizarActividad(id, result.getData());
                                        } else {
                                            añadirActividad(result.getData());
                                        }
                                    }
                                    else{
                                        long id = result.getData().getLongExtra("actividad_id", -1);
                                        for (int i = 0; i < actividades.size(); i++) {
                                            if (actividades.get(i).getId() == id) {
                                                eliminarElemento(i);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        });

        RecyclerView lista = findViewById(R.id.mrv);
        actividades = gestorDB.getActividades();

        eladaptador = new MainRecyclerAdapter(actividades, this);
        lista.setAdapter(eladaptador);
        lista.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton Addbutton = findViewById(R.id.Addbutton);
        Addbutton.setOnClickListener(view -> llamarAñadirActividad());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tool, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.opciones) {
            abrirConfig();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    public void llamarAñadirActividad() {
        Intent intent = new Intent(this, DetallesActivity.class);
        startActivityIntent.launch(intent);
    }

    private void actualizarActividad(long id, Intent data) {
        String nombre = data.getStringExtra("nombre");
        String descripcion = data.getStringExtra("descripcion");
        double latitud = data.getDoubleExtra("latitud", 0.0);
        double longitud = data.getDoubleExtra("longitud", 0.0);
        double distancia = data.getDoubleExtra("distancia", 0.0);
        long duracion = data.getLongExtra("duracion", 0);

        gestorDB.updateActividad(id, nombre, latitud, longitud, descripcion, distancia, (double) duracion);

        for (int i = 0; i < actividades.size(); i++) {
            if (actividades.get(i).getId() == id) {
                actividades.get(i).update(nombre, latitud, longitud, distancia, (double) duracion, descripcion);
                eladaptador.notifyItemChanged(i);
                break;
            }
        }
    }

    public void añadirActividad(Intent data){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
            }
        }

        String nombre = data.getStringExtra("nombre");
        String descripcion = data.getStringExtra("descripcion");
        double latitud = data.getDoubleExtra("latitud", 0.0);
        double longitud = data.getDoubleExtra("longitud", 0.0);
        double distancia = data.getDoubleExtra("distancia", 0.0);
        long duracion = data.getLongExtra("duracion", 0);
        if (nombre != null && !nombre.isEmpty()) {
            long id = gestorDB.addActividad(nombre, latitud, longitud, descripcion, distancia, (double) duracion);
            notificar();
            Actividad nuevaActividad = new Actividad(id, nombre, latitud, longitud, distancia, (double) duracion, descripcion, null);
            eladaptador.addItem(nuevaActividad);
        }
    }

    public void notificar(){
        NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String idCanal = "IdCanal";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal = new NotificationChannel(idCanal, "NombreCanal", NotificationManager.IMPORTANCE_DEFAULT);
            elCanal.setDescription("Descripción del canal");
            elCanal.enableLights(true);
            elCanal.setLightColor(Color.RED);
            elManager.createNotificationChannel(elCanal);
        }

        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, idCanal)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getString(R.string.notificacionAñadida))
                .setContentText(getString(R.string.notificacionDesc))
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setAutoCancel(true);

        elManager.notify(1, elBuilder.build());
    }

    public void eliminarElemento(int position) {
        Actividad actividadAEliminar = actividades.get(position);
        gestorDB.deleteActividadPorId(actividadAEliminar.getId());
        eladaptador.removeItem(position);
    }

    public void abrirDetalles(int position) {
        Intent intent = new Intent(this, DetallesActivity.class);
        Actividad actividadSeleccionada = actividades.get(position);
        intent.putExtra("actividad_id", actividadSeleccionada.getId());
        startActivityIntent.launch(intent);
    }

    public void abrirConfig() {
        Intent intent = new Intent(this, ConfigActivity.class);
        startActivityIntent.launch(intent);
    }
}
