package com.example.projeto_padm;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Ambiente_DAO {
    @Insert
    long insert(Ambiente ambiente);

    @Update
    int update(Ambiente ambiente);

    @Delete
    int delete(Ambiente ambiente);

    @Query("SELECT * FROM ambiente_tabela")
    List<Ambiente> getAllAmbiente();

    @Query("SELECT COUNT(*) FROM ambiente_tabela")
    int getCount();
}