package com.tpo.adivinanzas.modelo;

public class PreguntaFiltro extends Jugada {

    private final Filtro filtro;

    public PreguntaFiltro(Filtro filtro) { this.filtro = filtro; }

    public Filtro getFiltro() { return filtro; }

    @Override
    public Tipo getTipo() { return Tipo.PREGUNTA_FILTRO; }

    @Override
    public String toString() { return "Pregunta: " + filtro.getDescripcion(); }
}
