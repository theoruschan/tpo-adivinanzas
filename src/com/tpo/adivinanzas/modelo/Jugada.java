package com.tpo.adivinanzas.modelo;

public abstract class Jugada {

    public enum Tipo { PREGUNTA_FILTRO, SUPOSICION }

    public abstract Tipo getTipo();
}
