package com.example.das_entrega_1;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainViewHolder extends RecyclerView.ViewHolder {
    public TextView eltexto;
    public ImageView laimagen;
    public ArrayList<Boolean> seleccion;

    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClicked(int position);
    }

    public MainViewHolder(@NonNull View itemView, final OnItemClickListener clickListener, final OnItemLongClickListener longClickListener) {
        super(itemView);
        eltexto = itemView.findViewById(R.id.txtItem);
        laimagen = itemView.findViewById(R.id.imgItem);

        itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onItemClicked(position);
                }
            }
        });

        itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    longClickListener.onItemLongClicked(position);
                    return true;
                }
            }
            return false;
        });
    }
}
