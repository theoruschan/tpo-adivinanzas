package com.tpo.adivinanzas.controlador;

import com.tpo.adivinanzas.jugador.Jugador;
import com.tpo.adivinanzas.persistencia.MarcadorRepositorio;
import com.tpo.adivinanzas.vista.Vista;

public class ControladorPrincipal {

    /** inicializamos las variables*/
    private final Vista vista;
    private final MarcadorRepositorio marcadorRepo;
    private final MotorDeJuego motor;

    /** constructor de la clase ControladorPrincipal */
    public ControladorPrincipal(Vista vista, MarcadorRepositorio marcadorRepo) {
        this.vista = vista;
        this.marcadorRepo = marcadorRepo;
        this.motor = new MotorDeJuego(vista);
    }

    /** método que inicia el juego y muestra el menú principal */
    public void iniciar() {
        vista.mostrarBienvenida();

        boolean continuar = true;
        while (continuar) {
            vista.mostrarMenuPrincipal();
            String opcion = vista.leerLinea();
            switch (opcion) {
                case "1" -> jugar(new JugadorVsMaquina());
                case "2" -> jugar(new MaquinaVsMaquina());
                case "3" -> jugarDosMaquinas();
                case "4" -> vista.mostrarRanking(marcadorRepo.topN(10), 10);
                case "5" -> continuar = false;
                default  -> vista.mostrarOpcionInvalida();
            }
        }

        vista.mostrarDespedida();
        vista.cerrar();
    }

    /** método que inicia una partida según el modo de juego seleccionado */
    private void jugar(ModoJuego modo) {
        String nombreUsuario = "Usuario";
        if (modo instanceof JugadorVsMaquina) {
            nombreUsuario = vista.pedirNombreUsuario();
            if (nombreUsuario.isEmpty()) nombreUsuario = "Jugador";
        }

        Partida partida = new Partida(modo, nombreUsuario, vista, marcadorRepo);
        Jugador ganador = motor.ejecutar(partida);
        mostrarResumenFinal(ganador);
    }

    /** método que inicia una partida entre dos máquinas y muestra el resumen final */
    private void jugarDosMaquinas() {
        String nombre = vista.pedirNombreUsuario();
        if (nombre.isEmpty()) nombre = "Jugador";

        new JugadorVsDosMaquinas().ejecutar(nombre, vista, motor, marcadorRepo);

        vista.pedirEnter("\nPresioná Enter para volver al menú...");
    }

    /** método que muestra el resumen final de la partida y el ranking de los jugadores */
    private void mostrarResumenFinal(Jugador ganador) {
        int victorias = marcadorRepo.getVictorias(ganador.getNombre());
        vista.mostrarResumenFinal(ganador.getNombre(), victorias);

        vista.mostrarRanking(marcadorRepo.topN(3), 3);

        vista.pedirEnter("\nPresioná Enter para volver al menú...");
    }
}
