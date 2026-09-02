package com.tpo.adivinanzas.controlador;

import com.tpo.adivinanzas.jugador.Jugador;
import com.tpo.adivinanzas.modelo.Filtro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HistorialPreguntas {

    /** variables de la clase  */
    private final List<EntradaHistorial> entradas = new ArrayList<>();

    /** método que registra una nueva entrada en el historial de preguntas */
    public void registrar(int turno, Jugador preguntador, Filtro filtro, boolean respuesta) {
        entradas.add(new EntradaHistorial(turno, preguntador, filtro, respuesta));
    }

    /** método que devuelve una lista inmodificable de todas las entradas del historial */
    public List<EntradaHistorial> getEntradas() {
        return Collections.unmodifiableList(entradas);
    }

    /** método que devuelve una lista de entradas del historial filtradas por un jugador específico */
    public List<EntradaHistorial> getEntradasDe(Jugador jugador) {
        return entradas.stream()
                .filter(e -> e.getPreguntador() == jugador)
                .collect(Collectors.toList());
    }
}
