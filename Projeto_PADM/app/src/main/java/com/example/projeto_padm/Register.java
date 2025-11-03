package com.example.projeto_padm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
/**
 * Activity responsável pelo registo de novos utilizadores.
 *
 * Esta classe realiza:
 *     Validação dos dados introduzidos pelo utilizador;
 *     Upload e codificação da imagem de perfil (em bytes e Base64);
 *     Armazenamento local (Room) e sincronização remota (Firebase);
 *     Verificação de duplicação de email antes do registo.
 */
public class Register extends AppCompatActivity {

    // Declarações
    private EditText inputNome, inputIdade, inputPeso, inputAltura, inputEmail, inputPassword;
    private Spinner spinnerSexo;
    private MaterialButton btnRegister, btnCancel, btnUpload;
    private ImageView imageViewFoto;

    private AppDatabase db;
    private DatabaseReference databaseRef;

    private static final int PICK_IMAGE_REQUEST = 1;
    private byte[] selectedImageBytes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // --- Associação dos elementos do layout aos objetos Java ---
        inputNome = findViewById(R.id.register_nome);
        inputIdade = findViewById(R.id.register_idade);
        inputPeso = findViewById(R.id.register_peso);
        inputAltura = findViewById(R.id.register_altura);
        inputEmail = findViewById(R.id.register_email);
        inputPassword = findViewById(R.id.register_password);
        spinnerSexo = findViewById(R.id.register_sexo);
        btnRegister = findViewById(R.id.register_btnRegister);
        btnCancel = findViewById(R.id.register_btnCancel);
        btnUpload = findViewById(R.id.register_Upload);
        imageViewFoto = findViewById(R.id.register_imageView);

        // Define imagem padrão até o utilizador escolher uma
        imageViewFoto.setImageResource(R.mipmap.ic_launcher_round);

        db = AppDatabase.getInstance(getApplicationContext());
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // --- Liga os botões às respetivas ações ---
        btnUpload.setOnClickListener(v -> escolherImagem());
        btnRegister.setOnClickListener(v -> handleRegister());
        btnCancel.setOnClickListener(v -> finish());
    }

    // ============================================================
    // === SELECIONAR IMAGEM DA GALERIA ===========================
    // ============================================================
    private void escolherImagem() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Recebe o resultado da seleção de imagem e converte a imagem escolhida em bytes.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Converte o URI da imagem em Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageViewFoto.setImageBitmap(bitmap);

                // Converte o bitmap em bytes comprimidos (JPEG a 80% de qualidade)
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                selectedImageBytes = baos.toByteArray();

            } catch (IOException e) {
                Toast.makeText(this, "Erro ao carregar imagem!", Toast.LENGTH_SHORT).show();
                selectedImageBytes = null;
            }
        }
    }

    // ============================================================
    // === PROCESSAMENTO DO REGISTO ===============================
    // ============================================================

    /**
     * Valida os campos introduzidos e inicia o processo de registo do utilizador.
     */
    private void handleRegister() {
        // --- Leitura dos dados ---
        String nome = inputNome.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String sexo = spinnerSexo.getSelectedItem() == null ? "" : spinnerSexo.getSelectedItem().toString();
        String idadeStr = inputIdade.getText().toString().trim();
        String pesoStr = inputPeso.getText().toString().trim();
        String alturaStr = inputAltura.getText().toString().trim();

        // --- Verifica se todos os campos estão preenchidos ---
        if (nome.isEmpty() || email.isEmpty() || password.isEmpty() ||
                idadeStr.isEmpty() || pesoStr.isEmpty() || alturaStr.isEmpty() || sexo.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Verifica se foi carregada uma foto ---
        if (selectedImageBytes == null) {
            Toast.makeText(this, "Não te esqueças de adicionar a tua foto!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // --- Conversões de valores numéricos ---
            int idade = Integer.parseInt(idadeStr);
            float peso = Float.parseFloat(pesoStr.replace(",", "."));
            float altura = (float) Math.round(Float.parseFloat(alturaStr.replace(",", ".")) * 100) / 100f;

            // --- Encripta a palavra-passe com BCrypt ---
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // --- Verifica duplicação de email no Firebase ---
            databaseRef.orderByChild("email").equalTo(email)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(Register.this, "Já existe um utilizador com este email!", Toast.LENGTH_LONG).show();
                            } else {
                                // passa os bytes da foto como argumento
                                criarNovoUser(nome, email, hashedPassword, sexo, idade, peso, altura, selectedImageBytes);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(Register.this, "Erro ao verificar email: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ============================================================
    // === CRIAR E GUARDAR NOVO UTILIZADOR ========================
    // ============================================================

    /**
     * Cria um novo utilizador localmente e sincroniza com a Firebase.
     *
     * @param nome      Nome completo.
     * @param email     Endereço de email.
     * @param hashedPassword Palavra-passe encriptada.
     * @param sexo      Género selecionado.
     * @param idade     Idade do utilizador.
     * @param peso      Peso corporal (kg).
     * @param altura    Altura (m).
     * @param fotoBytes Imagem do perfil em bytes.
     */
    private void criarNovoUser(String nome, String email, String hashedPassword, String sexo,
                               int idade, float peso, float altura, byte[] fotoBytes) {

        if (fotoBytes == null) {  // verificação adicional de segurança
            runOnUiThread(() ->
                    Toast.makeText(this, "Não te esqueças de adicionar a tua foto!", Toast.LENGTH_LONG).show());
            return;
        }

        // --- Criação do objeto User com todos os dados ---
        User novoUser = new User(nome, email, hashedPassword, sexo, idade, peso, altura, fotoBytes);

        new Thread(() -> {
            try {
                // --- Geração de ID único com base no timestamp atual ---
                long uniqueId = System.currentTimeMillis();
                novoUser.setId(uniqueId);

                // --- Inserção local na base de dados Room ---
                db.userDao().insert(novoUser);

                // --- Converte a imagem em Base64 para armazenamento no Firebase ---
                String fotoBase64 = Base64.encodeToString(fotoBytes, Base64.DEFAULT);

                // --- Cria versão do utilizador sem foto (para o nó principal "users") ---
                User userSemFoto = new User(novoUser.getNome(), novoUser.getEmail(),
                        novoUser.getPassword(), novoUser.getSexo(),
                        novoUser.getIdade(), novoUser.getPeso(), novoUser.getAltura(), null);
                userSemFoto.setId(uniqueId);

                // --- Guarda o utilizador no Firebase ---
                databaseRef.child(String.valueOf(uniqueId))
                        .setValue(userSemFoto)
                        .addOnSuccessListener(aVoid -> {
                            // --- Após sucesso, envia a foto codificada separadamente ---
                            databaseRef.child(String.valueOf(uniqueId)).child("foto").setValue(fotoBase64);

                            runOnUiThread(() -> {
                                Toast.makeText(this, "Utilizador registado com sucesso!", Toast.LENGTH_SHORT).show();
                                finish(); // Fecha a Activity e retorna ao login
                            });
                        })
                        .addOnFailureListener(e -> runOnUiThread(() ->
                                Toast.makeText(this, "Erro ao guardar no Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show()));

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao registar utilizador: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
