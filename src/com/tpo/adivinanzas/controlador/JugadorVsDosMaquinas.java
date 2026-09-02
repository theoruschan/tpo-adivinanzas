package com.tpo.adivinanzas.controlador;

import com.tpo.adivinanzas.catalogo.CatalogoPersonajes;
import com.tpo.adivinanzas.jugador.Jugador;
import com.tpo.adivinanzas.jugador.JugadorUsuario;
import com.tpo.adivinanzas.jugador.MaquinaAvanzada;
import com.tpo.adivinanzas.jugador.MaquinaBasica;
import com.tpo.adivinanzas.modelo.Personaje;
import com.tpo.adivinanzas.persistencia.MarcadorRepositorio;
import com.tpo.adivinanzas.vista.Vista;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class JugadorVsDosMaquinas {

    /** generador de números aleatorios para seleccionar personajes al azar */
    private static final Random RND = new Random();

    public Jugador ejecutar(String nombreUsuario, Vista vista, MotorDeJuego motor, MarcadorRepositorio repo) {

        /** seleccion de un personaje de la lista */
        List<Personaje> todos = CatalogoPersonajes.getInstance().getPersonajesPorGenero();
        vista.mostrarCatalogoCompleto(nombreUsuario, todos);

        /** el usuario elige un personaje secreto de la lista */
        int opcion = vista.pedirPersonajeSecretoDosMaquinas(todos);
        Personaje personajeUsuario = todos.get(opcion);
        vista.mostrarPersonajeElegido(personajeUsuario.getNombre());

        /** RONDA 1 - usuario contra la maquina basica */
        vista.mostrarEncabezadoRonda1(nombreUsuario);

        /** creación de los jugadores y asignación de personajes secretos */
        JugadorUsuario usuario1 = new JugadorUsuario(nombreUsuario);
        usuario1.asignarPersonajeSecreto(personajeUsuario);

        /** creación de la máquina básica y asignación de un personaje secreto aleatorio */
        MaquinaBasica maquinaBasica = new MaquinaBasica("Máquina Básica");
        maquinaBasica.asignarPersonajeSecreto(personajeAleatorio(personajeUsuario, null));

        /** creación de la partida con los jugadores y el modo de juego JugadorVsMaquina, y ejecución de la ronda 1 */
        Partida ronda1 = new Partida(
                Arrays.asList(usuario1, maquinaBasica),
                new JugadorVsMaquina(),
                repo);
        Jugador ganadorR1 = motor.ejecutar(ronda1);

        boolean usuarioGanoR1 = ganadorR1 instanceof JugadorUsuario;

        /** si el usuario pierde la primera ronda, se muestra el mensaje de derrota y el ranking, y se termina la ejecución */
        if (!usuarioGanoR1) {
            vista.mostrarDerrotaRondaUno();
            vista.mostrarTablaTop(repo.topN(3));
            return ganadorR1;
        }

        /** RONDA 2 - usuario contra la maquina avanzada */
        vista.mostrarEncabezadoRonda2(nombreUsuario);
        vista.pedirEnter("Presioná Enter para iniciar la segunda ronda...");

        /** creación de los jugadores y asignación de personajes secretos para la segunda ronda
         * el usuario conserva el mismo personaje secreto, mientras que la máquina avanzada recibe un personaje aleatorio diferente
         */
        JugadorUsuario usuario2 = new JugadorUsuario(nombreUsuario);
        usuario2.asignarPersonajeSecreto(personajeUsuario);   // mismo personaje secreto

        /** creación de la máquina avanzada y asignación de un personaje secreto aleatorio diferente al del usuario */
        MaquinaAvanzada maquinaAvanzada = new MaquinaAvanzada("Máquina Avanzada");
        maquinaAvanzada.setMaquinaBasica(maquinaBasica);    // ventaja: conoce filtros de R1
        maquinaAvanzada.asignarPersonajeSecreto(
                personajeAleatorio(personajeUsuario, null));

        /** hereda tambien las respuestas de r1, no solo que filtros uso: el secreto del usuario
         * es el mismo en las dos rondas, asi que reaplicarlas ya le achica los candidatos de entrada. */
        List<EntradaHistorial> preguntasBasicaR1 = ronda1.getEstado().getHistorial().getEntradasDe(maquinaBasica);
        for (EntradaHistorial entrada : preguntasBasicaR1) {
            maquinaAvanzada.notificarRespuesta(entrada.getFiltro(), entrada.getRespuesta());
        }
        vista.mostrarCandidatosMaquina(maquinaAvanzada.getNombre(), maquinaAvanzada.getCantidadCandidatos(),
                maquinaAvanzada.getCantidadCandidatos() <= 5
                        ? maquinaAvanzada.getCandidatos().stream().map(Personaje::getNombre).collect(Collectors.toList())
                        : null);

        /** creación de la partida con los jugadores y el modo de juego JugadorVsMaquina, y ejecución de la ronda 2 */
        Partida ronda2 = new Partida(
                Arrays.asList(usuario2, maquinaAvanzada),
                new JugadorVsMaquina(),
                repo);
        Jugador ganadorR2 = motor.ejecutar(ronda2);

        boolean usuarioGanoR2 = ganadorR2 instanceof JugadorUsuario;

        /** resumen de las dos rondas y ranking final */
        vista.mostrarResumenDosRondas(nombreUsuario, usuarioGanoR1, usuarioGanoR2);
        vista.mostrarTablaTop(repo.topN(3));
        return ganadorR2;
    }

    /** elige un personaje al azar, salteando los que se pasen (pueden ser null). */
    private static Personaje personajeAleatorio(Personaje excluir1, Personaje excluir2) {
        List<Personaje> disponibles =
                new ArrayList<>(CatalogoPersonajes.getInstance().getPersonajesPorGenero());
        if (excluir1 != null) disponibles.removeIf(p -> p.getId() == excluir1.getId());
        if (excluir2 != null) disponibles.removeIf(p -> p.getId() == excluir2.getId());
        return disponibles.get(RND.nextInt(disponibles.size()));
    }
}
