package com.tpo.adivinanzas.jugador;

import com.tpo.adivinanzas.catalogo.CatalogoPersonajes;
import com.tpo.adivinanzas.modelo.Filtro;
import com.tpo.adivinanzas.modelo.Personaje;

import java.util.ArrayList;
import java.util.List;

public class JugadorUsuario extends Jugador {

    /** declaracion de lalista de candidatos restantes según las respuestas recibidas hasta ahora. */
    private final List<Personaje> candidatosRestantes;

    /** persistencia local del personaje secreto para que el usuario lo consulte durante su turno. */
    private Personaje miPersonaje;

    /** constructor que inicializa el jugador usuario con su nombre y la lista de candidatos. */
    public JugadorUsuario(String nombre) {
        super(nombre);
        this.candidatosRestantes = new ArrayList<>(
                CatalogoPersonajes.getInstance().getPersonajesPorGenero());
    }

    /** asignamos el personaje secreto al jugador usuario y lo guarda localmente. */
    @Override
    public void asignarPersonajeSecreto(Personaje p) {
        super.asignarPersonajeSecreto(p);
        this.miPersonaje = p;
    }

    /** hook para que el jugador usuario actualice su lista de candidatos según la respuesta recibida.
     * y elimina a aquellos que no coinciden con la respuesta del filtro.
     */
    @Override
    public void notificarRespuesta(Filtro filtro, boolean respuesta) {
        candidatosRestantes.removeIf(p -> filtro.evaluar(p) != respuesta);
    }

    /** candidatos restantes según las respuestas recibidas hasta ahora. Para que el Controlador se los muestre. */
    public List<Personaje> getCandidatosRestantes() {
        return new ArrayList<>(candidatosRestantes);
    }

    /** el personaje secreto propio; la clase base no lo expone para no romper el encapsulamiento del rival. */
    public Personaje getPersonajeSecretoPropio() {
        return miPersonaje;
    }
}
