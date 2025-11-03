package com.example.projeto_padm.ui.slideshow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projeto_padm.AppDatabase;
import com.example.projeto_padm.R;
import com.example.projeto_padm.Treino;
import com.example.projeto_padm.TreinoComDetalhes;
import com.example.projeto_padm.databinding.FragmentSlideshowBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * Fragmento responsável por listar e mostrar os detalhes dos treinos do utilizador.
 */
public class SlideshowFragment extends Fragment {

    // Declarações
    private FragmentSlideshowBinding binding;
    private DatabaseReference treinoRef;
    private AppDatabase db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Define o RecyclerView (lista de treinos) com layout vertical
        RecyclerView recyclerView = binding.recyclerTreinos;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Inicializa referências do Firebase e Room
        treinoRef = FirebaseDatabase.getInstance().getReference("Treinos");
        db = AppDatabase.getInstance(requireContext());

        // Obtém o ID do utilizador guardado em SharedPreferences (sessão)
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);

        // Se não encontrar o ID do utilizador, mostra erro
        if (userId == -1) {
            binding.textNoTreinos.setText("Erro: utilizador não encontrado.");
            binding.textNoTreinos.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return root;
        }

        // Verifica se há ligação à internet
        if (isConnected()) {
            // Online → Busca os treinos da Firebase e sincroniza com o Room
            buscarTreinosFirebase(userId, true);
        } else {
            // Offline → Carrega apenas os dados guardados localmente
            carregarTreinosOffline(userId);
        }
        return root;
    }

    // ============================================================
    // === Buscar dados da Firebase e sincronizar com o Room ======
    // ============================================================
    private void buscarTreinosFirebase(long userId, boolean syncToRoom) {
        // Obtém todos os registos do nó "Treinos" no Firebase
        treinoRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Listas temporárias para armazenar treinos
                List<TreinoComDetalhes> listaTreinos = new ArrayList<>(); // Para mostrar no ecrã
                List<Treino> listaTreinosRoom = new ArrayList<>(); // Para sincronizar com Room

                // Percorre todos os registos da Firebase
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Treino t = snapshot.getValue(Treino.class);
                    if (t != null && t.getUserId() == userId) {

                        // Adiciona o treino na lista local
                        listaTreinosRoom.add(t);

                        // Cria objeto com detalhes para exibir na interface
                        TreinoComDetalhes detalhe = new TreinoComDetalhes();
                        detalhe.id = t.getId();
                        detalhe.data = t.getData();
                        detalhe.calorias = t.getCalorias();
                        detalhe.distanciaPercorrida = t.getDistanciaPercorrida();
                        detalhe.velocidadeMedia = t.getVelocidadeMedia();
                        detalhe.tempo = t.getTempo();

                        //Traduz os IDs numéricos em nomes legíveis para o utilizador
                        // Categoria
                        if (t.getCategoriaId() == 1) detalhe.categoriaNome = "Caminhada";
                        else if (t.getCategoriaId() == 2) detalhe.categoriaNome = "Corrida";
                        else if (t.getCategoriaId() == 3) detalhe.categoriaNome = "Ciclismo";
                        else detalhe.categoriaNome = "Outro";

                        // Ambiente
                        if (t.getAmbienteId() == 1) detalhe.ambienteNome = "Interior";
                        else if (t.getAmbienteId() == 2) detalhe.ambienteNome = "Exterior";
                        else detalhe.ambienteNome = "Desconhecido";

                        // Tipo de treino
                        if (t.getTipoTreinoId() == 1) detalhe.tipoNome = "Livre";
                        else if (t.getTipoTreinoId() == 2) detalhe.tipoNome = "Programado";
                        else detalhe.tipoNome = "Outro";

                        // Percurso
                        if (t.getPercursoId() != null) {
                            if (t.getPercursoId() == 1) detalhe.percursoNome = "Percurso Curto";
                            else if (t.getPercursoId() == 2) detalhe.percursoNome = "Percurso Longo";
                            else detalhe.percursoNome = "Percurso Desconhecido";
                        } else {
                            detalhe.percursoNome = "Sem percurso";
                        }

                        listaTreinos.add(detalhe);
                    }
                }

                // ============================================================
                // ======= Sincronização com Room (guarda localmente) =========
                // ============================================================
                if (syncToRoom) {
                    // 🔹 Atualiza Room com dados da Firebase
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            // Remove os treinos antigos e insere os novos atualizados
                            List<Treino> antigos = db.treinoDao().getAllTreinos(userId);
                            for (Treino antigo : antigos) db.treinoDao().delete(antigo);

                            for (Treino novo : listaTreinosRoom) db.treinoDao().insert(novo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                // ============================================================
                // ============= Exibição no ecrã (RecyclerView) ==============
                // ============================================================

                if (!listaTreinos.isEmpty()) {
                    // Ordena por ID decrescente (treinos mais recentes primeiro)
                    listaTreinos.sort((a, b) -> Long.compare(b.id, a.id));

                    RecyclerView recyclerView = binding.recyclerTreinos;
                    recyclerView.setVisibility(View.VISIBLE);
                    binding.textNoTreinos.setVisibility(View.GONE);

                    // Define o adaptador do RecyclerView
                    recyclerView.setAdapter(new TreinoAdapter(requireContext(), listaTreinos, this::mostrarPopupTreino));
                } else {
                    // Caso o utilizador ainda não tenha treinos
                    mostrarMensagemSemTreinos();
                }

            } else {
                // Em caso de falha no Firebase, mostra erro e tenta carregar offline
                Toast.makeText(requireContext(), "Erro ao carregar treinos da Firebase!", Toast.LENGTH_SHORT).show();
                carregarTreinosOffline(userId);
            }
        });
    }

    // ============================================================
    // === Carregar treinos da base de dados local (Room) ========
    // ============================================================
    private void carregarTreinosOffline(long userId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Obtém a lista de treinos armazenada localmente
            List<TreinoComDetalhes> listaOffline = db.treinoDao().getAllTreinosDetail(userId);

            // Atualiza a interface na thread principal
            requireActivity().runOnUiThread(() -> {
                if (listaOffline != null && !listaOffline.isEmpty()) {
                    RecyclerView recyclerView = binding.recyclerTreinos;
                    recyclerView.setVisibility(View.VISIBLE);
                    binding.textNoTreinos.setVisibility(View.GONE);
                    recyclerView.setAdapter(new TreinoAdapter(requireContext(), listaOffline, this::mostrarPopupTreino));
                } else {
                    // Caso não existam treinos locais
                    mostrarMensagemSemTreinos();
                }
            });
        });
    }

    // ============================================================
    // === Verificar ligação à Internet (versão simples) ==========
    // ============================================================
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // Retorna verdadeiro se houver ligação ativa e conectada
        return networkInfo != null && networkInfo.isConnected();
    }

    // ============================================================
    // === Exibir popup com detalhes de um treino selecionado =====
    // ============================================================
    private void mostrarPopupTreino(TreinoComDetalhes treino) {

        // Infla o layout do diálogo de detalhes do treino
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_treino_detalhes, null);

        // Associa os campos do layout às variáveis
        TextView txtCategoria = dialogView.findViewById(R.id.txtCategoria);
        TextView txtData = dialogView.findViewById(R.id.txtData);
        TextView txtAmbiente = dialogView.findViewById(R.id.txtAmbiente);
        TextView txtTipo = dialogView.findViewById(R.id.txtTipo);
        TextView txtPercurso = dialogView.findViewById(R.id.txtPercurso);
        TextView txtCalorias = dialogView.findViewById(R.id.txtCalorias);
        TextView txtDistancia = dialogView.findViewById(R.id.txtDistancia);
        TextView txtTempo = dialogView.findViewById(R.id.txtTempo);
        TextView txtVelocidade = dialogView.findViewById(R.id.txtVelocidade);

        // Preenche os campos com as informações do treino selecionado
        txtCategoria.setText(treino.categoriaNome);
        txtData.setText(treino.data);
        txtAmbiente.setText(treino.ambienteNome);
        txtTipo.setText(treino.tipoNome);
        txtPercurso.setText(treino.percursoNome);
        txtCalorias.setText(String.format(Locale.getDefault(), "%d kcal", treino.calorias));
        txtDistancia.setText(String.format(Locale.getDefault(), "%.2f km", treino.distanciaPercorrida));
        txtTempo.setText(treino.tempo);
        txtVelocidade.setText(String.format(Locale.getDefault(), "%.1f km/h", treino.velocidadeMedia));

        // Cria e mostra o pop up com os detalhes
        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()
                .show();
    }

    // ============================================================
    // === Mostrar mensagem quando não existem treinos ============
    // ============================================================
    private void mostrarMensagemSemTreinos() {
        binding.textNoTreinos.setVisibility(View.VISIBLE);
        binding.recyclerTreinos.setVisibility(View.GONE);
        binding.textNoTreinos.setText("Ainda não registou nenhum treino.");
    }

    // ============================================================
    // === Limpar binding ao destruir a view =======================
    // ============================================================
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
