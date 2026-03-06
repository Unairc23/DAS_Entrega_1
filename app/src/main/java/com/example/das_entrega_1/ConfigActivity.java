package com.example.das_entrega_1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class ConfigActivity extends AppCompatActivity {

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

        RadioGroup radioGroupIdioma = findViewById(R.id.radioGroupIdioma);
        RadioGroup radioGroupTema = findViewById(R.id.radioGroupTema);
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

        // Marcar el color
        String currentColor = ThemeHelper.getColor(this);
        String currentMode = ThemeHelper.getMode(this);

        if (currentMode.equals("oscuro")) {
            ((RadioButton)findViewById(R.id.radioButtonOscuro)).setChecked(true);
        }
        else if (currentColor.equals("azul")) {
            ((RadioButton)findViewById(R.id.radioButtonAzul)).setChecked(true);
        }
        else if (currentColor.equals("rojo")) {
            ((RadioButton)findViewById(R.id.radioButtonRojo)).setChecked(true);
        }
        else {
            ((RadioButton)findViewById(R.id.radioButtonClaro)).setChecked(true);
        }

        btnGuardar.setOnClickListener(v -> {
            // Guardar Idioma
            int selectedIdiomaId = radioGroupIdioma.getCheckedRadioButtonId();
            String newLang = "es";
            if (selectedIdiomaId == R.id.radioButtonIngles) {
                newLang = "en";
            }
            else if (selectedIdiomaId == R.id.radioButtonEuskera) {
                newLang = "eu";
            }
            LocaleHelper.setLocale(this, newLang);

            // Guardar Tema y Modo
            int selectedTemaId = radioGroupTema.getCheckedRadioButtonId();
            String newColor = "base";
            String newMode = "claro";

            if (selectedTemaId == R.id.radioButtonOscuro) {
                newMode = "oscuro";
                newColor = ThemeHelper.getColor(this); // Mantener el color base actual
            } else if (selectedTemaId == R.id.radioButtonAzul) {
                newColor = "azul";
            } else if (selectedTemaId == R.id.radioButtonRojo) {
                newColor = "rojo";
            }
            
            ThemeHelper.setSettings(this, newColor, newMode);

            // Reiniciar app para aplicar cambios globalmente
            Intent i = new Intent(ConfigActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }
}
