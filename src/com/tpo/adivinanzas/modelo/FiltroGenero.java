package com.tpo.adivinanzas.modelo;

public class FiltroGenero extends Filtro {

    private final Genero genero;

    public FiltroGenero(Genero genero) { this.genero = genero; }

    public Genero getGenero() { return genero; }

    @Override
    public boolean evaluar(Personaje p) { return p.getGenero() == genero; }

    @Override
    public String getDescripcion() {
        return genero == Genero.MASCULINO ? "¿Es hombre?" : "¿Es mujer?";
    }
}
