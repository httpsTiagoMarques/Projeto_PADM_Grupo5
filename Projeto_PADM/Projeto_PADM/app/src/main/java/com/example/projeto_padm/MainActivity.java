package com.example.projeto_padm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.projeto_padm.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // --- Elementos ---
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private TextView dashboard_lbl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- Ligação ao layout (View Binding) ---
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- Definir a toolbar (barra superior) ---
        setSupportActionBar(binding.appBarMain.toolbar);

        // --- Ação do botão flutuante (FAB) ---
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Aqui vamos adicionar um novo treino", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab)
                        .show();
            }
        });

        // --- Configuração do DrawerLayout (menu lateral) ---
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // --- Configuração das opções do menu (destinos principais) ---
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        // --- Configurar a navegação (ligação entre menu e fragmentos) ---
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // --- Ligação do TextView principal do dashboard ---
        dashboard_lbl = findViewById(R.id.dashboard_lbl);

        // ============================================================
        // ============ Mostrar nome e email do utilizador ============
        // ============================================================

        // Aceder ao header (parte superior) do NavigationView
        View headerView = navigationView.getHeaderView(0);
        TextView userNameTextView = headerView.findViewById(R.id.main_lbl_userName);
        TextView userEmailTextView = headerView.findViewById(R.id.main_lbl_userEmail);

        // Ler os dados do utilizador guardados nas SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userNome", "Nome do Utilizador");
        String userEmail = prefs.getString("userEmail", "email@exemplo.com");

        // Mostrar as informações no cabeçalho do menu lateral
        userNameTextView.setText(userName);
        userEmailTextView.setText(userEmail);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Expande o menu (adiciona os itens à barra superior, se existir)
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Garante que o botão de navegação (←) funciona corretamente
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // --- Atualiza o texto do label principal do dashboard ---
    protected void changeString() {
        dashboard_lbl.setText(R.string.lbl_main_ultimoTreino);
    }
}
