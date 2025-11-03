package com.example.projeto_padm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.projeto_padm.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.util.Base64;

import java.util.concurrent.Executors;

/**
 * Activity principal da aplicação.
 *
 * Responsável por:
 *     Configurar a barra lateral de navegação (Navigation Drawer);
 *     Exibir as informações do utilizador autenticado (nome, email, foto);
 *     Gerir a sessão ativa (logout, sincronização de imagem);
 *     Redirecionar entre os fragments (Home, Gallery, Slideshow, Settings)
 */
public class MainActivity extends AppCompatActivity {

    // Declarações
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private TextView dashboard_lbl;
    private DatabaseReference firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Define a Toolbar personalizada como barra superior
        setSupportActionBar(binding.appBarMain.toolbar);

        // Criação do Drawer (menu lateral)
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Define quais fragments fazem parte da navegação principal
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow,
                R.id.nav_settings
        ).setOpenableLayout(drawer).build();

        // Controlador de navegação entre fragments
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Label opcional de exemplo
        dashboard_lbl = findViewById(R.id.dashboard_lbl);

        // ============================================================
        // === Configuração dos dados do utilizador ===================
        // ============================================================
        View headerView = navigationView.getHeaderView(0);
        TextView userNameTextView = headerView.findViewById(R.id.main_lbl_userName);
        TextView userEmailTextView = headerView.findViewById(R.id.main_lbl_userEmail);
        ImageView userImageView = headerView.findViewById(R.id.imageView);
        LinearLayout btnLogout = headerView.findViewById(R.id.btnLogout);

        // Recupera os dados do utilizador guardados localmente na sessão
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userNome", "Nome do Utilizador");
        String userEmail = prefs.getString("userEmail", "email@exemplo.com");
        long userId = prefs.getLong("userId", -1);

        // Atualiza o cabeçalho do menu com os dados do utilizador
        userNameTextView.setText(userName);
        userEmailTextView.setText(userEmail);

        // Inicializa referências do Firebase e da base de dados local
        firebaseRef = FirebaseDatabase.getInstance().getReference("users");
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        // ============================================================
        // === Mostrar imagem do utilizador (local ou Firebase) =======
        // ============================================================
        if (userId != -1) {
            // A imagem é carregada numa thread separada para evitar bloqueios na UI
            Executors.newSingleThreadExecutor().execute(() -> {
                User user = db.userDao().getUserById(userId);

                runOnUiThread(() -> {
                    if (user != null && user.getFoto() != null && user.getFoto().length > 0) {
                        // Imagem local já existente → converte bytes para Bitmap
                        Bitmap bitmap = BitmapFactory.decodeByteArray(user.getFoto(), 0, user.getFoto().length);
                        userImageView.setImageBitmap(bitmap);
                    } else {
                        // Caso não exista imagem local, tenta buscar da Firebase
                        carregarFotoDaFirebase(userId, db, userImageView);
                    }
                });
            });
        } else {
            // Se o ID do utilizador for inválido, mostra o ícone padrão
            userImageView.setImageResource(R.mipmap.ic_launcher_round);
        }

        // ============================================================
        // =================== BOTÃO LOGOUT ============================
        // ============================================================

        btnLogout.setOnClickListener(v -> {
            // Remove todos os dados guardados do utilizador
            SharedPreferences prefsLogout = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefsLogout.edit().clear().apply();

            // Redireciona para a tela de login e limpa o histórico
            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // ============================================================
    // === Carregar foto da Firebase se faltar localmente ===
    // ============================================================
    private void carregarFotoDaFirebase(long userId, AppDatabase db, ImageView userImageView) {
        firebaseRef.child(String.valueOf(userId)).child("foto").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Recupera a string Base64 da Firebase
                    String base64 = snapshot.getValue(String.class);
                    if (base64 != null && !base64.isEmpty()) {
                        // Decodifica Base64 → bytes → Bitmap
                        byte[] fotoBytes = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
                        userImageView.setImageBitmap(bitmap);

                        // Guarda a imagem localmente no Room para uso offline
                        Executors.newSingleThreadExecutor().execute(() -> {
                            User user = db.userDao().getUserById(userId);
                            if (user != null) {
                                user.setFoto(fotoBytes);
                                db.userDao().update(user);
                            }
                        });
                    } else {
                        // Caso o utilizador não tenha imagem, mostra o ícone padrão
                        userImageView.setImageResource(R.mipmap.ic_launcher_round);
                    }
                } else {
                    // Caso o nó "foto" não exista na Firebase
                    userImageView.setImageResource(R.mipmap.ic_launcher_round);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Em caso de erro de leitura da Firebase, mostra imagem padrão
                userImageView.setImageResource(R.mipmap.ic_launcher_round);
            }
        });
    }

    // ============================================================
    // === Métodos auxiliares de navegação ========================
    // ============================================================

    // Cria o menu superior da Toolbar a partir do arquivo XML (menu/main.xml).
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Garante que o botão de navegação (menu lateral) funcione corretamente.
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Metodo auxiliar que altera dinamicamente o texto da label principal.
     * Pode ser chamado por fragments para atualizar o título do dashboard.
     */
    protected void changeString() {
        dashboard_lbl.setText(R.string.lbl_main_ultimoTreino);
    }
}
