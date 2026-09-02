package com.tpo.adivinanzas.controlador;

import com.tpo.adivinanzas.jugador.Jugador;

import java.util.List;

public class EstadoPartida {

    /** variables de la clase  */
    private final List<Jugador> jugadores;
    private final HistorialPreguntas historial;
    private int turnoActual;
    private Jugador ganador;

    /** constructor de la clase  */
    public EstadoPartida(List<Jugador> jugadores) {
        this.jugadores = jugadores;
        this.historial = new HistorialPreguntas();
        this.turnoActual = 0;
        this.ganador = null;
    }

    /** getters y setters de la clase */
    public List<Jugador> getJugadores()       { return jugadores; }
    public HistorialPreguntas getHistorial()   { return historial; }
    public int getTurnoActual()                { return turnoActual; }
    public void incrementarTurno()             { turnoActual++; }
    public Jugador getGanador()                { return ganador; }
    public void setGanador(Jugador g)          { ganador = g; }
    public boolean hayGanador()                { return ganador != null; }
}
