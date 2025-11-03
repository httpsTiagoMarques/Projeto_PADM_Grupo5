package com.example.projeto_padm.ui.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
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

import com.example.projeto_padm.AppDatabase;
import com.example.projeto_padm.R;
import com.example.projeto_padm.User;
import com.example.projeto_padm.databinding.FragmentSettingBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

// Fragmento responsável pelas definições de perfil do utilizador
public class SettingFragment extends Fragment {

    // Declarações
    private FragmentSettingBinding binding;
    private AppDatabase db;
    private DatabaseReference userRef;
    private DatabaseReference treinoRef;
    private User currentUser;

    private static final int PICK_IMAGE_REQUEST = 1;
    private byte[] selectedImageBytes;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializa BD local e referências Firebase
        db = AppDatabase.getInstance(requireContext());
        userRef = FirebaseDatabase.getInstance().getReference("users");
        treinoRef = FirebaseDatabase.getInstance().getReference("Treinos");

        // Obtém o ID do utilizador guardado nas preferências
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);

        // Se o utilizador não for encontrado, mostra erro
        if (userId == -1) {
            Toast.makeText(requireContext(), "Erro: utilizador não encontrado!", Toast.LENGTH_SHORT).show();
            return root;
        }

        // Carrega dados do utilizador localmente
        Executors.newSingleThreadExecutor().execute(() -> {
            currentUser = db.userDao().getUserById(userId);
            requireActivity().runOnUiThread(() -> {
                if (currentUser != null) {
                    preencherCampos(currentUser); // Preenche os campos no layout
                    // Se não houver foto local, tenta carregar da Firebase
                    if (currentUser.getFoto() == null || currentUser.getFoto().length == 0) {
                        carregarFotoDaFirebase(currentUser.getId());
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar dados do utilizador!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Configura os botões
        binding.settingsUpload.setOnClickListener(v -> escolherImagem());
        binding.settingsBtnSave.setOnClickListener(v -> guardarAlteracoes());
        binding.settingsBtnDelete.setOnClickListener(v -> confirmarRemocaoConta());

        return root;
    }

    // ============================================================
    // ============== Verificar conexão com a Internet ============
    // ============================================================
    private boolean isOnline() {
        // Obtém o serviço de conectividade do sistema (para verificar estado da rede)
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Se o serviço não estiver disponível (situação muito rara), considera offline
        if (cm == null) return false;

        // Obtém a rede atualmente ativa (pode ser Wi-Fi, dados móveis, etc.)
        Network network = cm.getActiveNetwork();

        // Se não houver rede ativa, o dispositivo está offline
        if (network == null) return false;

        // Obtém as capacidades dessa rede (ex.: Wi-Fi, dados móveis, ethernet)
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);

        // Retorna verdadeiro se a rede for válida e tiver transporte ativo (Wi-Fi, dados ou ethernet)
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }

    // ============================================================
    // === Carregar foto da Firebase ===
    // ============================================================
    private void carregarFotoDaFirebase(long userId) {
        // Acede ao nó "users/{userId}/foto" na base de dados Firebase
        userRef.child(String.valueOf(userId)).child("foto").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Executa quando os dados são recebidos da Firebase
                if (snapshot.exists()) { // Verifica se o nó "foto" existe
                    String base64 = snapshot.getValue(String.class); // Lê a string Base64 armazenada no Firebase

                    // Se a string não estiver vazia, converte-a em bytes
                    if (base64 != null && !base64.isEmpty()) {
                        byte[] fotoBytes = Base64.decode(base64, Base64.DEFAULT);  // Decodifica Base64 em array de bytes

                        // Atualiza o utilizador atual com a nova imagem
                        currentUser.setFoto(fotoBytes);

                        // Atualiza a base de dados local (Room) em thread separada
                        Executors.newSingleThreadExecutor().execute(() -> db.userDao().update(currentUser));

                        // Converte os bytes da imagem num Bitmap e mostra na ImageView
                        Bitmap bitmap = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
                        binding.settingsImageView.setImageBitmap(bitmap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Caso haja erro de comunicação com a Firebase
                Toast.makeText(requireContext(), "Erro ao carregar foto: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ============================================================
    // ============ Preencher campos do utilizador ================
    // ============================================================
    private void preencherCampos(User user) {
        // Define os valores do utilizador atual nos respetivos campos do layout
        binding.settingsNome.setText(user.getNome());
        binding.settingsIdade.setText(String.valueOf(user.getIdade()));
        binding.settingsPeso.setText(String.valueOf(user.getPeso()));
        binding.settingsAltura.setText(String.format("%.2f", user.getAltura()));
        binding.settingsEmail.setText(user.getEmail());
        binding.settingsPassword.setText(""); // Por segurança, o campo de password é deixado vazio

        // Seleciona o sexo no spinner (baseado no valor guardado na BD)
        String sexo = user.getSexo();
        if (sexo != null) {
            String[] opcoes = getResources().getStringArray(R.array.sexo_opcoes);
            for (int i = 0; i < opcoes.length; i++) {
                // Compara ignorando maiúsculas/minúsculas
                if (opcoes[i].equalsIgnoreCase(sexo)) {
                    binding.settingsSexo.setSelection(i);
                    break;
                }
            }
        }

        // Mostra a imagem de perfil (se existir) ou o ícone padrão
        if (user.getFoto() != null && user.getFoto().length > 0) {
            // Converte o array de bytes para Bitmap e mostra na ImageView
            Bitmap bitmap = BitmapFactory.decodeByteArray(user.getFoto(), 0, user.getFoto().length);
            binding.settingsImageView.setImageBitmap(bitmap);
        } else {
            // Usa a imagem padrão da aplicação
            binding.settingsImageView.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    // ============================================================
    // ============= Escolher imagem da galeria ===================
    // ============================================================
    private void escolherImagem() {
        // Cria um Intent para abrir a galeria do dispositivo
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Inicia a atividade esperando o resultado (imagem selecionada)
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // ============================================================
    // ============== Resultado da seleção da imagem ==============
    // ============================================================
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Verifica se o pedido corresponde à seleção da imagem e se foi bem-sucedido
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData(); // Obtém o URI da imagem escolhida
            try {
                // Converte o URI num objeto Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);

                // Exibe a imagem selecionada no ecrã
                binding.settingsImageView.setImageBitmap(bitmap);

                // Converte o Bitmap para bytes (JPEG comprimido a 80%)
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                selectedImageBytes = baos.toByteArray(); // Guarda a imagem em bytes
            } catch (IOException e) {
                // Trata erros de leitura da imagem
                e.printStackTrace();
                Toast.makeText(requireContext(), "Erro ao carregar imagem!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ============================================================
    // ====== Guardar alterações no perfil (apenas online) ========
    // ============================================================
    private void guardarAlteracoes() {
        if (currentUser == null) return; // Se não houver utilizador carregado, sai

        // Bloqueia alterações se o utilizador estiver offline
        if (!isOnline()) {
            Toast.makeText(requireContext(), "Precisas de estar ligado à internet para atualizar o perfil!", Toast.LENGTH_LONG).show();
            return;
        }

        // Obtém os valores introduzidos nos campos
        String nome = binding.settingsNome.getText().toString().trim();
        String idadeStr = binding.settingsIdade.getText().toString().trim();
        String pesoStr = binding.settingsPeso.getText().toString().trim();
        String alturaStr = binding.settingsAltura.getText().toString().trim();
        String sexo = binding.settingsSexo.getSelectedItem().toString();
        String email = binding.settingsEmail.getText().toString().trim();
        String novaPassword = binding.settingsPassword.getText().toString().trim();

        // Valida que todos os campos obrigatórios estão preenchidos
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(idadeStr) ||
                TextUtils.isEmpty(pesoStr) || TextUtils.isEmpty(alturaStr) || TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Converte valores de texto para números
        int idade = Integer.parseInt(idadeStr);
        float peso = Float.parseFloat(pesoStr.replace(",", "."));
        float altura = Float.parseFloat(alturaStr.replace(",", "."));

        // Atualiza os dados do objeto local do utilizador
        currentUser.setNome(nome);
        currentUser.setIdade(idade);
        currentUser.setPeso(peso);
        currentUser.setAltura(altura);
        currentUser.setSexo(sexo);
        currentUser.setEmail(email);

        // Se o campo de password foi preenchido, gera novo hash bcrypt
        if (!TextUtils.isEmpty(novaPassword)) {
            String hashedPassword = BCrypt.hashpw(novaPassword, BCrypt.gensalt());
            currentUser.setPassword(hashedPassword);
        }

        // Se uma nova imagem foi escolhida, atualiza o campo de foto
        if (selectedImageBytes != null) {
            currentUser.setFoto(selectedImageBytes);
        }

        // Atualiza os dados localmente e na Firebase em background
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Converte a foto para Base64 (para guardar na Firebase)
                String fotoBase64 = null;
                if (currentUser.getFoto() != null) {
                    fotoBase64 = Base64.encodeToString(currentUser.getFoto(), Base64.DEFAULT);
                }

                // Acede ao nó do utilizador na Firebase e atualiza os campos
                DatabaseReference userNode = userRef.child(String.valueOf(currentUser.getId()));
                userNode.child("nome").setValue(currentUser.getNome());
                userNode.child("idade").setValue(currentUser.getIdade());
                userNode.child("peso").setValue(currentUser.getPeso());
                userNode.child("altura").setValue(currentUser.getAltura());
                userNode.child("sexo").setValue(currentUser.getSexo());
                userNode.child("email").setValue(currentUser.getEmail());
                if (!TextUtils.isEmpty(novaPassword)) {
                    userNode.child("password").setValue(currentUser.getPassword());
                }
                if (fotoBase64 != null) userNode.child("foto").setValue(fotoBase64);

                // Atualiza a base de dados local
                db.userDao().update(currentUser);

                // Mensagem de sucesso no UI thread
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                // Mostra erro, se algo falhar
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Erro ao atualizar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    // ============================================================
    // ======== Confirma Remover conta (só se online) =============
    // ============================================================
    private void confirmarRemocaoConta() {
        if (currentUser == null) return;

        // Garante que há ligação à Internet antes de continuar
        if (!isOnline()) {
            Toast.makeText(requireContext(), "Precisas de estar ligado à internet para remover a conta!", Toast.LENGTH_LONG).show();
            return;
        }

        // Cria uma caixa de diálogo para confirmar a remoção da conta
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Confirmar remoção");
        builder.setMessage("Insira a sua palavra-passe para confirmar:");

        // Cria um campo de input para digitar a palavra-passe
        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Botão "Confirmar"
        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String passwordInput = input.getText().toString().trim();

            // Se o campo estiver vazio, alerta o utilizador
            if (passwordInput.isEmpty()) {
                Toast.makeText(requireContext(), "Introduza a palavra-passe!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verifica se a palavra-passe introduzida corresponde ao hash guardado
            if (BCrypt.checkpw(passwordInput, currentUser.getPassword())) {
                removerConta(); // Se correta, prossegue com a remoção
            } else {
                Toast.makeText(requireContext(), "Palavra-passe incorreta!", Toast.LENGTH_SHORT).show();
            }
        });

        // Botão "Cancelar"
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // ============================================================
    // ======== Remover conta (dados locais e Firebase) ============
    // ============================================================
    private void removerConta() {
        // Verifica novamente se há ligação à Internet antes de continuar
        if (!isOnline()) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Precisas de estar ligado à internet para remover a conta!", Toast.LENGTH_LONG).show());
            return;
        }

        // Executa em background para não bloquear a interface
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                long userId = currentUser.getId();

                // Remove todos os treinos locais do utilizador da base de dados Room
                db.treinoDao().getAllTreinos(userId)
                        .forEach(treino -> db.treinoDao().delete(treino));

                // Apaga também todos os treinos desse utilizador na Firebase
                treinoRef.orderByChild("userId").equalTo(userId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // Itera sobre cada treino e remove
                                for (DataSnapshot treinoSnapshot : snapshot.getChildren()) {
                                    treinoSnapshot.getRef().removeValue();
                                }

                                // Remove o próprio utilizador na Firebase
                                userRef.child(String.valueOf(userId)).removeValue();

                                // Apaga também da base de dados local
                                Executors.newSingleThreadExecutor().execute(() -> db.userDao().delete(currentUser));

                                // No UI thread, limpa sessão e redireciona para o login
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Conta removida com sucesso!", Toast.LENGTH_LONG).show();

                                    // Limpa as preferências guardadas (sessão do utilizador)
                                    SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                    prefs.edit().clear().apply();

                                    // Abre o ecra de login
                                    Intent intent = new Intent(requireContext(), com.example.projeto_padm.Login.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    requireActivity().finish();  // Fecha a atividade atual
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Se algo falhar ao remover dados da Firebase
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Erro ao remover treinos: " + error.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        });
            } catch (Exception e) {
                // Captura qualquer erro inesperado e mostra mensagem
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Erro ao remover conta: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
