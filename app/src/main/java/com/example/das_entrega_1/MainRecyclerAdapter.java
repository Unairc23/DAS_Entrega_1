package com.example.das_entrega_1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainViewHolder> implements MainViewHolder.OnItemClickListener, MainViewHolder.OnItemLongClickListener {
    private ArrayList<Actividad> lasActividades;
    private ArrayList<Integer> lasimagenes;
    private MainActivity mainActivity;

    public MainRecyclerAdapter(ArrayList<Actividad> actividades, ArrayList<Integer> imagenes, MainActivity activity) {
        this.lasActividades = actividades;
        this.lasimagenes = imagenes;
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
        holder.eltexto.setText(lasActividades.get(position).getNombre());
        holder.laimagen.setImageResource(lasimagenes.get(position));
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

    public void addItem(Actividad actividad, int imagen) {
        lasActividades.add(actividad);
        lasimagenes.add(imagen);
        notifyItemInserted(lasActividades.size() - 1);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < lasActividades.size()) {
            lasActividades.remove(position);
            lasimagenes.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, lasActividades.size());
        }
    }
}
