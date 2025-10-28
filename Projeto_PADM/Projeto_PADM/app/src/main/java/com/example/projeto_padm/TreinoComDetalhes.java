package com.example.projeto_padm;

import androidx.room.ColumnInfo;

public class TreinoComDetalhes {

    public long id;
    public long ambienteId;
    public long categoriaId;
    public long tipoTreinoId;

    @ColumnInfo(name = "percursoId")
    public Long percursoId;

    public long userId;
    public double distanciaPercorrida;
    public double velocidadeMedia;
    public int calorias;
    public String tempo;
    public String hora_inicio;
    public String hora_fim;
    public String data;

    @ColumnInfo(name = "categoriaNome")
    public String categoriaNome;

    @ColumnInfo(name = "tipoNome")
    public String tipoNome;

    @ColumnInfo(name = "ambienteNome")
    public String ambienteNome;

    @ColumnInfo(name = "percursoNome")
    public String percursoNome; //
}