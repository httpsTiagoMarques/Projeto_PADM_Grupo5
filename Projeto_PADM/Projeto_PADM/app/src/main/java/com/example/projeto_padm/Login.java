package com.example.projeto_padm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class Login extends AppCompatActivity {

    // --- Elementos ---
    private Button btnLogin, btnRegister;
    private EditText inputEmail, inputPassword;

    // --- Base de dados ---
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // --- Find Views ---
        inputEmail = findViewById(R.id.login_email);
        inputPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.login_btnLogin);
        btnRegister = findViewById(R.id.login_btnRegister);

        // --- Inicializar Base de dados (Room) ---
        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "users_db")
                .build();

        // --- Botão Register ---
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });

        // --- Botão Login ---
        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            // Validação Basica
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar user com bg thread
            new Thread(() -> {
                List<User> allUsers = db.userDao().getAllUsers();
                User foundUser = null;

                // Procurar user com respetivo email e password
                for (User u : allUsers) {
                    if (u.getEmail().equals(email) && BCrypt.checkpw(password, u.getPassword())) {
                        foundUser = u;
                        break;
                    }
                }

                User finalFoundUser = foundUser;

                runOnUiThread(() -> {
                    if (finalFoundUser != null) {
                        // --- Guardar informação ---
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putLong("userId", finalFoundUser.getId());
                        editor.putString("userNome", finalFoundUser.getNome());
                        editor.putString("userEmail", finalFoundUser.getEmail());
                        editor.apply();

                        Toast.makeText(Login.this, "Login efetuado com sucesso!", Toast.LENGTH_SHORT).show();

                        // --- Encaminhar para MainActivity ---
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(Login.this, "Email ou password incorretos!", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });
    }
}