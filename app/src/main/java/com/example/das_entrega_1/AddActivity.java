package com.example.das_entrega_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button aButton = findViewById(R.id.Aceptarbutton);
        aButton.setOnClickListener(view -> Aceptar());
    }

    public void Aceptar(){
        EditText nombreInput = findViewById(R.id.nombreInput);
        String valor = nombreInput.getText().toString();

        Intent intent = new Intent();
        intent.putExtra("nombre", valor);
        setResult(RESULT_OK, intent);
        finish();
    }
}