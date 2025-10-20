package com.example.projeto_padm;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface User_DAO {
    @Insert
    long insert(User user);

    @Update
    int update(User user);

    @Delete
    int delete(User user);

    @Query("SELECT * FROM user_tabela")
    List<User> getAllUsers();

    @Query("SELECT * FROM user_tabela WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);



}