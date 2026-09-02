package com.tpo.adivinanzas.modelo;

public abstract class Filtro {

    public abstract boolean evaluar(Personaje p);

    public abstract String getDescripcion();

    @Override
    public String toString() { return getDescripcion(); }
}
