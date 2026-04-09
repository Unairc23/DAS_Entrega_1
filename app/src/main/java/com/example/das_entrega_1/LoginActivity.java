package com.example.das_entrega_1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

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

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
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

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(view -> login());

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(view -> register());
    }

    private void login(){
        String nombre = ((EditText) findViewById(R.id.usarioText)).getText().toString();
        String contraseña = ((EditText) findViewById(R.id.contraseñaText)).getText().toString();

        if (nombre.isEmpty() || contraseña.isEmpty()) {
            return;
        }

        Data input = new Data.Builder()
                .putString("accion", miDBRemota.ACCION_LOGIN)
                .putString("nombre", nombre)
                .putString("contraseña", contraseña)
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
                    long userId = workInfo.getOutputData().getLong("id", -1);
                    if (userId != -1){
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("user_id", userId);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }
        };
        liveData.observeForever(observerRef[0]);
    }

    private void register(){
        String nombre = ((EditText) findViewById(R.id.usarioText)).getText().toString();
        String contraseña = ((EditText) findViewById(R.id.contraseñaText)).getText().toString();

        if (nombre.isEmpty() || contraseña.isEmpty()) {
            return;
        }

        Data input = new Data.Builder()
                .putString("accion", miDBRemota.ACCION_REGISTER)
                .putString("nombre", nombre)
                .putString("contraseña", contraseña)
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
                    long userId = workInfo.getOutputData().getLong("id", -1);
                    if (userId != -1){
                        Log.d("LoginActivity", "User registered with ID: " + userId);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("user_id", userId);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }
        };
        liveData.observeForever(observerRef[0]);
    }
}