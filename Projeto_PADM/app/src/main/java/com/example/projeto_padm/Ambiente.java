package com.example.projeto_padm;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Classe que representa a entidade "Ambiente" na base de dados local (Room).
 *
 * Cada instância desta classe corresponde a um registo na tabela "ambiente_tabela".
 * Um "Ambiente" define o tipo de local onde o treino ocorre (ex: Interior, Exterior, etc.).
 */
@Entity(tableName = "ambiente_tabela")
public class Ambiente {

    /**
     * Chave primária da tabela.
     * É gerada automaticamente pela base de dados (auto-incremento).
     */
    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * Nome do ambiente e descricao (exemplo: "Interior", "Exterior").
     */
    private String nome, descricao;

    // Construtores
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
