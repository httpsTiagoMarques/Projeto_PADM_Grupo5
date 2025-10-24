package com.example.projeto_padm;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "percurso_tabela")
public class Percurso {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String nome, descricao;

    private Float totalKm;

    // Construtor
    public Percurso(String nome, String descricao, Float totalKm) {
        this.nome = nome;
        this.descricao = descricao;
        this.totalKm = totalKm;
    }

    public Percurso() {}

    // Getters e setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Float getTotalKm() { return totalKm; }
    public void setTotalKm(Float totalKm) { this.totalKm = totalKm; }

}