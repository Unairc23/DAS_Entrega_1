package com.example.das_entrega_1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainViewHolder> {
    private String[] losnombres;
    private int[] lasimagenes;
    private static boolean[] seleccionados;

    public MainRecyclerAdapter(String[] nombres, int[] imagenes) {
        losnombres = nombres;
        lasimagenes = imagenes;
        seleccionados = new boolean[nombres.length];
    }

    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View elLayoutDeCadaItem= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        MainViewHolder mvh = new MainViewHolder(elLayoutDeCadaItem);
        mvh.seleccion = seleccionados;
        return mvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        holder.eltexto.setText(losnombres[position]);
        holder.laimagen.setImageResource(lasimagenes[position]);
    }
    @Override
    public int getItemCount() {
        return losnombres.length;
    }
}