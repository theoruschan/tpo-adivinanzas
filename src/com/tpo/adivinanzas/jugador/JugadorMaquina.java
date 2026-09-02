package com.tpo.adivinanzas.jugador;

import com.tpo.adivinanzas.catalogo.CatalogoPersonajes;
import com.tpo.adivinanzas.controlador.EstadoPartida;
import com.tpo.adivinanzas.modelo.Filtro;
import com.tpo.adivinanzas.modelo.Jugada;
import com.tpo.adivinanzas.modelo.Personaje;

import java.util.ArrayList;
import java.util.List;

public abstract class JugadorMaquina extends Jugador {

    /** subconjunto del catalogo que aun no fue descartado por las respuestas recibidas. */
    protected List<Personaje> candidatos;

    /** constructor protegido: se usa solo desde subclases. */
    protected JugadorMaquina(String nombre) {
        super(nombre);
        // usa la vista por id, el orden interno que maneja la maquina
        this.candidatos = new ArrayList<>(CatalogoPersonajes.getInstance().getPersonajesPorId());
    }

    /** al recibir una respuesta, achica candidatos a los compatibles con esa respuesta. */
    @Override
    public void notificarRespuesta(Filtro filtro, boolean respuesta) {
        candidatos = filtrarCandidatos(candidatos, filtro, respuesta);
    }

    /** filtrado de una sola pasada: no es divide and conquer (no hay recursion ni combinacion
     * de subproblemas, se descarta directamente el grupo que no corresponde a la respuesta
     * real). el D&C de este TPO esta en MaquinaAvanzada.elegirMejorFiltro, no aca. */
    protected List<Personaje> filtrarCandidatos(List<Personaje> candidatosActuales, Filtro filtro, boolean respuesta) {
        if (candidatosActuales.size() <= 1) {
            return candidatosActuales;
        }

        List<List<Personaje>> particion = particionar(candidatosActuales, filtro);
        return respuesta ? particion.get(0) : particion.get(1);
    }

    /** separa en cumplen/no cumplen; la avanzada reusa esto para comparar filtros en su greedy. */
    protected List<List<Personaje>> particionar(List<Personaje> candidatos, Filtro filtro) {
        List<Personaje> cumplen = new ArrayList<>();
        List<Personaje> noCumplen = new ArrayList<>();

        for (Personaje p : candidatos) {
            if (filtro.evaluar(p)) {
                cumplen.add(p);
            } else {
                noCumplen.add(p);
            }
        }
        return List.of(cumplen, noCumplen);
    }

    /** devuelve la cantidad de candidatos que le quedan a la maquina. */
    public int getCantidadCandidatos() { return candidatos.size(); }

    /** copia de los candidatos, para que nadie de afuera pueda tocar el estado interno. */
    public List<Personaje> getCandidatos() { return new ArrayList<>(candidatos); }

    /** decision autonoma de la maquina para este turno. */
    public abstract Jugada elegirJugada(EstadoPartida estado);
}
