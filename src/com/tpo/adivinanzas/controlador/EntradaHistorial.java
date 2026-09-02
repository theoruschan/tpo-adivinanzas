package com.tpo.adivinanzas.controlador;

import com.tpo.adivinanzas.jugador.Jugador;
import com.tpo.adivinanzas.modelo.Filtro;

public class EntradaHistorial {

    /** declaracion de las variables */
    private final int turno;
    private final Jugador preguntador;
    private final Filtro filtro;
    private final boolean respuesta;

    /** constructor de la clase  */
    public EntradaHistorial(int turno, Jugador preguntador, Filtro filtro, boolean respuesta) {
        this.turno = turno;
        this.preguntador = preguntador;
        this.filtro = filtro;
        this.respuesta = respuesta;
    }

    /** getters de la clase */
    public int getTurno()            { return turno; }
    public Jugador getPreguntador()  { return preguntador; }
    public Filtro getFiltro()        { return filtro; }
    public boolean getRespuesta()    { return respuesta; }

    /** método toString que devuelve una representación en cadena de la entrada del historial */
    @Override
    public String toString() {
        return String.format("T%d | %s pregunta: %s → %s",
                turno, preguntador.getNombre(),
                filtro.getDescripcion(), respuesta ? "SÍ" : "NO");
    }
}
