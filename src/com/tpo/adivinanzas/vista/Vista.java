package com.tpo.adivinanzas.vista;

import com.tpo.adivinanzas.modelo.EvaluacionFiltro;
import com.tpo.adivinanzas.modelo.Filtro;
import com.tpo.adivinanzas.modelo.Personaje;
import com.tpo.adivinanzas.persistencia.Marcador;

import java.util.List;

/**
 * todo lo que el motor de juego necesita de una vista: mostrar información y
 * pedir decisiones al jugador humano. el motor y los modos de juego solo
 * conocen esta interfaz, nunca una implementación concreta, para poder
 * cambiar de vista (consola, swing) sin tocar la lógica de juego.
 */
public interface Vista {

    void mostrarBienvenida();
    void mostrarMenuPrincipal();
    String leerLinea();
    void mostrarOpcionInvalida();
    void mostrarDespedida();
    void cerrar();

    String pedirNombreUsuario();
    void pedirEnter(String mensaje);

    int pedirPersonajeSecreto(String nombreJugador, List<Personaje> disponibles);
    void mostrarCatalogoCompleto(String nombreJugador, List<Personaje> todos);
    int pedirPersonajeSecretoDosMaquinas(List<Personaje> todos);
    void mostrarPersonajeElegido(String nombrePersonaje);

    void mostrarInicioPartida(String nombreModo, List<String> nombresJugadores);
    void mostrarTurno(int numeroTurno, String nombreActivo, String nombreObjetivo);
    void mostrarPregunta(String nombreActivo, String descripcionFiltro);
    void mostrarRespuesta(String nombreObjetivo, boolean respuesta);
    void mostrarCandidatosMaquina(String nombreActivo, int cantidad, List<String> nombresCandidatos);
    void mostrarRazonamientoGreedy(String nombreMaquina, List<EvaluacionFiltro> evaluaciones, Filtro elegido);
    void mostrarSuposicion(String nombreActivo, String nombrePersonaje);
    void mostrarAcierto(String nombreObjetivo, String nombrePersonaje);
    void mostrarFallo(String nombreObjetivo, String nombrePersonaje);
    void mostrarFinPartida(String nombreGanador);
    void mostrarLineaEnBlanco();

    void mostrarCandidatosPropios(List<Personaje> candidatos);
    int pedirAccionTurno();
    void mostrarPersonajeSecreto(Personaje personaje);
    int pedirSeleccionFiltro(List<Filtro> filtros);
    int pedirSeleccionPersonajeSupuesto(List<Personaje> todos);

    void mostrarResumenFinal(String nombreGanador, int victorias);
    void mostrarRanking(List<Marcador> top, int n);

    void mostrarEncabezadoRonda1(String nombreUsuario);
    void mostrarEncabezadoRonda2(String nombreUsuario);
    void mostrarDerrotaRondaUno();
    void mostrarResumenDosRondas(String nombreUsuario, boolean usuarioGanoR1, boolean usuarioGanoR2);
    void mostrarTablaTop(List<Marcador> top);
}
