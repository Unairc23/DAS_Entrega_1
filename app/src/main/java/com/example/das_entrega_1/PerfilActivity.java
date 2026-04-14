package com.example.das_entrega_1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.content.Intent;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PerfilActivity extends AppCompatActivity {
    private static long userId;
    private static final int REQUEST_CAMERA = 100;
    private Uri fotoUri;

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

        ImageView perfilImage = findViewById(R.id.perfilImage);
        perfilImage.setOnClickListener(view -> cambiarFoto());

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
                    Data output = workInfo.getOutputData();
                    String nombre = output.getString("nombre");
                    if (nombre != null){
                        ((TextView) findViewById(R.id.nombreText)).setText(nombre);
                    }

                    String path = output.getString("imagenPath");
                    Log.d("miDBRemota", "path: " + path);
                    if (path != null){
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        if (bitmap != null){
                            perfilImage.setImageBitmap(bitmap);
                        }
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

    private void cambiarFoto(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            return;
        }
        abrirCamara();
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Archivo temporal para la foto, antes de mandar a remoto
        File fotoFile = new File(getCacheDir(), "foto_temp.jpg");
        fotoUri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", fotoFile);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            try {
                // Leer imagen
                InputStream inputStream = getContentResolver().openInputStream(fotoUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                ImageView perfil = findViewById(R.id.perfilImage);
                perfil.setImageBitmap(bitmap);

                // Convertir a Base64 para mandar a remoto
                subirFotoServidor(bitmap);

            } catch (Exception e) {
                Toast.makeText(this, "Error al leer la foto", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        }
    }

    private void subirFotoServidor(Bitmap bitmap) {
        try {
            // Guardar el bitmap en archivo temporal
            File archivoTemp = new File(getCacheDir(), "foto_perfil_temp.jpg");
            FileOutputStream fos = new FileOutputStream(archivoTemp);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();

            // Pasar solo la ruta al Worker
            Data inputData = new Data.Builder()
                    .putString("accion", miDBRemota.ACCION_CAMBIAR_PERFIL)
                    .putString("imagen", archivoTemp.getAbsolutePath())
                    .putLong("id", userId)
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(miDBRemota.class)
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(this).enqueue(workRequest);

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
