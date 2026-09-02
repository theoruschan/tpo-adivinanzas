package com.tpo.adivinanzas.controlador;

import com.tpo.adivinanzas.jugador.Jugador;

import java.util.List;

public interface ModoJuego {

    /** nos da el nombre del modo de juego. */
    String getNombre();

    /** arma la lista de jugadores segun el modo (humano, maquinas, o ambos). */
    List<Jugador> inicializarJugadores(String nombreUsuario);

    /** a quien le toca responder/adivinar en este turno del jugador activo. */
    Jugador getObjetivo(Jugador activo, EstadoPartida estado);

    /** si hay que ir mostrando cada paso del razonamiento (solo maquina vs maquina). */
    boolean isLoguearPasos();
}
