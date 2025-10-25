package com.example.projeto_padm.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import com.example.projeto_padm.AppDatabase;
import com.example.projeto_padm.R;
import com.example.projeto_padm.UltimoTreinoInfo;
import com.example.projeto_padm.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // === Get userId from SharedPreferences ===
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);

        // === Build the Room database ===
        AppDatabase db = Room.databaseBuilder(
                        requireContext(),
                        AppDatabase.class,
                        "AppDatabase"
                )
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        // === Fetch the most recent training record for this user ===
        UltimoTreinoInfo ultimoTreino = db.treinoDao().getUltimoTreino(userId);

        // === Update the TextViews with query results ===
        if (ultimoTreino != null) {
            binding.dashboardLbl.setText(getString(R.string.lbl_main_ultimoTreino));
            // Show cards
            binding.cardTipoTreino.setVisibility(View.VISIBLE);
            binding.cardCalorias.setVisibility(View.VISIBLE);
            binding.cardTempo.setVisibility(View.VISIBLE);
            binding.cardDistancia.setVisibility(View.VISIBLE);
            binding.cardVelocidade.setVisibility(View.VISIBLE);

            // Update values
            binding.tvTipoTreino.setText(ultimoTreino.nomeCategoria);
            binding.tvCalorias.setText(ultimoTreino.calorias + " kcal");
            binding.tvTempo.setText(ultimoTreino.tempo);
            binding.tvDistancia.setText(ultimoTreino.distanciaPercorrida + " km");
            binding.tvVelocidade.setText(ultimoTreino.velocidadeMedia + " km/h");
        } else {
            // Hide all cards
            binding.cardTipoTreino.setVisibility(View.GONE);
            binding.cardCalorias.setVisibility(View.GONE);
            binding.cardTempo.setVisibility(View.GONE);
            binding.cardDistancia.setVisibility(View.GONE);
            binding.cardVelocidade.setVisibility(View.GONE);

            // Keep dashboard label text from XML (@string/lbl_main_empty)
            binding.dashboardLbl.setText(getString(com.example.projeto_padm.R.string.lbl_main_empty));
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}