package com.tpo.adivinanzas.jugador;

import com.tpo.adivinanzas.modelo.Filtro;
import com.tpo.adivinanzas.modelo.Personaje;

public abstract class Jugador {

    /** declaracion de las variables de la calse */
    private final String nombre;
    private Personaje personajeSecreto;

    /** constructor protegido para que solo las subclases puedan instanciarlo. */
    protected Jugador(String nombre) {
        this.nombre = nombre;
    }

    /**
     * se puede llamar solo cuando hay una partida iniciada. tira excepción si se intenta
     * reasignar después de iniciada la partida.
     */
    public void asignarPersonajeSecreto(Personaje p) {
        if (personajeSecreto != null) {
            throw new IllegalStateException("El personaje secreto ya fue asignado a " + nombre);
        }
        personajeSecreto = p;
    }

    /** interfaz de consulta indirecta: evalúa el filtro contra el personaje
     *  secreto sin exponerlo.*/
    public boolean responder(Filtro filtro) {
        return filtro.evaluar(personajeSecreto);
    }

    /** interfaz de consulta directa: verifica si el nombre coincide con el personaje secreto. */
    public boolean verificarSuposicion(String nombrePersonaje) {
        return personajeSecreto.getNombre().equalsIgnoreCase(nombrePersonaje.trim());
    }

    /** metodo para que las máquinas actualicen sus candidatos tras recibir respuesta. */
    public void notificarRespuesta(Filtro filtro, boolean respuesta) {
        // no-op para JugadorUsuario
    }

    public String getNombre() { return nombre; }
}
