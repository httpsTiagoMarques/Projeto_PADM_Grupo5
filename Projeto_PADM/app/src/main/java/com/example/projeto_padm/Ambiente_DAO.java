package com.example.projeto_padm;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Interface Data Access Object (DAO) para a entidade {@link Ambiente}.
 *
 * Esta interface define os métodos de acesso à tabela "ambiente_tabela"
 * usada pelo Room para executar operações CRUD (Create, Read, Update, Delete).
 *
 * O Room gera automaticamente a implementação destes métodos durante a compilação.
 */
@Dao
public interface Ambiente_DAO {

    // Insert
    @Insert
    long insert(Ambiente ambiente);

    // Update
    @Update
    int update(Ambiente ambiente);

    // Delete
    @Delete
    int delete(Ambiente ambiente);

    // Todos os dados de ambiente
    @Query("SELECT * FROM ambiente_tabela")
    List<Ambiente> getAllAmbiente();

    // Numero total de dados
    @Query("SELECT COUNT(*) FROM ambiente_tabela")
    int getCount();




}