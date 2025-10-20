package com.example.projeto_padm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.mindrot.jbcrypt.BCrypt;

public class Register extends AppCompatActivity {

    // --- Elementos ---
    private EditText inputNome, inputIdade, inputPeso, inputAltura, inputEmail, inputPassword;
    private Spinner spinnerSexo;
    private MaterialButton btnRegister, btnCancel;

    // --- Base de dados ---
    private AppDatabase db;
    private DatabaseReference databaseRef; // Firebase Realtime Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // --- Find Views ---
        inputNome = findViewById(R.id.register_nome);
        inputIdade = findViewById(R.id.register_idade);
        inputPeso = findViewById(R.id.register_peso);
        inputAltura = findViewById(R.id.register_altura);
        inputEmail = findViewById(R.id.register_email);
        inputPassword = findViewById(R.id.register_password);
        spinnerSexo = findViewById(R.id.register_sexo);
        btnRegister = findViewById(R.id.register_btnRegister);
        btnCancel = findViewById(R.id.register_btnCancel);

        // --- Inicializar Base de dados (Room) ---
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "users_db").build();

        // --- Initializar Firebase ---
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // --- Botão Register ---
        btnRegister.setOnClickListener(v -> handleRegister());

        // --- Botão Cancelar ---
        btnCancel.setOnClickListener(v -> finish());
    }

    // --- Logica de registo ---
    private void handleRegister() {

        // Ler valores dos inputs
        String nome = inputNome.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String sexo = spinnerSexo.getSelectedItem().toString();
        String idadeStr = inputIdade.getText().toString().trim();
        String pesoStr = inputPeso.getText().toString().trim();
        String alturaStr = inputAltura.getText().toString().trim();

        // Validar campos
        if (nome.isEmpty() || email.isEmpty() || password.isEmpty() ||
                idadeStr.isEmpty() || pesoStr.isEmpty() || alturaStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Converter valores
            int idade = Integer.parseInt(idadeStr);
            float peso = Float.parseFloat(pesoStr);
            float altura = Float.parseFloat(alturaStr);

            // Encriptar password com BCrypt
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // Novo objeto User
            User novoUser = new User(nome, email, hashedPassword, sexo, idade, peso, altura, null);

            // Guardar localmente, e depois no Firebase
            new Thread(() -> {
                long userId = db.userDao().insert(novoUser);
                novoUser.setId(userId);

                // Publicar no Firebase com o mesmo ID
                databaseRef.child(String.valueOf(userId))
                        .setValue(novoUser)
                        .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                            Toast.makeText(this,
                                    "Utilizador registado localmente e no Firebase!",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }))
                        .addOnFailureListener(e -> runOnUiThread(() ->
                                Toast.makeText(this,
                                        "Erro ao guardar no Firebase: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()));
            }).start();

        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
