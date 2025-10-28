package com.example.projeto_padm.ui.slideshow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
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

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private DatabaseReference treinoRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerTreinos;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        treinoRef = FirebaseDatabase.getInstance().getReference("Treinos");

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);

        if (userId == -1) {
            binding.textNoTreinos.setText("Erro: utilizador não encontrado.");
            binding.textNoTreinos.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return root;
        }

        // 🔹 Buscar diretamente na Firebase
        buscarTreinosFirebase(userId);

        return root;
    }

    private void buscarTreinosFirebase(long userId) {
        treinoRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<TreinoComDetalhes> listaTreinos = new ArrayList<>();

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Treino t = snapshot.getValue(Treino.class);
                    if (t != null && t.getUserId() == userId) {
                        TreinoComDetalhes detalhe = new TreinoComDetalhes();
                        detalhe.id = t.getId();
                        detalhe.data = t.getData();
                        detalhe.calorias = t.getCalorias();
                        detalhe.distanciaPercorrida = t.getDistanciaPercorrida();
                        detalhe.velocidadeMedia = t.getVelocidadeMedia();
                        detalhe.tempo = t.getTempo();

                        // 🔹 Nome da categoria por ID
                        if (t.getCategoriaId() == 1) detalhe.categoriaNome = "Caminhada";
                        else if (t.getCategoriaId() == 2) detalhe.categoriaNome = "Corrida";
                        else if (t.getCategoriaId() == 3) detalhe.categoriaNome = "Ciclismo";
                        else detalhe.categoriaNome = "Outro";

                        // 🔹 Ambiente
                        if (t.getAmbienteId() == 1) detalhe.ambienteNome = "Interior";
                        else if (t.getAmbienteId() == 2) detalhe.ambienteNome = "Exterior";
                        else detalhe.ambienteNome = "Desconhecido";

                        // 🔹 Tipo de treino
                        if (t.getTipoTreinoId() == 1) detalhe.tipoNome = "Livre";
                        else if (t.getTipoTreinoId() == 2) detalhe.tipoNome = "Programado";
                        else detalhe.tipoNome = "Outro";

                        // 🔹 Percurso
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

                if (!listaTreinos.isEmpty()) {
                    // Ordenar por ID decrescente (mais recente primeiro)
                    listaTreinos.sort((a, b) -> Long.compare(b.id, a.id));

                    RecyclerView recyclerView = binding.recyclerTreinos;
                    recyclerView.setVisibility(View.VISIBLE);
                    binding.textNoTreinos.setVisibility(View.GONE);

                    recyclerView.setAdapter(new TreinoAdapter(requireContext(), listaTreinos, this::mostrarPopupTreino));

                } else {
                    mostrarMensagemSemTreinos();
                }

            } else {
                Toast.makeText(requireContext(), "Erro ao carregar treinos da Firebase!", Toast.LENGTH_SHORT).show();
                mostrarMensagemSemTreinos();
            }
        });
    }

    private void mostrarPopupTreino(TreinoComDetalhes treino) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_treino_detalhes, null);

        TextView txtCategoria = dialogView.findViewById(R.id.txtCategoria);
        TextView txtData = dialogView.findViewById(R.id.txtData);
        TextView txtAmbiente = dialogView.findViewById(R.id.txtAmbiente);
        TextView txtTipo = dialogView.findViewById(R.id.txtTipo);
        TextView txtPercurso = dialogView.findViewById(R.id.txtPercurso);
        TextView txtCalorias = dialogView.findViewById(R.id.txtCalorias);
        TextView txtDistancia = dialogView.findViewById(R.id.txtDistancia);
        TextView txtTempo = dialogView.findViewById(R.id.txtTempo);
        TextView txtVelocidade = dialogView.findViewById(R.id.txtVelocidade);

        txtCategoria.setText(treino.categoriaNome);
        txtData.setText(treino.data);
        txtAmbiente.setText(treino.ambienteNome);
        txtTipo.setText(treino.tipoNome);
        txtPercurso.setText(treino.percursoNome);
        txtCalorias.setText(String.format(Locale.getDefault(), "%d kcal", treino.calorias));
        txtDistancia.setText(String.format(Locale.getDefault(), "%.1f km", treino.distanciaPercorrida));
        txtTempo.setText(treino.tempo);
        txtVelocidade.setText(String.format(Locale.getDefault(), "%.1f km/h", treino.velocidadeMedia));

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)

                .create()
                .show();
    }

    private void mostrarMensagemSemTreinos() {
        binding.textNoTreinos.setVisibility(View.VISIBLE);
        binding.recyclerTreinos.setVisibility(View.GONE);
        binding.textNoTreinos.setText("Ainda não registou nenhum treino.");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
