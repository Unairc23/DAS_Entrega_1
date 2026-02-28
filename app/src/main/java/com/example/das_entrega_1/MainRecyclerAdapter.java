package com.example.das_entrega_1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainViewHolder> {
    private ArrayList<String> losnombres;
    private ArrayList<Integer> lasimagenes;
    private static ArrayList<Boolean> seleccionados;

    public MainRecyclerAdapter(ArrayList<String> nombres, ArrayList<Integer> imagenes) {
        losnombres = nombres;
        lasimagenes = imagenes;
        seleccionados  = new ArrayList<Boolean>();
        for (int i = 0; i < losnombres.size(); i++){
            seleccionados.add(false);
        }
    }

    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View elLayoutDeCadaItem= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        MainViewHolder mvh = new MainViewHolder(elLayoutDeCadaItem);
        mvh.seleccion = seleccionados;
        return mvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        holder.eltexto.setText(losnombres.get(position));
        holder.laimagen.setImageResource(lasimagenes.get(position));
    }
    @Override
    public int getItemCount() {
        return losnombres.size();
    }

    public void addItem(String nombre, int imagen) {
        losnombres.add(nombre);
        lasimagenes.add(imagen);
        seleccionados.add(false);
        notifyItemInserted(losnombres.size() - 1);
    }
}