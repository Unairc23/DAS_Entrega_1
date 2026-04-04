package com.example.das_entrega_1;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.views.MapView;

public class MainViewHolder extends RecyclerView.ViewHolder {
    public TextView eltexto;
    public TextView eltiempo;
    public TextView ladistancia;
    public TextView ladescripcion;
    public ImageView elmapa;
    public View elmapaClick; //El mapa consume los clics, poniendo una vista por encima esta peude detectar el click

    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    public MainViewHolder(@NonNull View itemView, final OnItemClickListener clickListener) {
        super(itemView);
        eltexto = itemView.findViewById(R.id.txtItem);
        elmapa = itemView.findViewById(R.id.mapItem);
        elmapaClick = itemView.findViewById(R.id.mapClickOverlay);
        eltiempo = itemView.findViewById(R.id.txtTiempoItem);
        ladistancia = itemView.findViewById(R.id.txtDistanciaItem);
        ladescripcion = itemView.findViewById(R.id.txtDescripcionItem);

        View.OnClickListener listener = v -> {
            if (clickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onItemClicked(position);
                }
            }
        };

        itemView.setOnClickListener(listener);
        if (elmapaClick != null) {
            elmapaClick.setOnClickListener(listener);
        }
    }
}
