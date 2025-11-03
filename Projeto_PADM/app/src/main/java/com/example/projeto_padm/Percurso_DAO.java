package com.example.projeto_padm;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object (DAO) responsável pelas operações de base de dados
 * relacionadas à entidade {@link Percurso}.
 *
 * Esta interface define os métodos que o Room utiliza para aceder e manipular
 * os dados armazenados na tabela "percurso_tabela".
 *
 * Cada metodo aqui mapeia automaticamente para comandos SQL executados
 * sobre a base de dados local.
 */
@Dao
public interface Percurso_DAO {
    // Insert
    @Insert
    long insert(Percurso percurso);

    // Update
    @Update
    int update(Percurso percurso);

    // Delete
    @Delete
    int delete(Percurso percurso);

    // Todos os percursos
    @Query("SELECT * FROM percurso_tabela")
    List<Percurso> getAllPercurso();




}