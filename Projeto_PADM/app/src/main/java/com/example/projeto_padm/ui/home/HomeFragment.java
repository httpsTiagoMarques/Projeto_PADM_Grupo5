package com.example.projeto_padm.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projeto_padm.AppDatabase;
import com.example.projeto_padm.Categoria;
import com.example.projeto_padm.R;
import com.example.projeto_padm.Treino;
import com.example.projeto_padm.UltimoTreinoInfo;
import com.example.projeto_padm.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.Executors;

// Fragmento responsável por exibir o último treino do utilizador
public class HomeFragment extends Fragment {

    // Declarações

    private FragmentHomeBinding binding; // Ligação ao layout
    private DatabaseReference treinoRef; // Referência à base de dados Firebase

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inicializa o ViewModel (não usado diretamente aqui)
        new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Obtém o ID do utilizador guardado nas preferências
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);

        // Se o utilizador não estiver autenticado, mostra mensagem
        if (userId == -1) {
            mostrarMensagemSemTreino();
            return root;
        }

        // Liga à base de dados local e à Firebase
        AppDatabase db = AppDatabase.getInstance(requireContext());
        treinoRef = FirebaseDatabase.getInstance().getReference("Treinos");

        // Tenta primeiro buscar o último treino localmente
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                UltimoTreinoInfo ultimoTreino = db.treinoDao().getUltimoTreino(userId);

                if (ultimoTreino != null) {
                    // Se encontrou localmente, mostra
                    requireActivity().runOnUiThread(() -> mostrarTreino(ultimoTreino));
                } else {
                    // Caso contrário, tenta buscar na Firebase
                    buscarUltimoTreinoFirebase(db, userId);
                }
            } catch (Exception e) {
                // Se ocorrer erro, tenta buscar na Firebase
                buscarUltimoTreinoFirebase(db, userId);
            }
        });

        return root;
    }

    // Função para obter o último treino da Firebase
    private void buscarUltimoTreinoFirebase(AppDatabase db, long userId) {
        treinoRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Treino ultimoTreino = null;

                // Percorre todos os treinos da Firebase e seleciona o último do utilizador
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Treino t = snapshot.getValue(Treino.class);
                    if (t != null && t.getUserId() == userId) {
                        if (ultimoTreino == null || t.getId() > ultimoTreino.getId()) {
                            ultimoTreino = t;
                        }
                    }
                }

                if (ultimoTreino != null) {
                    Treino finalTreino = ultimoTreino;

                    // Buscar o nome da categoria localmente
                    Executors.newSingleThreadExecutor().execute(() -> {
                        Categoria categoria = db.categoriaDao().getCategoriaById(finalTreino.getCategoriaId());
                        String nomeCategoria = (categoria != null) ? categoria.getNome() : "Sem categoria";

                        // Guarda o treino obtido na base de dados local (Room)
                        try {
                            db.treinoDao().insert(finalTreino);
                        } catch (Exception ignored) {}

                        // Atualiza a UI com os dados
                        requireActivity().runOnUiThread(() ->
                                mostrarTreinoFirebase(finalTreino, nomeCategoria));
                    });

                } else {
                    // Nenhum treino encontrado
                    requireActivity().runOnUiThread(this::mostrarMensagemSemTreino);
                }

            } else {
                // Erro de comunicação com Firebase
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Erro ao buscar dados na Firebase!", Toast.LENGTH_SHORT).show();
                    mostrarMensagemSemTreino();
                });
            }
        });
    }

    // Mostra treino obtido da base de dados local
    private void mostrarTreino(UltimoTreinoInfo treino) {
        binding.dashboardLbl.setText(getString(R.string.lbl_main_ultimoTreino));

        // Torna os cartões visíveis
        binding.cardTipoTreino.setVisibility(View.VISIBLE);
        binding.cardCalorias.setVisibility(View.VISIBLE);
        binding.cardTempo.setVisibility(View.VISIBLE);
        binding.cardDistancia.setVisibility(View.VISIBLE);
        binding.cardVelocidade.setVisibility(View.VISIBLE);

        // Exibe as informações do último treino
        binding.tvTipoTreino.setText(treino.nomeCategoria);
        binding.tvCalorias.setText(treino.calorias + " kcal");
        binding.tvTempo.setText(treino.tempo);
        binding.tvDistancia.setText(treino.distanciaPercorrida + " km");
        binding.tvVelocidade.setText(treino.velocidadeMedia + " km/h");
    }

    // Mostra treino obtido diretamente da Firebase
    private void mostrarTreinoFirebase(Treino treino, String nomeCategoria) {
        binding.dashboardLbl.setText(getString(R.string.lbl_main_ultimoTreino));

        // Torna os cartões visíveis
        binding.cardTipoTreino.setVisibility(View.VISIBLE);
        binding.cardCalorias.setVisibility(View.VISIBLE);
        binding.cardTempo.setVisibility(View.VISIBLE);
        binding.cardDistancia.setVisibility(View.VISIBLE);
        binding.cardVelocidade.setVisibility(View.VISIBLE);

        // Exibe as informações do último treino
        binding.tvTipoTreino.setText(nomeCategoria);
        binding.tvCalorias.setText(treino.getCalorias() + " kcal");
        binding.tvTempo.setText(treino.getTempo());
        binding.tvDistancia.setText(treino.getDistanciaPercorrida() + " km");
        binding.tvVelocidade.setText(treino.getVelocidadeMedia() + " km/h");
    }

    // Mostra mensagem quando não há treino
    private void mostrarMensagemSemTreino() {
        binding.cardTipoTreino.setVisibility(View.GONE);
        binding.cardCalorias.setVisibility(View.GONE);
        binding.cardTempo.setVisibility(View.GONE);
        binding.cardDistancia.setVisibility(View.GONE);
        binding.cardVelocidade.setVisibility(View.GONE);
        binding.dashboardLbl.setText(getString(R.string.lbl_main_empty));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Liberta referência ao layout
    }
}
