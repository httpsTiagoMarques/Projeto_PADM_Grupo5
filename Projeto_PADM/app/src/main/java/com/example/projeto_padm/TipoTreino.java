package com.example.projeto_padm;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidade que representa o tipo de treino disponível na aplicação.
 *
 * Esta tabela é utilizada para categorizar os treinos realizados,
 * permitindo distinguir, por exemplo, entre "Treino Livre" e "Treino Programado".
 *
 * Estrutura da tabela:
 *
 * id — identificador único gerado automaticamente;
 * nome — nome do tipo de treino (ex: "Livre", "Programado");
 * descricao — explicação adicional sobre o tipo de treino.
 *
 * Relacionamento:
 * Esta entidade é referenciada pela tabela <code>Treino</code>
 * através do campo <code>tipoTreinoId</code>, indicando o tipo de treino realizado.
 */
@Entity(tableName = "tipoTreino_tabela")
public class TipoTreino {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String nome, descricao;

    // Construtores
    public TipoTreino(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public TipoTreino() {}

    // Getters e setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

}
