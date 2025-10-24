package com.example.projeto_padm;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Percurso_DAO {
    @Insert
    long insert(Percurso percurso);

    @Update
    int update(Percurso percurso);

    @Delete
    int delete(Percurso percurso);

    @Query("SELECT * FROM percurso_tabela")
    List<Percurso> getAllPercurso();




}