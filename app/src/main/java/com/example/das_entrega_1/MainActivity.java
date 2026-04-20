package com.example.das_entrega_1;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MainRecyclerAdapter eladaptador;
    private ActivityResultLauncher<Intent> startActivityIntent;
    private ArrayList<Actividad> actividades;
    private Long userId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeHelper.getThemeStyle(this)); // Aplicar colores
        ThemeHelper.applySettings(this); // Aplicar modo
        super.onCreate(savedInstanceState);
        LocaleHelper.onAttach(this); // Aplicar idioma
        MapaHelper.init(this); // Inicializar mapa
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Cargar userId persistente
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        long savedUserId = prefs.getLong("userId", -1);
        if (savedUserId != -1) {
            userId = savedUserId;
        }

        startActivityIntent = // Logica para recibir datos de los intents
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK) {
                                // Si hay datos no hay actividades que crear / actualizar / borrar
                                if (result.getData() != null) {
                                    boolean borrar = result.getData().getBooleanExtra("borrar", false);
                                    boolean logout = result.getData().getBooleanExtra("logout", false);
                                    if (borrar){
                                        long id = result.getData().getLongExtra("actividad_id", -1);
                                        borrarActividad(id);
                                    }
                                    else if (logout){
                                        userId = null;
                                        Log.d("miMain", "logouteado");
                                        SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
                                        editor.remove("userId");
                                        editor.apply();
                                    }
                                    else {
                                        // Si hay un id es que hay una actividad que actualizar
                                        if (result.getData().hasExtra("actividad_id")) {
                                            long id = result.getData().getLongExtra("actividad_id", -1);
                                            actualizarActividad(id, result.getData());
                                        }
                                        else if (result.getData().hasExtra("user_id")) {
                                            userId = result.getData().getLongExtra("user_id", -1);
                                            
                                            // Guardar userId persistente
                                            SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
                                            editor.putLong("userId", userId);
                                            editor.apply();
                                        }
                                        else {
                                            añadirActividad(result.getData());
                                        }
                                    }
                                    cargarActividades();
                                }
                            }
                        });

        if (userId!=null){
            cargarActividades();
        }

        FloatingActionButton Addbutton = findViewById(R.id.Addbutton);
        Addbutton.setOnClickListener(view -> abrirDetalles(-1)); // -1 para indicar que la actividad no está en la lista y hay que crearla
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tool, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.opciones) { // Abrir config desde la toolbar
            abrirConfig();
            return true;
        }
        else if (item.getItemId() == R.id.perfil) { // Abrir perfil desde la toolbar
            gestionarLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private void cargarActividades(){
        RecyclerView lista = findViewById(R.id.mrv);

        if (userId == null) {
            actividades = new ArrayList<>();
            eladaptador = new MainRecyclerAdapter(actividades, this);
            lista.setAdapter(eladaptador);
            lista.setLayoutManager(new LinearLayoutManager(this));

            SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
            editor.putFloat("duracion", 0f);
            editor.putFloat("distancia", 0f);
            editor.apply();
            WidgetHelper.ejecutarActualizacion(this, -1);
            return;
        }

        Data inputGetAll = new Data.Builder()
                .putString("accion", miDBRemota.ACCION_GET)
                .putLong("userId", userId)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(miDBRemota.class)
                .setInputData(inputGetAll)
                .build();

        WorkManager.getInstance(this).enqueue(request);

        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(request.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        String listaJson = workInfo.getOutputData().getString("lista");
                        actividades = parsearLista(listaJson);
                        eladaptador = new MainRecyclerAdapter(actividades, this);
                        lista.setAdapter(eladaptador);
                        lista.setLayoutManager(new LinearLayoutManager(this));

                        if (!actividades.isEmpty()) {
                            Actividad ultima = actividades.get(actividades.size() - 1);
                            float duracion = (float) ultima.getDuracion();
                            float distancia = (float) ultima.getDistancia();

                            SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
                            editor.putFloat("duracion", duracion);
                            editor.putFloat("distancia", distancia);
                            editor.commit();
                        }
                        try{
                            WidgetHelper.ejecutarActualizacion(MainActivity.this, userId != null ? userId : -1);
                            Log.d("miWidget", "widget actualizado manualmente");
                        }
                        catch (Exception e){
                            Log.d("miWIdget", e.getMessage());
                        }
                    }
                });
    }

    private void actualizarActividad(long id, Intent data) {
        String nombre = data.getStringExtra("nombre");
        String descripcion = data.getStringExtra("descripcion");
        double latitud = data.getDoubleExtra("latitud", 0.0);
        double longitud = data.getDoubleExtra("longitud", 0.0);
        double distancia = data.getDoubleExtra("distancia", 0.0);
        double duracion = data.getDoubleExtra("duracion", 0);

        Data input = new Data.Builder()
                .putString("accion", miDBRemota.ACCION_UPDATE)
                .putLong("id", id)
                .putString("nombre", nombre)
                .putDouble("latitud", latitud)
                .putDouble("longitud", longitud)
                .putString("descripcion", descripcion)
                .putDouble("distancia", distancia)
                .putDouble("duracion", duracion)
                .build();
        WorkManager.getInstance(this).enqueue(
                new OneTimeWorkRequest.Builder(miDBRemota.class).setInputData(input).build()
        );

        for (int i = 0; i < actividades.size(); i++) { // Buscar id en la lista
            if (actividades.get(i).getId() == id) {
                actividades.get(i).update(nombre, latitud, longitud, distancia, duracion, descripcion);
                Log.d("miMain", "Duracion" + duracion);
                eladaptador.notifyItemChanged(i);
                break;
            }
        }
    }

    public void añadirActividad(Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Pide permisos para mandar notificaciones solo cuando lo va a hacer por primera vez
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
            }
        }

        String nombre = data.getStringExtra("nombre");
        String descripcion = data.getStringExtra("descripcion");
        double latitud = data.getDoubleExtra("latitud", 0.0);
        double longitud = data.getDoubleExtra("longitud", 0.0);
        double distancia = data.getDoubleExtra("distancia", 0.0);
        double duracion = data.getDoubleExtra("duracion", 0);

        if (nombre == null || nombre.isEmpty()) return;

        Data input = new Data.Builder()
                .putString("accion", miDBRemota.ACCION_ADD)
                .putString("nombre", nombre)
                .putDouble("latitud", latitud)
                .putDouble("longitud", longitud)
                .putString("descripcion", descripcion)
                .putDouble("distancia", distancia)
                .putDouble("duracion", duracion)
                .putLong("userId", userId)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(miDBRemota.class)
                .setInputData(input).build();

        WorkManager.getInstance(this).enqueue(request);
        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(request.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        long id = workInfo.getOutputData().getLong("id", -1);
                        for (Actividad a : actividades){ // Evitar que al saltar varias veces el observer la actividad se añada dos veces
                            if (a.getId() == id){
                                return;
                            }
                        }
                        notificar();
                        Actividad nueva = new Actividad(id, nombre, latitud, longitud,
                                distancia, duracion, descripcion);
                        Log.d("miMain", "Duracion" + duracion);
                        eladaptador.addItem(nueva);
                    }
                });
    }

    public void notificar(){ // Crea / gestiona la notificación
        NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String idCanal = "IdCanal";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal = new NotificationChannel(idCanal, "NombreCanal", NotificationManager.IMPORTANCE_DEFAULT);
            elCanal.setDescription("Descripción del canal");
            elCanal.enableLights(true);
            elCanal.setLightColor(Color.RED);
            elManager.createNotificationChannel(elCanal);
        }

        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, idCanal)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getString(R.string.notificacionAñadida))
                .setContentText(getString(R.string.notificacionDesc))
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setAutoCancel(true);

        elManager.notify(1, elBuilder.build());
    }

    public void borrarActividad(long id) {
        Data input = new Data.Builder()
                .putString("accion", miDBRemota.ACCION_DELETE)
                .putLong("id", id)
                .build();
        WorkManager.getInstance(this).enqueue(
                new OneTimeWorkRequest.Builder(miDBRemota.class).setInputData(input).build()
        );

        for (int i = 0; i < actividades.size(); i++) {
            if (actividades.get(i).getId() == id) {
                eladaptador.removeItem(i);
                break;
            }
        }
    }

    public void abrirDetalles(int position) {
        if (userId != null){
            Intent intent = new Intent(this, DetallesActivity.class);
            if (position != -1){
                Actividad actividadSeleccionada = actividades.get(position);
                intent.putExtra("actividad_id", actividadSeleccionada.getId());
                intent.putExtra("userId", userId);
            }
            startActivityIntent.launch(intent);
        }
        else{
            gestionarLogin();
        }
    }

    public void abrirConfig() {
        Intent intent = new Intent(this, ConfigActivity.class);
        startActivityIntent.launch(intent);
    }

    public void gestionarLogin(){
        if (userId == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityIntent.launch(intent);
        }
        else{
            Intent intent = new Intent(this, PerfilActivity.class);
            intent.putExtra("user_id", userId);
            startActivityIntent.launch(intent);
        }
    }

    private ArrayList<Actividad> parsearLista(String json) {
        ArrayList<Actividad> lista = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                lista.add(new Actividad(
                        obj.getLong("id"),
                        obj.getString("nombre"),
                        obj.getDouble("latitud"),
                        obj.getDouble("longitud"),
                        obj.getDouble("distancia"),
                        obj.getDouble("duracion"),
                        obj.getString("descripcion")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
