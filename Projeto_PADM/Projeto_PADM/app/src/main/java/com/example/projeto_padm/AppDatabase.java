package com.example.projeto_padm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;

@Database(
        // ============================================================
        // === Definição das entidades incluídas na base de dados ===
        // ============================================================
        entities = {
                User.class,
                Ambiente.class,
                Categoria.class,
                TipoTreino.class,
                Percurso.class,
                Treino.class
        },
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // ============================================================
    // === Declaração dos DAOs (Data Access Objects) ===
    // ============================================================
    public abstract User_DAO userDao();
    public abstract Ambiente_DAO ambienteDao();
    public abstract Categoria_DAO categoriaDao();
    public abstract TipoTreino_DAO tipoTreinoDao();
    public abstract Percurso_DAO percursoDao();
    public abstract Treino_DAO treinoDao();

    // ============================================================
    // === Implementação do padrão Singleton ===
    // ============================================================
    // Garante que apenas uma instância da base de dados é criada
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "app_database"
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(prepopulateCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // ============================================================
    // === Callback de pré-população da base de dados ===
    // ============================================================
    // Executado automaticamente após a criação inicial da base
    private static final RoomDatabase.Callback prepopulateCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase database = INSTANCE;
                if (database != null) {

                    // ----- CATEGORIAS -----
                    Categoria_DAO categoriaDao = database.categoriaDao();
                    if (categoriaDao.getAllCategories().isEmpty()) {
                        categoriaDao.insert(new Categoria("Caminhada", "Exercício de ritmo leve"));
                        categoriaDao.insert(new Categoria("Corrida", "Corrida a pé"));
                        categoriaDao.insert(new Categoria("Ciclismo", "Exercício com bicicleta"));
                    }

                    // ----- AMBIENTES -----
                    Ambiente_DAO ambienteDao = database.ambienteDao();
                    if (ambienteDao.getCount() == 0) {
                        ambienteDao.insert(new Ambiente("Interior", "Ambiente interno"));
                        ambienteDao.insert(new Ambiente("Exterior", "Ambiente externo"));
                    }

                    // ----- TIPOS DE TREINO -----
                    TipoTreino_DAO tipoTreinoDao = database.tipoTreinoDao();
                    if (tipoTreinoDao.getAllTipoTreino().isEmpty()) {
                        tipoTreinoDao.insert(new TipoTreino("Livre", "Treino livre apenas por tempo"));
                        tipoTreinoDao.insert(new TipoTreino("Programado", "Treino programado por percurso"));
                    }

                    // ----- PERCURSOS -----
                    Percurso_DAO percursoDao = database.percursoDao();
                    if (percursoDao.getAllPercurso().isEmpty()) {
                        percursoDao.insert(new Percurso("Percurso Curto", "Pequeno percurso de 3 km", 3.0f));
                        percursoDao.insert(new Percurso("Percurso Longo", "Percurso de 10 km", 10.0f));
                    }

                }
            });
        }
    };
}