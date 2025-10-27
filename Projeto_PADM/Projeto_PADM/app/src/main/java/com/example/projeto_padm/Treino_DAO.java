package com.example.projeto_padm;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
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

    //  Obter todos os treinos de um utilizador
    @Query("SELECT * FROM treino_tabela WHERE userId = :userId ORDER BY id DESC")
    List<Treino> getAllTreinos(long userId);




    //  Obter um treino específico pelo ID (Update GalleryFragment)
    @Query("SELECT * FROM treino_tabela WHERE id = :treinoId LIMIT 1")
    Treino getTreinoById(long treinoId);

    //  Obter o treino mais recente de um utilizador (HomeFragment)
    @Query("SELECT c.nome AS nomeCategoria, " +
            "t.calorias AS calorias, " +
            "t.tempo AS tempo, " +
            "t.distanciaPercorrida AS distanciaPercorrida, " +
            "t.velocidadeMedia AS velocidadeMedia " +
            "FROM treino_tabela t " +
            "INNER JOIN categoria_tabela c ON t.categoriaId = c.id " +
            "WHERE t.userId = :userId " +
            "ORDER BY t.id DESC " +
            "LIMIT 1")
    UltimoTreinoInfo getUltimoTreino(long userId);
}