package com.example.projeto_padm;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

/**
 * Classe responsável pela tela de login do utilizador.
 *
 * Esta Activity implementa um sistema de autenticação híbrido:
 * - Faz login online através do Firebase (com sincronização para Room);
 * - Suporta login offline usando os dados locais guardados no Room;
 * - Permite redefinir a palavra-passe.
 */
public class Login extends AppCompatActivity {

    // Declarações
    private Button btnLogin, btnRegister;
    private EditText inputEmail, inputPassword;
    private TextView login_forgotPassword;

    private AppDatabase db;
    private DatabaseReference firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicialização dos componentes de interface
        inputEmail = findViewById(R.id.login_email);
        inputPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.login_btnLogin);
        btnRegister = findViewById(R.id.login_btnRegister);
        login_forgotPassword = findViewById(R.id.login_forgotPassword);

        // Base de dados local (Room)
        db = AppDatabase.getInstance(getApplicationContext());

        // Referência à base de dados Firebase
        firebaseRef = FirebaseDatabase.getInstance().getReference("users");

        // ============================================================
        // === AUTO-LOGIN: Verifica se já existe sessão ativa ========
        // ============================================================
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        long savedUserId = prefs.getLong("userId", -1);
        if (savedUserId != -1) {
            // Se já existe utilizador logado → redireciona diretamente para MainActivity
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // ============================================================
        // ====================== BOTÃO REGISTAR ======================
        // ============================================================
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });

        // ============================================================
        // ==================== BOTÃO "LOGIN" =========================
        // ============================================================
        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Login executado em thread
            Executors.newSingleThreadExecutor().execute(() -> {
                if (isNetworkAvailable()) {
                    // =====================================
                    // === ONLINE LOGIN (Firebase + Sync)
                    // =====================================
                    try {
                        firebaseRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                boolean loginSuccess = false;
                                User firebaseUser = null;

                                // Verifica cada utilizador guardado no Firebase
                                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                                    try {
                                        User u = snapshot.getValue(User.class);
                                        if (u != null && u.getEmail() != null && u.getEmail().equals(email)) {
                                            if (BCrypt.checkpw(password, u.getPassword())) {
                                                firebaseUser = u;
                                                loginSuccess = true;
                                                break;
                                            }
                                        }
                                    } catch (Exception ex) {
                                        runOnUiThread(() -> Toast.makeText(Login.this,
                                                "Erro ao ler utilizador da Firebase: " + ex.getMessage(),
                                                Toast.LENGTH_LONG).show());
                                    }
                                }

                                if (loginSuccess && firebaseUser != null) {
                                    final User userToLogin = firebaseUser;

                                    // Guarda localmente no Room para suporte offline
                                    Executors.newSingleThreadExecutor().execute(() -> db.userDao().insert(userToLogin));

                                    // Sincroniza Firebase → Room e inicia sessão
                                    syncFirebaseToRoom(() -> runOnUiThread(() -> handleSuccessfulLogin(userToLogin)));
                                } else {
                                    // Se o utilizador não existir online, tenta login local
                                    runLocalLogin(email, password);
                                }

                            } else {
                                runLocalLogin(email, password);
                            }
                        });
                    } catch (Exception e) {
                        runLocalLogin(email, password);
                    }
                } else {
                    // =====================================
                    // === LOGIN OFFLINE  (Local Room)
                    // =====================================
                    runOnUiThread(() ->
                            Toast.makeText(Login.this,
                                    "Sem ligação à internet — modo offline ativado",
                                    Toast.LENGTH_SHORT).show());
                    runLocalLogin(email, password);
                }
            });
        });

        // ============================================================
        // ============== ESQUECI A PASSWORD (pop up)  ================
        // ============================================================
        login_forgotPassword.setOnClickListener(v -> showForgotPasswordPopup());
    }

    // ============================================================
    // === Verificar ligação à internet ============================
    // ============================================================
    private boolean isNetworkAvailable() {
        // Obtém o serviço do sistema responsável por conexões de rede
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        // Obtém informações sobre a rede atualmente ativa (pode ser nula)
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        // Retorna verdadeiro se houver rede e ela estiver conectada
        return activeNetwork != null && activeNetwork.isConnected();
    }

    // ============================================================
    // === LOGIN LOCAL (modo offline) ==============================
    // ============================================================
    private void runLocalLogin(String email, String password) {
        // Executa em thread separada para não bloquear a interface do utilizador
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Busca todos os utilizadores locais
                List<User> allUsers = db.userDao().getAllUsers();
                User foundUser = null;

                // Verifica se o email e a password correspondem a algum utilizador local
                for (User u : allUsers) {
                    // BCrypt.checkpw → compara a password digitada com a hash armazenada
                    if (u.getEmail().equals(email) && BCrypt.checkpw(password, u.getPassword())) {
                        foundUser = u;
                        break; // Sai do loop ao encontrar o utilizador válido
                    }
                }

                if (foundUser != null) {
                    // Login válido → chama o metodo de login bem-sucedido na thread principal
                    User finalFoundUser = foundUser;
                    runOnUiThread(() -> handleSuccessfulLogin(finalFoundUser));
                } else {
                    // Login inválido → mostra mensagem de erro
                    runOnUiThread(() ->
                            Toast.makeText(Login.this,
                                    "Email ou password incorretos!",
                                    Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                // Captura e exibe erros durante a verificação local
                runOnUiThread(() ->
                        Toast.makeText(Login.this,
                                "Erro ao verificar login local: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        });
    }

    // ============================================================
    // === SINCRONIZAÇÃO FIREBASE → ROOM ==========================
    // ============================================================
    private void syncFirebaseToRoom(Runnable onComplete) {
        firebaseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Executa em background para não bloquear a UI
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        // Itera sobre todos os utilizadores do Firebase
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            User u = snapshot.getValue(User.class);
                            if (u != null) {
                                // Insere ou substitui utilizador no Room (modo offline)
                                db.userDao().insert(u);
                            }
                        }
                        // Se existir um callback de conclusão, executa-o
                        if (onComplete != null) onComplete.run();
                    } catch (Exception e) {
                        // Em caso de erro durante a sincronização
                        runOnUiThread(() ->
                                Toast.makeText(Login.this,
                                        "Erro ao sincronizar dados: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show());
                        if (onComplete != null) onComplete.run();
                    }
                });
            } else {
                // Falha ao obter dados da Firebase
                runOnUiThread(() ->
                        Toast.makeText(Login.this,
                                "Falha ao obter dados da Firebase.",
                                Toast.LENGTH_LONG).show());
                if (onComplete != null) onComplete.run();
            }
        });
    }

    // ============================================================
    // === LOGIN BEM-SUCEDIDO =====================================
    // ============================================================
    private void handleSuccessfulLogin(User user) {
        // Armazena dados da sessão no dispositivo
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("userId", user.getId());
        editor.putString("userNome", user.getNome());
        editor.putString("userEmail", user.getEmail());
        editor.apply(); // Guarda as alterações

        // Mostra mensagem de sucesso
        Toast.makeText(Login.this, "Login efetuado com sucesso!", Toast.LENGTH_SHORT).show();

        // Redireciona o utilizador para o ecrã principal
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // ============================================================
    // === POPUP DE RECUPERAÇÃO DE SENHA ==========================
    // ============================================================
    private void showForgotPasswordPopup() {
        // Cria a interface do diálogo com base no layout XML
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);

        // Referências aos elementos da interface
        EditText emailInput = dialogView.findViewById(R.id.dialog_email);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm);
        TextView newPasswordText = dialogView.findViewById(R.id.dialog_newPassword);

        // Cria o diálogo (popup)
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Define a ação do botão "Confirmar"
        confirmButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Insira o email!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Executa a recuperação da senha em nova thread
            new Thread(() -> {
                List<User> allUsers = db.userDao().getAllUsers();
                User foundUser = null;

                // Verifica se o email existe localmente
                for (User u : allUsers) {
                    if (u.getEmail().equals(email)) {
                        foundUser = u;
                        break;
                    }
                }

                if (foundUser != null) {
                    // Gera uma nova senha aleatória
                    String newPass = generateRandomPassword(8);
                    // Criptografa a senha antes de salvar
                    String hashedPass = BCrypt.hashpw(newPass, BCrypt.gensalt());

                    // Atualiza a senha localmente (Room)
                    foundUser.setPassword(hashedPass);
                    db.userDao().update(foundUser);

                    // Atualiza a senha na Firebase
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");
                    databaseRef.child(String.valueOf(foundUser.getId()))
                            .child("password")
                            .setValue(hashedPass)
                            .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                                // Mostra a nova password ao utilizador
                                newPasswordText.setText("Nova Password: " + newPass);
                                Toast.makeText(this, "Senha redefinida com sucesso!", Toast.LENGTH_SHORT).show();
                            }))
                            .addOnFailureListener(e -> runOnUiThread(() ->
                                    Toast.makeText(this, "Erro ao atualizar no Firebase: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show()));
                } else {
                    // Email não encontrado localmente
                    runOnUiThread(() -> Toast.makeText(this, "Email não encontrado!", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        dialog.show();
    }

    // ============================================================
    // === GERADOR DE SENHAS ALEATÓRIAS ===========================
    // ============================================================
    /**
     * Gera uma nova palavra-passe aleatória composta por letras e números.
     *
     * @param length Tamanho da senha gerada.
     * @return String contendo a nova senha.
     */
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // Escolhe aleatoriamente 'length' caracteres da lista
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString(); // Retorna a senha gerada
    }
}
