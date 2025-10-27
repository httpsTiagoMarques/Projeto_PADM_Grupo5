package com.example.projeto_padm;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Categoria_DAO {

    @Insert
    long insert(Categoria categoria);

    @Update
    int update(Categoria categoria);

    @Delete
    int delete(Categoria categoria);

    @Query("SELECT * FROM categoria_tabela")
    List<Categoria> getAllCategories();

    // Metodo adicionado para permitir buscar categoria por ID
    @Query("SELECT * FROM categoria_tabela WHERE id = :id LIMIT 1")
    Categoria getCategoriaById(long id);
}