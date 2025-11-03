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

/**
 * Adaptador responsável por ligar os dados dos treinos (TreinoComDetalhes)
 * ao RecyclerView na interface do utilizador.
 *
 * Cada item da lista representa um treino, exibindo a categoria,
 * data e um ícone representativo.
 */
public class TreinoAdapter extends RecyclerView.Adapter<TreinoAdapter.TreinoViewHolder> {

    /**
     * Interface de callback para detectar cliques em itens da lista.
     * O Fragment que usa este adaptador deve implementar este listener.
     */

    public interface OnTreinoClickListener {
        void onTreinoClick(TreinoComDetalhes treino);
    }

    // Declarações
    private final List<TreinoComDetalhes> treinoList;
    private final Context context;
    private final OnTreinoClickListener listener;

    /**
     * Construtor do adaptador.
     *
     * @param context     Contexto da aplicação (necessário para inflar layouts)
     * @param treinoList  Lista de treinos a serem exibidos
     * @param listener    Callback que reage quando um treino é clicado
     */
    public TreinoAdapter(Context context, List<TreinoComDetalhes> treinoList, OnTreinoClickListener listener) {
        this.context = context;
        this.treinoList = treinoList;
        this.listener = listener;
    }

    /**
     * Cria novas "views" (itens) quando necessário.
     * É chamado pelo RecyclerView quando precisa de um novo item para exibir.
     */
    @NonNull
    @Override
    public TreinoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_treino, parent, false);
        return new TreinoViewHolder(view);
    }

    /**
     * Liga os dados de um objeto TreinoComDetalhes a um ViewHolder específico.
     * Este metodo é chamado automaticamente à medida que o utilizador faz scroll.
     */
    @Override
    public void onBindViewHolder(@NonNull TreinoViewHolder holder, int position) {
        // Obtém o treino correspondente à posição atual da lista
        TreinoComDetalhes treino = treinoList.get(position);

        // Define nome da categoria (ou "Outro" caso esteja vazio)
        String nomeCategoria = treino.categoriaNome != null ? treino.categoriaNome : "Outro";

        // Variáveis para armazenar o ícone e cor da categoria
        int iconRes;
        int color;

        // Escolhe o ícone e a cor de acordo com a categoria do treino
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

        // Define o texto e imagem no layout
        holder.textCategoria.setText(nomeCategoria);
        holder.textData.setText(treino.data);
        holder.imgIcon.setImageResource(iconRes);
        holder.imgIcon.setColorFilter(color);

        // Define o evento de clique para abrir os detalhes do treino
        holder.itemView.setOnClickListener(v -> listener.onTreinoClick(treino));
    }

    /**
     * Retorna o número total de itens na lista.
     */
    @Override
    public int getItemCount() {
        return treinoList.size();
    }

    /**
     * Classe interna responsável por armazenar as referências
     * aos elementos visuais (views) de cada item do RecyclerView.
     *
     * O ViewHolder serve para evitar chamadas repetidas de findViewById,
     * melhorando a performance do RecyclerView.
     */
    static class TreinoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView textCategoria, textData;

        /**
         * Construtor do ViewHolder — associa os elementos do layout XML.
         */
        TreinoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            textCategoria = itemView.findViewById(R.id.textCategoria);
            textData = itemView.findViewById(R.id.textData);
        }
    }
}
