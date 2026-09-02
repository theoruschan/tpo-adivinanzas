package com.tpo.adivinanzas.modelo;

public class FiltroCalvo extends Filtro {

    private final boolean esCalvo;

    public FiltroCalvo(boolean esCalvo) { this.esCalvo = esCalvo; }

    public boolean isEsCalvo() { return esCalvo; }

    @Override
    public boolean evaluar(Personaje p) { return p.isCalvo() == esCalvo; }

    @Override
    public String getDescripcion() {
        return esCalvo ? "¿Es calvo?" : "¿Tiene pelo?";
    }
}
