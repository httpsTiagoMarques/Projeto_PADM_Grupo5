package com.example.projeto_padm;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Random;

public class Login extends AppCompatActivity {

    private Button btnLogin, btnRegister;
    private EditText inputEmail, inputPassword;
    private TextView login_forgotPassword;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.login_email);
        inputPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.login_btnLogin);
        btnRegister = findViewById(R.id.login_btnRegister);
        login_forgotPassword = findViewById(R.id.login_forgotPassword);

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

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                List<User> allUsers = db.userDao().getAllUsers();
                User foundUser = null;

                for (User u : allUsers) {
                    if (u.getEmail().equals(email) && BCrypt.checkpw(password, u.getPassword())) {
                        foundUser = u;
                        break;
                    }
                }

                User finalFoundUser = foundUser;

                runOnUiThread(() -> {
                    if (finalFoundUser != null) {
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putLong("userId", finalFoundUser.getId());
                        editor.putString("userNome", finalFoundUser.getNome());
                        editor.putString("userEmail", finalFoundUser.getEmail());
                        editor.apply();

                        Toast.makeText(Login.this, "Login efetuado com sucesso!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(Login.this, "Email ou password incorretos!", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        // --- Botão Forgot Password ---
        login_forgotPassword.setOnClickListener(v -> showForgotPasswordPopup());
    }

    // --- Método para mostrar o pop-up ---
    private void showForgotPasswordPopup() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);

        EditText emailInput = dialogView.findViewById(R.id.dialog_email);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm);
        TextView newPasswordText = dialogView.findViewById(R.id.dialog_newPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        confirmButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Insira o email!", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                // Procurar utilizador pelo email
                List<User> allUsers = db.userDao().getAllUsers();
                User foundUser = null;

                for (User u : allUsers) {
                    if (u.getEmail().equals(email)) {
                        foundUser = u;
                        break;
                    }
                }

                if (foundUser != null) {
                    // Gerar nova password aleatória
                    String newPass = generateRandomPassword(8);
                    String hashedPass = BCrypt.hashpw(newPass, BCrypt.gensalt());

                    // Atualizar password no objeto e guardar na BD local
                    foundUser.setPassword(hashedPass);
                    db.userDao().update(foundUser);

                    // Atualizar também na Firebase
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");
                    databaseRef.child(String.valueOf(foundUser.getId()))
                            .child("password")
                            .setValue(hashedPass)
                            .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                                newPasswordText.setText("Nova Password: " + newPass);
                                Toast.makeText(this, "Senha redefinida com sucesso!", Toast.LENGTH_SHORT).show();
                            }))
                            .addOnFailureListener(e -> runOnUiThread(() ->
                                    Toast.makeText(this, "Erro ao atualizar no Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show()));

                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Email não encontrado!", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        dialog.show();
    }

    // --- Gerar nova password aleatória ---
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
