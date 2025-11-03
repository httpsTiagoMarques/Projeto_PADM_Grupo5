package com.example.projeto_padm;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Interface DAO (Data Access Object) responsável pelas operações de acesso
 * à base de dados relacionadas à entidade TipoTreino.
 *
 * Esta interface define os métodos que o Room utiliza para manipular os dados
 * da tabela "tipoTreino_tabela", incluindo inserção, atualização, eliminação
 * e consulta de tipos de treino.
 *
 * Cada metodo é automaticamente implementado pelo Room com base nas anotações.
 */
@Dao
public interface TipoTreino_DAO {

    // Insert
    @Insert
    long insert(TipoTreino tipoTreino);

    // Update
    @Update
    int update(TipoTreino tipoTreino);

    // Delete
    @Delete
    int delete(TipoTreino tipoTreino);

    // Todos os tipoTreino
    @Query("SELECT * FROM tipoTreino_tabela")
    List<TipoTreino> getAllTipoTreino();




}