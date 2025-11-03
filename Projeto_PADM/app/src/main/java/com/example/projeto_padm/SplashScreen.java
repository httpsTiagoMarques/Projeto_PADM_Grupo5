package com.example.projeto_padm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Classe responsável pelo ecrã inicial (Splash Screen) da aplicação.
 *
 * Esta Activity apresenta uma tela de introdução por alguns segundos antes
 * de abrir a interface de login. É útil para:
 *
 *     Exibir o logotipo ou nome da aplicação;
 *     Executar inicializações leves antes do carregamento;
 *     Melhorar a experiência de transição entre a abertura do app e o primeiro ecrã.
 *
 */
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ativa o modo Edge-to-Edge (permite que o layout ocupe toda a área útil da tela)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        // Ajusta o layout para se adaptar automaticamente às barras do sistema (status/navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ============================================================
        // === LÓGICA DO SPLASH SCREEN ================================
        // ============================================================

        /**
         * Utiliza um Handler para atrasar a execução da próxima Activity.
         * Após 3 segundos (3000 milissegundos), o utilizador é redirecionado
         * para a tela de Login.
         */
        new Handler().postDelayed(() -> {
            // Inicia a Activity de login
            startActivity(new Intent(SplashScreen.this, Login.class));
            // Encerra a SplashScreen para impedir que o utilizador volte a ela
            finish();
        }, 3000); // duração do splash: 3 segundos
    }
}
