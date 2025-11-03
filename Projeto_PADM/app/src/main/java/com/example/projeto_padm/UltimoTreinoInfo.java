// File: UltimoTreinoInfo.java
package com.example.projeto_padm;

/**
 * Classe auxiliar utilizada para armazenar informações resumidas
 * sobre o último treino realizado por um utilizador.
 *
 * Este modelo é usado como resultado de consultas SQL específicas
 * (como em Treino_DAO.getUltimoTreino) que retornam apenas os dados
 * essenciais para exibição rápida no ecrã principal (HomeFragment).
 *
 * Não é uma entidade da base de dados — apenas um objeto temporário
 * para leitura de dados combinados.
 */
public class UltimoTreinoInfo {
    public String nomeCategoria;
    public int calorias;
    public String tempo;
    public double distanciaPercorrida;
    public double velocidadeMedia;
}
