package com.tpo.adivinanzas.controlador;

import com.tpo.adivinanzas.catalogo.CatalogoPersonajes;
import com.tpo.adivinanzas.jugador.Jugador;
import com.tpo.adivinanzas.jugador.JugadorUsuario;
import com.tpo.adivinanzas.modelo.Personaje;
import com.tpo.adivinanzas.persistencia.MarcadorRepositorio;
import com.tpo.adivinanzas.vista.Vista;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Partida {

    /** variables de la clase  */
    private final ModoJuego modo;
    private final EstadoPartida estado;
    private final MarcadorRepositorio marcadorRepo;

    /** constructor normal: el modo crea los jugadores y se asignan personajes secretos. */
    public Partida(ModoJuego modo, String nombreUsuario,
                   Vista vista, MarcadorRepositorio marcadorRepo) {
        this.modo = modo;
        this.marcadorRepo = marcadorRepo;

        List<Jugador> jugadores = modo.inicializarJugadores(nombreUsuario);
        asignarPersonajesSecretos(jugadores, vista);
        this.estado = new EstadoPartida(jugadores);
    }

    /** constructor para rondas con personajes ya asignados de antes (ej: JugadorVsDosMaquinas). */
    public Partida(List<Jugador> jugadoresConPersonajes, ModoJuego modo,
                   MarcadorRepositorio marcadorRepo) {
        this.modo = modo;
        this.marcadorRepo = marcadorRepo;
        this.estado = new EstadoPartida(new ArrayList<>(jugadoresConPersonajes));
    }

    /** el usuario elige su personaje, las maquinas reciben uno al azar. */
    private void asignarPersonajesSecretos(List<Jugador> jugadores, Vista vista) {
        List<Personaje> disponibles =
                new ArrayList<>(CatalogoPersonajes.getInstance().getPersonajesPorGenero());
        Random rnd = new Random();

        for (Jugador j : jugadores) {
            if (j instanceof JugadorUsuario) {
                int opcion = vista.pedirPersonajeSecreto(j.getNombre(), disponibles);
                j.asignarPersonajeSecreto(disponibles.remove(opcion));
            } else {
                int idx = rnd.nextInt(disponibles.size());
                j.asignarPersonajeSecreto(disponibles.remove(idx));
            }
        }
    }

    public ModoJuego getModo()                   { return modo; }
    public EstadoPartida getEstado()             { return estado; }
    public MarcadorRepositorio getMarcadorRepo() { return marcadorRepo; }
}
