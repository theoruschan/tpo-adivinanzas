package com.tpo.adivinanzas.controlador;

import com.tpo.adivinanzas.catalogo.CatalogoPersonajes;
import com.tpo.adivinanzas.jugador.Jugador;
import com.tpo.adivinanzas.jugador.JugadorUsuario;
import com.tpo.adivinanzas.jugador.JugadorMaquina;
import com.tpo.adivinanzas.jugador.MaquinaAvanzada;
import com.tpo.adivinanzas.modelo.Filtro;
import com.tpo.adivinanzas.modelo.FiltroFactory;
import com.tpo.adivinanzas.modelo.Jugada;
import com.tpo.adivinanzas.modelo.Personaje;
import com.tpo.adivinanzas.modelo.PreguntaFiltro;
import com.tpo.adivinanzas.modelo.Suposicion;
import com.tpo.adivinanzas.vista.Vista;

import java.util.List;
import java.util.stream.Collectors;

public class MotorDeJuego {

    private final Vista vista;

    public MotorDeJuego(Vista vista) {
        this.vista = vista;
    }

    /** arranca la partida y va alternando turnos hasta que alguien acierta; devuelve al ganador. */
    public Jugador ejecutar(Partida partida) {
        EstadoPartida estado = partida.getEstado();
        ModoJuego modo = partida.getModo();
        List<Jugador> jugadores = estado.getJugadores();
        int indiceActivo = 0;

        vista.mostrarInicioPartida(modo.getNombre(),
                jugadores.stream().map(Jugador::getNombre).collect(Collectors.toList()));

        while (!estado.hayGanador()) {
            // le toca a uno por vez, en orden circular
            Jugador activo  = jugadores.get(indiceActivo % jugadores.size());
            Jugador objetivo = modo.getObjetivo(activo, estado);

            vista.mostrarTurno(estado.getTurnoActual() + 1, activo.getNombre(), objetivo.getNombre());

            // las maquinas deciden solas; al usuario hay que pedirle la jugada por consola
            Jugada jugada = (activo instanceof JugadorMaquina maquina)
                    ? maquina.elegirJugada(estado)
                    : obtenerJugadaUsuario((JugadorUsuario) activo);

            if (jugada.getTipo() == Jugada.Tipo.PREGUNTA_FILTRO) {
                procesarPregunta(activo, objetivo, (PreguntaFiltro) jugada, estado, modo);
            } else {
                procesarSuposicion(activo, objetivo, (Suposicion) jugada, estado);
            }

            estado.incrementarTurno();
            indiceActivo++;

            if (!estado.hayGanador() && activo instanceof JugadorUsuario) {
                vista.pedirEnter("\nPresioná Enter para continuar...");
            }
            vista.mostrarLineaEnBlanco();

            // en modos que loguean pasos las maquinas juegan muy rapido y pueden inundar
            // la edt de swing; esta pausa deja que la cola de eventos respire y no se trabe la ui
            if (modo.isLoguearPasos()) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        Jugador ganador = estado.getGanador();
        vista.mostrarFinPartida(ganador.getNombre());

        partida.getMarcadorRepo().registrarVictoria(ganador.getNombre());

        return ganador;
    }

    /** arma la jugada del usuario hablando con la vista; el jugador en si no lee entrada por su cuenta. */
    private Jugada obtenerJugadaUsuario(JugadorUsuario usuario) {
        vista.mostrarCandidatosPropios(usuario.getCandidatosRestantes());

        while (true) {
            int accion = vista.pedirAccionTurno();

            if (accion == 0) {
                vista.mostrarPersonajeSecreto(usuario.getPersonajeSecretoPropio());
            } else if (accion == 1) {
                List<Filtro> filtros = FiltroFactory.todosFiltros();
                int idx = vista.pedirSeleccionFiltro(filtros);
                return new PreguntaFiltro(filtros.get(idx));
            } else {
                List<Personaje> todos = CatalogoPersonajes.getInstance().getPersonajesPorGenero();
                int idx = vista.pedirSeleccionPersonajeSupuesto(todos);
                return new Suposicion(todos.get(idx).getNombre());
            }
        }
    }

    /** muestra pregunta/respuesta, guarda en el historial y notifica al que pregunto para que achique candidatos. */
    private void procesarPregunta(Jugador activo, Jugador objetivo,
                                   PreguntaFiltro jugada, EstadoPartida estado,
                                   ModoJuego modo) {
        Filtro filtro = jugada.getFiltro();

        // si pregunta la avanzada, mostramos el razonamiento greedy antes de la pregunta
        // (se lee en orden: primero el "por que", despues el "que")
        if (modo.isLoguearPasos() && activo instanceof MaquinaAvanzada avanzada) {
            vista.mostrarRazonamientoGreedy(avanzada.getNombre(), avanzada.getUltimaEvaluacion(), filtro);
        }

        boolean respuesta = objetivo.responder(filtro);

        vista.mostrarPregunta(activo.getNombre(), filtro.getDescripcion());
        vista.mostrarRespuesta(objetivo.getNombre(), respuesta);

        estado.getHistorial().registrar(estado.getTurnoActual(), activo, filtro, respuesta);

        activo.notificarRespuesta(filtro, respuesta);

        if (modo.isLoguearPasos() && activo instanceof JugadorMaquina maquina) {
            List<String> nombresCandidatos = maquina.getCantidadCandidatos() <= 5
                    ? maquina.getCandidatos().stream().map(Personaje::getNombre).collect(Collectors.toList())
                    : null;
            vista.mostrarCandidatosMaquina(activo.getNombre(), maquina.getCantidadCandidatos(), nombresCandidatos);
        }
    }

    /** muestra la suposicion y, si acierta, marca al que pregunto como ganador. */
    private void procesarSuposicion(Jugador activo, Jugador objetivo,
                                     Suposicion jugada, EstadoPartida estado) {
        String nombre = jugada.getNombrePersonaje();
        vista.mostrarSuposicion(activo.getNombre(), nombre);

        boolean acierto = objetivo.verificarSuposicion(nombre);
        if (acierto) {
            vista.mostrarAcierto(objetivo.getNombre(), nombre);
            estado.setGanador(activo);
        } else {
            vista.mostrarFallo(objetivo.getNombre(), nombre);
        }
    }
}
