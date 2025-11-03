package com.example.projeto_padm;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Interface Data Access Object (DAO) para a entidade {@link Categoria}.
 *
 * Esta interface define todos os métodos de acesso à tabela "categoria_tabela",
 * permitindo realizar operações CRUD (Create, Read, Update, Delete)
 * de forma segura e automática através do Room.
 *
 * O Room gera automaticamente a implementação concreta desta interface.
 */
@Dao
public interface Categoria_DAO {

    // Insert
    @Insert
    long insert(Categoria categoria);

    // Update
    @Update
    int update(Categoria categoria);

    // Delete
    @Delete
    int delete(Categoria categoria);

    // Todas as categorias
    @Query("SELECT * FROM categoria_tabela")
    List<Categoria> getAllCategories();

    // Categoria por id
    @Query("SELECT * FROM categoria_tabela WHERE id = :id LIMIT 1")
    Categoria getCategoriaById(long id);
}
