package com.tpo.adivinanzas.controlador;

import com.tpo.adivinanzas.jugador.Jugador;
import com.tpo.adivinanzas.jugador.JugadorUsuario;
import com.tpo.adivinanzas.jugador.MaquinaBasica;

import java.util.Arrays;
import java.util.List;

public class JugadorVsMaquina implements ModoJuego {

    @Override
    public String getNombre() { return "Jugador vs Máquina"; }

    @Override
    public List<Jugador> inicializarJugadores(String nombreUsuario) {
        JugadorUsuario usuario = new JugadorUsuario(nombreUsuario);
        MaquinaBasica maquina  = new MaquinaBasica("Máquina");
        return Arrays.asList(usuario, maquina);
    }

    @Override
    public Jugador getObjetivo(Jugador activo, EstadoPartida estado) {
        return estado.getJugadores().stream()
                .filter(j -> j != activo)
                .findFirst()
                .orElseThrow();
    }

    /** se loguean los pasos tambien aca, para que se vea el razonamiento de la maquina
     * (avanzada, en el modo dos maquinas) aunque el rival sea el usuario. */
    @Override
    public boolean isLoguearPasos() { return true; }
}
