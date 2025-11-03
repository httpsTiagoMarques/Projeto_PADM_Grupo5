package com.example.projeto_padm;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * Classe responsável por gerir a sincronização de dados entre
 * a base de dados local (Room) e o Firebase Realtime Database.
 *
 * Esta classe atua como um "gestor de sincronização" (Sync Manager),
 * permitindo enviar para o Firebase todos os utilizadores e treinos
 * guardados localmente, garantindo que ambas as bases de dados estão atualizadas.
 *
 * Principais funções:
 *
 *     Sincronização completa (utilizadores + treinos);
 *     Sincronização apenas de utilizadores;
 *     Execução assíncrona (através de {@link Executors#newSingleThreadExecutor()});
 *     Envio de dados para os nós "users" e "Treinos" do Firebase.
 *
 */
public class SyncManager {
    // ============================================================
    // === Atributos principais ===
    // ============================================================
    // Base de dados local (Room)
    private final AppDatabase db;

    // Referência principal do Firebase Realtime Database
    private final DatabaseReference firebaseRef;


    // ============================================================
    // === Construtor ===
    // ============================================================
    // Inicializa a base de dados local e a referência ao Firebase
    public SyncManager(Context context) {
        db = AppDatabase.getInstance(context);
        firebaseRef = FirebaseDatabase.getInstance().getReference();
    }

    // ============================================================
    // === Sincronização completa (users + treinos) ===
    // ============================================================
    // Executa a sincronização de todos os dados do utilizador atual
    public void syncAll(long userId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                syncUsers();
                syncTreinos(userId);
                Log.d("SYNC", "Sincronização total concluída!");
            } catch (Exception e) {
                Log.e("SYNC", "Erro durante sincronização: " + e.getMessage());
            }
        });
    }

    // ============================================================
    // === Sincronização apenas dos utilizadores ===
    // ============================================================
    // Utilizado quando apenas os dados dos utilizadores precisam ser enviados
    public void syncUsersOnly() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                syncUsers();
                Log.d("SYNC", "Sincronização de utilizadores concluída!");
            } catch (Exception e) {
                Log.e("SYNC", "Erro na sincronização de utilizadores: " + e.getMessage());
            }
        });
    }

    // ============================================================
    // === Função interna: sincroniza utilizadores ===
    // ============================================================
    // Envia todos os registos da tabela "User" da base local para o Firebase
    private void syncUsers() {
        List<User> users = db.userDao().getAllUsers();
        for (User u : users) {
            firebaseRef.child("users").child(String.valueOf(u.getId())).setValue(u);
        }
    }

    // ============================================================
    // === Função interna: sincroniza treinos ===
    // ============================================================
    // Envia todos os treinos do utilizador específico para o Firebase
    private void syncTreinos(long userId) {
        List<Treino> treinos = db.treinoDao().getAllTreinos(userId);
        for (Treino t : treinos) {
            firebaseRef.child("Treinos").child(String.valueOf(t.getId())).setValue(t);
        }
    }
}
