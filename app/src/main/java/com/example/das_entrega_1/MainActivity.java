package com.example.das_entrega_1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

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

        RecyclerView lista = findViewById(R.id.mrv);
        int[] personajes= {R.drawable.togetchi1, R.drawable.togetchi1, R.drawable.togetchi1, R.drawable.togetchi1,
                R.drawable.togetchi1};
        String[] nombres={"AAAAAAAAAAAAA","AAAAA AAAAA AAAAAAA", "AAAA AAAAAAA AAAAAAAAAAA AAAAAa AAAAAAAAA", "",
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"};
        MainRecyclerAdapter eladaptador = new MainRecyclerAdapter(nombres,personajes);
        lista.setAdapter(eladaptador);
        lista.setLayoutManager(new LinearLayoutManager(this));
    }
}