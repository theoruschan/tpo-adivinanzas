package com.tpo.adivinanzas.controlador;

import com.tpo.adivinanzas.jugador.Jugador;
import com.tpo.adivinanzas.jugador.MaquinaAvanzada;
import com.tpo.adivinanzas.jugador.MaquinaBasica;

import java.util.Arrays;
import java.util.List;

public class MaquinaVsMaquina implements ModoJuego {

    @Override
    public String getNombre() { return "Máquina vs Máquina"; }

    /** dos maquinas se adivinan entre si, no reciben nombre de usuario. */
    @Override
    public List<Jugador> inicializarJugadores(String nombreUsuario) {
        MaquinaBasica   basica   = new MaquinaBasica("Máquina Básica");
        MaquinaAvanzada avanzada = new MaquinaAvanzada("Máquina Avanzada");
        return Arrays.asList(basica, avanzada);
    }

    @Override
    public Jugador getObjetivo(Jugador activo, EstadoPartida estado) {
        return estado.getJugadores().stream()
                .filter(j -> j != activo)
                .findFirst()
                .orElseThrow();
    }

    /** aca si se loguean los pasos, para poder ver el razonamiento de ambas maquinas. */
    @Override
    public boolean isLoguearPasos() { return true; }
}
