package com.example.das_entrega_1;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class ConfigActivity extends AppCompatActivity {

    private String currentMode;
    private RadioGroup radioGroupIdioma;
    private RadioGroup radioGroupTema;

    // Aplicar el idioma
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeHelper.getThemeStyle(this));
        ThemeHelper.applySettings(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Boton para volver a main

        radioGroupIdioma = findViewById(R.id.radioGroupIdioma);
        radioGroupTema = findViewById(R.id.radioGroupTema);
        MaterialButton btnToggleDarkMode = findViewById(R.id.btnToggleDarkMode);
        Button btnGuardar = findViewById(R.id.aceptarButton);

        // Marcar el idioma
        String lang = LocaleHelper.getLanguage(this);
        if (lang.equals("en")) {
            ((RadioButton)findViewById(R.id.radioButtonIngles)).setChecked(true);
        }
        else if (lang.equals("eu")) {
            ((RadioButton)findViewById(R.id.radioButtonEuskera)).setChecked(true);
        }
        else {
            ((RadioButton)findViewById(R.id.radioButtonEspañol)).setChecked(true);
        }

        // Marcar el color y modo actual
        String currentColor = ThemeHelper.getColor(this);
        currentMode = ThemeHelper.getMode(this);

        cambiarIcono(btnToggleDarkMode);

        if ("azul".equals(currentColor)) {
            ((RadioButton)findViewById(R.id.radioButtonAzul)).setChecked(true);
        }
        else if ("rojo".equals(currentColor)) {
            ((RadioButton)findViewById(R.id.radioButtonRojo)).setChecked(true);
        }
        else {
            ((RadioButton)findViewById(R.id.radioButtonClaro)).setChecked(true);
        }

        btnToggleDarkMode.setOnClickListener(v -> {
            currentMode = "oscuro".equals(currentMode) ? "claro" : "oscuro";
            guardarCambios();
        });

        btnGuardar.setOnClickListener(v -> {
            guardarCambios();
            // Reiniciar para guardar cambios y volver a la pantalla principal
            Intent i = new Intent(ConfigActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    private void guardarCambios() {
        // Obtener idioma seleccionado
        int selectedIdiomaId = radioGroupIdioma.getCheckedRadioButtonId();
        String newLang = "es";
        if (selectedIdiomaId == R.id.radioButtonIngles) newLang = "en";
        else if (selectedIdiomaId == R.id.radioButtonEuskera) newLang = "eu";
        LocaleHelper.setLocale(this, newLang);

        // Obtener color seleccionado
        int selectedTemaId = radioGroupTema.getCheckedRadioButtonId();
        String newColor = "base";
        if (selectedTemaId == R.id.radioButtonAzul) newColor = "azul";
        else if (selectedTemaId == R.id.radioButtonRojo) newColor = "rojo";
        
        // Guardar y aplicar
        ThemeHelper.setSettings(this, newColor, currentMode);
    }

    private void cambiarIcono(MaterialButton btn) {
        if ("oscuro".equals(currentMode)) {
            btn.setIconResource(R.drawable.ic_sun); // Icono de sol para volver a claro
            btn.setIconTint(ColorStateList.valueOf(Color.parseColor("#FFD700")));
            btn.setBackgroundColor(Color.parseColor("#333333"));
        } else {
            btn.setIconResource(R.drawable.ic_moon); // Icono de luna para ir a oscuro
            btn.setIconTint(ColorStateList.valueOf(Color.parseColor("#5C6BC0")));
            btn.setBackgroundColor(Color.parseColor("#F5F5F5"));
        }
    }
}
