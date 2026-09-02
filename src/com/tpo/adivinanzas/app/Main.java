package com.tpo.adivinanzas.app;

import com.tpo.adivinanzas.controlador.ControladorPrincipal;
import com.tpo.adivinanzas.persistencia.MarcadorRepositorio;
import com.tpo.adivinanzas.vista.VistaSwing;

import javax.swing.SwingUtilities;

public class Main {

    private static final String ARCHIVO_MARCADOR = "marcador.txt";

    /** punto de entrada del juego. arranca directo con la vista swing. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VistaSwing vista = new VistaSwing();
            MarcadorRepositorio marcadorRepo = new MarcadorRepositorio(ARCHIVO_MARCADOR);

            // el motor de juego se queda esperando cada respuesta del usuario (bloquea), así
            // que corre en un hilo aparte para no trabar la ventana mientras espera.
            Thread hiloJuego = new Thread(
                    () -> new ControladorPrincipal(vista, marcadorRepo).iniciar(),
                    "hilo-juego");
            hiloJuego.setDaemon(true);
            hiloJuego.start();
        });
    }
}
