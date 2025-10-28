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

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private TextView dashboard_lbl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow,
                R.id.nav_settings
        ).setOpenableLayout(drawer).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        dashboard_lbl = findViewById(R.id.dashboard_lbl);

        // ============================================================
        // ============ Display user info in navigation header =========
        // ============================================================

        View headerView = navigationView.getHeaderView(0);
        TextView userNameTextView = headerView.findViewById(R.id.main_lbl_userName);
        TextView userEmailTextView = headerView.findViewById(R.id.main_lbl_userEmail);
        ImageView userImageView = headerView.findViewById(R.id.imageView);
        LinearLayout btnLogout = headerView.findViewById(R.id.btnLogout);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userNome", "Nome do Utilizador");
        String userEmail = prefs.getString("userEmail", "email@exemplo.com");
        long userId = prefs.getLong("userId", -1);

        userNameTextView.setText(userName);
        userEmailTextView.setText(userEmail);

        // ============================================================
        // ========== Mostrar imagem do utilizador (ou padrão) ========
        // ============================================================

        if (userId != -1) {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());

            Executors.newSingleThreadExecutor().execute(() -> {
                User user = db.userDao().getUserById(userId);

                runOnUiThread(() -> {
                    if (user != null && user.getFoto() != null && user.getFoto().length > 0) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(user.getFoto(), 0, user.getFoto().length);
                        userImageView.setImageBitmap(bitmap);
                    } else {
                        // Caso não tenha imagem → mantém o ícone da app
                        userImageView.setImageResource(R.mipmap.ic_launcher_round);
                    }
                });
            });
        } else {
            // Caso não haja utilizador logado
            userImageView.setImageResource(R.mipmap.ic_launcher_round);
        }

        // ============================================================
        // =================== BOTÃO LOGOUT ============================
        // ============================================================

        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefsLogout = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefsLogout.edit().clear().apply();

            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    protected void changeString() {
        dashboard_lbl.setText(R.string.lbl_main_ultimoTreino);
    }
}