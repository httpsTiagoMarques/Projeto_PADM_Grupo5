package com.example.projeto_padm;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Representa a entidade "Percurso" na base de dados local Room.
 *
 * Cada instância desta classe corresponde a um registo na tabela "percurso_tabela".
 *
 * O percurso define informações sobre um trajeto de treino, incluindo:
 * - Nome (ex.: "Percurso Curto");
 * - Descrição (ex.: "Pequeno percurso de 3 km");
 * - Total de quilómetros (Numero de kilometros do percurso).
 */
@Entity(tableName = "percurso_tabela")
public class Percurso {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String nome, descricao;

    private Float totalKm;

    // Construtores
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
