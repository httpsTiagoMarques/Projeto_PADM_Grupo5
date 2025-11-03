package com.example.projeto_padm;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

/**
 * Entidade que representa um treino realizado por um utilizador.
 *
 * Esta tabela armazena as informações completas de cada sessão de treino,
 * incluindo dados como ambiente, categoria, tipo de treino, percurso,
 * distância, tempo, velocidade média, calorias e data.
 *
 * Possui várias chaves estrangeiras que garantem integridade referencial
 * com as tabelas Ambiente, Categoria, TipoTreino, Percurso e User.
 */
@Entity(
        tableName = "treino_tabela",
        foreignKeys = {
                @ForeignKey(
                        entity = Ambiente.class,
                        parentColumns = "id",
                        childColumns = "ambienteId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Categoria.class,
                        parentColumns = "id",
                        childColumns = "categoriaId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = TipoTreino.class,
                        parentColumns = "id",
                        childColumns = "tipoTreinoId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Percurso.class,
                        parentColumns = "id",
                        childColumns = "percursoId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class Treino {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(index = true)
    private long ambienteId;

    @ColumnInfo(index = true)
    private long categoriaId;

    @ColumnInfo(index = true)
    private long tipoTreinoId;

    @ColumnInfo(index = true)
    private Long percursoId;

    @ColumnInfo(index = true)
    private long userId;

    private double distanciaPercorrida, velocidadeMedia;
    private String tempo, hora_inicio, hora_fim, data;
    private int calorias;

    // Constructores
    public Treino(long ambienteId, long categoriaId, long tipoTreinoId, Long percursoId, long userId,
                  double distanciaPercorrida, String tempo, double velocidadeMedia, int calorias,
                  String data, String hora_inicio, String hora_fim) {
        this.ambienteId = ambienteId;
        this.categoriaId = categoriaId;
        this.tipoTreinoId = tipoTreinoId;
        this.percursoId = percursoId;
        this.userId = userId;
        this.distanciaPercorrida = distanciaPercorrida;
        this.tempo = tempo;
        this.velocidadeMedia = velocidadeMedia;
        this.calorias = calorias;
        this.data = data;
        this.hora_inicio = hora_inicio;
        this.hora_fim = hora_fim;
    }

    public Treino() {}

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getAmbienteId() { return ambienteId; }
    public void setAmbienteId(long ambienteId) { this.ambienteId = ambienteId; }

    public long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(long categoriaId) { this.categoriaId = categoriaId; }

    public long getTipoTreinoId() { return tipoTreinoId; }
    public void setTipoTreinoId(long tipoTreinoId) { this.tipoTreinoId = tipoTreinoId; }

    public Long getPercursoId() { return percursoId; }
    public void setPercursoId(Long percursoId) { this.percursoId = percursoId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public double getDistanciaPercorrida() { return distanciaPercorrida; }
    public void setDistanciaPercorrida(double distanciaPercorrida) { this.distanciaPercorrida = distanciaPercorrida; }

    public String getTempo() { return tempo; }
    public void setTempo(String tempo) { this.tempo = tempo; }

    public double getVelocidadeMedia() { return velocidadeMedia; }
    public void setVelocidadeMedia(double velocidadeMedia) { this.velocidadeMedia = velocidadeMedia; }

    public int getCalorias() { return calorias; }
    public void setCalorias(int calorias) { this.calorias = calorias; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getHora_inicio() { return hora_inicio; }
    public void setHora_inicio(String hora_inicio) { this.hora_inicio = hora_inicio; }

    public String getHora_fim() { return hora_fim; }
    public void setHora_fim(String hora_fim) { this.hora_fim = hora_fim; }
}
