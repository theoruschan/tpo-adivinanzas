package com.tpo.adivinanzas.persistencia;

public class Marcador implements Comparable<Marcador> {

    private final String nombreUsuario;
    private int victorias;

    public Marcador(String nombreUsuario, int victorias) {
        this.nombreUsuario = nombreUsuario;
        this.victorias = victorias;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public int getVictorias()        { return victorias; }
    public void incrementarVictorias() { victorias++; }
    public void sumarVictorias(int cantidad) { victorias += cantidad; }

    @Override
    public int compareTo(Marcador otro) {
        return Integer.compare(otro.victorias, this.victorias); // descendente
    }

    @Override
    public String toString() {
        return String.format("%-20s %d victoria%s",
                nombreUsuario, victorias, victorias == 1 ? "" : "s");
    }
}
