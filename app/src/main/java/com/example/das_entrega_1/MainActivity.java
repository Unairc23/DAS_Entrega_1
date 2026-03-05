package com.example.das_entrega_1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private MainRecyclerAdapter eladaptador;
    private ActivityResultLauncher<Intent> startActivityIntent;
    private miDB gestorDB;
    private ArrayList<Actividad> actividades;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                                        String nombre = result.getData().getStringExtra("nombre");
                                        double latitud = result.getData().getDoubleExtra("latitud", 0.0);
                                        double longitud = result.getData().getDoubleExtra("longitud", 0.0);
                                        if (nombre != null && !nombre.isEmpty()) {
                                            long id = gestorDB.addActividad(nombre, latitud, longitud);
                                            Actividad nuevaActividad = new Actividad(id, nombre, latitud, longitud);
                                            eladaptador.addItem(nuevaActividad);
                                        }
                                    }
                                    else{
                                        long id = result.getData().getLongExtra("actividad_id", -1);
                                        for (int i = 0; i < actividades.size(); i++) {
                                            if (actividades.get(i).getId() == id) {
                                                eliminarElemento(i);
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

        Button Addbutton = findViewById(R.id.Addbutton);
        Addbutton.setOnClickListener(view -> añadirActividad());
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

    public void añadirActividad() {
        Intent intent = new Intent(this, AddActivity.class);
        startActivityIntent.launch(intent);
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

    public void abrirConfig(){
        Intent intent = new Intent(this, ConfigActivity.class);
        startActivityIntent.launch(intent);
    }
}
