package com.example.das_entrega_1;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class miDBRemota extends Worker {
    public static String direccion = "http://35.238.248.169:81/";

    public static final String ACCION_ADD    = "add";
    public static final String ACCION_UPDATE = "update";
    public static final String ACCION_GET    = "getAll";
    public static final String ACCION_GET_ID = "getById";
    public static final String ACCION_DELETE = "delete";
    public static final String ACCION_LOGIN = "login";
    public static final String ACCION_REGISTER = "register";
    public static final String ACCION_GET_USER = "getuser";
    public static final String ACCION_CAMBIAR_PERFIL = "cambiarImagen";
    public miDBRemota(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String accion = getInputData().getString("accion");
        if (accion == null) return Result.failure();

        try {
            switch (accion) {
                case ACCION_ADD: {
                    long id = addActividad(
                            getInputData().getString("nombre"),
                            getInputData().getDouble("latitud", 0),
                            getInputData().getDouble("longitud", 0),
                            getInputData().getString("descripcion"),
                            getInputData().getDouble("distancia", 0),
                            getInputData().getDouble("duracion", 0)
                    );
                    return id != -1
                            ? Result.success(new Data.Builder().putLong("id", id).build())
                            : Result.failure();
                }

                case ACCION_UPDATE: {
                    updateActividad(
                            getInputData().getLong("id", -1),
                            getInputData().getString("nombre"),
                            getInputData().getDouble("latitud", 0),
                            getInputData().getDouble("longitud", 0),
                            getInputData().getString("descripcion"),
                            getInputData().getDouble("distancia", 0),
                            getInputData().getDouble("duracion", 0)
                    );
                    return Result.success();
                }

                case ACCION_GET: {
                    ArrayList<Actividad> lista = getActividades();
                    JSONArray jsonArray = new JSONArray();
                    for (Actividad a : lista) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", a.getId());
                        obj.put("nombre", a.getNombre());
                        obj.put("latitud", a.getLat());
                        obj.put("longitud", a.getLon());
                        obj.put("descripcion", a.getDescripcion());
                        obj.put("distancia", a.getDistancia());
                        obj.put("duracion", a.getDuracion());
                        jsonArray.put(obj);
                    }
                    return Result.success(new Data.Builder()
                            .putString("lista", jsonArray.toString())
                            .build());
                }

                case ACCION_GET_ID: {
                    Actividad a = getActividadPorId(getInputData().getLong("id", -1));
                    if (a == null) return Result.failure();
                    JSONObject obj = new JSONObject();
                    obj.put("id", a.getId());
                    obj.put("nombre", a.getNombre());
                    obj.put("latitud", a.getLat());
                    obj.put("longitud", a.getLon());
                    obj.put("descripcion", a.getDescripcion());
                    obj.put("distancia", a.getDistancia());
                    obj.put("duracion", a.getDuracion());
                    return Result.success(new Data.Builder()
                            .putString("actividad", obj.toString())
                            .build());
                }

                case ACCION_DELETE: {
                    deleteActividadPorId(getInputData().getLong("id", -1));
                    return Result.success();
                }

                case ACCION_LOGIN: {
                    long userId = usuarioLogin(
                            getInputData().getString("nombre"),
                            getInputData().getString("contraseña"));
                    return userId != -1
                            ? Result.success(new Data.Builder().putLong("id", userId).build())
                            : Result.failure();
                }

                case ACCION_REGISTER: {
                    long userId = usuarioRegister(
                            getInputData().getString("nombre"),
                            getInputData().getString("contraseña"));
                    return userId != -1
                            ? Result.success(new Data.Builder().putLong("id", userId).build())
                            : Result.failure();
                }

                case ACCION_GET_USER:{
                    JSONObject user = getUsuario(getInputData().getLong("id", -1));
                    if (user != null) {
                        String nombre = user.optString("Nombre");
                        String imagenBase64 = user.optString("imagen");
                        
                        Data.Builder outputData = new Data.Builder().putString("nombre", nombre);
                        
                        if (imagenBase64 != null && !imagenBase64.isEmpty()) {
                            try {
                                byte[] bytes = Base64.decode(imagenBase64, Base64.DEFAULT);
                                File file = new File(getApplicationContext().getCacheDir(), "perfil_recibido.jpg");
                                FileOutputStream fos = new FileOutputStream(file);
                                fos.write(bytes);
                                fos.close();
                                outputData.putString("imagenPath", file.getAbsolutePath());
                            } catch (Exception e) {
                                Log.e("miDBRemota", "Error guardando imagen: " + e.getMessage());
                            }
                        }
                        
                        return Result.success(outputData.build());
                    }
                    return Result.failure();
                }

                case ACCION_CAMBIAR_PERFIL:{
                    Log.d("miDBRemota", "cambiarFotoPerfil enrutador");
                    cambiarFotoPerfil(
                            getInputData().getLong("id", -1),
                            getInputData().getString("imagen"));
                    return Result.success();
                }

                default:
                    return Result.failure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private long addActividad(String nombre, double latitud, double longitud,
                              String descripcion, double distancia, double duracion) {
        HttpURLConnection conn = null;
        try {
            JSONObject params = new JSONObject();
            params.put("accion", "add");
            params.put("nombre", nombre);
            params.put("latitud", latitud);
            params.put("longitud", longitud);
            params.put("descripcion", descripcion);
            params.put("distancia", distancia);
            params.put("duracion", duracion);

            String respuesta = enviarPost(params);
            Log.d("miDBRemota", "Respuesta add: " + respuesta);

            if (respuesta != null) {
                return new JSONObject(respuesta).getLong("id");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    private void updateActividad(long id, String nombre, double latitud, double longitud,
                                 String descripcion, double distancia, double duracion) {
        try {
            JSONObject params = new JSONObject();
            params.put("accion", "update");
            params.put("id", id);
            params.put("nombre", nombre);
            params.put("latitud", latitud);
            params.put("longitud", longitud);
            params.put("descripcion", descripcion);
            params.put("distancia", distancia);
            params.put("duracion", duracion);

            String respuesta = enviarPost(params);
            Log.d("miDBRemota", "Respuesta update: " + respuesta);

        } catch (Exception e) { e.printStackTrace(); }
    }

    private ArrayList<Actividad> getActividades() {
        ArrayList<Actividad> lista = new ArrayList<>();
        try {
            JSONObject params = new JSONObject();
            params.put("accion", "getAll");

            String respuesta = enviarPost(params);
            if (respuesta != null) {
                JSONObject jsonObj = new JSONObject(respuesta);
                JSONArray array = jsonObj.getJSONArray("actividades");
                for (int i = 0; i < array.length(); i++) {
                    lista.add(parsearActividad(array.getJSONObject(i)));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    private Actividad getActividadPorId(long id) {
        try {
            JSONObject params = new JSONObject();
            params.put("accion", "getById");
            params.put("id", id);

            String respuesta = enviarPost(params);
            Log.d("miDBRemota", "Respuesta getById: " + respuesta);
            if (respuesta != null) {
                JSONObject root = new JSONObject(respuesta);
                JSONObject obj = root.getJSONObject("actividad");
                return parsearActividad(obj);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private void deleteActividadPorId(long id) {
        try {
            JSONObject params = new JSONObject();
            params.put("accion", "delete");
            params.put("id", id);

            enviarPost(params);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private long usuarioLogin(String nombre, String contraseña){
        try {
            JSONObject params = new JSONObject();
            params.put("accion", "login");
            params.put("nombre", nombre);
            params.put("contraseña", contraseña);

            String respuesta = enviarPost(params);

            if (respuesta != null) {
                return new JSONObject(respuesta).getLong("id");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private long usuarioRegister(String nombre, String contraseña){
        try{
            JSONObject params = new JSONObject();
            params.put("accion", "register");
            params.put("nombre", nombre);
            params.put("contraseña", contraseña);

            String respuesta = enviarPost(params);

            if (respuesta != null) {
                return new JSONObject(respuesta).getLong("id");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    private JSONObject getUsuario(long id){
        try{
            JSONObject params = new JSONObject();
            params.put("accion", "getuser");
            params.put("id", id);

            Log.d("miDBRemota", "Params: " + params.toString() + "");
            String respuesta = enviarPost(params);
            if (respuesta != null) {
                JSONObject root = new JSONObject(respuesta);
                return root.getJSONObject("usuario");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cambiarFotoPerfil(long id, String rutaImagen){
        try {
            Log.d("miDBRemota", "cambiarFotoPerfil dentrp");
            // Leer el archivo y convertir a Base64 aquí dentro
            File archivo = new File(rutaImagen);
            byte[] bytes = new byte[(int) archivo.length()];
            FileInputStream fis = new FileInputStream(archivo);
            fis.read(bytes);
            fis.close();
            Log.d("miDBRemota", "cambiarFotoPerfil archivo leido");

            String imagenBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
            Log.d("miDBRemota", "cambiarFotoPerfil imagenBase64");

            JSONObject params = new JSONObject();
            params.put("accion", "cambiarImagen");
            params.put("id", id);
            params.put("imagen", imagenBase64);
            Log.d("miDBRemota", "cambiarFotoPerfil params creados");


            enviarPost(params);

            // Borrar archivo temporal
            archivo.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Funcion para no repetir constantemente código
    private String enviarPost(JSONObject params) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(direccion).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(params.toString());
            out.close();

            int code = conn.getResponseCode();
            Log.d("miDBRemota", "Codigo respuesta: " + code);

            if (code == 200 || code == 201) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                Log.d("miDBRemota", "Respuesta: " + sb);
                return sb.toString();
            }
        } catch (Exception e) {
            Log.e("miDBRemota", "Error en enviarPost: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    private Actividad parsearActividad(JSONObject obj) throws JSONException {
        return new Actividad(
                obj.getLong("Id"),
                obj.getString("Nombre"),
                obj.getDouble("Latitud"),
                obj.getDouble("Longitud"),
                obj.getDouble("Distancia"),
                obj.getDouble("Duracion"),
                obj.optString("Descripcion", "")
                );
    }
}