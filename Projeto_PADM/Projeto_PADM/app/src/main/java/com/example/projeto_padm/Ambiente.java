package com.example.projeto_padm;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ambiente_tabela")
public class Ambiente {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String nome, descricao;

    // Construtor
    public Ambiente(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public Ambiente() {}

    // Getters e setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

}