package com.example.das_entrega_1;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class MainViewHolder extends RecyclerView.ViewHolder {
    public TextView eltexto;
    public MapView elmapa;
    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClicked(int position);
    }

    public MainViewHolder(@NonNull View itemView, final OnItemClickListener clickListener, final OnItemLongClickListener longClickListener) {
        super(itemView);
        eltexto = itemView.findViewById(R.id.txtItem);
        elmapa = itemView.findViewById(R.id.mapItem);

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
