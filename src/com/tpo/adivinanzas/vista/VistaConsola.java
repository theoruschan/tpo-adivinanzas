package com.tpo.adivinanzas.vista;

import com.tpo.adivinanzas.modelo.EvaluacionFiltro;
import com.tpo.adivinanzas.modelo.Filtro;
import com.tpo.adivinanzas.modelo.Personaje;
import com.tpo.adivinanzas.persistencia.Marcador;

import java.util.List;
import java.util.Scanner;

/** toda la entrada/salida por consola. no conoce reglas del juego, solo muestra y devuelve lo tipeado. */
public class VistaConsola implements Vista {

    private final Scanner scanner = new Scanner(System.in);

    public void mostrarBienvenida() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   JUEGO DE ADIVINANZAS — TPO Prog 3  ║");
        System.out.println("╚══════════════════════════════════════╝");
    }

    public void mostrarMenuPrincipal() {
        System.out.println("\n¿Qué querés hacer?");
        System.out.println("  1. Jugador vs Máquina");
        System.out.println("  2. Máquina vs Máquina");
        System.out.println("  3. Jugador vs Dos Máquinas");
        System.out.println("  4. Ver ranking");
        System.out.println("  5. Salir");
        System.out.print("Opción: ");
    }

    public String leerLinea() {
        return scanner.nextLine().trim();
    }

    public void mostrarOpcionInvalida() {
        System.out.println("Opción inválida.");
    }

    public void mostrarDespedida() {
        System.out.println("\n¡Hasta la próxima!");
    }

    public void cerrar() {
        scanner.close();
    }

    public String pedirNombreUsuario() {
        System.out.print("\nIngresá tu nombre de usuario: ");
        return leerLinea();
    }

    public void pedirEnter(String mensaje) {
        System.out.print(mensaje);
        scanner.nextLine();
    }

    /** reintenta hasta que el usuario tipee un numero dentro del rango. */
    private int pedirOpcionNumerica(int min, int max) {
        int opcion = Integer.MIN_VALUE;
        while (opcion < min || opcion > max) {
            System.out.print("Opción (" + min + "-" + max + "): ");
            try {
                opcion = Integer.parseInt(leerLinea());
            } catch (NumberFormatException e) {
                opcion = Integer.MIN_VALUE;
            }
            if (opcion < min || opcion > max) {
                System.out.println("Opción inválida, reintentá.");
            }
        }
        return opcion;
    }

    /** devuelve el indice (0-based) del personaje elegido dentro de disponibles. */
    public int pedirPersonajeSecreto(String nombreJugador, List<Personaje> disponibles) {
        System.out.println("\n" + nombreJugador + ", elegí tu personaje secreto:");
        for (int i = 0; i < disponibles.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, disponibles.get(i));
        }
        return pedirOpcionNumerica(1, disponibles.size()) - 1;
    }

    public void mostrarCatalogoCompleto(String nombreJugador, List<Personaje> todos) {
        System.out.println("\n" + nombreJugador + ", aquí están todos los personajes disponibles:");
        todos.forEach(p -> System.out.println("  " + p));
    }

    public int pedirPersonajeSecretoDosMaquinas(List<Personaje> todos) {
        System.out.println("\nElegí tu personaje secreto:");
        System.out.println("(Este personaje no podrá cambiarse durante ninguna de las rondas)");
        for (int i = 0; i < todos.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, todos.get(i).getNombre());
        }
        return pedirOpcionNumerica(1, todos.size()) - 1;
    }

    public void mostrarPersonajeElegido(String nombrePersonaje) {
        System.out.println("Elegiste: " + nombrePersonaje + ". ¡Comienza el juego!");
    }

    public void mostrarInicioPartida(String nombreModo, List<String> nombresJugadores) {
        System.out.println("\n========================================");
        System.out.println("  MODO: " + nombreModo);
        System.out.println("  Jugadores: ");
        nombresJugadores.forEach(n -> System.out.println("    - " + n));
        System.out.println("========================================\n");
    }

    public void mostrarTurno(int numeroTurno, String nombreActivo, String nombreObjetivo) {
        System.out.println("--- Turno " + numeroTurno
                + " | " + nombreActivo
                + " → " + nombreObjetivo + " ---");
    }

    public void mostrarPregunta(String nombreActivo, String descripcionFiltro) {
        System.out.println(nombreActivo + " pregunta: " + descripcionFiltro);
    }

    public void mostrarRespuesta(String nombreObjetivo, boolean respuesta) {
        System.out.println("Respuesta de " + nombreObjetivo + ": " + (respuesta ? "SÍ" : "NO"));
    }

    /** nombresCandidatos puede venir null (por ejemplo si son demasiados para listar). */
    public void mostrarCandidatosMaquina(String nombreActivo, int cantidad, List<String> nombresCandidatos) {
        System.out.println(">>> Candidatos restantes para " + nombreActivo + ": " + cantidad);
        if (nombresCandidatos != null) {
            System.out.print("    Candidatos: ");
            nombresCandidatos.forEach(n -> System.out.print(n + " "));
            System.out.println();
        }
    }

    /** imprime la evaluacion greedy de cada filtro y cual eligio la maquina, para ver el porque. */
    public void mostrarRazonamientoGreedy(String nombreMaquina, List<EvaluacionFiltro> evaluaciones, Filtro elegido) {
        System.out.println(">>> " + nombreMaquina + " evalúa filtros (greedy, criterio minimax):");
        for (EvaluacionFiltro e : evaluaciones) {
            System.out.printf("      %-28s cumplen: %2d | no cumplen: %2d | peor caso: %2d%n",
                    e.getFiltro().getDescripcion(), e.getCumplen(), e.getNoCumplen(), e.peorCaso());
        }
        System.out.println(">>> " + nombreMaquina + " elige: \"" + elegido.getDescripcion()
                + "\" (el que minimiza el peor caso entre los evaluados)");
    }

    public void mostrarSuposicion(String nombreActivo, String nombrePersonaje) {
        System.out.println(nombreActivo + " supone: ¿Es " + nombrePersonaje + "?");
    }

    public void mostrarAcierto(String nombreObjetivo, String nombrePersonaje) {
        System.out.println("¡CORRECTO! El personaje de " + nombreObjetivo
                + " era " + nombrePersonaje + ".");
    }

    public void mostrarFallo(String nombreObjetivo, String nombrePersonaje) {
        System.out.println("Incorrecto. El personaje de " + nombreObjetivo
                + " no es " + nombrePersonaje + ". La partida continúa.");
    }

    public void mostrarFinPartida(String nombreGanador) {
        System.out.println("========================================");
        System.out.println("  FIN DE PARTIDA — Ganador: " + nombreGanador);
        System.out.println("========================================\n");
    }

    public void mostrarLineaEnBlanco() {
        System.out.println();
    }

    public void mostrarCandidatosPropios(List<Personaje> candidatos) {
        System.out.println("\nTus candidatos restantes (" + candidatos.size() + "):");
        candidatos.forEach(p -> System.out.println("  " + p));
    }

    /** devuelve 0 (ver personaje), 1 (preguntar) o 2 (adivinar). */
    public int pedirAccionTurno() {
        System.out.println("\n¿Qué querés hacer?");
        System.out.println("  0. Ver mi personaje secreto");
        System.out.println("  1. Hacer una pregunta de filtro");
        System.out.println("  2. Adivinar el personaje del rival");
        return pedirOpcionNumerica(0, 2);
    }

    public void mostrarPersonajeSecreto(Personaje personaje) {
        System.out.println("\nTu personaje secreto: " + personaje);
        pedirEnter("(Presioná Enter para continuar)");
    }

    public int pedirSeleccionFiltro(List<Filtro> filtros) {
        System.out.println("\nElegí un filtro:");
        for (int i = 0; i < filtros.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, filtros.get(i).getDescripcion());
        }
        return pedirOpcionNumerica(1, filtros.size()) - 1;
    }

    public int pedirSeleccionPersonajeSupuesto(List<Personaje> todos) {
        System.out.println("\nElegí el personaje que creés que tiene el rival:");
        for (int i = 0; i < todos.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, todos.get(i).getNombre());
        }
        return pedirOpcionNumerica(1, todos.size()) - 1;
    }

    public void mostrarResumenFinal(String nombreGanador, int victorias) {
        System.out.printf("%n%s tiene ahora %d victoria%s en total.%n",
                nombreGanador, victorias, victorias == 1 ? "" : "s");
    }

    public void mostrarRanking(List<Marcador> top, int n) {
        System.out.println("\n--- RANKING (top " + n + ") ---");
        if (top.isEmpty()) {
            System.out.println("  Sin partidas registradas aún.");
        } else {
            for (int i = 0; i < top.size(); i++) {
                System.out.printf("  %2d. %s%n", i + 1, top.get(i));
            }
        }
    }

    public void mostrarEncabezadoRonda1(String nombreUsuario) {
        System.out.println("\n════════════════════════════════════════");
        System.out.println("  RONDA 1 — " + nombreUsuario + " vs maquina basica");
        System.out.println("════════════════════════════════════════");
    }

    public void mostrarEncabezadoRonda2(String nombreUsuario) {
        System.out.println("\n════════════════════════════════════════");
        System.out.println("  RONDA 2 — " + nombreUsuario + " vs maquina avanzada");
        System.out.println("  (maquina avanzada conoce las preguntas de la Ronda 1)");
        System.out.println("════════════════════════════════════════");
    }

    public void mostrarDerrotaRondaUno() {
        System.out.println("\n════════════════════════════════════════");
        System.out.println("  RESUMEN: Perdiste en la primera ronda.");
        System.out.println("  La segunda ronda no se jugará.");
        System.out.println("════════════════════════════════════════");
    }

    public void mostrarResumenDosRondas(String nombreUsuario, boolean usuarioGanoR1, boolean usuarioGanoR2) {
        System.out.println("\n════════════════════════════════════════");
        System.out.println("  RESUMEN FINAL — " + nombreUsuario + " vs 2  maquinas");
        System.out.println("════════════════════════════════════════");
        System.out.println("  Ronda 1 (vs Máquina Básica):   "
                + (usuarioGanoR1 ? "VICTORIA del usuario" : "DERROTA del usuario"));
        System.out.println("  Ronda 2 (vs Máquina Avanzada): "
                + (usuarioGanoR2 ? "VICTORIA del usuario" : "DERROTA del usuario"));
        if (usuarioGanoR1 && usuarioGanoR2) {
            System.out.println("  ¡Ganaste las dos rondas! Resultado perfecto.");
        } else if (usuarioGanoR1) {
            System.out.println("  Ganaste la primera pero perdiste la segunda. ¡Buen intento!");
        }
    }

    public void mostrarTablaTop(List<Marcador> top) {
        System.out.println("\n--- TOP 3 ---");
        if (top.isEmpty()) {
            System.out.println("  Sin registros aún.");
        } else {
            for (int i = 0; i < top.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, top.get(i));
            }
        }
    }
}
