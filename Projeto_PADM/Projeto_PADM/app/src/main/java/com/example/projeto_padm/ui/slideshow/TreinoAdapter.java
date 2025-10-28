package com.example.projeto_padm.ui.slideshow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projeto_padm.R;
import com.example.projeto_padm.TreinoComDetalhes;

import java.util.List;

public class TreinoAdapter extends RecyclerView.Adapter<TreinoAdapter.TreinoViewHolder> {

    public interface OnTreinoClickListener {
        void onTreinoClick(TreinoComDetalhes treino);
    }

    private final List<TreinoComDetalhes> treinoList;
    private final Context context;
    private final OnTreinoClickListener listener;

    public TreinoAdapter(Context context, List<TreinoComDetalhes> treinoList, OnTreinoClickListener listener) {
        this.context = context;
        this.treinoList = treinoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TreinoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_treino, parent, false);
        return new TreinoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreinoViewHolder holder, int position) {
        TreinoComDetalhes treino = treinoList.get(position);

        String nomeCategoria = treino.categoriaNome != null ? treino.categoriaNome : "Outro";
        int iconRes;
        int color;

        switch (nomeCategoria.toLowerCase()) {
            case "caminhada":
                iconRes = R.drawable.treino_walk;
                color = 0xFFFFB300;
                break;
            case "corrida":
                iconRes = R.drawable.treino_run;
                color = 0xFF7C4DFF;
                break;
            case "ciclismo":
                iconRes = R.drawable.treino_cycling;
                color = 0xFF2196F3;
                break;
            default:
                iconRes = R.drawable.treino_run;
                color = 0xFF757575;
                break;
        }

        holder.textCategoria.setText(nomeCategoria);
        holder.textData.setText(treino.data);
        holder.imgIcon.setImageResource(iconRes);
        holder.imgIcon.setColorFilter(color);

        holder.itemView.setOnClickListener(v -> listener.onTreinoClick(treino));
    }

    @Override
    public int getItemCount() {
        return treinoList.size();
    }

    static class TreinoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView textCategoria, textData;

        TreinoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            textCategoria = itemView.findViewById(R.id.textCategoria);
            textData = itemView.findViewById(R.id.textData);
        }
    }
}