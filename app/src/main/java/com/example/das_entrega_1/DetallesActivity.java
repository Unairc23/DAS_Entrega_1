package com.example.das_entrega_1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetallesActivity extends AppCompatActivity {

    private miDB gestorDB;
    private long actividadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.onAttach(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gestorDB = new miDB(this, "Actividades", null, 1);

        Button dButton = findViewById(R.id.dAceptarButton);
        dButton.setOnClickListener(view -> Volver());

        // Añadimos el listener para el botón de borrar
        Button borrarButton = findViewById(R.id.dBorrarButon);
        borrarButton.setOnClickListener(view -> Borrar());

        TextView dTexto = findViewById(R.id.dTexto);

        Intent intent = getIntent();
        actividadId = intent.getLongExtra("actividad_id", -1);

        if (actividadId != -1) {
            Actividad actividad = gestorDB.getActividadPorId(actividadId);
            if (actividad != null) {
                dTexto.setText(actividad.getNombre());
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    public void Volver(){
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    public void Borrar(){
        Intent intent = new Intent();
        intent.putExtra("borrar", true);
        intent.putExtra("actividad_id", actividadId);
        setResult(RESULT_OK, intent);
        finish();
    }
}
