package com.example.projeto_padm;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TipoTreino_DAO {
    @Insert
    long insert(TipoTreino tipoTreino);

    @Update
    int update(TipoTreino tipoTreino);

    @Delete
    int delete(TipoTreino tipoTreino);

    @Query("SELECT * FROM tipoTreino_tabela")
    List<TipoTreino> getAllTipoTreino();




}