package com.example.das_entrega_1;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.content.Intent;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.appbar.MaterialToolbar;

public class PerfilActivity extends AppCompatActivity {
    private static long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Boton para volver a main

        Button outButton = findViewById(R.id.logoutbutton);
        outButton.setOnClickListener(view -> logout());

        // Obtener datos del usuario
        userId = getIntent().getLongExtra("user_id", -1);
        Data input = new Data.Builder()
                .putString("accion", miDBRemota.ACCION_GET_USER)
                .putLong("id", userId)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(miDBRemota.class)
                .setInputData(input).build();

        WorkManager.getInstance(this).enqueue(request);

        LiveData<WorkInfo> liveData = WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(request.getId());

        Observer<WorkInfo>[] observerRef = new Observer[1];
        observerRef[0] = workInfo -> {
            Log.d("workinfo", String.valueOf(workInfo));
            if (workInfo != null && workInfo.getState().isFinished()) {
                liveData.removeObserver(observerRef[0]);
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    String nombre = workInfo.getOutputData().getString("nombre");
                    if (nombre != null){
                        ((TextView) findViewById(R.id.nombreText)).setText(nombre);
                    }
                }
            }
        };
        liveData.observeForever(observerRef[0]);
    }

    private void logout(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("logout", true);
        startActivity(intent);
        finish();
    }
}