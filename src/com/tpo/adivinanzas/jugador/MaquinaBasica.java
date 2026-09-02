package com.tpo.adivinanzas.jugador;

import com.tpo.adivinanzas.controlador.EstadoPartida;
import com.tpo.adivinanzas.modelo.Filtro;
import com.tpo.adivinanzas.modelo.FiltroFactory;
import com.tpo.adivinanzas.modelo.Jugada;
import com.tpo.adivinanzas.modelo.PreguntaFiltro;
import com.tpo.adivinanzas.modelo.Suposicion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MaquinaBasica extends JugadorMaquina {

    private final List<Filtro> filtrosUsados = new ArrayList<>();
    private final Random rnd = new Random();

    public MaquinaBasica(String nombre) { super(nombre); }

    /** si queda un solo candidato lo supone, sino pregunta un filtro al azar entre los no
     * usados (sin greedy) para poder compararla contra la avanzada. */
    @Override
    public Jugada elegirJugada(EstadoPartida estado) {
        if (candidatos.size() == 1) {
            return new Suposicion(candidatos.get(0).getNombre());
        }

        List<Filtro> disponibles = filtrosDisponibles();
        if (disponibles.isEmpty()) {
            return new Suposicion(candidatos.get(0).getNombre());
        }

        Filtro elegido = disponibles.get(rnd.nextInt(disponibles.size()));
        filtrosUsados.add(elegido);
        return new PreguntaFiltro(elegido);
    }

    /** filtros que todavia no se preguntaron en esta partida. */
    private List<Filtro> filtrosDisponibles() {
        List<Filtro> todos = FiltroFactory.todosFiltros();
        todos.removeIf(f -> filtrosUsados.stream()
                .anyMatch(u -> u.getDescripcion().equals(f.getDescripcion())));
        return todos;
    }

    /** filtros ya usados por esta maquina. */
    public List<Filtro> getFiltrosUsados() {
        return Collections.unmodifiableList(filtrosUsados);
    }
}
