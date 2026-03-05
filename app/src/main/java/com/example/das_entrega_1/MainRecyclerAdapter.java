package com.example.das_entrega_1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainViewHolder> implements MainViewHolder.OnItemClickListener, MainViewHolder.OnItemLongClickListener {
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
        return new MainViewHolder(elLayoutDeCadaItem, this, this);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        Actividad actividad = lasActividades.get(position);
        holder.eltexto.setText(actividad.getNombre());
        
        // Inicializar osmdroid para el item
        MapaHelper.init(holder.itemView.getContext());
        // Configurar el mapa del item (no interactivo para no molestar al scroll)
        MapaHelper.basicConfig(holder.elmapa, actividad.getLat(), actividad.getLon(), 15.0, false);
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

    @Override
    public void onItemLongClicked(int position) {
        if (mainActivity != null) {
            mainActivity.eliminarElemento(position);
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
