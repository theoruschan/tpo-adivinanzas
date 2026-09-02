package com.tpo.adivinanzas.modelo;

public class FiltroUsaLentes extends Filtro {

    private final boolean usaLentes;

    public FiltroUsaLentes(boolean usaLentes) { this.usaLentes = usaLentes; }

    public boolean isUsaLentes() { return usaLentes; }

    @Override
    public boolean evaluar(Personaje p) { return p.isUsaLentes() == usaLentes; }

    @Override
    public String getDescripcion() {
        return usaLentes ? "¿Usa lentes?" : "¿No usa lentes?";
    }
}
