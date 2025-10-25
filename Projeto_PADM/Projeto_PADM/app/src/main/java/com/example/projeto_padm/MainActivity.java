package com.example.projeto_padm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.projeto_padm.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // --- UI Elements ---
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private TextView dashboard_lbl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- View Binding setup ---
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- Toolbar setup ---
        setSupportActionBar(binding.appBarMain.toolbar);

        // --- DrawerLayout and NavigationView setup ---
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // --- Top-level destinations (main menu options) ---
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow,
                R.id.nav_settings
        )
                .setOpenableLayout(drawer)
                .build();

        // --- Navigation setup ---
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // --- Dashboard label ---
        dashboard_lbl = findViewById(R.id.dashboard_lbl);

        // ============================================================
        // ============ Display user name and email in menu ===========
        // ============================================================

        // Access the header (top part) of the NavigationView
        View headerView = navigationView.getHeaderView(0);
        TextView userNameTextView = headerView.findViewById(R.id.main_lbl_userName);
        TextView userEmailTextView = headerView.findViewById(R.id.main_lbl_userEmail);

        // Retrieve user data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userNome", "Nome do Utilizador");
        String userEmail = prefs.getString("userEmail", "email@exemplo.com");
        long userId = prefs.getLong("userId", -1);  // ✅ fixed type



        // Display user info in the navigation drawer header
        userNameTextView.setText(userName);
        userEmailTextView.setText(userEmail);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu (adds items to the app bar, if present)
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Ensure navigation works properly with the back arrow (←)
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // --- Example method: updates dashboard label text ---
    protected void changeString() {
        dashboard_lbl.setText(R.string.lbl_main_ultimoTreino);
    }
}