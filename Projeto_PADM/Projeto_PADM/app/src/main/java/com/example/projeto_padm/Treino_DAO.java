package com.example.projeto_padm;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Treino_DAO {

    @Insert
    long insert(Treino treino);

    @Update
    int update(Treino treino);

    @Delete
    int delete(Treino treino);

    // Get all Treinos from a specific user
    @Query("SELECT * FROM treino_tabela WHERE userId = :userId")
    List<Treino> getAllTreinos(long userId);

    //Treino mais recente
    // Get all Treinos from a specific user
    @Query("SELECT c.nome AS nomeCategoria, " +
            "t.calorias AS calorias, " +
            "t.tempo AS tempo, " +
            "t.distanciaPercorrida AS distanciaPercorrida, " +
            "t.velocidadeMedia AS velocidadeMedia " +
            "FROM treino_tabela t " +
            "INNER JOIN categoria_tabela c ON t.categoriaId = c.id " +
            "WHERE t.userId = :userId " +
            "ORDER BY t.data DESC " +
            "LIMIT 1")
    UltimoTreinoInfo getUltimoTreino(long userId);



}