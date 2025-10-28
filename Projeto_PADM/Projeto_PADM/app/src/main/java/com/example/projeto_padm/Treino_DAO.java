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


    //  Obter os detalhes de um treino
    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT t.*, " +
            "c.nome AS categoriaNome, " +
            "tp.nome AS tipoNome, " +
            "a.nome AS ambienteNome, " +
            "p.nome AS percursoNome " +
            "FROM treino_tabela t " +
            "INNER JOIN categoria_tabela c ON t.categoriaId = c.id " +
            "INNER JOIN tipoTreino_tabela tp ON t.tipoTreinoId = tp.id " +
            "INNER JOIN ambiente_tabela a ON t.ambienteId = a.id " +
            "LEFT JOIN percurso_tabela p ON t.percursoId = p.id " +
            "WHERE t.userId = :userId " +
            "ORDER BY t.id DESC")
    List<TreinoComDetalhes> getAllTreinosDetail(long userId);


    //  Obter um treino especÃ­fico pelo ID (Update GalleryFragment)
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