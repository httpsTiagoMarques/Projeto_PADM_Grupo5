package com.example.projeto_padm.ui.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.projeto_padm.AppDatabase;
import com.example.projeto_padm.R;
import com.example.projeto_padm.User;
import com.example.projeto_padm.databinding.FragmentSettingBinding;
import com.example.projeto_padm.ui.home.HomeFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

public class SettingFragment extends Fragment {

    private FragmentSettingBinding binding;
    private AppDatabase db;
    private DatabaseReference userRef;
    private User currentUser;

    private static final int PICK_IMAGE_REQUEST = 1;
    private byte[] selectedImageBytes;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = AppDatabase.getInstance(requireContext());
        userRef = FirebaseDatabase.getInstance().getReference("users");

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);

        if (userId == -1) {
            Toast.makeText(requireContext(), "Erro: utilizador não encontrado!", Toast.LENGTH_SHORT).show();
            return root;
        }

        // 🔹 Buscar dados do utilizador atual (do Room)
        Executors.newSingleThreadExecutor().execute(() -> {
            currentUser = db.userDao().getUserById(userId);

            requireActivity().runOnUiThread(() -> {
                if (currentUser != null) {
                    preencherCampos(currentUser);
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar dados do utilizador!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 🔹 Upload da imagem
        binding.settingsUpload.setOnClickListener(v -> escolherImagem());

        // 🔹 Guardar alterações
        binding.settingsBtnSave.setOnClickListener(v -> guardarAlteracoes());

        return root;
    }

    private void preencherCampos(User user) {
        binding.settingsNome.setText(user.getNome());
        binding.settingsIdade.setText(String.valueOf(user.getIdade()));
        binding.settingsPeso.setText(String.valueOf(user.getPeso()));
        binding.settingsAltura.setText(String.valueOf(user.getAltura()));
        binding.settingsEmail.setText(user.getEmail());

        String sexo = user.getSexo();
        if (sexo != null) {
            String[] opcoes = getResources().getStringArray(R.array.sexo_opcoes);
            for (int i = 0; i < opcoes.length; i++) {
                if (opcoes[i].equalsIgnoreCase(sexo)) {
                    binding.settingsSexo.setSelection(i);
                    break;
                }
            }
        }

        // 🔹 Mostrar imagem de perfil se existir
        if (user.getFoto() != null && user.getFoto().length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(user.getFoto(), 0, user.getFoto().length);
            binding.settingsImageView.setImageBitmap(bitmap);
        }
    }

    private void escolherImagem() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                binding.settingsImageView.setImageBitmap(bitmap);

                // Converter imagem em byte[]
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                selectedImageBytes = baos.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Erro ao carregar imagem!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarAlteracoes() {
        if (currentUser == null) return;

        String nome = binding.settingsNome.getText().toString().trim();
        String idadeStr = binding.settingsIdade.getText().toString().trim();
        String pesoStr = binding.settingsPeso.getText().toString().trim();
        String alturaStr = binding.settingsAltura.getText().toString().trim();
        String sexo = binding.settingsSexo.getSelectedItem().toString();
        String email = binding.settingsEmail.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(idadeStr) ||
                TextUtils.isEmpty(pesoStr) || TextUtils.isEmpty(alturaStr) || TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        int idade = Integer.parseInt(idadeStr);
        float peso = Float.parseFloat(pesoStr.replace(",", "."));
        float altura = Float.parseFloat(alturaStr.replace(",", "."));

        currentUser.setNome(nome);
        currentUser.setIdade(idade);
        currentUser.setPeso(peso);
        currentUser.setAltura(altura);
        currentUser.setSexo(sexo);
        currentUser.setEmail(email);

        if (selectedImageBytes != null) {
            currentUser.setFoto(selectedImageBytes);
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            // 🔹 Atualizar no Room
            db.userDao().update(currentUser);

            // 🔹 Converter imagem em Base64 (para Firebase)
            String fotoBase64 = null;
            if (currentUser.getFoto() != null) {
                fotoBase64 = Base64.encodeToString(currentUser.getFoto(), Base64.DEFAULT);
            }

            // 🔹 Atualizar na Firebase
            DatabaseReference userNode = userRef.child(String.valueOf(currentUser.getId()));
            userNode.child("nome").setValue(currentUser.getNome());
            userNode.child("idade").setValue(currentUser.getIdade());
            userNode.child("peso").setValue(currentUser.getPeso());
            userNode.child("altura").setValue(currentUser.getAltura());
            userNode.child("sexo").setValue(currentUser.getSexo());
            userNode.child("email").setValue(currentUser.getEmail());
            if (fotoBase64 != null) userNode.child("foto").setValue(fotoBase64);

            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}