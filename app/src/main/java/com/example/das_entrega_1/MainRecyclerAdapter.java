package com.example.das_entrega_1;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainViewHolder> implements MainViewHolder.OnItemClickListener{
    private ArrayList<Actividad> lasActividades;
    private MainActivity mainActivity;

    public MainRecyclerAdapter(ArrayList<Actividad> actividades, MainActivity activity) {
        this.lasActividades = actividades;
        this.mainActivity = activity;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View elLayoutDeCadaItem= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new MainViewHolder(elLayoutDeCadaItem, this);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) { // Rellena la información de los diferentes campos
        Actividad actividad = lasActividades.get(position);
        holder.eltexto.setText(actividad.getNombre());
        holder.ladistancia.setText(String.format("%.2f km", actividad.getDistancia()));

        int horas = (int) (actividad.getDuracion() / 3600);
        int minutos = (int) ((actividad.getDuracion() % 3600) / 60);
        holder.eltiempo.setText(String.format("%02d:%02d", horas, minutos));
        holder.ladescripcion.setText(actividad.getDescripcion());

        String url = "https://maps.googleapis.com/maps/api/staticmap?"
                + "center=" + actividad.getLat() + "," + actividad.getLon()
                + "&zoom=15"
                + "&size=400x200"
                + "&markers=color:red%7C" + actividad.getLat() + "," + actividad.getLon()
                + "&key=" + BuildConfig.MAPS_API_KEY;

        Glide.with(holder.itemView.getContext())
                .load(url)
                .placeholder(R.drawable.ic_launcher_background) // imagen mientras carga
                .into(holder.elmapa);
    }

    @Override
    public int getItemCount() {
        return lasActividades.size();
    }

    @Override
    public void onItemClicked(int position) {
        if (mainActivity != null) {
            mainActivity.abrirDetalles(position);
        }
    }

    public void addItem(Actividad actividad) {
        lasActividades.add(actividad);
        notifyItemInserted(lasActividades.size() - 1);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < lasActividades.size()) {
            lasActividades.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, lasActividades.size());
        }
    }
}
