package com.example.projeto_padm;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Ambiente.class, Categoria.class, TipoTreino.class, Percurso.class, Treino.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract User_DAO userDao();

    public abstract Ambiente_DAO ambienteDao();

    public abstract Categoria_DAO categoriaDao();

    public abstract TipoTreino_DAO tipoTreinoDao();

    public abstract Percurso_DAO percursoDao();

    public abstract Treino_DAO treinoDao();
}