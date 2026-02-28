package com.example.das_entrega_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gestorDB = new miDB(this, "Actividades", null, 1);

        startActivityIntent =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK) {
                                if (result.getData() != null) {
                                    String nombre  = result.getData().getStringExtra("nombre");
                                    if (nombre != null && !nombre.isEmpty()) {
                                        gestorDB.addNombre(nombre);
                                        eladaptador.addItem(nombre, R.drawable.togetchi1);
                                    }
                                }
                            }
                        });

        RecyclerView lista = findViewById(R.id.mrv);
        ArrayList<String> nombres = gestorDB.getNombres();
        ArrayList<Integer> personajes = new ArrayList<>(Collections.nCopies(nombres.size(), R.drawable.togetchi1));

        eladaptador = new MainRecyclerAdapter(nombres, personajes);
        lista.setAdapter(eladaptador);
        lista.setLayoutManager(new LinearLayoutManager(this));

        Button Addbutton = findViewById(R.id.Addbutton);
        Addbutton.setOnClickListener(view -> añadirActividad());
    }

    public void añadirActividad() {
        Intent intent = new Intent(this, AddActivity.class);
        startActivityIntent.launch(intent);
    }
}
