package com.tpo.adivinanzas.modelo;

public class Suposicion extends Jugada {

    private final String nombrePersonaje;

    public Suposicion(String nombrePersonaje) {
        this.nombrePersonaje = nombrePersonaje;
    }

    public String getNombrePersonaje() { return nombrePersonaje; }

    @Override
    public Tipo getTipo() { return Tipo.SUPOSICION; }

    @Override
    public String toString() { return "Suposición: ¿Es " + nombrePersonaje + "?"; }
}
