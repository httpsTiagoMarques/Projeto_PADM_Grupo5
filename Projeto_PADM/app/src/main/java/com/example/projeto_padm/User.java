package com.example.projeto_padm;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.database.Exclude;

/**
 * Entidade que representa a tabela "user_tabela" na base de dados local Room.
 *
 * Esta classe define os atributos de um utilizador e é utilizada tanto
 * para armazenamento local (Room) como para sincronização com o
 * Firebase Realtime Database.
 *
 * O campo "foto" é armazenado apenas localmente (Room), sendo
 * excluído das operações com o Firebase através da anotação @Exclude.
 */

@Entity(tableName = "user_tabela")
public class User {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String nome;
    private String email;
    private String password;
    private String sexo;
    private int idade;
    private float peso;
    private float altura;

    @Exclude
    private byte[] foto;

    // Construtores
    public User() {}

    public User(String nome, String email, String password, String sexo, int idade, float peso, float altura, byte[] foto) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.sexo = sexo;
        this.idade = idade;
        this.peso = peso;
        this.altura = altura;
        this.foto = foto;
    }

    // Getters e setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public int getIdade() { return idade; }
    public void setIdade(int idade) { this.idade = idade; }

    public float getPeso() { return peso; }
    public void setPeso(float peso) { this.peso = peso; }

    public float getAltura() { return altura; }
    public void setAltura(float altura) { this.altura = altura; }

    @Exclude
    public byte[] getFoto() { return foto; }
    public void setFoto(byte[] foto) { this.foto = foto; }
}
