package com.example.das_entrega_1;

import android.graphics.Color;
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

    public MainViewHolder(@NonNull View itemView) {
        super(itemView);
        eltexto = itemView.findViewById(R.id.txtItem);
        laimagen = itemView.findViewById(R.id.imgItem);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (seleccion.get(getAdapterPosition()) == true) {
                    seleccion.set(getAdapterPosition(), false);
                    laimagen.setColorFilter(null);
                } else {
                    seleccion.set(getAdapterPosition(), true);
                    laimagen.setColorFilter(Color.BLACK);
                }
            }
        });
    }
}
