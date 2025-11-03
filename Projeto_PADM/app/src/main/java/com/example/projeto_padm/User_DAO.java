package com.example.projeto_padm;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Interface DAO (Data Access Object) responsável por definir
 * as operações de acesso à base de dados relacionadas à entidade User.
 *
 * O Room gera automaticamente a implementação destes métodos
 * com base nas anotações aplicadas.
 *
 * Este DAO permite inserir, atualizar, eliminar e consultar
 * utilizadores na tabela "user_tabela".
 */

@Dao
public interface User_DAO {

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    // Update
    @Update
    int update(User user);

    // Delete
    @Delete
    int delete(User user);

    // Todos os users
    @Query("SELECT * FROM user_tabela")
    List<User> getAllUsers();

    // Verificação de user por email e password
    @Query("SELECT * FROM user_tabela WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    // User por id
    @Query("SELECT * FROM user_tabela WHERE id = :id LIMIT 1")
    User getUserById(long id);
}
