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

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseReference treinoRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inicializa o ViewModel e o binding
        new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Recupera o ID do utilizador armazenado nas preferências
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);

        // Se não houver utilizador guardado, mostra mensagem de ausência de treino
        if (userId == -1) {
            mostrarMensagemSemTreino();
            return root;
        }

        // Inicializa a base de dados local (Room) e a referência ao Firebase
        AppDatabase db = AppDatabase.getInstance(requireContext());
        treinoRef = FirebaseDatabase.getInstance().getReference("Treinos");

        // Tenta buscar o último treino localmente (Room)
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                UltimoTreinoInfo ultimoTreino = db.treinoDao().getUltimoTreino(userId);

                if (ultimoTreino != null) {
                    // Se encontrar localmente, mostra o treino no ecrã
                    requireActivity().runOnUiThread(() -> mostrarTreino(ultimoTreino));
                } else {
                    // Se não existir localmente, busca na Firebase
                    buscarUltimoTreinoFirebase(db, userId);
                }
            } catch (Exception e) {
                // Caso ocorra erro no acesso local, tenta buscar na Firebase
                buscarUltimoTreinoFirebase(db, userId);
            }
        });

        return root;
    }

    // Busca o último treino do utilizador na Firebase
    private void buscarUltimoTreinoFirebase(AppDatabase db, long userId) {
        treinoRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Treino ultimoTreino = null;

                // Itera sobre os registos do Firebase e encontra o treino mais recente do utilizador
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

                    // Busca o nome da categoria correspondente localmente
                    Executors.newSingleThreadExecutor().execute(() -> {
                        Categoria categoria = db.categoriaDao().getCategoriaById(finalTreino.getCategoriaId());
                        String nomeCategoria = (categoria != null) ? categoria.getNome() : "Sem categoria";

                        // Guarda o treino no Room (cache local)
                        try {
                            db.treinoDao().insert(finalTreino);
                        } catch (Exception ignored) {}

                        // Mostra os dados do treino no ecrã principal
                        requireActivity().runOnUiThread(() ->
                                mostrarTreinoFirebase(finalTreino, nomeCategoria));
                    });

                } else {
                    // Caso o utilizador não tenha treinos
                    requireActivity().runOnUiThread(this::mostrarMensagemSemTreino);
                }

            } else {
                // Caso haja erro na comunicação com a Firebase
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Erro ao buscar dados na Firebase!", Toast.LENGTH_SHORT).show();
                    mostrarMensagemSemTreino();
                });
            }
        });
    }

    // Exibe as informações do treino obtido localmente
    private void mostrarTreino(UltimoTreinoInfo treino) {
        binding.dashboardLbl.setText(getString(R.string.lbl_main_ultimoTreino));

        binding.cardTipoTreino.setVisibility(View.VISIBLE);
        binding.cardCalorias.setVisibility(View.VISIBLE);
        binding.cardTempo.setVisibility(View.VISIBLE);
        binding.cardDistancia.setVisibility(View.VISIBLE);
        binding.cardVelocidade.setVisibility(View.VISIBLE);

        binding.tvTipoTreino.setText(treino.nomeCategoria);
        binding.tvCalorias.setText(treino.calorias + " kcal");
        binding.tvTempo.setText(treino.tempo);
        binding.tvDistancia.setText(treino.distanciaPercorrida + " km");
        binding.tvVelocidade.setText(treino.velocidadeMedia + " km/h");
    }

    // Exibe as informações do treino obtido na Firebase
    private void mostrarTreinoFirebase(Treino treino, String nomeCategoria) {
        binding.dashboardLbl.setText(getString(R.string.lbl_main_ultimoTreino));

        binding.cardTipoTreino.setVisibility(View.VISIBLE);
        binding.cardCalorias.setVisibility(View.VISIBLE);
        binding.cardTempo.setVisibility(View.VISIBLE);
        binding.cardDistancia.setVisibility(View.VISIBLE);
        binding.cardVelocidade.setVisibility(View.VISIBLE);

        // Mostra o nome da categoria em vez do ID
        binding.tvTipoTreino.setText(nomeCategoria);
        binding.tvCalorias.setText(treino.getCalorias() + " kcal");
        binding.tvTempo.setText(treino.getTempo());
        binding.tvDistancia.setText(treino.getDistanciaPercorrida() + " km");
        binding.tvVelocidade.setText(treino.getVelocidadeMedia() + " km/h");
    }

    // Mostra uma mensagem caso o utilizador não tenha treinos
    private void mostrarMensagemSemTreino() {
        binding.cardTipoTreino.setVisibility(View.GONE);
        binding.cardCalorias.setVisibility(View.GONE);
        binding.cardTempo.setVisibility(View.GONE);
        binding.cardDistancia.setVisibility(View.GONE);
        binding.cardVelocidade.setVisibility(View.GONE);
        binding.dashboardLbl.setText(getString(R.string.lbl_main_empty));
    }

    // Limpa o binding quando a view é destruída
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}