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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        RadioGroup radioGroupIdioma = findViewById(R.id.radioGroupIdioma);
        RadioGroup radioGroupTema = findViewById(R.id.radioGroupTema);
        Button btnGuardar = findViewById(R.id.aceptarButton);

        // Mira que idioma es el que esta seleccionado y lo marca
        String lang = LocaleHelper.getLanguage(this);
        if (lang.equals("en")) {
            ((RadioButton)findViewById(R.id.radioButtonIngles)).setChecked(true);
        } else if (lang.equals("eu")) {
            ((RadioButton)findViewById(R.id.radioButtonEuskera)).setChecked(true);
        } else {
            ((RadioButton)findViewById(R.id.radioButtonEspañol)).setChecked(true);
        }

        // Mira que tema es el que esta seleccionado y lo marca
        String tema = ThemeHelper.getTheme(this);
        if (tema.equals("oscuro")) {
            ((RadioButton)findViewById(R.id.radioButtonOscuro)).setChecked(true);
        } else {
            ((RadioButton)findViewById(R.id.radioButtonClaro)).setChecked(true);
        }

        btnGuardar.setOnClickListener(v -> { // Ahora mismo se cambia todo al salir, igual mejor cambiarlo
            int selectedIdiomaId = radioGroupIdioma.getCheckedRadioButtonId();
            String newLang = "es";
            if (selectedIdiomaId == R.id.radioButtonIngles){
                newLang = "en";
            }
            else if (selectedIdiomaId == R.id.radioButtonEuskera){
                newLang = "eu";
            }
            LocaleHelper.setLocale(this, newLang);

            int selectedTemaId = radioGroupTema.getCheckedRadioButtonId();
            String newTema = "claro";
            if (selectedTemaId == R.id.radioButtonOscuro){
                newTema = "oscuro";
            }
            ThemeHelper.setTheme(this, newTema);

            // Reiniciar para ques e vean los cambios
            Intent i = new Intent(ConfigActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }
}
